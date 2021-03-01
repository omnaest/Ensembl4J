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
package org.omnaest.genomics.ensembl.domain.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sequence
{
    @JsonProperty("desc")
    private String description;

    @JsonProperty
    private String query;

    @JsonProperty
    private String id;

    @JsonProperty("seq")
    private String sequence;

    @JsonProperty
    private String molecule;

    @JsonProperty
    private int version;

    public int getVersion()
    {
        return this.version;
    }

    public void setVersion(int version)
    {
        this.version = version;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getQuery()
    {
        return this.query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getId()
    {
        return this.id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSequence()
    {
        return this.sequence;
    }

    public void setSequence(String sequence)
    {
        this.sequence = sequence;
    }

    public String getMolecule()
    {
        return this.molecule;
    }

    public void setMolecule(String molecule)
    {
        this.molecule = molecule;
    }

    @Override
    public String toString()
    {
        return "Sequence [descr=" + this.description + ", query=" + this.query + ", id=" + this.id + ", sequence=" + this.sequence + ", molecule="
                + this.molecule + "]";
    }

}
