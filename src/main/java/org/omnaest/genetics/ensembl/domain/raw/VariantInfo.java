package org.omnaest.genetics.ensembl.domain.raw;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    private List<String> clinicalSignifance = new ArrayList<>();

    @JsonProperty
    private List<VariantMapping> mappings = new ArrayList<>();

    @JsonProperty
    private List<String> evidence = new ArrayList<>();

    @JsonProperty
    private List<Phenotype> phenotypes = new ArrayList<>();

    public List<String> getClinicalSignifance()
    {
        return this.clinicalSignifance;
    }

    public void setClinicalSignifance(List<String> clinicalSignifance)
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

}
