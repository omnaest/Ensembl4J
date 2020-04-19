package org.omnaest.genetics.ensembl.domain;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;

public interface VariantDetail
{
    public static enum VariantSeverity
    {
        NONE, LOW, MEDIUM, HIGH
    }

    public static enum ClinicalSignificance
    {
        RISK_FACTOR("risk factor", "risk"), BENIGN("benign", "benign"), OTHER("", "");

        private String matchStr;
        private String label;

        private ClinicalSignificance(String matchStr, String label)
        {
            this.matchStr = matchStr;
            this.label = label;
        }

        public boolean matches(String input)
        {
            return StringUtils.equalsAnyIgnoreCase(input, matchStr);
        }

        public String getLabel()
        {
            return this.label;
        }
    }

    public static enum VariantConsequence
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
            return StringUtils.equalsIgnoreCase(input, matchStr);
        }

        public VariantSeverity getSeverity()
        {
            return severity;
        }

    }

    public VariantConsequence getConsequence();

    public SortedSet<String> getTraits();

    public double getMinorAlleleFrequency();

    public Set<ClinicalSignificance> getClinicalSignificances();

    public List<String> getSynonyms();

    public int getProteinPosition();
}
