package uk.ac.ebi.uniprot.glygen.util;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.getAbsFileName;

public class GetSars2EntriesFromPreRelease {
    private static void writeHeader(BufferedWriter writer) throws IOException {
        writer.write("<?xml version='1.0' encoding='UTF-8'?>");
        writer.newLine();
        writer.write("<rdf:RDF xml:base=\"http://purl.uniprot.org/uniprot/\" xmlns=\"http://purl.uniprot.org/core/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\" xmlns:bibo=\"http://purl.org/ontology/bibo/\" xmlns:foaf=\"http://xmlns.com/foaf/0.1/\" xmlns:void=\"http://rdfs.org/ns/void#\" xmlns:sd=\"http://www.w3.org/ns/sparql-service-description#\" xmlns:faldo=\"http://biohackathon.org/resource/faldo#\">");
        writer.newLine();
        writer.write("<owl:Ontology rdf:about=\"http://purl.uniprot.org/uniprot/\">");
        writer.newLine();
        writer.write("<owl:imports rdf:resource=\"http://purl.uniprot.org/core/\"/>");
        writer.newLine();
        writer.write("</owl:Ontology>");
        writer.newLine();
    }

    private static void extractProteinsFromRDF(HashSet<String> set) {
        File inFile = new File(getAbsFileName("in/covid-19.rdf"));
        File outFile = new File(getAbsFileName("in/UP000464024_2697049_uniprot_proteome.rdf"));

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writeHeader(writer);
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (line.startsWith("<rdf:Description")) {
                    String key=line.substring(line.indexOf('=')+2);
                    if (set.contains(key)) {
                        set.remove(key);
                        writer.write(line);
                        writer.newLine();
                        while ((line = fileReader.readLine()) != null && !line.startsWith("</rdf")) {
                            if (line.contains("rdf:res") && !line.startsWith("<rdf:type") && ! line.startsWith("<owl:sameAs")) {
                                String[] ar = line.split(" ");
                                set.add(ar[1].substring(ar[1].indexOf('=')+2, ar[1].lastIndexOf('"')+1) + '>');
                                if (ar.length > 2) {
                                    set.add('#' + ar[2].substring(ar[2].indexOf('=')+2, ar[2].lastIndexOf('"')+1) + '>');
                                }
                            } else if (line.contains("rdf:ID")) { // rdf:id of attributes, eg: fullName
                                set.add('#' + line.substring(line.indexOf('=')+2, line.indexOf("\">")+2));
                            }
                            writer.write(line);
                            writer.newLine();
                        }
                        if (line != null) {
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                }
            }
            writer.write("</rdf:RDF>");
            writer.flush();
        } catch (IOException ex) {
            throw new GlyGenException(ex);
        }
    }

    private static void extractProteinsFromFasta(String oxCode) {
        File inFile = new File(getAbsFileName("in/covid-19.fasta"));
        File outFile = new File(getAbsFileName("in/UP000464024_2697049.fasta"));

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            String line = fileReader.readLine();
            while (line != null) {
                if (line.startsWith(">") && line.contains(oxCode)) {
                    writer.write(line);
                    writer.newLine();
                    while ((line = fileReader.readLine()) != null && !line.startsWith(">")) {
                        writer.write(line);
                        writer.newLine();
                    }
                } else {
                    line = fileReader.readLine();
                }
            }
            writer.flush();
        } catch (IOException ex) {
            throw new GlyGenException(ex);
        }
    }


    public static void main(String[] args) {
        // 17 accessions
        HashSet<String> setSars2 = new HashSet<>();
        setSars2.add("http://purl.uniprot.org/uniprot/A0A663DJA2\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC1\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC2\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC3\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC4\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC5\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC6\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC7\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC8\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTC9\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTD1\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTD2\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTD3\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTD8\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTF1\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTG0\">");
        setSars2.add("http://purl.uniprot.org/uniprot/P0DTG1\">");
        extractProteinsFromRDF(setSars2);
        extractProteinsFromFasta("OX=2697049");
        System.out.println("Generated in/UP000464024_2697049_uniprot_proteome.rdf & in/UP000464024_2697049.fasta");
    }
}