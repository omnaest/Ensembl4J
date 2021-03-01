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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Phenotype
{
    @JsonProperty
    private String variants;

    @JsonProperty
    private String source;

    @JsonProperty("risk_allele")
    private String riskAllele;

    @JsonProperty
    private String trait;

    @JsonProperty
    private String genes;

    @JsonProperty
    private String study;

    @JsonProperty("ontology_accessions")
    private List<String> ontologyAccessions;

    public String getVariants()
    {
        return this.variants;
    }

    public void setVariants(String variants)
    {
        this.variants = variants;
    }

    public String getSource()
    {
        return this.source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getRiskAllele()
    {
        return this.riskAllele;
    }

    public void setRiskAllele(String riskAllele)
    {
        this.riskAllele = riskAllele;
    }

    public String getTrait()
    {
        return this.trait;
    }

    public void setTrait(String trait)
    {
        this.trait = trait;
    }

    public String getGenes()
    {
        return this.genes;
    }

    public void setGenes(String genes)
    {
        this.genes = genes;
    }

    public String getStudy()
    {
        return this.study;
    }

    public void setStudy(String study)
    {
        this.study = study;
    }

    public List<String> getOntologyAccessions()
    {
        return this.ontologyAccessions;
    }

    public void setOntologyAccessions(List<String> ontologyAccessions)
    {
        this.ontologyAccessions = ontologyAccessions;
    }

}
