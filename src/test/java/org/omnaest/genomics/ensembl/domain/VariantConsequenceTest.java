package org.omnaest.genomics.ensembl.domain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.omnaest.genomics.ensembl.domain.VariantConsequence;
import org.omnaest.utils.ComparatorUtils;

public class VariantConsequenceTest
{
    @Test
    public void testSeveritySort() throws Exception
    {
        assertEquals("regulatory_region_variant", Arrays.asList(VariantConsequence.values())
                                                        .stream()
                                                        .sorted(ComparatorUtils.comparatorFunction(VariantConsequence::getSeverity))
                                                        .findFirst()
                                                        .map(VariantConsequence::getMatchStr)
                                                        .get());
    }
}
