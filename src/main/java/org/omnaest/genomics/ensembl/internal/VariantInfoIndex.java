package org.omnaest.genomics.ensembl.internal;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.genetics.vcf.domain.VCFRecord;
import org.omnaest.genetics.vcf.domain.VCFRecord.AdditionalInfo;
import org.omnaest.genomics.ensembl.domain.ClinicalSignificance;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genomics.ensembl.ftp.EnsemblFTPUtils;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.ComparatorUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.counter.Counter;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.MapElementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariantInfoIndex
{
    private static final Logger LOG = LoggerFactory.getLogger(VariantInfoIndex.class);

    private Cache                                                       cache              = CacheUtils.newNoOperationCache();
    private Map<String, Index>                                          speciesToIndexData = new ConcurrentHashMap<String, Index>();
    private Function<String, Index>                                     indexDataProvider  = species -> new Index();
    private Function<String, MapElementRepository<String, VariantInfo>> repositoryProvider = species -> ElementRepository.ofNonSupplied(new ConcurrentHashMap<>());
    private Predicate<VCFRecord>                                        variantFilter      = PredicateUtils.allMatching();

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
            BiConsumer<VCFRecord, VariantInfo> variationVcfRecordAndVariantInfoMerger = (record, variantInfo) ->
            {
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
            };

            MapElementRepository<String, VariantInfo> variantIdToVariantInfo = this.repositoryProvider.apply(species);
            Counter includedVariantsCounter = Counter.fromZero();
            Counter processedVariantsCounter = Counter.fromZero();
            try (Stream<VCFRecord> records = Stream.concat(EnsemblFTPUtils.load()
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
                                                                          .getRecords()))
            {
                LOG.info("Start reading raw variant vcf files...");
                StreamUtils.framedNonNullAsList(1000, records.peek(record -> processedVariantsCounter.increment()
                                                                                                     .ifModulo(100000,
                                                                                                               count -> LOG.info("    Processed variation index records: "
                                                                                                                       + count)))
                                                             .filter(this.variantFilter)
                                                             .peek(record -> includedVariantsCounter.increment()
                                                                                                    .ifModulo(100000,
                                                                                                              count -> LOG.info("Current number of included variation index records: "
                                                                                                                      + count))))
                           .forEach(recordsBatch ->
                           {
                               Map<String, VariantInfo> existingVariantIdToVariantInfo = variantIdToVariantInfo.getAll(recordsBatch.stream()
                                                                                                                                   .map(record -> record.getId())
                                                                                                                                   .collect(Collectors.toList()));
                               recordsBatch.forEach(record -> variationVcfRecordAndVariantInfoMerger.accept(record,
                                                                                                            existingVariantIdToVariantInfo.computeIfAbsent(record.getId(),
                                                                                                                                                           id -> new VariantInfo())));
                               variantIdToVariantInfo.putAll(existingVariantIdToVariantInfo);
                           });
                LOG.info("...finished reading raw variant vcf files");
            }
            catch (Exception e)
            {
                LOG.error("Unexpected error during indexing of variants", e);
                throw e;
            }
            return new Index(variantIdToVariantInfo);
        };
        return this;
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