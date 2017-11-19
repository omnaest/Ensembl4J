# Ensembl4J
REST utils for the ensembl rest api at http://rest.ensembl.org/

##Example

		GeneLocation location = EnsemblUtils.getInstance()
											.findSpecies("human")
											.findGene("BHMT")
											.getLocation();
