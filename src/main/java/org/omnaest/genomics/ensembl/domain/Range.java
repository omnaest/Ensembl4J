package org.omnaest.genomics.ensembl.domain;

public class Range
{
	private long	start;
	private long	end;

	public Range(long start, long end)
	{
		super();
		this.start = start;
		this.end = end;
	}

	public long getStart()
	{
		return start;
	}

	public long getEnd()
	{
		return end;
	}

	@Override
	public String toString()
	{
		return "Range [start=" + start + ", end=" + end + "]";
	}

}