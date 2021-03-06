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
package org.omnaest.genomics.ensembl.domain.raw;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_EMPTY)
public class VariantInfo
{
    @JsonProperty
    private String source;

    @JsonProperty
    private String name;

    @JsonProperty("MAF")
    private String maf;

    @JsonProperty
    private String ambiguity;

    @JsonProperty("var_class")
    private String variantClass;

    @JsonProperty()
    private List<String> synonyms = new ArrayList<>();

    @JsonProperty("ancestral_allele")
    private String ancestralAllele;

    @JsonProperty("minor_allele")
    private String minorAllele;

    @JsonProperty("most_severe_consequence")
    private String consequence;

    @JsonProperty("clinical_significance")
    private Set<String> clinicalSignifance = new HashSet<>();

    @JsonProperty
    private List<VariantMapping> mappings = new ArrayList<>();

    @JsonProperty
    private List<String> evidence = new ArrayList<>();

    @JsonProperty
    private List<Phenotype> phenotypes = new ArrayList<>();

    public Set<String> getClinicalSignifance()
    {
        return this.clinicalSignifance;
    }

    public void setClinicalSignifance(Set<String> clinicalSignifance)
    {
        this.clinicalSignifance = clinicalSignifance;
    }

    public List<Phenotype> getPhenotypes()
    {
        return this.phenotypes;
    }

    public void setPhenotypes(List<Phenotype> phenotypes)
    {
        this.phenotypes = phenotypes;
    }

    public List<VariantMapping> getMappings()
    {
        return this.mappings;
    }

    public void setMappings(List<VariantMapping> mappings)
    {
        this.mappings = mappings;
    }

    public String getSource()
    {
        return this.source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Minor allele frequency
     * 
     * @return
     */
    public String getMaf()
    {
        return this.maf;
    }

    public void setMaf(String maf)
    {
        this.maf = maf;
    }

    public String getAmbiguity()
    {
        return this.ambiguity;
    }

    public void setAmbiguity(String ambiguity)
    {
        this.ambiguity = ambiguity;
    }

    public String getVariantClass()
    {
        return this.variantClass;
    }

    public void setVariantClass(String variantClass)
    {
        this.variantClass = variantClass;
    }

    public List<String> getSynonyms()
    {
        return this.synonyms;
    }

    public void setSynonyms(List<String> synonyms)
    {
        this.synonyms = synonyms;
    }

    public String getAncestralAllele()
    {
        return this.ancestralAllele;
    }

    public void setAncestralAllele(String ancestralAllele)
    {
        this.ancestralAllele = ancestralAllele;
    }

    public String getMinorAllele()
    {
        return this.minorAllele;
    }

    public void setMinorAllele(String minorAllele)
    {
        this.minorAllele = minorAllele;
    }

    public String getConsequence()
    {
        return this.consequence;
    }

    public void setConsequence(String consequence)
    {
        this.consequence = consequence;
    }

    public List<String> getEvidence()
    {
        return this.evidence;
    }

    public void setEvidence(List<String> evidence)
    {
        this.evidence = evidence;
    }

    @Override
    public String toString()
    {
        return "VariantInfo [source=" + this.source + ", name=" + this.name + ", maf=" + this.maf + ", ambiguity=" + this.ambiguity + ", variantClass="
                + this.variantClass + ", synonyms=" + this.synonyms + ", ancestralAllele=" + this.ancestralAllele + ", minorAllele=" + this.minorAllele
                + ", consequence=" + this.consequence + ", clinicalSignifance=" + this.clinicalSignifance + ", mappings=" + this.mappings + ", evidence="
                + this.evidence + ", phenotypes=" + this.phenotypes + "]";
    }

    @JsonIgnore
    public VariantInfo addClinicalSignifance(String clinicalSignificance)
    {
        this.clinicalSignifance.add(clinicalSignificance);
        return this;
    }

}
