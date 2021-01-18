package org.omnaest.genetics.ensembl.domain;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface VariantDetail
{
    public VariantConsequence getConsequence();

    public SortedSet<String> getTraits();

    public double getMinorAlleleFrequency();

    public Set<ClinicalSignificance> getClinicalSignificances();

    public List<String> getSynonyms();

    public int getProteinPosition();
}
