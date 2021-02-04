package org.omnaest.genomics.ensembl.domain;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Consumer;

import org.omnaest.genomics.ensembl.domain.raw.VariantInfo;

public interface VariantDetail
{
    public String getId();

    public VariantConsequence getConsequence();

    public SortedSet<String> getTraits();

    public double getMinorAlleleFrequency();

    public Set<ClinicalSignificance> getClinicalSignificances();

    public List<String> getSynonyms();

    public int getProteinPosition();

    VariantDetail withRESTResolvingConsumer(Consumer<VariantInfo> variantByRESTConsumer);

    VariantDetail withFTPResolvingConsumer(Consumer<VariantInfo> variantByFTPConsumer);
}
