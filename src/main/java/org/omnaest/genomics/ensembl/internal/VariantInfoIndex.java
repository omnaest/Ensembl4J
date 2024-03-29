/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.genomics.ensembl.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.omnaest.genomics.ensembl.domain.ClinicalSignificance;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genomics.ensembl.ftp.EnsemblFTPUtils;
import org.omnaest.genomics.vcf.domain.VCFRecord;
import org.omnaest.genomics.vcf.domain.VCFRecord.AdditionalInfo;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.ComparatorUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.ProcessorUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.DurationProgressCounter;
import org.omnaest.utils.counter.ImmutableDurationProgressCounter.DurationProgress;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;
import org.omnaest.utils.functional.UnaryBiFunction;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.MapElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VariantInfoIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(VariantInfoIndex.class);

    private Cache                                                            cache                 = CacheUtils.newNoOperationCache();
    private Map<String, Index>                                               speciesToIndexData    = new ConcurrentHashMap<String, Index>();
    private Function<String, Index>                                          indexDataProvider     = species -> new Index();
    private Function<String, MapElementRepository<String, IndexVariantInfo>> repositoryProvider    = species -> ElementRepository.ofNonSupplied(new ConcurrentHashMap<>());
    private Predicate<VCFRecord>                                             variantFilter         = PredicateUtils.allMatching();
    private int                                                              distributionBatchSize = 100000;

    public VariantInfoIndex usingCache(Cache cache)
    {
        this.cache = cache;
        return this;
    }

    public VariantInfoIndex usingLocalCache()
    {
        return this.usingCache(CacheUtils.newLocalJsonFolderCache("ensembl/ftp2")
                                         .withNativeByteArrayStorage(true)
                                         .withNativeStringStorage(true));
    }

    public VariantInfoIndex withRepositoryProvider(Function<String, MapElementRepository<String, IndexVariantInfo>> repositoryProvider)
    {
        this.repositoryProvider = repositoryProvider;
        return this;
    }

    public VariantInfoIndex withDistributionBatchSize(int distributionBatchSize)
    {
        this.distributionBatchSize = distributionBatchSize;
        return this;
    }

    private VariantInfoIndex()
    {
        super();
    }

    public static VariantInfoIndex getInstance()
    {
        return new VariantInfoIndex();
    }

    private Map<String, DurationProgressCounter> speciesToProcessedVariantsDurationCounter = new ConcurrentHashMap<>();
    private int                                  variantLoadLimit                          = Integer.MAX_VALUE;

    public VariantInfoIndex enable()
    {
        this.indexDataProvider = species ->
        {
            try
            {
                MapElementRepository<String, IndexVariantInfo> variantIdToVariantInfo = this.repositoryProvider.apply(species);

                // *****************

                LOG.info("Start counting matching variants...");
                Counter numberOfAllSourceVariants = Counter.fromZero();
                int numberOfVariants = (int) this.createVariantsStream(species)
                                                 .peek(record -> numberOfAllSourceVariants.increment())
                                                 .filter(this.variantFilter)
                                                 .count();
                LOG.info("...finished counting. Found " + numberOfVariants + " matching variants in " + numberOfAllSourceVariants.getAsLong()
                        + " source variants.");

                long currentVariantIndexSize = variantIdToVariantInfo.size();
                {
                    LOG.info("Rebuilding index with current index having " + currentVariantIndexSize + " variants, and " + numberOfVariants
                            + " are to be matched.");

                    UnaryBiFunction<IndexVariantInfo> variantInfoMerger = (info1, info2) ->
                    {
                        IndexVariantInfo variantInfo = new IndexVariantInfo();
                        variantInfo.setRsId(Optional.ofNullable(info1.getRsId())
                                                    .orElse(info2.getRsId()));
                        variantInfo.setMaf(Optional.ofNullable(info1.getMaf())
                                                   .orElse(info2.getMaf()));
                        variantInfo.setConsequence(Stream.of(info1.getConsequence(), info2.getConsequence())
                                                         .filter(PredicateUtils.notBlank())
                                                         .map(VariantConsequence::of)
                                                         .filter(Optional::isPresent)
                                                         .map(Optional::get)
                                                         .sorted(ComparatorUtils.comparatorFunction(VariantConsequence::getSeverity))
                                                         .findFirst()
                                                         .map(VariantConsequence::getMatchStr)
                                                         .orElse(null));

                        Stream.concat(Optional.ofNullable(info1.getClinicalSignifance())
                                              .orElse(Collections.emptySet())
                                              .stream(),
                                      Optional.ofNullable(info2.getClinicalSignifance())
                                              .orElse(Collections.emptySet())
                                              .stream())
                              .filter(PredicateUtils.notNull())
                              .forEach(variantInfo::addClinicalSignifance);

                        return variantInfo;
                    };

                    Function<VCFRecord, IndexVariantInfo> variationVcfRecordToVariantInfoMapper = record ->
                    {
                        IndexVariantInfo variantInfo = new IndexVariantInfo();

                        variantInfo.setRsId(record.getId());

                        variantInfo.setMaf(record.getInfoValue(AdditionalInfo.MAF)
                                                 .orElse(variantInfo.getMaf()));
                        variantInfo.setConsequence(record.getInfoTokenGroups(AdditionalInfo.CSQ)
                                                         .map(group -> group.getValueAt(1))
                                                         .filter(Optional::isPresent)
                                                         .map(Optional::get)
                                                         .map(VariantConsequence::of)
                                                         .filter(Optional::isPresent)
                                                         .map(Optional::get)
                                                         .sorted(ComparatorUtils.comparatorFunction(VariantConsequence::getSeverity))
                                                         .findFirst()
                                                         .map(VariantConsequence::getMatchStr)
                                                         .orElse(variantInfo.getConsequence()));

                        if (record.getInfoExists(AdditionalInfo.CLIN_risk_factor))
                        {
                            variantInfo.addClinicalSignifance(ClinicalSignificance.RISK_FACTOR.getMatchStr());
                        }
                        if (record.getInfoExists(AdditionalInfo.CLIN_benign))
                        {
                            variantInfo.addClinicalSignifance(ClinicalSignificance.BENIGN.getMatchStr());
                        }

                        return variantInfo;
                    };

                    DurationProgressCounter overallDurationCounter = this.speciesToProcessedVariantsDurationCounter.getOrDefault(species, Counter.fromZero()
                                                                                                                                                 .asDurationProgressCounter()
                                                                                                                                                 .withMaximum(100));
                    DurationProgressCounter processedVariantsDurationCounter = Counter.fromZero()
                                                                                      .asDurationProgressCounter()
                                                                                      .withMaximum(numberOfVariants);
                    overallDurationCounter.synchronizeProgressContinouslyFrom(processedVariantsDurationCounter);

                    LOG.info("Start reading raw variant vcf files...");
                    ProcessorUtils.newRepeatingFilteredProcessorWithInMemoryCacheAndRepository(variantIdToVariantInfo, IndexVariantInfo.class)
                                  .withDistributionFactor(this.distributionBatchSize, numberOfVariants)
                                  .process((cacheId, distributionFactor) -> this.createVariantsStream(species)
                                                                                .peek(StreamUtils.peekProgressCounter(100000,
                                                                                                                      numberOfAllSourceVariants.getAsLong(),
                                                                                                                      progress -> LOG.info("    Processed variation index records: "
                                                                                                                              + progress.getCounter() + " "
                                                                                                                              + progress.getProgressAsString()
                                                                                                                              + " ( Cycle " + (cacheId + 1)
                                                                                                                              + "/" + distributionFactor
                                                                                                                              + " )")))
                                                                                .filter(this.variantFilter))
                                  .withAggregatingOperation(VCFRecord::getId, variationVcfRecordToVariantInfoMapper, variantInfoMerger)
                                  .forEach(result -> processedVariantsDurationCounter.increment()
                                                                                     .ifModulo(10000,
                                                                                               (DurationProgress progress) -> LOG.info("Current number of processed records: "
                                                                                                       + progress.getCounter() + " "
                                                                                                       + progress.getProgressAsString() + " ( "
                                                                                                       + progress.getETA()
                                                                                                                 .map(DisplayableDuration::asCanonicalString)
                                                                                                                 .orElse("")
                                                                                                       + " )")));
                    LOG.info("...finished reading raw variant vcf files");
                    LOG.info("Cached " + variantIdToVariantInfo.size() + " records");
                }

                return new Index(variantIdToVariantInfo);
            }
            catch (Throwable e)
            {
                LOG.error("Unexpected exception during variant index creation", e);
                throw new IllegalStateException(e);
            }
        };
        return this;

    }

    private Stream<VCFRecord> createVariantsStream(String species)
    {
        Stream<VCFRecord> records = StreamUtils.concat(EnsemblFTPUtils.load()
                                                                      .withCache(this.cache)
                                                                      .variationVCFFiles()
                                                                      .current()
                                                                      .forSpecies(species)
                                                                      .forChromosomes()
                                                                      .flatMap(resource -> resource.asParsedVCF()
                                                                                                   .getRecords()),
                                                       EnsemblFTPUtils.load()
                                                                      .withCache(this.cache)
                                                                      .variationVCFFiles()
                                                                      .current()
                                                                      .forSpecies(species)
                                                                      .forClinicallyAssociated()
                                                                      .asParsedVCF()
                                                                      .getRecords(),
                                                       EnsemblFTPUtils.load()
                                                                      .withCache(this.cache)
                                                                      .variationVCFFiles()
                                                                      .current()
                                                                      .forSpecies(species)
                                                                      .forPhenotypeAssociated()
                                                                      .asParsedVCF()
                                                                      .getRecords());
        if (this.variantLoadLimit < Integer.MAX_VALUE)
        {
            records = records.limit(this.variantLoadLimit);
        }
        return records;
    }

    public static class Index
    {
        private MapElementRepository<String, IndexVariantInfo> variantIdToVariantInfo = ElementRepository.ofNonSupplied(new ConcurrentHashMap<>());

        public Index()
        {
            super();
        }

        public Index(MapElementRepository<String, IndexVariantInfo> variantIdToVariantInfo)
        {
            super();
            this.variantIdToVariantInfo = variantIdToVariantInfo;
        }

        public NullOptional<IndexVariantInfo> get(String variantId)
        {
            return this.variantIdToVariantInfo.get(variantId);
        }

        public Stream<IndexVariantInfo> stream()
        {
            return this.variantIdToVariantInfo.values();
        }
    }

    public VariantInfoIndex withMaximumNumberOfVariants(int maxiumum)
    {
        this.variantLoadLimit = maxiumum;
        return this;
    }

    public VariantInfoIndex initializeSpecies(String species, Consumer<DurationProgressCounter> counterConsumer)
    {
        counterConsumer.accept(VariantInfoIndex.this.speciesToProcessedVariantsDurationCounter.compute(species, (s, previous) -> Counter.fromZero()
                                                                                                                                        .asDurationProgressCounter()
                                                                                                                                        .withMaximum(100)));
        Optional.ofNullable(this.speciesToIndexData.computeIfAbsent(species, this.indexDataProvider));
        return this;
    }

    public Optional<IndexVariantInfo> getVariantInfo(String species, String variantId)
    {
        return Optional.ofNullable(this.speciesToIndexData.computeIfAbsent(species, this.indexDataProvider))
                       .orElse(new Index())
                       .get(variantId)
                       .asOptional();
    }

    public VariantInfoIndex withVariantIdFilter(Predicate<String> variantIdFilter)
    {
        this.variantFilter = record -> variantIdFilter.test(record.getId());
        return this;
    }

    public Stream<IndexVariantInfo> stream()
    {
        return this.speciesToIndexData.values()
                                      .stream()
                                      .flatMap(index -> index.stream());
    }

    public static class IndexVariantInfo extends VariantInfo
    {
        @JsonProperty
        private String rsId;

        public String getRsId()
        {
            return this.rsId;
        }

        public void setRsId(String rsId)
        {
            this.rsId = rsId;
        }

    }
}
