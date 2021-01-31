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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Species
{
	@JsonProperty
	private String division;

	@JsonProperty("taxon_id")
	private String taxonId;

	@JsonProperty
	private String name;

	@JsonProperty
	private int release;

	@JsonProperty("display_name")
	private String displayName;

	@JsonProperty
	private String accession;

	@JsonProperty("strain_collection")
	private String strainCollection;

	@JsonProperty("common_name")
	private String commonName;

	@JsonProperty
	private String strain;

	@JsonProperty
	private List<String> aliases = new ArrayList<>();

	@JsonProperty
	private List<String> groups = new ArrayList<>();

	@JsonProperty
	private String assembly;

	public String getDivision()
	{
		return this.division;
	}

	public void setDivision(String division)
	{
		this.division = division;
	}

	public String getTaxonId()
	{
		return this.taxonId;
	}

	public void setTaxonId(String taxonId)
	{
		this.taxonId = taxonId;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public int getRelease()
	{
		return this.release;
	}

	public void setRelease(int release)
	{
		this.release = release;
	}

	public String getDisplayName()
	{
		return this.displayName;
	}

	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}

	public String getAccession()
	{
		return this.accession;
	}

	public void setAccession(String accession)
	{
		this.accession = accession;
	}

	public String getStrainCollection()
	{
		return this.strainCollection;
	}

	public void setStrainCollection(String strainCollection)
	{
		this.strainCollection = strainCollection;
	}

	public String getCommonName()
	{
		return this.commonName;
	}

	public void setCommonName(String commonName)
	{
		this.commonName = commonName;
	}

	public String getStrain()
	{
		return this.strain;
	}

	public void setStrain(String strain)
	{
		this.strain = strain;
	}

	public List<String> getAliases()
	{
		return this.aliases;
	}

	public void setAliases(List<String> aliases)
	{
		this.aliases = aliases;
	}

	public List<String> getGroups()
	{
		return this.groups;
	}

	public void setGroups(List<String> groups)
	{
		this.groups = groups;
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
		return "Specie [division=" + this.division + ", taxonId=" + this.taxonId + ", name=" + this.name + ", release=" + this.release + ", displayName="
				+ this.displayName + ", accession=" + this.accession + ", strainCollection=" + this.strainCollection + ", commonName=" + this.commonName
				+ ", strain=" + this.strain + ", aliases=" + this.aliases + ", groups=" + this.groups + ", assembly=" + this.assembly + "]";
	}

}
