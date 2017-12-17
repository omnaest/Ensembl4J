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
package org.omnaest.genetics.ensembl;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.omnaest.genetics.ensembl.EnsemblRESTUtils.EnsembleRESTAccessor;
import org.omnaest.genetics.ensembl.domain.Exon;
import org.omnaest.genetics.ensembl.domain.GeneAccessor;
import org.omnaest.genetics.ensembl.domain.GeneLocation;
import org.omnaest.genetics.ensembl.domain.ProteinTranscriptAccessor;
import org.omnaest.genetics.ensembl.domain.Range;
import org.omnaest.genetics.ensembl.domain.SpeciesAccessor;
import org.omnaest.genetics.ensembl.domain.Variant;
import org.omnaest.genetics.ensembl.domain.raw.ExonRegions;
import org.omnaest.genetics.ensembl.domain.raw.RegionLocation;
import org.omnaest.genetics.ensembl.domain.raw.RegionMappings;
import org.omnaest.genetics.ensembl.domain.raw.Sequence;
import org.omnaest.genetics.ensembl.domain.raw.Sequences;
import org.omnaest.genetics.ensembl.domain.raw.Species;
import org.omnaest.genetics.ensembl.domain.raw.SpeciesList;
import org.omnaest.genetics.ensembl.domain.raw.Transcript;
import org.omnaest.genetics.ensembl.domain.raw.Transcript.BioType;
import org.omnaest.genetics.ensembl.domain.raw.Transcripts;
import org.omnaest.genetics.ensembl.domain.raw.Variations;
import org.omnaest.genetics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.ExceptionUtils;
import org.omnaest.utils.ExceptionUtils.RuntimeExceptionHandler;
import org.omnaest.utils.ListUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.JsonFolderFilesCache;
import org.omnaest.utils.element.CachedElement;
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
	private static final Logger LOG = LoggerFactory.getLogger(EnsemblUtils.class);

	public static interface EnsemblDataSetAccessor
	{

		Stream<SpeciesAccessor> getSpecies();

		Optional<SpeciesAccessor> findSpecies(String name);

		EnsemblDataSetAccessor usingCache(Cache cache);

		EnsemblDataSetAccessor usingLocalCache();

		EnsemblDataSetAccessor withProxy(Proxy proxy);

	}

	public static EnsemblDataSetAccessor getInstance()
	{
		return new EnsemblDataSetAccessor()
		{
			private EnsembleRESTAccessor restAccessor = EnsemblRESTUtils.getInstance();

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
				return this.usingCache(new JsonFolderFilesCache(new File("cache/ensembl")));
			}

			@Override
			public Optional<SpeciesAccessor> findSpecies(String name)
			{
				return this	.getSpecies()
							.filter(species -> StringUtils.equalsIgnoreCase(name, species.getName()) || StringUtils.contains(name, species.getName())
									|| StringUtils.contains(name, species.getDisplayName()) || StringUtils.equalsIgnoreCase(name, species.getDisplayName())
									|| species	.getAliases()
												.anyMatch(alias -> StringUtils.equalsIgnoreCase(name, alias)))
							.findFirst();
			}

			private Supplier<List<SpeciesAccessor>> species = CachedElement.of(() ->
			{
				SpeciesList speciesList = this.restAccessor.getSpecies();
				if (speciesList != null && speciesList.getSpecies() != null)
				{
					return speciesList	.getSpecies()
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
				return this.species	.get()
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
						return rawSpecies	.getAliases()
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

						return new GeneAccessor()
						{
							@Override
							public String getName()
							{
								//FIXME how to get the gene name like BHMT?
								throw new UnsupportedOperationException();
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
							public List<String> getProteinSequences()
							{
								return proteinSequences != null ? proteinSequences	.stream()
																					.map(seq -> seq.getSequence())
																					.collect(Collectors.toList())
										: Collections.emptyList();
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
									long start = mainLocation	.getPosition()
																.getStart();
									long end = mainLocation	.getPosition()
															.getEnd();
									RegionMappings regionMappings = restAccessor.getRegionMappings(	rawSpecies.getName(), mainLocation.getReferenceAssembly(),
																									referenceAssembly, chromosome, start, end);
									if (regionMappings != null && regionMappings.getMappings() != null && !regionMappings	.getMappings()
																															.isEmpty())
									{
										RegionLocation regionLocation = regionMappings	.getMappings()
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
												retval = new GeneLocation(	regionLocation.getSequenceRegionName(), regionLocation.getAssembly(),
																			new Range(regionLocation.getStart(), regionLocation.getEnd()));
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
								return variations	.stream()
													.map(variation -> new Variant(	new Range(variation.getStart(), variation.getEnd()),
																					ListUtils.defaultIfNull(variation.getAlleles())))
													.collect(Collectors.toList());
							}

							@Override
							public List<Exon> getExons()
							{
								Map<String, String> exonIdToSequence = determineExonSequences(exonRegions);
								return exonRegions	.stream()
													.map(region -> new Exon(new Range(region.getStart(), region.getEnd()),
																			exonIdToSequence.get(region.getExonId())))
													.collect(Collectors.toList());
							}

							@Override
							public Stream<ProteinTranscriptAccessor> getProteinTranscripts()
							{
								Transcripts transcripts = restAccessor.getTranscripts(id);
								return transcripts	.stream()
													.filter(transcript -> StringUtils.equalsIgnoreCase(transcript.getParent(), id))
													.filter(transcript -> transcript.hasBiotype(BioType.protein_coding))
													.map(rawTanscript -> this.newTranscriptAccessor(rawTanscript));
							}

							private ProteinTranscriptAccessor newTranscriptAccessor(Transcript rawTanscript)
							{
								return new ProteinTranscriptAccessor()
								{
									@Override
									public String getProteinSequence()
									{
										return restAccessor	.getProteinSequences(rawTanscript.getId())
															.get(0)
															.getSequence();
									}

									@Override
									public List<String> getVariantSequences()
									{
										// TODO Auto-generated method stub
										throw new UnsupportedOperationException();
									}
								};
							}

						};

					}

					private Map<String, String> determineExonSequences(ExonRegions exonRegions)
					{
						List<Sequence> sequences = exonRegions	.stream()
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
								if (tokens.length >= 5)
								{
									String chromosome = tokens[2];
									String referenceAssembly = tokens[1];
									Range position = new Range(NumberUtils.toLong(tokens[3]), NumberUtils.toLong(tokens[4]));
									geneLocation = new GeneLocation(chromosome, referenceAssembly, position);
								}
							}
						}

						return geneLocation;
					}

				};
			}
		};
	}

}
