package uk.ac.ebi.uniprot.glygen;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.generator.ObsoleteAccessionDatasetGenerator;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.FileInputStream;
import java.util.List;

public class AccessionHistoryGenerator {
    private static final Logger logger = LoggerFactory.getLogger(AccessionHistoryGenerator.class);

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java AccessionHistoryGenerator <glygen_dir> <output_dir>");
            System.out.println("Example: java AccessionHistoryGenerator ./glygen ./releases/2026_01");
            System.exit(1);
        }

        String glygenDir = args[0];
        String outputDir = args[1];
        String configFile = glygenDir + "/src/main/resources/glygenConfig.properties";

        logger.info("Generating accession-history files...");
        logger.info("Config: {}", configFile);
        logger.info("Output: {}", outputDir);

        try {
            List<GlygenConfig> configList = ConfigReader.getGlygenConfigList(
                    new FileInputStream(configFile), glygenDir);

            for (GlygenConfig config : configList) {
                logger.info("Processing {}", config.getName());
                
                // Load the TDB database
                Dataset dataset = TDBFactory.createDataset(config.getDbDir());
                dataset.begin(ReadWrite.READ);
                
                try {
                    // Generate accession-history for this organism
                    new ObsoleteAccessionDatasetGenerator(config, dataset.getDefaultModel(), null, outputDir)
                            .generateDataset();
                    logger.info("Generated accession-history for {}", config.getName());
                } finally {
                    dataset.end();
                }
            }
            
            logger.info("Accession-history generation complete!");
        } catch (Exception ex) {
            logger.error("Error generating accession-history", ex);
            throw new GlyGenException("Failed to generate accession-history files", ex);
        }
    }
}
