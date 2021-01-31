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
package org.omnaest.genomics.ensembl.domain.raw;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Variation
{
	@JsonProperty
	private String id;

	@JsonProperty
	private long start;

	@JsonProperty
	private long end;

	@JsonProperty
	private List<String> alleles;

	@JsonProperty("seq_region_name")
	private String sequenceRegionName;

	@JsonProperty
	private int strand;

	@JsonProperty
	private String source;

	@JsonProperty("feature_type")
	private String featureType;

	@JsonProperty("assembly_name")
	private String assemblyName;

	@JsonProperty("clinical_significance")
	private List<String> clinicalSignificance;

	@JsonProperty("consequence_type")
	private String consequenceType;

	public String getConsequenceType()
	{
		return this.consequenceType;
	}

	public void setConsequenceType(String consequenceType)
	{
		this.consequenceType = consequenceType;
	}

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
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

	public List<String> getAlleles()
	{
		return this.alleles;
	}

	public void setAlleles(List<String> alleles)
	{
		this.alleles = alleles;
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

	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
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

	public List<String> getClinicalSignificance()
	{
		return this.clinicalSignificance;
	}

	public void setClinicalSignificance(List<String> clinicalSignificance)
	{
		this.clinicalSignificance = clinicalSignificance;
	}

}