package org.omnaest.genomics.ensembl.domain.raw;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VariantInfoBatchRequest
{
    @JsonProperty
    private List<String> ids;

    public VariantInfoBatchRequest(List<String> ids)
    {
        super();
        this.ids = ids;
    }

    @Override
    public String toString()
    {
        return "VariantInfoBatchRequest [ids=" + this.ids + "]";
    }

}
