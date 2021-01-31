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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Transcript
{
	@JsonProperty
	private String source;

	@JsonProperty("logic_name")
	private String logicName;

	@JsonProperty("feature_type")
	private String featureType;

	@JsonProperty("external_name")
	private String externalName;

	@JsonProperty("Parent")
	private String parent;

	@JsonProperty("transcript_support_level")
	private String transcriptSupportLevel;

	@JsonProperty("seq_region_name")
	private String sequenceRegionName;

	@JsonProperty
	private int strand;

	@JsonProperty
	private String id;

	@JsonProperty("transcript_id")
	private String transcriptId;

	@JsonProperty
	private int version;

	@JsonProperty("assembly_name")
	private String assemblyName;

	@JsonProperty
	private String description;

	@JsonProperty
	private long end;

	@JsonProperty
	private long start;

	@JsonProperty
	private String biotype;

	@JsonProperty("ccdsid")
	private String ccdsId;

	@JsonProperty
	private String tag;

	public String getTag()
	{
		return this.tag;
	}

	public void setTag(String tag)
	{
		this.tag = tag;
	}

	public String getCcdsId()
	{
		return this.ccdsId;
	}

	public void setCcdsId(String ccdsId)
	{
		this.ccdsId = ccdsId;
	}

	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getLogicName()
	{
		return this.logicName;
	}

	public void setLogicName(String logicName)
	{
		this.logicName = logicName;
	}

	public String getFeatureType()
	{
		return this.featureType;
	}

	public void setFeatureType(String featureType)
	{
		this.featureType = featureType;
	}

	public String getExternalName()
	{
		return this.externalName;
	}

	public void setExternalName(String externalName)
	{
		this.externalName = externalName;
	}

	public String getParent()
	{
		return this.parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

	public String getTranscriptSupportLevel()
	{
		return this.transcriptSupportLevel;
	}

	public void setTranscriptSupportLevel(String transcriptSupportLevel)
	{
		this.transcriptSupportLevel = transcriptSupportLevel;
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

	public String getId()
	{
		return this.id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getTranscriptId()
	{
		return this.transcriptId;
	}

	public void setTranscriptId(String transcriptId)
	{
		this.transcriptId = transcriptId;
	}

	public int getVersion()
	{
		return this.version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getAssemblyName()
	{
		return this.assemblyName;
	}

	public void setAssemblyName(String assemblyName)
	{
		this.assemblyName = assemblyName;
	}

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public long getEnd()
	{
		return this.end;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}

	public long getStart()
	{
		return this.start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public String getBiotype()
	{
		return this.biotype;
	}

	public boolean hasBiotype(BioType bioType)
	{
		return bioType	.name()
						.equalsIgnoreCase(this.biotype);
	}

	public void setBiotype(String biotype)
	{
		this.biotype = biotype;
	}

	@Override
	public String toString()
	{
		return "Transcript [source=" + this.source + ", logicName=" + this.logicName + ", featureType=" + this.featureType + ", externalName="
				+ this.externalName + ", parent=" + this.parent + ", transcriptSupportLevel=" + this.transcriptSupportLevel + ", sequenceRegionName="
				+ this.sequenceRegionName + ", strand=" + this.strand + ", id=" + this.id + ", transcriptId=" + this.transcriptId + ", version=" + this.version
				+ ", assemblyName=" + this.assemblyName + ", description=" + this.description + ", end=" + this.end + ", start=" + this.start + ", biotype="
				+ this.biotype + "]";
	}

}
