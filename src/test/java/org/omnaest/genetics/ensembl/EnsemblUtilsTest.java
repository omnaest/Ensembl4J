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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genetics.ensembl.domain.GeneLocation;
import org.omnaest.genetics.ensembl.domain.Variant;

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
		assertEquals(79111779, location	.getPosition()
										.getStart());
		assertEquals(79132290, location	.getPosition()
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
		assertEquals(79111779, location	.getPosition()
										.getStart());
		assertEquals(79132290, location	.getPosition()
										.getEnd());
	}

	@Test
	public void testGetVariants() throws Exception
	{
		List<Variant> variants = EnsemblUtils	.getInstance()
												.findSpecies("human")
												.get()
												.findGene("BHMT")
												.get()
												.getVariants();
		System.out.println(variants);

	}

}
