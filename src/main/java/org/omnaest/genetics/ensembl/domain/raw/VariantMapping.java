package org.omnaest.genetics.ensembl.domain.raw;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VariantMapping
{
    @JsonProperty
    private String location;

    @JsonProperty("assembly_name")
    private String assemblyName;

    @JsonProperty
    private long end;

    @JsonProperty
    private long start;

    @JsonProperty("seq_region_name")
    private String sequenceRegionName;

    @JsonProperty
    private int strand;

    @JsonProperty("coord_system")
    private String coordinateSystem;

    @JsonProperty("allele_string")
    private String allele;

    public String getLocation()
    {
        return this.location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getAssemblyName()
    {
        return this.assemblyName;
    }

    public void setAssemblyName(String assemblyName)
    {
        this.assemblyName = assemblyName;
    }

    public long getEnd()
    {
        return this.end;
    }

    public void setEnd(long end)
    {
        this.end = end;
    }

    public long getStart()
    {
        return this.start;
    }

    public void setStart(long start)
    {
        this.start = start;
    }

    public String getSequenceRegionName()
    {
        return this.sequenceRegionName;
    }

    public void setSequenceRegionName(String sequenceRegionName)
    {
        this.sequenceRegionName = sequenceRegionName;
    }

    public int getStrand()
    {
        return this.strand;
    }

    public void setStrand(int strand)
    {
        this.strand = strand;
    }

    public String getCoordinateSystem()
    {
        return this.coordinateSystem;
    }

    public void setCoordinateSystem(String coordinateSystem)
    {
        this.coordinateSystem = coordinateSystem;
    }

    public String getAllele()
    {
        return this.allele;
    }

    public void setAllele(String allele)
    {
        this.allele = allele;
    }

    @Override
    public String toString()
    {
        return "VariantMapping [location=" + this.location + ", assemblyName=" + this.assemblyName + ", end=" + this.end + ", start=" + this.start
                + ", sequenceRegionName=" + this.sequenceRegionName + ", strand=" + this.strand + ", coordinateSystem=" + this.coordinateSystem + ", allele="
                + this.allele + "]";
    }

}
