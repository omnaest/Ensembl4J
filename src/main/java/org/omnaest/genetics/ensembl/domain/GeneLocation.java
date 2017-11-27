package org.omnaest.genetics.ensembl.domain;

public class GeneLocation
{
	private String	chromosome;
	private String	referenceAssembly;
	private Range	position;

	public GeneLocation(String chromosome, String referenceAssembly, Range position)
	{
		super();
		this.chromosome = chromosome;
		this.referenceAssembly = referenceAssembly;
		this.position = position;
	}

	public String getChromosome()
	{
		return chromosome;
	}

	public String getReferenceAssembly()
	{
		return referenceAssembly;
	}

	public Range getPosition()
	{
		return position;
	}

	@Override
	public String toString()
	{
		return "GeneLocation [chromosome=" + chromosome + ", referenceAssembly=" + referenceAssembly + ", position=" + position + "]";
	}

}