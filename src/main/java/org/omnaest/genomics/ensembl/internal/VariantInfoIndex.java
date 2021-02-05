package org.omnaest.genomics.ensembl.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
import org.omnaest.utils.counter.DurationProgressCounter.DurationProgress;
import org.omnaest.utils.duration.DurationCapture.DisplayableDuration;
import org.omnaest.utils.functional.UnaryBiFunction;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.MapElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantInfoIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(VariantInfoIndex.class);

    private Cache                                                       cache                 = CacheUtils.newNoOperationCache();
    private Map<String, Index>                                          speciesToIndexData    = new ConcurrentHashMap<String, Index>();
    private Function<String, Index>                                     indexDataProvider     = species -> new Index();
    private Function<String, MapElementRepository<String, VariantInfo>> repositoryProvider    = species -> ElementRepository.ofNonSupplied(new ConcurrentHashMap<>());
    private Predicate<VCFRecord>                                        variantFilter         = PredicateUtils.allMatching();
    private int                                                         distributionBatchSize = 100000;

    public VariantInfoIndex usingCache(Cache cache)
    {
        this.cache = cache;
        return this;
    }

    public VariantInfoIndex usingLocalCache()
    {
        return this.usingCache(CacheUtils.newLocalJsonFolderCache("ensembl/ftp")
                                         .withNativeByteArrayStorage(true)
                                         .withNativeStringStorage(true));
    }

    public VariantInfoIndex withRepositoryProvider(Function<String, MapElementRepository<String, VariantInfo>> repositoryProvider)
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

    public VariantInfoIndex enable()
    {
        this.indexDataProvider = species ->
        {
            try
            {
                MapElementRepository<String, VariantInfo> variantIdToVariantInfo = this.repositoryProvider.apply(species);

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
                if (currentVariantIndexSize < numberOfVariants)
                {
                    LOG.info("Rebuilding index as current index has only " + currentVariantIndexSize + " variants, but " + numberOfVariants + " are needed.");

                    UnaryBiFunction<VariantInfo> variantInfoMerger = (info1, info2) ->
                    {
                        VariantInfo variantInfo = new VariantInfo();

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

                    Function<VCFRecord, VariantInfo> variationVcfRecordToVariantInfoMapper = record ->
                    {
                        VariantInfo variantInfo = new VariantInfo();

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

                    DurationProgressCounter processedVariantsDurationCounter = Counter.fromZero()
                                                                                      .asDurationProgressCounter()
                                                                                      .withMaximum(numberOfVariants);
                    LOG.info("Start reading raw variant vcf files...");
                    ProcessorUtils.newRepeatingFilteredProcessorWithInMemoryCacheAndRepository(variantIdToVariantInfo, VariantInfo.class)
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
        return StreamUtils.concat(EnsemblFTPUtils.load()
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
    }

    public static class Index
    {
        private MapElementRepository<String, VariantInfo> variantIdToVariantInfo = ElementRepository.ofNonSupplied(new ConcurrentHashMap<>());

        public Index()
        {
            super();
        }

        public Index(MapElementRepository<String, VariantInfo> variantIdToVariantInfo)
        {
            super();
            this.variantIdToVariantInfo = variantIdToVariantInfo;
        }

        public NullOptional<VariantInfo> get(String variantId)
        {
            return this.variantIdToVariantInfo.get(variantId);
        }
    }

    public Optional<VariantInfo> getVariantInfo(String species, String variantId)
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
}