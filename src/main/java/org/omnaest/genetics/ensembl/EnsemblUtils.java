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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.omnaest.genetics.ensembl.EnsemblRESTUtils.EnsembleRESTAccessor;
import org.omnaest.genetics.ensembl.domain.GeneAccessor;
import org.omnaest.genetics.ensembl.domain.GeneAccessor.GeneLocation;
import org.omnaest.genetics.ensembl.domain.GeneAccessor.Range;
import org.omnaest.genetics.ensembl.domain.SpeciesAccessor;
import org.omnaest.genetics.ensembl.domain.raw.Sequence;
import org.omnaest.genetics.ensembl.domain.raw.Species;
import org.omnaest.genetics.ensembl.domain.raw.SpeciesList;
import org.omnaest.genetics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.cache.JsonFolderFilesCache;
import org.omnaest.utils.element.CachedElement;
import org.omnaest.utils.rest.client.RestClient.Proxy;

/**
 * @see EnsemblRESTUtils
 * @author omnaest
 */
public class EnsemblUtils
{
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

					private GeneAccessor createGeneAccessor(String id)
					{
						Sequence rawSequence = restAccessor.getSequence(id);
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
							public String getSequence()
							{
								return rawSequence.getSequence();
							}

							@Override
							public GeneLocation getLocation()
							{
								return geneLocation;
							}
						};
					}

					private GeneLocation determineGeneLocation(Sequence rawSequence)
					{
						GeneLocation geneLocation = null;

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
								geneLocation = new GeneAccessor.GeneLocation(chromosome, referenceAssembly, position);
							}
						}

						return geneLocation;
					}

				};
			}
		};
	}

}
