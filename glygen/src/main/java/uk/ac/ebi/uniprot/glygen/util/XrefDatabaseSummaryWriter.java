package uk.ac.ebi.uniprot.glygen.util;

import com.beust.jcommander.Parameter;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.DATABASE;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getDistinctDbCrossRefDatabasesFromRdf;

public class XrefDatabaseSummaryWriter {
    private final String outputDir;

    private final static String rootDir = System.getProperty("user.dir");

    @Parameter(names = "-configFile", description = "Glygen configure location: glygenConfig.properties")
    private final static String configFile = rootDir.concat("/src/main/resources/glygenConfig.properties");

    public XrefDatabaseSummaryWriter(String outputDir) {
        this.outputDir = outputDir;
    }

    public void generateXrefDbCsv(List<GlygenConfig> configList) throws IOException {
        Map<String, Set<String>> mainMap = new TreeMap<>();
        Set<String> mainSet = new TreeSet<>();

        for (GlygenConfig config : configList) {
            Dataset dataset = TDBFactory.createDataset(config.getDbDirOut());
            dataset.begin(ReadWrite.READ);
            Model model = dataset.getDefaultModel();
            ResultSet resultSet = getDistinctDbCrossRefDatabasesFromRdf(model);
            Set<String> dbs = new TreeSet<>();
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.nextSolution();
                String uri = solution.getResource(DATABASE).getURI();
                dbs.add(uri.substring(uri.lastIndexOf('/') + 1));
            }
            mainMap.put(config.getName(), dbs);
            mainSet.addAll(dbs);
            System.out.println(
                    "Config : " + config.getName() + " count: " + dbs.size() + ". Main set count: " + mainSet.size());
            dataset.end();
        }
        String outputFile = outputDir + File.separator + GlygenConfig.getDbXrefMapOut();
        try (FileOutputStream output = new FileOutputStream(outputFile);
             Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {
            StringBuilder row = new StringBuilder("db");
            for (String org : mainMap.keySet()) {
                row.append(",").append(org);
            }
            writer.write(row + "\n");
            for (String db : mainSet) {
                row = new StringBuilder(db);
                for (String org : mainMap.keySet()) {
                    row.append(",").append(mainMap.get(org).contains(db) ? "true" : "false");
                }
                writer.write(row + "\n");
            }
            writer.flush();
        }

    }

    public static void main(String[] args) throws IOException {
        List<GlygenConfig> configList;
        String inputDataBaseDir = "";
        if (args.length == 1) {
            inputDataBaseDir = args[0];
        }
        try {
            configList = ConfigReader.getGlygenConfigList(
                    new FileInputStream(configFile), inputDataBaseDir);
        } catch (Exception ex) {
            throw new GlyGenException("Error reading Glygen config");
        }
        new XrefDatabaseSummaryWriter(".").generateXrefDbCsv(configList);
    }
}
