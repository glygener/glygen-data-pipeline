package uk.ac.ebi.uniprot.glygen.writer;

import uk.ac.ebi.uniprot.glygen.appender.BasicInfoAppender;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.File;
import java.io.IOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.cleanUp;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestRdfModel;

class NTriplesWriterTest {
    @TempDir
    File dir;
    @Test
    void testNtWriteOutput() throws IOException {
        String ntFileName = "uniprot-proteome-test.json";
        cleanUp(ntFileName);

        File file = new File(ntFileName);
        assertFalse(file.exists());

        GlygenConfig.setRdfOutput("uniprot-proteome-xxxx.json");
        GlygenConfig config = new GlygenConfig();
        config.setName("test");

        Model outModel = ModelFactory.createDefaultModel();
        GlygenDataset dataset = new GlygenDataset(getTestRdfModel(), outModel);

        BasicInfoAppender appender = new BasicInfoAppender();
        appender.appendData(dataset);

        new NTriplesWriter(dir.getAbsolutePath()).writeOutput(config, outModel);
        String fullname =dir.getAbsolutePath() + File.separator + ntFileName;
        File ntFile = new File(fullname +".gz");
        assertTrue(ntFile.exists());

        cleanUp(ntFileName);
    }

    @Test
    void testExceptionOnEmptyModel() {
        assertThrows(GlyGenException.class, () ->
                new NTriplesWriter(dir.getAbsolutePath()).writeOutput(new GlygenConfig(), ModelFactory.createDefaultModel()));
    }
}
