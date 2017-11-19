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

import static org.junit.Assert.assertNotNull;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genetics.ensembl.domain.raw.Sequence;
import org.omnaest.genetics.ensembl.domain.raw.XRefs;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.rest.client.RestClient;

public class EnsemblRESTUtilsTest
{

	@Test
	@Ignore
	public void testGetSequence() throws Exception
	{
		Sequence sequence = EnsemblRESTUtils.getInstance()
											.getSequence("ENSG00000145692");

		System.out.println(sequence	.getSequence()
									.substring(0, 100));
		assertNotNull(sequence);

	}

	@Test
	@Ignore
	public void testGetSpecies() throws Exception
	{
		String json = JSONHelper.prettyPrint(EnsemblRESTUtils	.getInstance()
																.getSpecies());
		System.out.println(json);
	}

	@Test
	public void testGetXRefs() throws Exception
	{
		XRefs xRefs = EnsemblRESTUtils	.getInstance()
										.withProxy(new RestClient.FiddlerLocalhostProxy())
										.getXRefs("homo_sapiens", "BHMT");
		System.out.println(JSONHelper.prettyPrint(xRefs));
	}

}
