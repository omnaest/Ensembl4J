package org.omnaest.genetics.ensembl.ftp;

import org.junit.Ignore;
import org.junit.Test;

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
