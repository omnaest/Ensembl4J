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

    public VariantDetail withRESTResolvingConsumer(Consumer<VariantInfo> variantByRESTConsumer);

    public VariantDetail withFTPResolvingConsumer(Consumer<VariantInfo> variantByFTPConsumer);

    public VariantDetail withCacheResolvingConsumer(Consumer<VariantInfo> variantByCacheConsumer);
}
