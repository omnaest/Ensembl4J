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
package org.omnaest.genetics.ensembl.domain;

import java.util.List;
import java.util.stream.Stream;

/**
 * Accessor for gene information
 * 
 * @author omnaest
 */
public interface GeneAccessor
{
    public String getName();

    public String getDescription();

    public String getDNASequence();

    public GeneLocation getLocation();

    public GeneLocation getLocation(String referenceAssembly);

    public List<Variant> getVariants();

    public List<Exon> getExons();

    public Stream<String> getProteinSequences();

    public Stream<ProteinTranscriptAccessor> getProteinTranscripts();

    public String getUniprotId();

    Stream<String> getcDNASequences();

}
