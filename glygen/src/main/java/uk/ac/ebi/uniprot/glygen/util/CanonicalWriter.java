package uk.ac.ebi.uniprot.glygen.util;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.kraken.util.fasta.FastaReader;
import uk.ac.ebi.uniprot.glygen.core.ConfigReader;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getCanonicalSequenceIdFromRdf;


/**
 * Class to generate glygen dataset stats
 */
public class CanonicalWriter {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void generateCanonicalCsv(List<GlygenConfig> configList) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(GlygenConfig.getCanonicalMapOut()))) {
            writer.write("Accession,GeneName,TaxId,Species");
            writer.newLine();

            for (GlygenConfig config : configList) {
                Dataset dataset = TDBFactory.createDataset(config.getDbDirOut());
                dataset.begin(ReadWrite.READ);
                Model model = dataset.getDefaultModel();
                try (FastaReader fastaReader = new FastaReader(getAbsFileName(config.getCanonical()))) {
                    fastaReader.openInputFile();

                    FastaReader.Sequence sequence;
                    while ((sequence = fastaReader.nextSequence()) != null) {
                        String accession = getUniprotIdFromSequenceName(sequence.name);
                        CanonicalCsvInfo canCsv = new CanonicalCsvInfo();
                        canCsv.accession = isSpEntry(sequence.name) ?
                                getCanonicalSequenceIdFromRdf(model, accession) : accession + CANONICAL_SUFFIX;
                        if (canCsv.accession == null) {
                            logger.info("accession is null: {}", accession);
                            continue;
                        }
                        if (!canCsv.accession.endsWith("-1")) {
                            logger.debug("Non -1 canonical found: {} ", canCsv.accession);
                        }
                        canCsv.geneName = sequence.name.contains("GN=") ? sequence.name.substring(sequence.name.indexOf("GN=") + 3, sequence.name.indexOf("PE=")).trim() : "";
                        canCsv.tax_id = config.getTaxId();
                        canCsv.species = config.getName();
                        //logger.debug("canCsv: {}", canCsv);
                        writer.write(canCsv.toString());
                        writer.newLine();
                    }
                }
                dataset.end();
            }
            writer.flush();
        }
    }

    static class CanonicalCsvInfo {
        public String accession;
        public String geneName;
        public String tax_id;
        public String species;

        public String toString() {
            return accession + "," + geneName + "," + tax_id + "," + species;
        }
    }
    public static void main(String[] args) throws IOException {
        List<GlygenConfig> configList;
        try {
            configList = ConfigReader.getGlygenConfigList(new FileInputStream(getAbsFileName("glygenConfig.properties")), "");
        } catch (Exception ex) {
            throw new GlyGenException("Error reading Glygen config");
        }
        new CanonicalWriter().generateCanonicalCsv(configList);
    }
}