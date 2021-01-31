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

import java.util.List;

public class Variant
{
	private Range			range;
	private List<String>	alleles;

	public Variant(Range range, List<String> alleles)
	{
		super();
		this.range = range;
		this.alleles = alleles;
	}

	public Range getRange()
	{
		return this.range;
	}

	public List<String> getAlleles()
	{
		return this.alleles;
	}

	@Override
	public String toString()
	{
		return "Variant [range=" + this.range + ", alleles=" + this.alleles + "]";
	}

}