package uk.ac.ebi.uniprot.glygen.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

public class AllAccessionWriter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String inputDirectory;
    private final String outputDirectory;
    private final static String SEC_AC_FILE ="sec_ac.txt";
    private final static String DELAC_SP_FILE ="delac_sp.txt";
    private final static String DELAC_TR_FILE ="delac_tr.txt";
    private final static String OUTPUT_FILE ="all_accessions.csv.gz";
    private final static String SEC_AC_START="Secondary AC";
    private final static String PRIMARY ="primary";
    private final static String SECONDARY ="secondary";
    private final static String OBSOLETE ="obsolete";
    private final static String COMA =",";
    private final static String LINE_TERM ="\n";
    private final static String DELACC_START ="Accession number";
    public AllAccessionWriter(String inputDirectory, String outputDirectory) {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
    }
    
    public void write() throws  IOException{
        logger.info("Writer "+OUTPUT_FILE +" file...");
        int count =0;
        String outputFullname = outputDirectory +File.separator + OUTPUT_FILE;
        try (FileOutputStream output = new FileOutputStream(outputFullname);
                Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), "UTF-8")){
            count += writePrimaryAccession(writer);
            count += writeSecAccession(writer);
            count += writeDelaccSp(writer);
            count += writeDelaccTr(writer);
        }
        System.out.println("Total size: " + count);
    }

    private int writePrimaryAccession(Writer writer) throws  IOException {
        String inputFullname = inputDirectory +File.separator + SEC_AC_FILE;
        int count=0;
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFullname)) ){
            skipSeconAccFileHead(reader);
            String line;
            while((line=reader.readLine()) !=null) {  
                String [] tokens = line.split("\\s+");
                if(tokens.length !=2) {
                    continue;
                }
                result.add(tokens[1] +COMA + PRIMARY + COMA );
                
            }
            List<String> sorted = result.stream().sorted().distinct().collect(Collectors.toList());
            for(String val: sorted) {
                writer.write(val + LINE_TERM);
                count++;
            }
            writer.flush();
            logger.info("Total primary Accession: " + count);
        }
       
        return count;
    }
    
    private int writeSecAccession(Writer writer) throws  IOException {
        String inputFullname = inputDirectory +File.separator + SEC_AC_FILE;
        int count=0;
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFullname)) ){
            skipSeconAccFileHead(reader);
            String line;
            while((line=reader.readLine()) !=null) {  
                String [] tokens = line.split("\\s+");
                if(tokens.length !=2) {
                    continue;
                }
                result.add(tokens[0] +COMA + SECONDARY + COMA + tokens[1]);
                
            }
            List<String> sorted = result.stream().sorted().collect(Collectors.toList());
            for(String val: sorted) {
                writer.write(val + LINE_TERM);
                count++;
            }
            writer.flush();
            logger.info("Total secondary Accession: " + count);
        }
       
        return count;
    }
    
    
    private int writeDelaccSp(Writer writer) throws  IOException {
        String inputFullname = inputDirectory +File.separator + DELAC_SP_FILE;
        int count=0;
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFullname)) ){
            skipDelacFileHead(reader);
            String line;
            while((line=reader.readLine()) !=null) {  
                if(line.isBlank()) {
                    continue;
                }
                if(line.startsWith("___")) {
                    continue;
                }
                if(line.startsWith("---")) {
                    break;
                }
               
                result.add(line +COMA + OBSOLETE + COMA );
                count++;
            }
            List<String> sorted = result.stream().sorted().distinct().collect(Collectors.toList());
            for(String val: sorted) {               
                writer.write(val + LINE_TERM);
            }
            writer.flush();
            logger.info("Total obsolete SwissProt: " + count);
        }
       
        return count;
    }
    
    private int writeDelaccTr(Writer writer) throws  IOException {
        String inputFullname = inputDirectory +File.separator + DELAC_TR_FILE;
        int count=0;
      
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFullname)) ){
            skipDelacFileHead(reader);
            String line;
            while((line=reader.readLine()) !=null) {              
                writer.write(line +COMA + OBSOLETE + COMA + LINE_TERM);
                count++;
            }
            writer.flush();
            logger.info("Total obsolete TrEMBL: " + count);
        }
       
        return count;
    }
    
    private void skipSeconAccFileHead(BufferedReader reader) throws IOException {
        String line;
        while((line=reader.readLine()) !=null) {           
            if(line.startsWith(SEC_AC_START)) {
                reader.readLine();
                return;
            }
           
        }
    }    
    private void skipDelacFileHead(BufferedReader reader) throws IOException {
        String line;
        while((line=reader.readLine()) !=null) {           
            if(line.startsWith(DELACC_START)) {
                reader.readLine();
                return;
            }
           
        }
    }
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Need to two arguments: first is the input file directory, "
                    + "second is output file directory");
            System.exit(1);
        }
        try {
            AllAccessionWriter writer = new AllAccessionWriter(args[0], args[1]);
            writer.write();
        } catch (Exception ex) {
            throw new GlyGenException("Write all_accessions.csv.gz failed");
        }
       
    }
    
}
