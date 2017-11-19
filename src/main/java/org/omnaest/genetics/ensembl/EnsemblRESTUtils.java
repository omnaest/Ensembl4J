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

import org.omnaest.genetics.ensembl.domain.raw.Sequence;
import org.omnaest.genetics.ensembl.domain.raw.SpeciesList;
import org.omnaest.genetics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.JSONRestClient;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestClient.Proxy;

/**
 * Raw request utils for the Ensembl REST API. <br>
 * <br>
 * Consider using the {@link EnsemblUtils} instead which represents a higher layer api
 * 
 * @see EnsemblUtils
 * @author omnaest
 */
public class EnsemblRESTUtils
{

	public static interface EnsembleRESTAccessor
	{

		EnsembleRESTAccessor withBaseUrl(String baseUrl);

		EnsembleRESTAccessor withProxy(Proxy proxy);

		XRefs getXRefs(String species, String symbol);

		SpeciesList getSpecies();

		Sequence getSequence(String id);

		EnsembleRESTAccessor usingCache(Cache cache);

	}

	public static EnsembleRESTAccessor getInstance()
	{
		return new EnsembleRESTAccessor()
		{
			private Proxy	proxy	= null;
			private String	baseUrl	= "http://rest.ensembl.org";
			private Cache	cache	= null;

			@Override
			public Sequence getSequence(String id)
			{
				String url = this.baseUrl + "/sequence/id/" + id;
				return this	.newRestClient()
							.requestGet(url, Sequence.class);
			}

			private RestClient newRestClient()
			{
				return new JSONRestClient()	.withProxy(this.proxy)
											.withCache(this.cache);
			}

			@Override
			public SpeciesList getSpecies()
			{
				String url = this.baseUrl + "/info/species";
				return this	.newRestClient()
							.requestGet(url, SpeciesList.class);
			}

			@Override
			public XRefs getXRefs(String species, String symbol)
			{
				RestClient restClient = this.newRestClient();
				String url = restClient	.urlBuilder()
										.setBaseUrl(this.baseUrl)
										.addPathToken("xrefs")
										.addPathToken("symbol")
										.addPathToken(species)
										.addPathToken(symbol)
										.build();
				return restClient.requestGet(url, XRefs.class);
			}

			@Override
			public EnsembleRESTAccessor withProxy(Proxy proxy)
			{
				this.proxy = proxy;
				return this;
			}

			@Override
			public EnsembleRESTAccessor withBaseUrl(String baseUrl)
			{
				this.baseUrl = baseUrl;
				return this;
			}

			@Override
			public EnsembleRESTAccessor usingCache(Cache cache)
			{
				this.cache = cache;
				return this;
			}

		};
	}

}
