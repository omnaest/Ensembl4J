package org.omnaest.genetics.ensembl.ftp;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.omnaest.genetics.VCFUtils;
import org.omnaest.genetics.domain.VCFRecord;
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
    }

    public static interface VariationVCFResource
    {
        public String getFileName();

        public byte[] getData();

        public VCFResourceAccessor asParsedVCF();
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
                                        String fileName = "/pub/current_variation/vcf/" + species + "/" + species + "_clinically_associated.vcf.gz";
                                        byte[] data = this.loadFileFromFtp(fileName);
                                        return new VariationVCFResource()
                                        {
                                            @Override
                                            public String getFileName()
                                            {
                                                return fileName;
                                            }

                                            @Override
                                            public byte[] getData()
                                            {
                                                return data;
                                            }

                                            @Override
                                            public VCFResourceAccessor asParsedVCF()
                                            {
                                                return gzipResourceAsParsedVCF(fileName, data);
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
                                                                      @Override
                                                                      public String getFileName()
                                                                      {
                                                                          return fileName;
                                                                      }

                                                                      @Override
                                                                      public byte[] getData()
                                                                      {
                                                                          return data.get();
                                                                      }

                                                                      @Override
                                                                      public String getChromosomeName()
                                                                      {
                                                                          return chromosomeName;
                                                                      }

                                                                      @Override
                                                                      public VCFResourceAccessor asParsedVCF()
                                                                      {
                                                                          return gzipResourceAsParsedVCF(fileName, data.get());
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