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
package org.omnaest.genomics.ensembl.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genomics.ensembl.domain.raw.BioType;
import org.omnaest.genomics.ensembl.domain.raw.ExonRegions;
import org.omnaest.genomics.ensembl.domain.raw.ExternalXRefs;
import org.omnaest.genomics.ensembl.domain.raw.Lookup;
import org.omnaest.genomics.ensembl.domain.raw.RegionMappings;
import org.omnaest.genomics.ensembl.domain.raw.Sequence;
import org.omnaest.genomics.ensembl.domain.raw.Sequences;
import org.omnaest.genomics.ensembl.domain.raw.Transcripts;
import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;
import org.omnaest.genomics.ensembl.domain.raw.Variations;
import org.omnaest.genomics.ensembl.domain.raw.XRef;
import org.omnaest.genomics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.rest.client.RestClient;

@Ignore
public class EnsemblRESTUtilsTest
{

    @Test
    public void testGetDNASequence() throws Exception
    {
        Sequence sequence = EnsemblRESTUtils.getInstance()
                                            .getDNASequence("ENSG00000145692");

        //		System.out.println(sequence	.getSequence()
        //									.substring(0, 100));
        assertNotNull(sequence);

    }

    @Test
    public void testGetCDNASequence() throws Exception
    {
        Sequences sequence = EnsemblRESTUtils.getInstance()
                                             .getCodingDNASequence("ENSG00000145692");

        //		System.out.println(sequence	.get(0)
        //									.getSequence()
        //									.substring(0, 100));
        assertNotNull(sequence);
    }

    @Test
    @Ignore
    public void testGetSpecies() throws Exception
    {
        String json = JSONHelper.prettyPrint(EnsemblRESTUtils.getInstance()
                                                             .getSpecies());
        System.out.println(json);
    }

    @Test
    @Ignore
    public void testGetXRefs() throws Exception
    {
        XRefs xRefs = EnsemblRESTUtils.getInstance()
                                      .withProxy(new RestClient.FiddlerLocalhostProxy())
                                      .getXRefs("homo_sapiens", "BHMT");
        System.out.println(JSONHelper.prettyPrint(xRefs));
    }

    @Test
    @Ignore
    public void testGetXRefsForExternalDB() throws Exception
    {
        XRef xRef = EnsemblRESTUtils.getInstance()
                                    .withProxy(new RestClient.FiddlerLocalhostProxy())
                                    .getXRefs("homo_sapiens", "BHMT")
                                    .get(0);
        ExternalXRefs externalXRefs = EnsemblRESTUtils.getInstance()
                                                      .withProxy(new RestClient.FiddlerLocalhostProxy())
                                                      .getXRefsForExternalDatabase(xRef.getId(), "WikiGene");
        System.out.println(JSONHelper.prettyPrint(externalXRefs));
    }

    @Test
    @Ignore
    public void testGetXRefsForExternalDBByName() throws Exception
    {
        ExternalXRefs externalXRefs = EnsemblRESTUtils.getInstance()
                                                      //                                                      .withProxy(new RestClient.FiddlerLocalhostProxy())
                                                      .getXRefsForExternalDatabaseByName("homo_sapiens", "BHMT", "Uniprot_gn");

        System.out.println(JSONHelper.prettyPrint(externalXRefs));
    }

    @Test
    @Ignore
    public void testGetVariations() throws Exception
    {
        Variations variations = EnsemblRESTUtils.getInstance()
                                                .getVariations("ENSG00000145692");

        System.out.println(JSONHelper.prettyPrint(variations));
    }

    @Test
    public void testGetExonRegions() throws Exception
    {
        ExonRegions regions = EnsemblRESTUtils.getInstance()
                                              .getExonRegions("ENSG00000145692");

        //System.out.println(JSONHelper.prettyPrint(regions));
        assertFalse(regions.isEmpty());
    }

    @Test
    public void testGetProteinSequences() throws Exception
    {
        Sequences sequences = EnsemblRESTUtils.getInstance()
                                              //.withProxy(new RestClient.FiddlerLocalhostProxy())
                                              .getProteinSequences("ENSG00000145692");

        //		System.out.println(sequence	.iterator()
        //									.next()
        //									.getSequence()
        //									.substring(0, 100));
        assertNotNull(sequences);
    }

    @Test
    @Ignore
    public void testGetTranscripts() throws Exception
    {
        Transcripts transcripts = EnsemblRESTUtils.getInstance()
                                                  .getTranscripts("ENSG00000145692");

        System.out.println(JSONHelper.prettyPrint(transcripts));
    }

    @Test
    public void testGetInstance() throws Exception
    {
        RegionMappings regionMappings = EnsemblRESTUtils.getInstance()
                                                        .getRegionMappings("human", "GRCh38", "GRCh37", "5", 79111779, 79132290);

        //System.out.println(JSONHelper.prettyPrint(regionMappings));

        assertEquals(78407602, regionMappings.getMappings()
                                             .get(0)
                                             .getMapped()
                                             .getStart());
        assertEquals(78428113, regionMappings.getMappings()
                                             .get(0)
                                             .getMapped()
                                             .getEnd());
    }

    @Test
    public void testGetLookUp() throws Exception
    {
        Lookup lookUp = EnsemblRESTUtils.getInstance()
                                        .getLookUp("ENST00000523732");
        //System.out.println(JSONHelper.prettyPrint(lookUp));
        assertEquals("protein_coding", lookUp.getBioType());
        assertEquals("ENSG00000132837", lookUp.getParent());
        assertTrue(lookUp.hasBiotype(BioType.PROTEIN_CODING));
    }

    @Test
    @Ignore
    public void testGetVariantDetails() throws Exception
    {
        VariantInfo variant = EnsemblRESTUtils.getInstance()
                                              .getVariantDetails("homo_sapiens", "rs699"
        //                                                             "rs682985"
        );

        System.out.println(JSONHelper.prettyPrint(variant));
    }

    @Test
    public void testGetVariantDetailsBatch() throws Exception
    {
        Map<String, VariantInfo> variantMap = EnsemblRESTUtils.getInstance()
                                                              .getVariantDetails("homo_sapiens", Arrays.asList("rs560833025", "wrongIdentifer"));
        assertEquals(1, variantMap.size());
        assertEquals(true, variantMap.containsKey("rs560833025"));
        assertEquals(true, variantMap.get("rs560833025")
                                     .getSynonyms()
                                     .contains("rs560833025"));
        assertNotNull(variantMap.get("rs560833025")
                                .getConsequence());
        //        System.out.println(JSONHelper.prettyPrint(variantMap));
    }

}
