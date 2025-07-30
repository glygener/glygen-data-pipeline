package uk.ac.ebi.uniprot.glygen.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.*;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.cleanUp;
import static uk.ac.ebi.uniprot.glygen.util.TestUtility.getTestResourcePath;

class DbSnpDatasetGeneratorTest {
    @TempDir
    File dir;

    @Test
    void testDbSnpAppendData() throws IOException {
        String statFileName = "dbSNP-test.json";
        cleanUp(statFileName);

        File file = new File(statFileName);
        assertFalse(file.exists());

        GlygenConfig.setDbSnpOut("dbSNP-xxxx.tsv");
        GlygenConfig config = new GlygenConfig();
        config.setName("test");

        config.setDbSnp(getTestResourcePath("in/testDbSnp.xml"));

        // new DbSnpDatasetGenerator(config, dir.getAbsolutePath()).generateDataset();

        int count = 0;
        String fullname = dir.getAbsolutePath() + File.separator + "dbSNP-test.tsv.gz";
        //  BufferedReader reader = new BufferedReader(new FileReader(fullname));
        GZIPInputStream cis = new GZIPInputStream(new FileInputStream(fullname));
        // FileOutputStream fos = new FileOutputStream(newFile);
        Reader decoder = new InputStreamReader(cis);
        BufferedReader reader = new BufferedReader(decoder);

        while (reader.readLine() != null) {
            count++;
        }
        reader.close();
        assertEquals(142, count);
    }

}
