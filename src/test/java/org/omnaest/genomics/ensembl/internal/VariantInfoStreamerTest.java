package org.omnaest.genomics.ensembl.internal;

import org.junit.Ignore;
import org.junit.Test;

public class VariantInfoStreamerTest
{
    private VariantInfoStreamer streamer = VariantInfoStreamer.getInstance();

    @Test
    @Ignore
    public void testLoadVariants() throws Exception
    {
        long count = this.streamer.usingLocalCache()
                                  .withMaximumNumberOfVariants(10000000)
                                  .withBatchSize(500000)
                                  .loadVariants("homo_sapiens")
                                  .limit(10000)
                                  .count();
        System.out.println(count);
    }

}
