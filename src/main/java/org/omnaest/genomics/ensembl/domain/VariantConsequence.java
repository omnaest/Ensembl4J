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
package org.omnaest.genomics.ensembl.domain;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * <a href="https://m.ensembl.org/info/genome/variation/prediction/predicted_data.html">Ensembl documentation</a>
 * 
 * @author omnaest
 */
public enum VariantConsequence
{
    MISSENSE("missense_variant", VariantSeverity.MEDIUM),
    PROTEIN_ALTERING_VARIANT("protein_altering_variant", VariantSeverity.MEDIUM),
    _3_PRIME_UTR("3_prime_UTR_variant", VariantSeverity.LOW),
    _5_PRIME_UTR("5_prime_UTR_variant", VariantSeverity.LOW),
    INTRON("intron_variant", VariantSeverity.LOW),
    STOP_GAINED("stop_gained", VariantSeverity.HIGH),
    START_LOST("start_lost", VariantSeverity.HIGH),
    STOP_LOST("stop_lost", VariantSeverity.HIGH),
    FRAME_SHIFT_VARIANT("frameshift_variant", VariantSeverity.HIGH),
    INFRAME_INSERTION("inframe_insertion", VariantSeverity.MEDIUM),
    INFRAME_DELETION("inframe_deletion", VariantSeverity.MEDIUM),
    REGULATORY_REGION("regulatory_region_variant", VariantSeverity.HIGH),
    REGULATORY_REGION_ABLATION("regulatory_region_ablation", VariantSeverity.HIGH),
    TRANSCRIPTION_FACTOR_BINDING_SITE("TF_binding_site_variant", VariantSeverity.HIGH),
    TRANSCRIPT_ABLATION("transcript_ablation", VariantSeverity.HIGH),
    TRANSCRIPT_AMPLIFICATION("transcript_amplification", VariantSeverity.HIGH),
    SPLICE_ACCEPTOR_VARIANT("splice_acceptor_variant", VariantSeverity.HIGH),
    UPSTREAM_GENE("upstream_gene_variant", VariantSeverity.LOW),
    DOWNSTREAM_GENE("downstream_gene_variant", VariantSeverity.LOW),
    SYNONYMOUS("synonymous_variant", VariantSeverity.NONE),
    UNKNOWN("", VariantSeverity.LOW);

    private String          matchStr;
    private VariantSeverity severity;

    private VariantConsequence(String matchStr, VariantSeverity severity)
    {
        this.matchStr = matchStr;
        this.severity = severity;
    }

    /**
     * Returns true, if the raw input from the rest api matches the current consequence
     * 
     * @param input
     * @return
     */
    public boolean matches(String input)
    {
        return StringUtils.equalsIgnoreCase(input, this.matchStr);
    }

    public VariantSeverity getSeverity()
    {
        return this.severity;
    }

    public String getMatchStr()
    {
        return this.matchStr;
    }

    public static Optional<VariantConsequence> of(String matchStr)
    {
        return Arrays.asList(values())
                     .stream()
                     .filter(consequence -> consequence.matches(matchStr))
                     .findFirst();
    }
}
