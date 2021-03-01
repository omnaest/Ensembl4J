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
package org.omnaest.genomics.ensembl.domain;

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
