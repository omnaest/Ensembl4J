package org.omnaest.genomics.ensembl.domain;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum VariantConsequence
{
    MISSENSE("missense_variant", VariantSeverity.MEDIUM),
    _3_PRIME_UTR("3_prime_UTR_variant", VariantSeverity.LOW),
    _5_PRIME_UTR("5_prime_UTR_variant", VariantSeverity.LOW),
    INTRON("intron_variant", VariantSeverity.LOW),
    REGULATORY_REGION("regulatory_region_variant", VariantSeverity.HIGH),
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