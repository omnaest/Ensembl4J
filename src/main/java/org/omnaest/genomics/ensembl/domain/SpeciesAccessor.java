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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.genomics.ensembl.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public interface SpeciesAccessor
{
    /**
     * E.g. "Homo sapiens"
     * 
     * @return
     */
    public String getName();

    /**
     * E.g. "human"
     * 
     * @return
     */
    public String getDisplayName();

    public Stream<String> getAliases();

    /**
     * Finds a gene by name, e.g. "BHMT" for homo sapiens
     * 
     * @param symbol
     * @return {@link GeneAccessor}
     */
    public Optional<GeneAccessor> findGene(String symbol);

    /**
     * Returns the details of a given mutation like e.g. "rs682985"
     * 
     * @param string
     * @return
     */
    public VariantDetail findVariantDetail(String variantId);

    /**
     * Similar to {@link #findVariantDetail(String)} for a batch of variant ids
     * 
     * @param variantIds
     * @return
     */
    public Map<String, VariantDetail> findVariantDetails(Collection<String> variantIds);

}
