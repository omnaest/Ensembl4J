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
package org.omnaest.genomics.ensembl.domain;

public class GeneLocation
{
	private String	referenceAssembly;
	private String	chromosome;
	private Range	position;
	private int		strand;

	public GeneLocation(String chromosome, String referenceAssembly, Range position, int strand)
	{
		super();
		this.chromosome = chromosome;
		this.referenceAssembly = referenceAssembly;
		this.position = position;
		this.strand = strand;
	}

	public int getStrand()
	{
		return this.strand;
	}

	public String getChromosome()
	{
		return this.chromosome;
	}

	public String getReferenceAssembly()
	{
		return this.referenceAssembly;
	}

	public Range getPosition()
	{
		return this.position;
	}

	@Override
	public String toString()
	{
		return "GeneLocation [referenceAssembly=" + this.referenceAssembly + ", chromosome=" + this.chromosome + ", position=" + this.position + ", strand="
				+ this.strand + "]";
	}

}
