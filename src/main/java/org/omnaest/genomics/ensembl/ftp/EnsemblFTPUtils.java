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

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.genomics.vcf.VCFUtils;
import org.omnaest.genomics.vcf.domain.VCFRecord;
import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.ftp.FTPUtils;
import org.omnaest.utils.ftp.FTPUtils.FileType;
import org.omnaest.utils.zip.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnsemblFTPUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(EnsemblFTPUtils.class);

    public static interface EnsemblFTPLoader
    {
        public EnsemblFTPLoader withCache(Cache cache);

        public EnsemblFTPLoader withLocalDirectoryCache();

        public EnsembleVariationVCFLoader variationVCFFiles();
    }

    public static interface EnsembleVariationVCFLoader
    {
        public EnsembleVariationVCFLoaderWithVersion current();

        public EnsembleVariationVCFLoaderWithVersion custom(String version);

        public EnsembleVariationVCFLoaderWithVersion releaseGRCH37(int releaseVersion);

        public EnsembleVariationVCFLoaderWithVersion currentGRCH37();

        public EnsembleVariationVCFLoaderWithVersion release(int releaseVersion);
    }

    public static interface EnsembleVariationVCFLoaderWithVersion
    {
        public EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies forHomoSapiens();

        public EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies forSpecies(String species);
    }

    public static interface EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies
    {
        public Stream<VariationChromosomeVCFResource> forChromosomes();

        public VariationVCFResource forClinicallyAssociated();

        public VariationVCFResource forPhenotypeAssociated();
    }

    public static interface VariationVCFResource
    {
        public String getFileName();

        public byte[] getData();

        public VCFResourceAccessor asParsedVCF();

        /**
         * Defines if the content should be kept even after reading the data. Default is true.
         * 
         * @param clearCache
         * @return
         */
        public VariationVCFResource withCacheClearanceAfterRead(boolean clearCache);
    }

    public static interface VariationChromosomeVCFResource extends VariationVCFResource
    {
        public String getChromosomeName();
    }

    public static interface VCFResourceAccessor
    {
        public Stream<VCFRecord> getRecords();
    }

    public static EnsemblFTPLoader load()
    {
        return new EnsemblFTPLoader()
        {
            private Cache cache = CacheUtils.newNoOperationCache();

            @Override
            public EnsemblFTPLoader withCache(Cache cache)
            {
                this.cache = cache;
                return this;
            }

            @Override
            public EnsemblFTPLoader withLocalDirectoryCache()
            {
                return this.withCache(CacheUtils.newLocalJsonFolderCache("ensembl/ftp")
                                                .withNativeByteArrayStorage(true)
                                                .withNativeStringStorage(true));
            }

            @Override
            public EnsembleVariationVCFLoader variationVCFFiles()
            {
                return new EnsembleVariationVCFLoader()
                {
                    @Override
                    public EnsembleVariationVCFLoaderWithVersion current()
                    {
                        return this.custom("current_variation");
                    }

                    @Override
                    public EnsembleVariationVCFLoaderWithVersion release(int releaseVersion)
                    {
                        return this.custom("release-" + releaseVersion + "/variation");
                    }

                    @Override
                    public EnsembleVariationVCFLoaderWithVersion currentGRCH37()
                    {
                        return this.custom("grch37/current/variation");
                    }

                    @Override
                    public EnsembleVariationVCFLoaderWithVersion releaseGRCH37(int releaseVersion)
                    {
                        return this.custom("grch37/release-" + releaseVersion + "/variation");
                    }

                    @Override
                    public EnsembleVariationVCFLoaderWithVersion custom(String version)
                    {
                        return new EnsembleVariationVCFLoaderWithVersion()
                        {
                            @Override
                            public EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies forHomoSapiens()
                            {
                                return this.forSpecies("homo_sapiens");
                            }

                            @Override
                            public EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies forSpecies(String species)
                            {
                                return new EnsembleVariationVCFLoaderWithVersionAndHomoSapiensSpecies()
                                {
                                    @Override
                                    public VariationVCFResource forClinicallyAssociated()
                                    {
                                        String fileName = "/pub/" + version + "/vcf/" + species + "/" + species + "_clinically_associated.vcf.gz";
                                        return this.createVariationVCFResource(fileName, () -> this.loadFileFromFtp(fileName));
                                    }

                                    @Override
                                    public VariationVCFResource forPhenotypeAssociated()
                                    {
                                        String fileName = "/pub/" + version + "/vcf/" + species + "/" + species + "_phenotype_associated.vcf.gz";
                                        return this.createVariationVCFResource(fileName, () -> this.loadFileFromFtp(fileName));
                                    }

                                    private VariationVCFResource createVariationVCFResource(String fileName, Supplier<byte[]> dataSupplier)
                                    {
                                        CachedElement<byte[]> cachedData = CachedElement.of(dataSupplier);
                                        return new VariationVCFResource()
                                        {
                                            private boolean clearCache = true;

                                            @Override
                                            public VariationVCFResource withCacheClearanceAfterRead(boolean clearCache)
                                            {
                                                this.clearCache = clearCache;
                                                return this;
                                            }

                                            @Override
                                            public String getFileName()
                                            {
                                                return fileName;
                                            }

                                            @Override
                                            public byte[] getData()
                                            {
                                                if (this.clearCache)
                                                {
                                                    return cachedData.getAndReset();
                                                }
                                                else
                                                {
                                                    return cachedData.get();
                                                }
                                            }

                                            @Override
                                            public VCFResourceAccessor asParsedVCF()
                                            {
                                                return gzipResourceAsParsedVCF(fileName, this.getData());
                                            }
                                        };
                                    }

                                    private byte[] loadFileFromFtp(String fileName)
                                    {
                                        LOG.info("Loading file: " + fileName);
                                        return cache.computeIfAbsent(fileName, () ->
                                        {
                                            LOG.info("...from ensembl ftp: " + fileName);
                                            return FTPUtils.load()
                                                           .withAnonymousCredentials()
                                                           .withFileType(FileType.BINARY)
                                                           .from("ftp.ensembl.org", fileName)
                                                           .orElseThrow(() -> new IllegalStateException("Unable to download file from ensembl ftp: "
                                                                   + fileName))
                                                           .asByteArray();
                                        }, byte[].class);
                                    }

                                    private VCFResourceAccessor gzipResourceAsParsedVCF(String fileName, byte[] data)
                                    {
                                        try
                                        {
                                            Stream<VCFRecord> records = VCFUtils.read()
                                                                                .from(ZipUtils.read()
                                                                                              .fromGzip(data)
                                                                                              .asInputStream())
                                                                                .parseOnce();
                                            return new VCFResourceAccessor()
                                            {
                                                @Override
                                                public Stream<VCFRecord> getRecords()
                                                {
                                                    return records;
                                                }
                                            };
                                        }
                                        catch (IOException e)
                                        {
                                            throw new IllegalStateException("Unable to unzip file: " + fileName, e);
                                        }
                                    }

                                    @Override
                                    public Stream<VariationChromosomeVCFResource> forChromosomes()
                                    {
                                        List<String> chromosomeNames = Stream.concat(Stream.of("Y", "X", "MT"), IntStream.rangeClosed(1, 22)
                                                                                                                         .mapToObj(index -> "" + index))
                                                                             .map(chromosomeIndex -> "chr" + chromosomeIndex)
                                                                             .collect(Collectors.toList());
                                        return chromosomeNames.stream()
                                                              .map(chromosomeName ->
                                                              {
                                                                  // direct release version path example: "/pub/release-102/variation/vcf/homo_sapiens/"
                                                                  // 'current_variation' points to the most actual one of those
                                                                  String fileName = "/pub/current_variation/vcf/" + species + "/" + species
                                                                          + "_incl_consequences-" + chromosomeName + ".vcf.gz";

                                                                  CachedElement<byte[]> data = CachedElement.of(() -> this.loadFileFromFtp(fileName));
                                                                  return new VariationChromosomeVCFResource()
                                                                  {
                                                                      private boolean clearCache = true;

                                                                      @Override
                                                                      public String getFileName()
                                                                      {
                                                                          return fileName;
                                                                      }

                                                                      @Override
                                                                      public byte[] getData()
                                                                      {
                                                                          if (this.clearCache)
                                                                          {
                                                                              return data.getAndReset();
                                                                          }
                                                                          else
                                                                          {
                                                                              return data.get();
                                                                          }
                                                                      }

                                                                      @Override
                                                                      public String getChromosomeName()
                                                                      {
                                                                          return chromosomeName;
                                                                      }

                                                                      @Override
                                                                      public VCFResourceAccessor asParsedVCF()
                                                                      {
                                                                          return gzipResourceAsParsedVCF(fileName, this.getData());
                                                                      }

                                                                      @Override
                                                                      public VariationVCFResource withCacheClearanceAfterRead(boolean clearCache)
                                                                      {
                                                                          this.clearCache = clearCache;
                                                                          return this;
                                                                      }
                                                                  };

                                                              });
                                    }
                                };
                            }

                        };
                    }
                };
            }
        };
    }
}
