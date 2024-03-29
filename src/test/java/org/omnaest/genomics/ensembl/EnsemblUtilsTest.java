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
package org.omnaest.genomics.ensembl;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genomics.ensembl.domain.Exon;
import org.omnaest.genomics.ensembl.domain.GeneLocation;
import org.omnaest.genomics.ensembl.domain.SpeciesAccessor;
import org.omnaest.genomics.ensembl.domain.Variant;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.utils.JSONHelper;

public class EnsemblUtilsTest
{

    @Test
    @Ignore
    public void testGetInstance() throws Exception
    {
        GeneLocation location = EnsemblUtils.getInstance()
                                            .findSpecies("human")
                                            .get()
                                            .findGene("BHMT")
                                            .get()
                                            .getLocation();
        //System.out.println(location);

        assertEquals(5, location.getChromosome());
        assertEquals("GRCh38", location.getReferenceAssembly());
        assertEquals(79111779, location.getPosition()
                                       .getStart());
        assertEquals(79132290, location.getPosition()
                                       .getEnd());
    }

    @Test
    @Ignore
    public void testGetILocation() throws Exception
    {
        GeneLocation location = EnsemblUtils.getInstance()
                                            .findSpecies("Homo sapiens (Human)")
                                            .get()
                                            .findGene("BHMT")
                                            .get()
                                            .getLocation();
        //System.out.println(location);

        assertEquals("5", location.getChromosome());
        assertEquals("GRCh38", location.getReferenceAssembly());
        assertEquals(79111779, location.getPosition()
                                       .getStart());
        assertEquals(79132290, location.getPosition()
                                       .getEnd());
    }

    @Test
    @Ignore
    public void testGetVariants() throws Exception
    {
        List<Variant> variants = EnsemblUtils.getInstance()
                                             .findSpecies("human")
                                             .get()
                                             .findGene("BHMT")
                                             .get()
                                             .getVariants();
        System.out.println(variants);

    }

    @Test
    @Ignore
    public void testGetUniprotId() throws Exception
    {
        String uniprotId = EnsemblUtils.getInstance()
                                       .findSpecies("human")
                                       .get()
                                       .findGene("BHMT")
                                       .get()
                                       .getUniprotId();
        System.out.println(uniprotId);

    }

    @Test
    @Ignore
    public void testGetExons() throws Exception
    {
        List<Exon> exons = EnsemblUtils.getInstance()
                                       .findSpecies("human")
                                       .get()
                                       .findGene("BHMT")
                                       .get()
                                       .getExons();
        System.out.println(JSONHelper.prettyPrint(exons));

    }

    @Test
    @Ignore
    public void testGetProteinTranscripts() throws Exception
    {
        String proteinSequence = EnsemblUtils.getInstance()
                                             .findSpecies("human")
                                             .get()
                                             .findGene("BHMT")
                                             .get()
                                             .getProteinTranscripts()
                                             .findFirst()
                                             .get()
                                             .getProteinSequence();
        System.out.println(proteinSequence.substring(0, 100));

    }

    @Test
    @Ignore
    public void testReferenceLocation() throws Exception
    {
        GeneLocation location = EnsemblUtils.getInstance()
                                            .findSpecies("human")
                                            .get()
                                            .findGene("BHMT")
                                            .get()
                                            .getLocation("GRCh37");

        assertEquals(78407632, location.getPosition()
                                       .getStart());
        assertEquals(78428111, location.getPosition()
                                       .getEnd());
        assertEquals("GRCh37", location.getReferenceAssembly());
        assertEquals("5", location.getChromosome());
    }

    @Test
    @Ignore
    public void testGetProteinSequences() throws Exception
    {
        List<String> proteinSequences = EnsemblUtils.getInstance()
                                                    .findSpecies("human")
                                                    .get()
                                                    .findGene("DMGDH")
                                                    .get()
                                                    .getProteinSequences()
                                                    .collect(Collectors.toList());

        //		System.out.println(JSONHelper.prettyPrint(proteinSequences));
        assertEquals(2, proteinSequences.size());
    }

    @Test
    @Ignore
    public void testVariantDetail() throws Exception
    {
        SpeciesAccessor speciesAccessor = EnsemblUtils.getInstance()
                                                      .getHuman();

        assertEquals(VariantConsequence.MISSENSE, speciesAccessor.findVariantDetail("rs682985")
                                                                 .getConsequence());
        assertEquals(VariantConsequence._3_PRIME_UTR, speciesAccessor.findVariantDetail("rs6114998")
                                                                     .getConsequence());
    }

    @Test
    @Ignore
    public void testVariantDetailUsingCacheAndRest() throws Exception
    {
        SpeciesAccessor speciesAccessor = EnsemblUtils.getInstance()
                                                      .usingLocalCache()
                                                      .getHuman();

        assertEquals(VariantConsequence.MISSENSE, speciesAccessor.findVariantDetail("rs682985")
                                                                 .getConsequence());
        assertEquals(VariantConsequence._3_PRIME_UTR, speciesAccessor.findVariantDetail("rs6114998")
                                                                     .getConsequence());
    }

    @Test
    @Ignore
    public void testVariantDetailUsingFTP() throws Exception
    {
        SpeciesAccessor speciesAccessor = EnsemblUtils.getInstance()
                                                      .usingFTPLargeVariationFileIndexSupport()
                                                      .usingLocalCache()
                                                      .getHuman();

        assertEquals(VariantConsequence.MISSENSE, speciesAccessor.findVariantDetail("rs682985")
                                                                 .getConsequence());
        assertEquals(VariantConsequence._3_PRIME_UTR, speciesAccessor.findVariantDetail("rs6114998")
                                                                     .getConsequence());

    }

}
