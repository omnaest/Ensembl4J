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
