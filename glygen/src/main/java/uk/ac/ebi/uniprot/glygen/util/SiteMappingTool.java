package uk.ac.ebi.uniprot.glygen.util;

import uk.ac.ebi.kraken.util.fasta.FastaReader;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;

public class SiteMappingTool {
    private static final HashMap<String, String> iso_can_map = new HashMap<>();
    private static final HashMap<String, String> seq_map = new HashMap<>();

    private static void readIsoformMap(String map_file) throws IOException {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(map_file))) {
            String line;
            while ((line = fileReader.readLine()) != null) {
                line = line.replace("\"", "");
                String[] result = line.split(",");
                if (result.length > 3 && !result[3].isEmpty()) iso_can_map.put(result[3], result[0]);
                if (result.length > 4 && !result[4].isEmpty()) iso_can_map.put(result[4], result[0]);
            }
            System.out.println(" iso_can_map.size() : " + iso_can_map.size());
        }
    }

    private static void readSequenceMap(String seq_file) throws IOException {
        try (FastaReader fastaReader = new FastaReader(seq_file)) {
            fastaReader.openInputFile();

            FastaReader.Sequence sequence;
            while ((sequence = fastaReader.nextSequence()) != null) {
                String name = sequence.name.split("\\|")[1];
                seq_map.put(name, sequence.seq);
            }
            System.out.println(" seq_map.size() : " + seq_map.size());
        }
    }



    public static void main(String[] args) throws Exception {
        if (args.length != 8) {
            System.out.println("Input parameters missing, please provide inputs. \nUsage: uk.ac.ebi.uniprot.glygen.util.GlygenUtility.SiteMappingTool -protein_list <can_iso_mapping_file> -glygen_fasta <seq_file> -isoform_info <isoform_file> -out_file <output_file>");
            System.exit(1);
        }
        String map_file = "", seq_file = "", iso_file = "", out_file = "";
        for (int i = 0; i < 8 ; i+=2) {
            if (args[i].equals("-protein_list")) {
                map_file = args[i+1];
                if (!Files.isReadable(new File(map_file).toPath())) {
                    System.out.println("Please provide valid GlyGen canonical isoform mapping file for '-protein_list'");
                    System.exit(1);
                }
            }
            if (args[i].equals("-glygen_fasta")) {
                seq_file = args[i+1];
                if (!Files.isReadable(new File(seq_file).toPath())) {
                    System.out.println("Please provide valid GlyGen fasta file for '-glygen_fasta'");
                    System.exit(1);
                }
            }
            if (args[i].equals("-isoform_info")) {
                iso_file = args[i+1];
                if (!Files.isReadable(new File(iso_file).toPath())) {
                    System.out.println("Please provide valid list of isoforms file for '-isoform_info'");
                    System.exit(1);
                }
            }
            if (args[i].equals("-out_file")) {
                out_file = args[i+1];
                if (Files.exists(new File(out_file).toPath())) {
                    System.out.println("File already exists. Please delete " + out_file + " & relaunch");
                    System.exit(1);
                }
            }
        }

        readIsoformMap(map_file);
        readSequenceMap(seq_file);
    }


}
