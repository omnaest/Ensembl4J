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

public class Lookup
{
	@JsonProperty
	private String source;

	@JsonProperty("object_type")
	private String objectType;

	@JsonProperty("logic_name")
	private String logicName;

	@JsonProperty("Parent")
	private String parent;

	@JsonProperty("seq_region_name")
	private String sequenceRegionName;

	@JsonProperty("db_type")
	private String dbType;

	@JsonProperty("is_canonical")
	private int isCanonical;

	@JsonProperty
	private int strand;

	@JsonProperty
	private String id;

	@JsonProperty
	private int version;

	@JsonProperty
	private String species;

	@JsonProperty("assembly_name")
	private String assemblyName;

	@JsonProperty("display_name")
	private String displayName;

	@JsonProperty
	private long start;

	@JsonProperty
	private long end;

	@JsonProperty("biotype")
	private String bioType;

	@JsonProperty
	private String length;

	@JsonProperty
	private String description;

	public String getDescription()
	{
		return this.description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getLength()
	{
		return this.length;
	}

	public void setLength(String length)
	{
		this.length = length;
	}

	public String getSource()
	{
		return this.source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getObjectType()
	{
		return this.objectType;
	}

	public void setObjectType(String objectType)
	{
		this.objectType = objectType;
	}

	public String getLogicName()
	{
		return this.logicName;
	}

	public void setLogicName(String logicName)
	{
		this.logicName = logicName;
	}

	public String getParent()
	{
		return this.parent;
	}

	public void setParent(String parent)
	{
		this.parent = parent;
	}

	public String getSequenceRegionName()
	{
		return this.sequenceRegionName;
	}

	public void setSequenceRegionName(String sequenceRegionName)
	{
		this.sequenceRegionName = sequenceRegionName;
	}

	public String getDbType()
	{
		return this.dbType;
	}

	public void setDbType(String dbType)
	{
		this.dbType = dbType;
	}

	public int getIsCanonical()
	{
		return this.isCanonical;
	}

	public void setIsCanonical(int isCanonical)
	{
		this.isCanonical = isCanonical;
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

	public int getVersion()
	{
		return this.version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getSpecies()
	{
		return this.species;
	}

	public void setSpecies(String species)
	{
		this.species = species;
	}

	public String getAssemblyName()
	{
		return this.assemblyName;
	}

	public void setAssemblyName(String assemblyName)
	{
		this.assemblyName = assemblyName;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
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

	public String getBioType()
	{
		return this.bioType;
	}

	public void setBioType(String bioType)
	{
		this.bioType = bioType;
	}

	public boolean hasBiotype(BioType bioType)
	{
		return bioType	.name()
						.equalsIgnoreCase(this.bioType);
	}

	@Override
	public String toString()
	{
		return "Lookup [source=" + this.source + ", objectType=" + this.objectType + ", logicName=" + this.logicName + ", parent=" + this.parent
				+ ", sequenceRegionName=" + this.sequenceRegionName + ", dbType=" + this.dbType + ", isCanonical=" + this.isCanonical + ", strand="
				+ this.strand + ", id=" + this.id + ", version=" + this.version + ", species=" + this.species + ", assemblyName=" + this.assemblyName
				+ ", displayName=" + this.displayName + ", start=" + this.start + ", end=" + this.end + ", bioType=" + this.bioType + ", length=" + this.length
				+ ", description=" + this.description + "]";
	}

}
