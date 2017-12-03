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
package org.omnaest.genetics.ensembl.domain;

public class Exon
{
	private Range	position;
	private String	sequence;

	public Exon(Range position, String sequence)
	{
		super();
		this.position = position;
		this.sequence = sequence;
	}

	public Range getPosition()
	{
		return this.position;
	}

	public String getSequence()
	{
		return this.sequence;
	}

	@Override
	public String toString()
	{
		return "Exon [position=" + this.position + ", sequence=" + this.sequence + "]";
	}

}
