package uk.ac.ebi.uniprot.glygen;

import com.beust.jcommander.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.util.AllAccessionWriter;
import uk.ac.ebi.uniprot.glygen.util.XrefDatabaseSummaryWriter;

import java.io.FileInputStream;
import java.util.List;

public class GlyGenOtherDataGeneratorMain {
    private static final Logger logger = LoggerFactory.getLogger(GlyGenToolMain.class);

    private final static String rootDir = System.getProperty("user.dir");

    @Parameter(names = "-configFile", description = "Glygen configure location: glygenConfig.properties")
    private final static String configFile = rootDir.concat("/src/main/resources/glygenConfig.properties");

    public static void main(String[] args) {
        logger.info("Starting GlyGenOtherDataGenerator...");
        if (args.length < 2) {
            System.out.println("Need to two arguments: first is the input file base directory, "
                    + "second is output file directory");
            System.exit(1);
        }
        String runType = "all";
        if (args.length == 3) {
            runType = args[2];
        }

        String inputDataBaseDir = args[0];
        String outputDir = args[1];
        if (runType.equalsIgnoreCase("all") || runType.equalsIgnoreCase("xref")) {
            xrefDataWriter(inputDataBaseDir, outputDir);
        }
        if (runType.equalsIgnoreCase("all") || runType.equalsIgnoreCase("acc")) {
            allAccessionsWriter(inputDataBaseDir, outputDir);
        }
        logger.info("GlyGenOtherDataGenerator DONE");

    }

    private static void xrefDataWriter(String inputDataBaseDir, String outputDir) {
        List<GlygenConfig> configList;
        try {
            configList = ConfigReader.getGlygenConfigList(
                    new FileInputStream(configFile), inputDataBaseDir);
        } catch (Exception ex) {
            throw new GlyGenException("Error reading Glygen config", ex);
        }
        try {
            new XrefDatabaseSummaryWriter(outputDir).generateXrefDbCsv(configList);
        } catch (Exception ex) {
            logger.error("Error generating Xref DB CSV", ex);
            throw new GlyGenException("failed to generate xref database summary");
        }
    }

    private static void allAccessionsWriter(String inputDataBaseDir, String outputDir) {
        try {
            AllAccessionWriter writer = new AllAccessionWriter(inputDataBaseDir + "/in", outputDir);
            writer.write();
        } catch (Exception ex) {
            throw new GlyGenException("Write all_accessions.csv.gz failed");
        }
    }

}
