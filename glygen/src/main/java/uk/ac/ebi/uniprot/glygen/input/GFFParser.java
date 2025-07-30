package uk.ac.ebi.uniprot.glygen.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GFFParser {
    private static final Pattern p = Pattern.compile("\t");

    public static List<GFFFeature> parse(String path) {
        Path pathFile = Paths.get(path);
        try (BufferedReader br = Files.newBufferedReader(pathFile)) {

            String s;
            List<GFFFeature> features = new ArrayList<>();
            for (s = br.readLine(); null != s; s = br.readLine()) {
                s = s.trim();

                if (!s.isEmpty()) {
                    if (s.charAt(0) == '#') {
                        //ignore comment lines
                        if (s.startsWith("##fasta")) break;
                    } else {

                        GFFFeature f = parseLine(s);
                        features.add(f);
                    }
                }
            }
            return features;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static GFFFeature parseLine(String s) {
        String[] line = p.split(s);
        String seqname = line[0].trim();

        String source = line[1].trim();

        String type = line[2].trim();


        String locStart = line[3].trim();

        String locEnd = line[4].trim();

        double score;

        try {
            score = Double.parseDouble(line[5].trim());
        } catch (Exception e) {
            score = 0.0;
        }


        char strand = line[6].trim().charAt(0);
        int locationStart = Integer.parseInt(locStart);
        int locationEnd = Integer.parseInt(locEnd);
        if (locationStart > locationEnd) {
            int temp = locationStart;
            locationStart = locationEnd;
            locationEnd = temp;

        }
        int frame;
        try {
            frame = Integer.parseInt(line[7].trim());
        } catch (Exception e) {
            frame = -1;
        }
        String attributes = line[8];
        return new GFFFeature(seqname, source, type, locationStart, locationEnd, strand, score, frame, attributes.split("#")[0]);
    }
}
