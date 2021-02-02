/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.genomics.ensembl;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.omnaest.genomics.ensembl.domain.ClinicalSignificance;
import org.omnaest.genomics.ensembl.domain.Exon;
import org.omnaest.genomics.ensembl.domain.GeneAccessor;
import org.omnaest.genomics.ensembl.domain.GeneLocation;
import org.omnaest.genomics.ensembl.domain.ProteinTranscriptAccessor;
import org.omnaest.genomics.ensembl.domain.Range;
import org.omnaest.genomics.ensembl.domain.SpeciesAccessor;
import org.omnaest.genomics.ensembl.domain.Variant;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.genomics.ensembl.domain.VariantDetail;
import org.omnaest.genomics.ensembl.domain.raw.BioType;
import org.omnaest.genomics.ensembl.domain.raw.ExonRegions;
import org.omnaest.genomics.ensembl.domain.raw.ExternalXRef;
import org.omnaest.genomics.ensembl.domain.raw.ExternalXRefs;
import org.omnaest.genomics.ensembl.domain.raw.RegionLocation;
import org.omnaest.genomics.ensembl.domain.raw.RegionMappings;
import org.omnaest.genomics.ensembl.domain.raw.Sequence;
import org.omnaest.genomics.ensembl.domain.raw.Sequences;
import org.omnaest.genomics.ensembl.domain.raw.Species;
import org.omnaest.genomics.ensembl.domain.raw.SpeciesList;
import org.omnaest.genomics.ensembl.domain.raw.Transcript;
import org.omnaest.genomics.ensembl.domain.raw.Transcripts;
import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genomics.ensembl.domain.raw.Variations;
import org.omnaest.genomics.ensembl.domain.raw.XRefs;
import org.omnaest.genomics.ensembl.internal.VariantInfoIndex;
import org.omnaest.genomics.ensembl.rest.EnsemblRESTUtils;
import org.omnaest.genomics.ensembl.rest.EnsemblRESTUtils.EnsembleRESTAccessor;
import org.omnaest.utils.CollectorUtils;
import org.omnaest.utils.ExceptionUtils;
import org.omnaest.utils.ExceptionUtils.RuntimeExceptionHandler;
import org.omnaest.utils.ListUtils;
import org.omnaest.utils.MatcherUtils;
import org.omnaest.utils.MatcherUtils.Match;
import org.omnaest.utils.ObjectUtils;
import org.omnaest.utils.PredicateUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.internal.JsonFolderFilesCache;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.repository.MapElementRepository;
import org.omnaest.utils.rest.client.RestClient.Proxy;
import org.omnaest.utils.rest.client.RestHelper.RESTAccessExeption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see EnsemblRESTUtils
 * @author omnaest
 */
public class EnsemblUtils
{
    static final Logger                  LOG                   = LoggerFactory.getLogger(EnsemblUtils.class);
    private static File                  localDefaultCacheFile = new File("cache/ensembl");
    private static Function<File, Cache> cacheFactory          = (file) -> new JsonFolderFilesCache(file).withNativeByteArrayStorage(true);
    private static Supplier<Cache>       cacheSupplier         = CachedElement.of(() -> cacheFactory.apply(localDefaultCacheFile));

    public static interface EnsemblDataSetAccessor
    {

        Stream<SpeciesAccessor> getSpecies();

        Optional<SpeciesAccessor> findSpecies(String name);

        SpeciesAccessor getHuman();

        EnsemblDataSetAccessor usingCache(Cache cache);

        /**
         * @see EnsemblUtils#getLocalCacheManager()
         * @return
         */
        EnsemblDataSetAccessor usingLocalCache();

        EnsemblDataSetAccessor withProxy(Proxy proxy);

        EnsemblDataSetAccessor usingFTPLargeVariationFileIndexSupport();

        EnsemblDataSetAccessor usingFTPLargeVariationFileIndexSupportWithRepositoryProvider(Function<String, MapElementRepository<String, VariantInfo>> repositoryProvider);

        EnsemblDataSetAccessor withVariantIdFilter(Predicate<String> variantFilter);

    }

    public static interface CacheManager
    {

        void setLocalDefaultCacheFile(File cacheFile);

        void setDefaultCacheFactory(Supplier<Cache> factory);

