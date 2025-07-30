package uk.ac.ebi.uniprot.glygen.model;

public class DbSnpAssociation {
    private String name = "";
    private String description = "";
    private String xrefs = ""; //<sourceName:ID>,<sourceName:ID>
    private String evidence269 = ""; // <sourceName:ID>,<sourceName:ID>
    private String evidence313 = ""; // <sourceName:ID>,<sourceName:ID>

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getXrefs() {
        return xrefs;
    }

    public void setXrefs(String val) {
        if (!xrefs.isEmpty()) {
            xrefs += ",";
        }
        xrefs += val;
    }

    public String getEvidence269() {
        return evidence269;
    }

    public void setEvidence269(String val) {
        if (!evidence269.isEmpty()) {
            evidence269 += ",";
        }
        evidence269 += val;
    }

    public String getEvidence313() {
        return evidence313;
    }

    public void setEvidence313(String val) {
        if (!evidence313.isEmpty()) {
            evidence313 += ",";
        }
        evidence313 += val;
    }

}
