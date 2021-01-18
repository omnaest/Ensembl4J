package org.omnaest.genetics.ensembl.domain;

import org.apache.commons.lang3.StringUtils;

public enum ClinicalSignificance
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

    public String getMatchStr()
    {
        return matchStr;
    }

}