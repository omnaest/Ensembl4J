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
package org.omnaest.genetics.ensembl.rest;

import java.util.concurrent.TimeUnit;

import org.omnaest.genetics.ensembl.EnsemblUtils;
import org.omnaest.genetics.ensembl.domain.raw.ExonRegions;
import org.omnaest.genetics.ensembl.domain.raw.ExternalXRefs;
import org.omnaest.genetics.ensembl.domain.raw.Lookup;
import org.omnaest.genetics.ensembl.domain.raw.RegionMappings;
import org.omnaest.genetics.ensembl.domain.raw.Sequence;
import org.omnaest.genetics.ensembl.domain.raw.Sequences;
import org.omnaest.genetics.ensembl.domain.raw.SpeciesList;
import org.omnaest.genetics.ensembl.domain.raw.Transcripts;
import org.omnaest.genetics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genetics.ensembl.domain.raw.Variations;
import org.omnaest.genetics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestClient.Proxy;
import org.omnaest.utils.rest.client.internal.JSONRestClient;

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

        EnsembleRESTAccessor usingCache(Cache cache);

        XRefs getXRefs(String species, String symbol);

        XRefs getXRefs(String id);

        SpeciesList getSpecies();

        /**
         * Returns the genome DNA sequence
         * 
         * @param id
         * @return
         */
        Sequence getDNASequence(String id);

        /**
         * Returns the cDNA sequence
         * 
         * @param id
         * @return
         */
        Sequences getCodingDNASequence(String id);

        /**
         * Returns the amino acid sequence of the protein
         * 
         * @param id
         * @return
         */
        Sequences getProteinSequences(String id);

        Variations getVariations(String id);

        Transcripts getTranscripts(String id);

        ExonRegions getExonRegions(String id);

        /**
         * Returns the mapping between regions of two reference genomes (assembly)
         * 
         * @param species
         * @param sourceReferenceAssembly
         * @param targetReferenceAssembly
         * @param chromosome
         * @param start
         * @param end
         * @return
         */
        RegionMappings getRegionMappings(String species, String sourceReferenceAssembly, String targetReferenceAssembly, String chromosome, long start,
                                         long end);

        /**
         * Returns the lookup of a given id like ENST00000523732
         * 
         * @param id
         * @return
         */
        Lookup getLookUp(String id);

        ExternalXRefs getXRefsForExternalDatabase(String id, String externalDatabase);

        ExternalXRefs getXRefsForExternalDatabase(String id);

        ExternalXRefs getXRefsForExternalDatabaseByName(String species, String name, String externalDatabase);

        VariantInfo getVariantDetails(String species, String variantId);

    }

    public static EnsembleRESTAccessor getInstance()
    {
        return new EnsembleRESTAccessor()
        {
            private Proxy  proxy   = null;
            private String baseUrl = "http://rest.ensembl.org";
            private Cache  cache   = null;

            @Override
            public ExonRegions getExonRegions(String id)
            {
                String url = this.baseUrl + "/overlap/id/" + id + "?feature=exon";
                return this.newRestClient()
                           .requestGet(url, ExonRegions.class);
            }

            @Override
            public RegionMappings getRegionMappings(String species, String sourceReferenceAssembly, String targetReferenceAssembly, String chromosome,
                                                    long start, long end)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("map")
                                       .addPathToken(species)
                                       .addPathToken(sourceReferenceAssembly)
                                       .addPathToken(chromosome + ":" + start + ":" + end)
                                       .addPathToken(targetReferenceAssembly)
                                       .build();
                return restClient.requestGet(url, RegionMappings.class);
            }

            @Override
            public Variations getVariations(String id)
            {
                String url = this.baseUrl + "/overlap/id/" + id + "?feature=variation";
                return this.newRestClient()
                           .requestGet(url, Variations.class);
            }

            @Override
            public Transcripts getTranscripts(String id)
            {
                String url = this.baseUrl + "/overlap/id/" + id + "?feature=transcript";
                return this.newRestClient()
                           .requestGet(url, Transcripts.class);
            }

            @Override
            public Sequence getDNASequence(String id)
            {
                String url = this.baseUrl + "/sequence/id/" + id;
                return this.newRestClient()
                           .requestGet(url, Sequence.class);
            }

            @Override
            public Sequences getCodingDNASequence(String id)
            {
                String url = this.baseUrl + "/sequence/id/" + id + "?type=cdna&multiple_sequences=true";
                return this.newRestClient()
                           .requestGet(url, Sequences.class);
            }

            @Override
            public Sequences getProteinSequences(String id)
            {
                String url = this.baseUrl + "/sequence/id/" + id + "?type=protein&multiple_sequences=true";
                return this.newRestClient()
                           .requestGet(url, Sequences.class);
            }

            @Override
            public VariantInfo getVariantDetails(String species, String variantId)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("variation")
                                       .addPathToken(species)
                                       .addPathToken(variantId)
                                       .addQueryParameter("phenotypes", "1")
                                       .build();

                return restClient.requestGet(url, VariantInfo.class);
            }

            private RestClient newRestClient()
            {
                return new JSONRestClient().withProxy(this.proxy)
                                           .withCache(this.cache)
                                           .withRetry(10, 6, TimeUnit.SECONDS);
            }

            @Override
            public SpeciesList getSpecies()
            {
                String url = this.baseUrl + "/info/species";
                return this.newRestClient()
                           .requestGet(url, SpeciesList.class);
            }

            @Override
            public XRefs getXRefs(String species, String symbol)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("xrefs")
                                       .addPathToken("symbol")
                                       .addPathToken(species)
                                       .addPathToken(symbol)
                                       .build();
                return restClient.requestGet(url, XRefs.class);
            }

            @Override
            public XRefs getXRefs(String id)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("xrefs")
                                       .addPathToken("id")
                                       .addPathToken(id)
                                       .build();
                return restClient.requestGet(url, XRefs.class);
            }

            @Override
            public ExternalXRefs getXRefsForExternalDatabase(String id, String externalDatabase)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("xrefs")
                                       .addPathToken("id")
                                       .addPathToken(id)
                                       .addQueryParameter("external_db", externalDatabase)
                                       .build();
                return restClient.requestGet(url, ExternalXRefs.class);
            }

            @Override
            public ExternalXRefs getXRefsForExternalDatabaseByName(String species, String name, String externalDatabase)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("xrefs")
                                       .addPathToken("name")
                                       .addPathToken(species)
                                       .addPathToken(name)
                                       .addQueryParameter("external_db", externalDatabase)
                                       .build();
                return restClient.requestGet(url, ExternalXRefs.class);
            }

            @Override
            public ExternalXRefs getXRefsForExternalDatabase(String id)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("xrefs")
                                       .addPathToken("id")
                                       .addPathToken(id)
                                       .build();
                return restClient.requestGet(url, ExternalXRefs.class);
            }

            @Override
            public Lookup getLookUp(String id)
            {
                RestClient restClient = this.newRestClient();
                String url = restClient.urlBuilder()
                                       .setBaseUrl(this.baseUrl)
                                       .addPathToken("lookup")
                                       .addPathToken("id")
                                       .addPathToken(id)
                                       .build();
                return restClient.requestGet(url, Lookup.class);
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
