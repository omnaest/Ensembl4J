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

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.omnaest.genomics.ensembl.domain.ClinicalSignificance;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genomics.ensembl.ftp.EnsemblFTPUtils;
import org.omnaest.genomics.ensembl.ftp.EnsemblFTPUtils.VariationVCFResource;
import org.omnaest.genomics.vcf.domain.VCFRecord;
import org.omnaest.genomics.vcf.domain.VCFRecord.AdditionalInfo;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.ComparatorUtils;
import org.omnaest.utils.FileUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.MapperUtils;
import org.omnaest.utils.PeekUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.counter.ProgressCounter;
import org.omnaest.utils.counter.ProgressCounterContainer;
import org.omnaest.utils.functional.UnaryBiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public class VariantInfoStreamer
{
    private static final Logger LOG = LoggerFactory.getLogger(VariantInfoStreamer.class);

    private Cache cache            = CacheUtils.newNoOperationCache();
    private int   batchSize        = 1000000;
    private int   variantLoadLimit = Integer.MAX_VALUE;

    private ProgressCounterContainer progressCounterContainer = ProgressCounterContainer.newInstance();

    public VariantInfoStreamer usingCache(Cache cache)
    {
        this.cache = cache;
        return this;
    }

    public VariantInfoStreamer usingLocalCache()
    {
        return this.usingCache(CacheUtils.newLocalJsonFolderCache("ensembl/ftp2")
                                         .withNativeByteArrayStorage(true)
                                         .withNativeStringStorage(true));
    }

    public VariantInfoStreamer withBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
        return this;
    }

    private VariantInfoStreamer()
    {
        super();
    }

    public static VariantInfoStreamer getInstance()
    {
        return new VariantInfoStreamer();
    }

    public static class VariantInfoBatch
    {
        @JsonProperty
        private List<IndexVariantInfo> variantInfos;

        @JsonCreator
        public VariantInfoBatch(List<IndexVariantInfo> variantInfos)
        {
            super();
            this.variantInfos = variantInfos;
        }

        @JsonValue
        public List<IndexVariantInfo> getVariantInfos()
        {
            return this.variantInfos;
        }

        @JsonIgnore
        public Stream<IndexVariantInfo> stream()
        {
            return Optional.ofNullable(this.variantInfos)
                           .orElse(Collections.emptyList())
                           .stream();
        }

    }

    public Stream<IndexVariantInfo> loadVariants(String species)
    {
        //
        ProgressCounter ftpLoaderProgressCounter = this.progressCounterContainer.newProgressCounterWithWeight("Loading Variants from FTP", 0.2);
        ProgressCounter sortAndSinkProgressCounter = this.progressCounterContainer.newProgressCounterWithWeight("Sorting Variants", 0.3);
        ProgressCounter consumptionProgressCounter = this.progressCounterContainer.newProgressCounterWithWeight("Processing Variants", 0.5);

        //
        LOG.info("Start counting matching variants...");
        Counter numberOfAllSourceVariants = Counter.fromZero();
        int numberOfVariants = (int) this.createVariantsStream(species, ftpLoaderProgressCounter)
                                         .peek(record -> numberOfAllSourceVariants.increment())
                                         //                                         .filter(this.variantFilter)
                                         .count();
        LOG.info("...finished counting. Found " + numberOfVariants + " matching variants in " + numberOfAllSourceVariants.getAsLong() + " source variants.");

        //
        sortAndSinkProgressCounter.withMaximum(numberOfVariants);
        consumptionProgressCounter.withMaximum(numberOfVariants);

        //
        Function<VCFRecord, IndexVariantInfo> variationVcfRecordToVariantInfoMapper = this.createVCFRecordToVariantInfoMapper();
        UnaryBiFunction<IndexVariantInfo> variantInfoMerger = this.createVariantInfoMerger();

        // sort the variants and restore them
        AtomicInteger fileIndexCounter = new AtomicInteger();
        return StreamUtils.merger()
                          .ofSorted()
                          .unary()
                          .withIdentityFunction(IndexVariantInfo.class, String.class, IndexVariantInfo::getRsId)
                          .withSourceStreams(StreamUtils.framedAsList(this.batchSize, this.createVariantsStream(species, Counter.fromZero()
                                                                                                                                .asProgressCounter()))
                                                        .map(this.createFrameSorter(variationVcfRecordToVariantInfoMapper))
                                                        .map(VariantInfoBatch::new)
                                                        .map(this.createBatchSinkToStreamMapper(fileIndexCounter))
                                                        .peek(PeekUtils.newDurationProgressCounterLogger(LOG::info, numberOfVariants)
                                                                       .by(this.batchSize))
                                                        .peek(PeekUtils.incrementCounter(sortAndSinkProgressCounter)
                                                                       .by(this.batchSize))
                                                        .collect(Collectors.toList()))
                          .reduce(variantInfoMerger)
                          .peek(PeekUtils.incrementCounter(consumptionProgressCounter));
    }

    private Function<List<VCFRecord>, List<IndexVariantInfo>> createFrameSorter(Function<VCFRecord, IndexVariantInfo> variationVcfRecordToVariantInfoMapper)
    {
        return batch ->
        {
            LOG.info("Sorting batch of size: " + batch.size());
            return batch.stream()
                        .map(variationVcfRecordToVariantInfoMapper)
                        .sorted(ComparatorUtils.builder()
                                               .of(IndexVariantInfo.class)
                                               .with(IndexVariantInfo::getRsId)
                                               .build())
                        .collect(Collectors.toList());
        };
    }

    private Function<VariantInfoBatch, Stream<IndexVariantInfo>> createBatchSinkToStreamMapper(AtomicInteger fileIndexCounter)
    {
        return batch ->
        {
            LOG.info("Sinking batch of size: " + batch.getVariantInfos()
                                                      .size());
            File file = new File(CacheUtils.createCacheFolder("ensembl/variantbatch"), fileIndexCounter.getAndIncrement() + ".json");
            return FileUtils.toFileSinkInputStreamSupplier(file)
                            .accept(JSONHelper.writerSerializer(VariantInfoBatch.class)
                                              .wrapObject(batch))
                            .toStream(JSONHelper.readerDeserializer(IndexVariantInfo.class)
                                                .forArray());
        };
    }

    private UnaryBiFunction<IndexVariantInfo> createVariantInfoMerger()
    {
        return (info1, info2) ->
        {
            IndexVariantInfo variantInfo = new IndexVariantInfo();
            variantInfo.setRsId(Optional.ofNullable(info1.getRsId())
                                        .filter(StringUtils::isNoneBlank)
                                        .orElse(info2.getRsId()));
            variantInfo.setMaf(Optional.ofNullable(info1.getMaf())
                                       .filter(StringUtils::isNoneBlank)
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
    }

    private Function<VCFRecord, IndexVariantInfo> createVCFRecordToVariantInfoMapper()
    {
        return record ->
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
    }

    private Stream<VCFRecord> createVariantsStream(String species, ProgressCounter progressCounter)
    {
        List<VariationVCFResource> resources = StreamUtils.concat(EnsemblFTPUtils.load()
                                                                                 .withCache(this.cache)
                                                                                 .variationVCFFiles()
                                                                                 .current()
                                                                                 .forSpecies(species)
                                                                                 .forChromosomes()
                                                                                 .map(MapperUtils.identityCast(VariationVCFResource.class)),
                                                                  Stream.of(EnsemblFTPUtils.load()
                                                                                           .withCache(this.cache)
                                                                                           .variationVCFFiles()
                                                                                           .current()
                                                                                           .forSpecies(species)
                                                                                           .forClinicallyAssociated(),
                                                                            EnsemblFTPUtils.load()
                                                                                           .withCache(this.cache)
                                                                                           .variationVCFFiles()
                                                                                           .current()
                                                                                           .forSpecies(species)
                                                                                           .forPhenotypeAssociated()))
                                                          .collect(Collectors.toList());
        progressCounter.withMaximum(resources.size());
        return resources.stream()
                        .peek(PeekUtils.incrementCounter(progressCounter))
                        .flatMap(resource -> resource.withCacheClearanceAfterRead(true)
                                                     .asParsedVCF()
                                                     .getRecords()
                                                     .limit(this.variantLoadLimit))
                        .limit(this.variantLoadLimit);
    }

    public VariantInfoStreamer withMaximumNumberOfVariants(int maxiumum)
    {
        this.variantLoadLimit = maxiumum;
        return this;
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

    public VariantInfoStreamer withProgressCounter(ProgressCounterContainer progressCounterContainer)
    {
        this.progressCounterContainer = progressCounterContainer;
        return this;
    }
}
