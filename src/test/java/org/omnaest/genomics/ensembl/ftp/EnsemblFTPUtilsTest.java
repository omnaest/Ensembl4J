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
package org.omnaest.genomics.ensembl.ftp;

import org.junit.Ignore;
import org.junit.Test;
import org.omnaest.genomics.ensembl.ftp.EnsemblFTPUtils;

public class EnsemblFTPUtilsTest
{
    @Test
    @Ignore
    public void testLoadHumanVariationVCFFiles() throws Exception
    {
        EnsemblFTPUtils.load()
                       .withLocalDirectoryCache()
                       .variationVCFFiles()
                       .current()
                       .forHomoSapiens()
                       .forChromosomes()
                       //                       .limit(1)
                       .forEach(variationFile ->
                       {
                           System.out.println(variationFile.getChromosomeName());
                           variationFile.getData();
                       });
    }

}
