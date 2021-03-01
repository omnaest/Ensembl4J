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
