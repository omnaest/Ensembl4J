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
package org.omnaest.genetics.ensembl.domain.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegionLocation
{
	@JsonProperty("seq_region_name")
	private String sequenceRegionName;

	@JsonProperty()
	private int strand;

	@JsonProperty("coord_system")
	private String coordinateSystem;

	@JsonProperty()
	private long start;

	@JsonProperty()
	private long end;

	@JsonProperty()
	private String assembly;

	public String getSequenceRegionName()
	{
		return this.sequenceRegionName;
	}

	public void setSequenceRegionName(String sequenceRegionName)
	{
		this.sequenceRegionName = sequenceRegionName;
	}

	public int getStrand()
	{
		return this.strand;
	}

	public void setStrand(int strand)
	{
		this.strand = strand;
	}

	public String getCoordinateSystem()
	{
		return this.coordinateSystem;
	}

	public void setCoordinateSystem(String coordinateSystem)
	{
		this.coordinateSystem = coordinateSystem;
	}

	public long getStart()
	{
		return this.start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getEnd()
	{
		return this.end;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}

	public String getAssembly()
	{
		return this.assembly;
	}

	public void setAssembly(String assembly)
	{
		this.assembly = assembly;
	}

	@Override
	public String toString()
	{
		return "RegionLocation [sequenceRegionName=" + this.sequenceRegionName + ", strand=" + this.strand + ", coordinateSystem=" + this.coordinateSystem
				+ ", start=" + this.start + ", end=" + this.end + ", assembly=" + this.assembly + "]";
	}

}
