package uk.ac.ebi.uniprot.glygen;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.tdb.TDB;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.appender.*;
import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.generator.DbSnpDatasetGenerator;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;
import uk.ac.ebi.uniprot.glygen.util.RdfUtility;
import uk.ac.ebi.uniprot.glygen.writer.NTriplesWriter;
import uk.ac.ebi.uniprot.glygen.writer.StatisticsWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Generates glygen dataset for each proteome in glygenConfig.properties
 */
public class GlygenDataGeneratorMain {
    //   private static String CONFIG_FILE = "glygenConfig.properties";
    private static final Logger logger = LoggerFactory.getLogger(GlygenDataGeneratorMain.class);


    public static void main(String[] args) {
        try {
            GlyGenDataGeneratorConfigure configure = GlyGenDataGeneratorConfigure.fromCommandLine(args);
            new GlygenDataGeneratorMain().generateData(configure);
        } catch (Exception ex) {
            logger.error("error: ", ex);
            System.exit(1);
        }
    }

    public void generateData(GlyGenDataGeneratorConfigure configure) throws GlyGenException {


        GlygenConfig.setReactomeNeo4jInfo(configure.getReactomeDb());
        // createEntityManager(configure.getUniprotDb());

        String inputDataBaseDir = configure.getInputBaseDir();

        String outputDir = configure.getOutputDir();


        List<GlygenConfig> configList;
        try {
            configList = ConfigReader.getGlygenConfigList(new FileInputStream(configure.getConfigFile()), inputDataBaseDir);
        } catch (Exception ex) {
            logger.error("Error reading Glygen config ", ex);
            throw new GlyGenException("Error reading Glygen config");
        }
        List<String> runSpecies = configure.getSpecies();
        boolean allGood = true;
        for (GlygenConfig config : configList) {
            if (!speciesToRun(runSpecies, config.getName())) {
                logger.info("Skip the species: {}", config.getName());
                continue;
            }
            try {
                logger.info("Generate dataset for {}", config.getName());
                generateCoreDataForConfig(config);
                writeDataAndStats(config, outputDir);
                generateDatasets(config, outputDir);
            } catch (Exception e) {
                logger.error("Error generating Glygen dataset for {}", config.getName(), e);
                allGood = false;
            }
        }

        if (!allGood) {
            throw new GlyGenException("Error generating one or more Glygen datasets.");
        }
    }

    private boolean speciesToRun(List<String> speciesToRun, String species) {
        if (speciesToRun.contains("all")) {
            return true;
        }
        return speciesToRun.contains(species);
    }

    private void generateDatasets(GlygenConfig config, String outputDir) {
        logger.info("Start DbSnpDatasetGenerator");
        new DbSnpDatasetGenerator(config, outputDir).generateDataset();

        logger.info("Start ObsoleteAccessionDatasetGenerator");
        Dataset dataset = TDBFactory.createDataset(config.getDbDir());
        dataset.begin(ReadWrite.READ);
//        new ObsoleteAccessionDatasetGenerator(config, dataset.getDefaultModel(), entityManager, outputDir).generateDataset();
        dataset.end();
    }

    private void generateCoreDataForConfig(GlygenConfig config) throws IOException {
        TDB.getContext().set(TDB.symUnionDefaultGraph, true);
        Dataset inDataset = TDBFactory.createDataset(config.getDbDir());
        inDataset.begin(ReadWrite.READ);

        File file = new File(config.getDbDirOut());
        if (file.exists() && file.isDirectory()) {
            FileUtils.cleanDirectory(file);
        }
        Dataset outDataset = TDBFactory.createDataset(config.getDbDirOut());
        outDataset.begin(ReadWrite.WRITE);
        OntModel outModel = RdfUtility.getOntModelBase(outDataset.getDefaultModel());

        GlygenDataset dataset = new GlygenDataset(inDataset.getDefaultModel(), outModel);

        try {
            for (DataAppender appender : getAppenders(config)) {
                logger.info("Start {}", appender.getClass());
                appender.appendData(dataset);
                logger.info("End {}", appender.getClass());
            }
        } finally {
            outDataset.commit();
            outDataset.end();
        }

        inDataset.end();
    }

    private void writeDataAndStats(GlygenConfig config, String outputDir) {
        Dataset dataset = TDBFactory.createDataset(config.getDbDirOut());
        dataset.begin(ReadWrite.READ);

        new NTriplesWriter(outputDir).writeOutput(config, dataset.getDefaultModel());
        new StatisticsWriter(outputDir).writeOutput(config, dataset.getDefaultModel());

        dataset.end();
    }

    private List<DataAppender> getAppenders(GlygenConfig config) {
        // appender order per dependency - eg: genomicCoordinate depends on transcript & sequence
        return asList(
                new BasicInfoAppender(),
                new NameAppender(),
                new GeneAppender(config),
                new TissueAppender(config),
                new SequenceAppender(config),
                new TranscriptResourceAppender(config),
                new GenomicCoordinateAppender(config),
                new ClassificationAppender(),
                new EnzymeAppender(),
                new InteractionAppender(config),
                new StructureAppender(),
                new ProteinComponentAppender(),
                new AnnotationAppender(config),
                new CrossReferenceAppender(),
                new CitationAppender(),
                new ReactionAnnotationAppender(config),
                new DatabaseInfoAppender(config));
    }
}
