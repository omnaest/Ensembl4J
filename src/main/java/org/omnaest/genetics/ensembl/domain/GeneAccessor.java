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

/**
 * Accessor for gene information
 * 
 * @author omnaest
 */
public interface GeneAccessor
{
	public static class Range
	{
		private long	start;
		private long	end;

		public Range(long start, long end)
		{
			super();
			this.start = start;
			this.end = end;
		}

		public long getStart()
		{
			return start;
		}

		public long getEnd()
		{
			return end;
		}

		@Override
		public String toString()
		{
			return "Range [start=" + start + ", end=" + end + "]";
		}

	}

	public static class GeneLocation
	{
		private int		chromosome;
		private String	referenceAssembly;
		private Range	position;

		public GeneLocation(int chromosome, String referenceAssembly, Range position)
		{
			super();
			this.chromosome = chromosome;
			this.referenceAssembly = referenceAssembly;
			this.position = position;
		}

		public int getChromosome()
		{
			return chromosome;
		}

		public String getReferenceAssembly()
		{
			return referenceAssembly;
		}

		public Range getPosition()
		{
			return position;
		}

		@Override
		public String toString()
		{
			return "GeneLocation [chromosome=" + chromosome + ", referenceAssembly=" + referenceAssembly + ", position=" + position + "]";
		}

	}

	public String getName();

	public String getDescription();

	public String getSequence();

	public GeneLocation getLocation();
}
