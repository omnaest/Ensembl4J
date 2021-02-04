package org.omnaest.genomics.ensembl.domain.raw;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class VariantInfoBatch
{
    private Map<String, VariantInfo> variantIdToVariantInfo;

    @JsonCreator
    public VariantInfoBatch(Map<String, VariantInfo> variantIdToVariantInfo)
    {
        super();
        this.variantIdToVariantInfo = variantIdToVariantInfo;
    }

    @JsonValue
    public Map<String, VariantInfo> getVariantIdToVariantInfo()
    {
        return this.variantIdToVariantInfo;
    }

    @Override
    public String toString()
    {
        return "VariantInfoBatch [variantIdToVariantInfo=" + this.variantIdToVariantInfo + "]";
    }

}
