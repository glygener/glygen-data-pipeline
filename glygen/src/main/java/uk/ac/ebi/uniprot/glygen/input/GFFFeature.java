package uk.ac.ebi.uniprot.glygen.input;

import java.util.HashMap;

public class GFFFeature {

    private String seqname;
    private String source;
    private String type;
    private double score;
    private int frame;
    private String attributes;
    private HashMap<String, String> userMap;
    private int locationStart;
    private int locationEnd;
    private char strand;

    public GFFFeature(String seqname, String source, String type, int locationStart, int locationEnd, char strand,
            Double score, int frame, String attributes) {

        this.seqname = seqname;
        this.source = source;
        this.type = type;
        this.locationStart = locationStart;
        this.locationEnd = locationEnd;
        this.strand = strand;
        this.score = score;
        this.frame = frame;
        this.attributes = attributes;
        initAttributeHashMap();
        this.userMap = new HashMap<>();

    }

    HashMap<String, String> attributeHashMap = new HashMap<>();

    private void initAttributeHashMap() {
        String[] values = this.attributes.split(";");
        for (String attribute : values) {
            attribute = attribute.trim();
            int equalindex = attribute.indexOf("=");
            String splitData = "=";
            if (equalindex == -1) // gtf uses space and gff3 uses =
                splitData = " ";
            String[] data = attribute.split(splitData);
            String value = "";
            if (data.length >= 2 && data[1].indexOf('"') != -1) { // an attibute field could be empty
                value = data[1].replaceAll("\"", "").trim();
            } else if (data.length >= 2) {
                value = data[1].trim();
            }
            attributeHashMap.put(data[0].trim(), value);
        }
    }

    public String getSeqname() {
        return seqname;
    }

    public void setSeqname(String seqname) {
        this.seqname = seqname;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public char getStrand() {
        return strand;
    }

    public void setStrand(char strand) {
        this.strand = strand;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public int getFrame() {
        return frame;
    }

    public void setFrame(int frame) {
        this.frame = frame;
    }

    public String getAttribute(String key) {
        return attributeHashMap.get(key);
    }

    public String getAttributes() {
        return attributes;
    }

    public void setAttributes(String attributes) {
        this.attributes = attributes;
    }

    public HashMap<String, String> getUserMap() {
        return userMap;
    }

    public void setUserMap(HashMap<String, String> userMap) {
        this.userMap = userMap;
    }

    public int getLocationStart() {
        return locationStart;
    }

    public void setLocationStart(int locationStart) {
        this.locationStart = locationStart;
    }

    public int getLocationEnd() {
        return locationEnd;
    }

    public void setLocationEnd(int locationEnd) {
        this.locationEnd = locationEnd;
    }

    public HashMap<String, String> getAttributeHashMap() {
        return attributeHashMap;
    }

    public void setAttributeHashMap(HashMap<String, String> attributeHashMap) {
        this.attributeHashMap = attributeHashMap;
    }

    @Override
    public String toString() {
        String s = this.seqname + '\t';
        s += this.source + '\t';
        s += this.type + '\t';
        s += this.locationStart + "\t";
        s += this.locationEnd + "\t";
        s += this.score + "\t";

        if (this.frame == -1) {
            s += ".\t";
        } else {
            s += this.frame + "\t";
        }

        s += this.attributes;

        return s;
    }

}
