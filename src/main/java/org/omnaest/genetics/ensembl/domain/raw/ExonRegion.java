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

public class ExonRegion
{
	@JsonProperty
	private String id;

	@JsonProperty("exon_id")
	private String exonId;

	@JsonProperty
	private long start;

	@JsonProperty
	private long end;

	@JsonProperty
	private int constitutive;

	@JsonProperty
	private int rank;

	@JsonProperty
	private String source;

	@JsonProperty
	private int version;

	@JsonProperty("feature_type")
	private String featureType;

	@JsonProperty("assembly_name")
	private String assemblyName;

	@JsonProperty("Parent")
	private String parent;

	@JsonProperty("ensembl_phase")
	private int ensemblPhase;

	@JsonProperty("ensembl_end_phase")
	private int ensemblEndPhase;

	@JsonProperty("seq_region_name")
	private String sequenceRegionName;

	@JsonProperty
	private int strand;

	public int getEnsemblEndPhase()
	{
		return this.ensemblEndPhase;
	}

	public void setEnsemblEndPhase(int ensemblEndPhase)
	{
		this.ensemblEndPhase = ensemblEndPhase;
	}

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getExonId()
	{
		return this.exonId;
	}

	public void setExonId(String exonId)
	{
		this.exonId = exonId;
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

	public int getConstitutive()
	{
		return this.constitutive;
	}

	public void setConstitutive(int constitutive)
	{
		this.constitutive = constitutive;
	}

	public int getRank()
	{
		return this.rank;
	}

	public void setRank(int rank)
	{
		this.rank = rank;
	}

	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public int getVersion()
	{
		return this.version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getFeatureType()
	{
		return this.featureType;
	}

	public void setFeatureType(String featureType)
	{
		this.featureType = featureType;
	}

	public String getAssemblyName()
	{
		return this.assemblyName;
	}

	public void setAssemblyName(String assemblyName)
	{
		this.assemblyName = assemblyName;
	}

	public String getParent()
	{
		return this.parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

	public int getEnsemblPhase()
	{
		return this.ensemblPhase;
	}

	public void setEnsemblPhase(int ensemblPhase)
	{
		this.ensemblPhase = ensemblPhase;
	}

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

	@Override
	public String toString()
	{
		return "ExonRegion [id=" + this.id + ", exonId=" + this.exonId + ", start=" + this.start + ", end=" + this.end + ", constitutive=" + this.constitutive
				+ ", rank=" + this.rank + ", source=" + this.source + ", version=" + this.version + ", featureType=" + this.featureType + ", assemblyName="
				+ this.assemblyName + ", parent=" + this.parent + ", ensemblPhase=" + this.ensemblPhase + ", sequenceRegionName=" + this.sequenceRegionName
				+ ", strand=" + this.strand + "]";
	}

}
