package org.omnaest.genetics.ensembl.domain.raw;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalXRef
{
    @JsonProperty("db_display_name")
    private String displayName;

    @JsonProperty("display_id")
    private String displayId;

    @JsonProperty("primary_id")
    private String primaryId;

    @JsonProperty
    private String version;

    @JsonProperty
    private String description;

    @JsonProperty("dbname")
    private String name;

    @JsonProperty
    private List<String> synonyms;

    @JsonProperty("info_text")
    private String infoText;

    @JsonProperty("info_type")
    private String infoType;

    @JsonProperty("ensembl_identity")
    private long ensemblIdentity;

    @JsonProperty("ensembl_start")
    private long ensemblStart;

    @JsonProperty("ensembl_end")
    private long ensemblEnd;

    public long getEnsemblIdentity()
    {
        return this.ensemblIdentity;
    }

    public void setEnsemblIdentity(long ensemblIdentity)
    {
        this.ensemblIdentity = ensemblIdentity;
    }

    public long getEnsemblStart()
    {
        return this.ensemblStart;
    }

    public void setEnsemblStart(long ensemblStart)
    {
        this.ensemblStart = ensemblStart;
    }

    public long getEnsemblEnd()
    {
        return this.ensemblEnd;
    }

    public void setEnsemblEnd(long ensemblEnd)
    {
        this.ensemblEnd = ensemblEnd;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    public String getDisplayId()
    {
        return this.displayId;
    }

    public void setDisplayId(String displayId)
    {
        this.displayId = displayId;
    }

    public String getPrimaryId()
    {
        return this.primaryId;
    }

    public void setPrimaryId(String primaryId)
    {
        this.primaryId = primaryId;
    }

    public String getVersion()
    {
        return this.version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<String> getSynonyms()
    {
        return this.synonyms;
    }

    public void setSynonyms(List<String> synonyms)
    {
        this.synonyms = synonyms;
    }

    public String getInfoText()
    {
        return this.infoText;
    }

    public void setInfoText(String infoText)
    {
        this.infoText = infoText;
    }

    public String getInfoType()
    {
        return this.infoType;
    }

    public void setInfoType(String infoType)
    {
        this.infoType = infoType;
    }

    @Override
    public String toString()
    {
        return "ExternalXRef [displayName=" + this.displayName + ", displayId=" + this.displayId + ", primaryId=" + this.primaryId + ", version=" + this.version
                + ", description=" + this.description + ", name=" + this.name + ", synonyms=" + this.synonyms + ", infoText=" + this.infoText + ", infoType="
                + this.infoType + "]";
    }

}
