package uk.ac.ebi.uniprot.glygen.writer;

import uk.ac.ebi.uniprot.glygen.appender.BasicInfoAppender;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.File;
import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.cleanUp;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

public class StatisticsWriterTest {
    @TempDir
    File dir;
    @Test
    void testStatsWriteOutput() throws IOException {
        String statFileName = "statistics-test.json";
        cleanUp(statFileName);

        File file = new File(statFileName);
        assertFalse(file.exists());

        GlygenConfig.setStatisticsFile("statistics-xxxx.json");
        GlygenConfig config = new GlygenConfig();
        config.setName("test");

        Model outModel = ModelFactory.createDefaultModel();

        GlygenDataset dataset = new GlygenDataset(getTestRdfModel(), outModel);
        BasicInfoAppender appender = new BasicInfoAppender();
        appender.appendData(dataset);

        new StatisticsWriter(dir.getAbsolutePath()).writeOutput(config, outModel);
        String fullname = dir.getAbsolutePath() +File.separator + statFileName;
        File ntFile = new File(fullname);
        assertTrue(ntFile.exists());

        cleanUp(statFileName);
    }
}
