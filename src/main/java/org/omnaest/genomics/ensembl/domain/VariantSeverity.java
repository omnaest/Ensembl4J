package org.omnaest.genomics.ensembl.domain;

public enum VariantSeverity implements Comparable<VariantSeverity>
{
    HIGH, MEDIUM, LOW, NONE;

    public boolean isEqualOrLowerComparedTo(VariantSeverity otherSeverity)
    {
        return this.compareTo(otherSeverity) >= 0;
    }
}