        void setDefaultCacheFactory(Function<File, Cache> factory);

    }

    public static EnsemblDataSetAccessor getInstance()
    {
        return new EnsemblDataSetAccessor()
        {
            private EnsembleRESTAccessor restAccessor     = EnsemblRESTUtils.getInstance();
            private VariantInfoIndex     variantInfoIndex = VariantInfoIndex.getInstance();

            @Override
            public EnsemblDataSetAccessor withProxy(Proxy proxy)
            {
                this.restAccessor.withProxy(proxy);
                return this;
            }

            @Override
            public EnsemblDataSetAccessor usingCache(Cache cache)
            {
                this.restAccessor.usingCache(cache);
                return this;
            }

            @Override
            public EnsemblDataSetAccessor usingLocalCache()
            {
                this.variantInfoIndex.usingLocalCache();
                return this.usingCache(cacheSupplier.get());
            }

            @Override
            public EnsemblDataSetAccessor usingFTPLargeVariationFileIndexSupport()
            {
                this.variantInfoIndex.enable();
                return this;
            }

            @Override
            public EnsemblDataSetAccessor usingFTPLargeVariationFileIndexSupportWithRepositoryProvider(Function<String, MapElementRepository<String, VariantInfo>> repositoryProvider)
            {
                this.variantInfoIndex.withRepositoryProvider(repositoryProvider)
                                     .enable();
                return this;
            }

            @Override
            public EnsemblDataSetAccessor withVariantIdFilter(Predicate<String> variantIdFilter)
            {
                this.variantInfoIndex.withVariantIdFilter(variantIdFilter);
                return this;
            }

            @Override
            public Optional<SpeciesAccessor> findSpecies(String name)
            {
                return this.getSpecies()
                           .filter(species -> StringUtils.equalsIgnoreCase(name, species.getName()) || StringUtils.contains(name, species.getName())
                                   || StringUtils.contains(name, species.getDisplayName()) || StringUtils.equalsIgnoreCase(name, species.getDisplayName())
                                   || species.getAliases()
                                             .anyMatch(alias -> StringUtils.equalsIgnoreCase(name, alias)))
                           .findFirst();
            }

            @Override
            public SpeciesAccessor getHuman()
            {
                return this.findSpecies("human")
                           .get();
            }

            private Supplier<List<SpeciesAccessor>> species = CachedElement.of(() ->
            {
                SpeciesList speciesList = this.restAccessor.getSpecies();
                if (speciesList != null && speciesList.getSpecies() != null)
                {
                    return speciesList.getSpecies()
                                      .stream()
                                      .map(rawSpecies -> this.createSpeciesAccessor(rawSpecies))
                                      .collect(Collectors.toList());
                }
                else
                {
                    return Collections.<SpeciesAccessor>emptyList();
                }
            });

            @Override
            public Stream<SpeciesAccessor> getSpecies()
            {
                return this.species.get()
                                   .stream();
            }

            private SpeciesAccessor createSpeciesAccessor(Species rawSpecies)
            {
                return new SpeciesAccessor()
                {
                    @Override
                    public String getName()
                    {
                        return rawSpecies.getCommonName();
                    }

                    @Override
                    public String getDisplayName()
                    {
                        return rawSpecies.getDisplayName();
                    }

                    @Override
                    public Stream<String> getAliases()
                    {
                        return rawSpecies.getAliases()
                                         .stream();
                    }

                    @Override
                    public Optional<GeneAccessor> findGene(String symbol)
                    {
                        XRefs xRefs = restAccessor.getXRefs(rawSpecies.getName(), symbol);

                        return xRefs.stream()
                                    .filter(xref -> StringUtils.equalsIgnoreCase("gene", xref.getType()))
                                    .map(xref -> this.createGeneAccessor(xref.getId()))
                                    .findFirst();
                    }

                    private RuntimeExceptionHandler restAccessExceptionHandler = e ->
                    {
                        if (e instanceof RESTAccessExeption)
                        {
                            if (((RESTAccessExeption) e).getStatusCode() != 400)
                            {
                                throw e;
                            }
                        }
                        else
                        {
                            throw e;
                        }
                    };

                    private GeneAccessor createGeneAccessor(String id)
                    {
                        Sequence rawSequence = ExceptionUtils.executeSilent(() -> restAccessor.getDNASequence(id), this.restAccessExceptionHandler);
                        Sequences proteinSequences = ExceptionUtils.executeSilent(() -> restAccessor.getProteinSequences(id), this.restAccessExceptionHandler);
                        ExonRegions exonRegions = ExceptionUtils.executeSilent(() -> restAccessor.getExonRegions(id), this.restAccessExceptionHandler);
                        GeneLocation geneLocation = this.determineGeneLocation(rawSequence);
                        ExternalXRefs xRefs = ExceptionUtils.executeSilent(() -> restAccessor.getXRefsForExternalDatabase(id, "WikiGene"),
                                                                           this.restAccessExceptionHandler);

                        //                        Map<String, ExonRegion> exonIdToExonRegion = this.determineExonRegions();

                        return new GeneAccessor()
                        {
                            @Override
                            public String getName()
                            {
                                String retval = null;
                                if (xRefs != null && !xRefs.isEmpty())
                                {
                                    retval = xRefs.stream()
                                                  .map(xref -> xref.getDescription())
                                                  .filter(StringUtils::isNotBlank)
                                                  .findFirst()
                                                  .orElse("");
                                }

                                return retval;
                            }

                            public String getSymbol()
                            {
                                String retval = null;
                                if (xRefs != null && !xRefs.isEmpty())
                                {
                                    retval = xRefs.stream()
                                                  .map(xref -> xref.getDisplayId())
                                                  .filter(StringUtils::isNotBlank)
                                                  .findFirst()
                                                  .orElse("");
                                }

                                return retval;
                            }

                            @Override
                            public String getUniprotId()
                            {
                                String retval = null;
                                String speciesName = rawSpecies.getName();
                                String geneName = this.getSymbol();
                                ExternalXRefs externalXRefs = restAccessor.getXRefsForExternalDatabaseByName(speciesName, geneName, "Uniprot_gn");
                                if (externalXRefs != null && !externalXRefs.isEmpty())
                                {
                                    ExternalXRef externalXRef = externalXRefs.get(0);
                                    if (externalXRef != null)
                                    {
                                        retval = externalXRef.getPrimaryId();
                                    }
                                }
                                return retval;
                            }

                            @Override
                            public String getDescription()
                            {
                                return rawSequence.getDescription();
                            }

                            @Override
                            public String getDNASequence()
                            {
                                return rawSequence.getSequence();
                            }

                            @Override
                            public Stream<String> getcDNASequences()
                            {
                                Sequences sequences = ExceptionUtils.executeSilent(() -> restAccessor.getCodingDNASequence(id), restAccessExceptionHandler);
                                return sequences.stream()
                                                .map(seq -> seq.getSequence());
                            }

                            @Override
                            public Stream<String> getProteinSequences()
                            {
                                return proteinSequences != null ? proteinSequences.stream()
                                                                                  .filter(seq -> restAccessor.getLookUp(restAccessor.getLookUp(seq.getId())
                                                                                                                                    .getParent())
                                                                                                             .hasBiotype(BioType.PROTEIN_CODING))
                                                                                  .filter(seq -> seq != null)
                                                                                  .map(seq -> seq.getSequence())
                                        : Stream.empty();
                            }

                            @Override
                            public GeneLocation getLocation()
                            {
                                return geneLocation;
                            }

                            @Override
                            public GeneLocation getLocation(String referenceAssembly)
                            {
                                GeneLocation retval = null;

                                GeneLocation mainLocation = this.getLocation();
                                if (mainLocation != null)
                                {
                                    String chromosome = mainLocation.getChromosome();
                                    long start = mainLocation.getPosition()
                                                             .getStart();
                                    long end = mainLocation.getPosition()
                                                           .getEnd();
                                    int strand = mainLocation.getStrand();
                                    RegionMappings regionMappings = restAccessor.getRegionMappings(rawSpecies.getName(), mainLocation.getReferenceAssembly(),
                                                                                                   referenceAssembly, chromosome, start, end);
                                    if (regionMappings != null && regionMappings.getMappings() != null && !regionMappings.getMappings()
                                                                                                                         .isEmpty())
                                    {
                                        RegionLocation regionLocation = regionMappings.getMappings()
                                                                                      .get(0)
                                                                                      .getMapped();
                                        if (regionLocation == null)
                                        {
                                            LOG.warn("No mapping region found for " + referenceAssembly + " " + chromosome + ":" + start + ":" + end);
                                        }
                                        else
                                        {
                                            if (!StringUtils.equalsIgnoreCase(regionLocation.getAssembly(), referenceAssembly))
                                            {
                                                LOG.warn("Incorrect mapping resolved: " + regionLocation);
                                            }
                                            else
                                            {
                                                retval = new GeneLocation(regionLocation.getSequenceRegionName(), regionLocation.getAssembly(),
                                                                          new Range(regionLocation.getStart(), regionLocation.getEnd()), strand);
                                            }
                                        }
                                    }
                                }

                                return retval;
                            }

                            @Override
                            public List<Variant> getVariants()
                            {

                                Variations variations = restAccessor.getVariations(id);
                                return variations.stream()
                                                 .map(variation -> new Variant(new Range(variation.getStart(), variation.getEnd()),
                                                                               ListUtils.defaultIfNull(variation.getAlleles())))
                                                 .collect(Collectors.toList());
                            }

                            @Override
                            public List<Exon> getExons()
                            {
                                Map<String, String> exonIdToSequence = determineExonSequences(exonRegions);
                                return exonRegions.stream()
                                                  .map(region -> new Exon(new Range(region.getStart(), region.getEnd()),
                                                                          exonIdToSequence.get(region.getExonId())))
                                                  .collect(Collectors.toList());
                            }

                            @Override
                            public Stream<ProteinTranscriptAccessor> getProteinTranscripts()
                            {
                                Transcripts transcripts = restAccessor.getTranscripts(id);
                                return transcripts.stream()
                                                  .filter(transcript -> StringUtils.equalsIgnoreCase(transcript.getParent(), id))
                                                  .filter(transcript -> transcript.hasBiotype(BioType.PROTEIN_CODING))
                                                  .map(rawTanscript -> this.newTranscriptAccessor(rawTanscript));
                            }

                            private ProteinTranscriptAccessor newTranscriptAccessor(Transcript rawTanscript)
                            {
                                return new ProteinTranscriptAccessor()
                                {
                                    @Override
                                    public String getProteinSequence()
                                    {
                                        return restAccessor.getProteinSequences(rawTanscript.getId())
                                                           .get(0)
                                                           .getSequence();
                                    }

                                    @Override
                                    public List<String> getVariantSequences()
                                    {
                                        throw new UnsupportedOperationException();
                                    }
                                };
                            }

                        };

                    }

                    private Map<String, String> determineExonSequences(ExonRegions exonRegions)
                    {
                        List<Sequence> sequences = exonRegions.stream()
                                                              .map(region -> region.getExonId())
                                                              .distinct()
                                                              .map(exonId -> restAccessor.getDNASequence(exonId))
                                                              .collect(Collectors.toList());
                        return sequences.stream()
                                        .collect(Collectors.toMap(seq -> seq.getId(), seq -> seq.getSequence()));
                    }

                    private GeneLocation determineGeneLocation(Sequence rawSequence)
                    {
                        GeneLocation geneLocation = null;

                        if (rawSequence != null)
                        {
                            String locationStr = rawSequence.getDescription();
                            if (locationStr != null)
                            {
                                //	chromosome:GRCh38:5:79069717:79089466:1
                                String[] tokens = StringUtils.splitPreserveAllTokens(locationStr, ":");
                                if (tokens.length >= 6)
                                {
                                    String chromosome = tokens[2];
                                    String referenceAssembly = tokens[1];
                                    Range position = new Range(NumberUtils.toLong(tokens[3]), NumberUtils.toLong(tokens[4]));
                                    int strand = NumberUtils.toInt(tokens[5]);
                                    geneLocation = new GeneLocation(chromosome, referenceAssembly, position, strand);
                                }
                            }
                        }

                        return geneLocation;
                    }

                    @Override
                    public VariantDetail findVariantDetail(String variantId)
                    {
                        CachedElement<VariantInfo> rawRESTVariantDetail = CachedElement.of(() -> restAccessor.getVariantDetails(rawSpecies.getName(),
                                                                                                                                variantId));
                        CachedElement<Optional<VariantInfo>> rawFtpVariantDetail = CachedElement.of(() -> variantInfoIndex.getVariantInfo(rawSpecies.getName(),
                                                                                                                                          variantId));

                        return new VariantDetail()
                        {
                            private <E> E getVariantInfo(Function<VariantInfo, E> methodReference)
                            {
                                return rawFtpVariantDetail.get()
                                                          .map(methodReference)
                                                          .filter(value -> value instanceof List ? !((List<?>) value).isEmpty() : true)
                                                          .filter(value -> value instanceof Set ? !((Set<?>) value).isEmpty() : true)
                                                          .filter(value -> value instanceof Map ? !((Map<?, ?>) value).isEmpty() : true)
                                                          .orElseGet(() -> ObjectUtils.getIfNotNull(rawRESTVariantDetail.get(), methodReference));
                            }

                            @Override
                            public List<String> getSynonyms()
                            {
                                return this.getVariantInfo(VariantInfo::getSynonyms);
                            }

                            @Override
                            public int getProteinPosition()
                            {
                                int retval = -1;

                                //NP_115990.3:p.Ala76Val
                                List<String> synonyms = this.getSynonyms();
                                for (String synonym : synonyms)
                                {
                                    Optional<Match> match = MatcherUtils.matcher()
                                                                        .of(Pattern.compile("[_a-zA-Z0-9]+\\:p\\.([a-zA-Z]+)([0-9]+)([a-zA-Z]+)"))
                                                                        .matchAgainst(synonym);
                                    if (match.isPresent())
                                    {
                                        retval = NumberUtils.toInt(match.get()
                                                                        .getGroups()
                                                                        .get(2),
                                                                   -1);
                                        break;
                                    }
                                }

                                return retval;
                            }

                            @Override
                            public Set<ClinicalSignificance> getClinicalSignificances()
                            {
                                Set<String> clinicalSignifance = this.getVariantInfo(VariantInfo::getClinicalSignifance);
                                return ObjectUtils.getOrDefaultIfNotNull(clinicalSignifance, () -> clinicalSignifance.stream()
                                                                                                                     .map(significance -> Arrays.asList(ClinicalSignificance.values())
                                                                                                                                                .stream()
                                                                                                                                                .filter(isignificance -> isignificance.matches(significance))
                                                                                                                                                .findFirst()
                                                                                                                                                .orElse(ClinicalSignificance.OTHER))
                                                                                                                     .collect(CollectorUtils.toSortedSet()),
                                                                         () -> Collections.emptySet());
                            }

                            @Override
                            public VariantConsequence getConsequence()
                            {
                                String currentConsequence = this.getVariantInfo(VariantInfo::getConsequence);
                                return Arrays.asList(VariantConsequence.values())
                                             .stream()
                                             .filter(consequence -> consequence.matches(currentConsequence))
                                             .findFirst()
                                             .orElse(VariantConsequence.UNKNOWN);
                            }

                            @Override
                            public SortedSet<String> getTraits()
                            {
                                return Optional.ofNullable(this.getVariantInfo(VariantInfo::getPhenotypes))
                                               .orElse(Collections.emptyList())
                                               .stream()
                                               .map(pt -> pt.getTrait())
                                               .filter(PredicateUtils.notBlank())
                                               .collect(CollectorUtils.toSortedSet(new TreeSet<>()));
                            }

                            @Override
                            public double getMinorAlleleFrequency()
                            {
                                return NumberUtils.toDouble(this.getVariantInfo(VariantInfo::getMaf));
                            }

                        };
                    }

                };
            }

        };
    }

    /**
     * Returns the {@link CacheManager} which is used when {@link EnsemblDataSetAccessor#usingLocalCache()} is called
     * 
     * @return
     */
    public static CacheManager getLocalCacheManager()
    {
        return new CacheManager()
        {
            @Override
            public void setDefaultCacheFactory(Supplier<Cache> factory)
            {
                cacheFactory = file -> factory.get();
            }

            @Override
            public void setLocalDefaultCacheFile(File cacheFile)
            {
                localDefaultCacheFile = cacheFile;
            }

            @Override
            public void setDefaultCacheFactory(Function<File, Cache> factory)
            {
                cacheFactory = factory;
            }

        };
    }

}
