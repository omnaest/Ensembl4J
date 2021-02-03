# Ensembl4J
REST and FTP utils for the ENSEMBL REST API at http://rest.ensembl.org/

## Example
### Retrieve gene location

    GeneLocation location = EnsemblUtils.getInstance()
                                        .findSpecies("human")
                                        .findGene("BHMT")
                                        .getLocation();

### Retrieve variant details											
    VariantConsequence consequence = EnsemblUtils.getInstance()
                                                 .getHuman();
                                                 .findVariantDetail("rs682985")
                                                 .getConsequence();		
							
## Example with local cache (folder /cache)
    EnsemblUtils.getInstance()
                .usingLocalCache()
                .getHuman();
					
## REST

    Sequences sequence = EnsemblRESTUtils.getInstance()
                                         .getCodingDNASequence("ENSG00000145692");

## FTP

    EnsemblFTPUtils.load()
                   .withLocalDirectoryCache()
                   .variationVCFFiles()
                   .current()
                   .forSpecies(species)
                   .forChromosomes()
                   .flatMap(resource -> resource.asParsedVCF()                                                      
                                                .getRecords())

# Maven Snapshots

    <dependency>
      <groupId>org.omnaest.genomics</groupId>
      <artifactId>Ensembl4J</artifactId>
      <version>0.0.1-SNAPSHOT</version>
    </dependency>
    
    <repositories>
    	<repository>
    		<id>ossrh</id>
    		<url>https://oss.sonatype.org/content/repositories/snapshots</url>
    		<snapshots>
    			<enabled>true</enabled>
    		</snapshots>
    	</repository>
    </repositories>