package uk.ac.ebi.uniprot.glygen.generator;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPOutputStream;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.PROTEIN;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getSecondaryAccessionsFromRdf;

@Deprecated
public class ObsoleteAccessionDatasetGenerator implements DatasetGenerator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String OBSOLETE_FILE_PREFIX = "accession-history-";

    private final GlygenConfig config;
    private final Model rdfModel;
    private final EntityManager entityManager;
    private final String outputDir;

    public ObsoleteAccessionDatasetGenerator(GlygenConfig config, Model rdfModel,
                                             EntityManager entityManager, String outputDir) {
        this.config = config;
        this.rdfModel = rdfModel;
        this.entityManager = entityManager;
        this.outputDir = outputDir;
    }

    @Override
    public void generateDataset() {
        // List<String> deletedAccs = getDeletedAccessions();
        Map<String, TreeSet<String>> secondaryAccs = getSecondaryAccessions();
        String gzfile = outputDir + File.separator + OBSOLETE_FILE_PREFIX + config.getName() + ".tsv";
        if (!gzfile.endsWith(".gz")) {
            gzfile += ".gz";
        }
        try (FileOutputStream output = new FileOutputStream(gzfile);
             Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {

//        try (BufferedWriter writer = new BufferedWriter(
//                new FileWriter(fullFilename))) {

            writer.write("old_accession\tcurrent_accession\n");

            for (String accession : secondaryAccs.keySet()) {
                writer.write(secondaryAccs.get(accession).toString().replace('[', ' ')
                        .replace(']', ' ').trim());
                writer.write("\t" + accession + "\n");
            }
            // for (String accession : deletedAccs) {
            //     writer.write(accession +"\n");
            // }
            writer.flush();

        } catch (Exception ex) {
            throw new GlyGenException("Error creating obsolete accession dataset " + ex);
        }
    }

    private Map<String, TreeSet<String>> getSecondaryAccessions() {
        ResultSet resultSet = getSecondaryAccessionsFromRdf(rdfModel);
        Map<String, TreeSet<String>> map = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String acc = solution.getResource(PROTEIN).getURI();
            acc = acc.substring(acc.lastIndexOf('/') + 1);
            if (!map.containsKey(acc)) map.put(acc, new TreeSet<>());
            String secAcc = solution.getResource("replaces").getURI();
            map.get(acc).add(secAcc.substring(secAcc.lastIndexOf('/') + 1));
        }
        logger.info("{} secondary accessions :{}", config.getUpid(), map.size());
        return map;
    }
    @Deprecated
    private List<String> getDeletedAccessions() {

        String sql = "select distinct d.accession\n" +
                "from dbentry d, dbentry_2_database d2d \n" +
                "where d.tax_id = ?1 and d.entry_type in (0,1) and d.merge_status <> 'R'\n" +
                "    and d2d.dbentry_id = d.dbentry_id\n" +
                "    and d2d.primary_id = ?2 and d2d.database_id = 'UPID'\n" +
                "intersect\n" +
                "select accession from deleted_accessions";

        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, Integer.parseInt(config.getTaxId()));
        q.setParameter(2, config.getUpid());
        List accs = q.getResultList();
        List<String> deletedList = new ArrayList<>();
        for (Object accession : accs) {
            deletedList.add(accession.toString());
        }
        logger.info("{} deleted accessions :{}", config.getUpid(), deletedList.size());
        return deletedList;
    }

//    public static void main(String[] args) {
//        GlygenConfig.setSpDbInfo("sptr/uni2read@swpread");
//        GlygenConfig config = new GlygenConfig();
//        config.setTaxId("9606");
//        config.setUpid("UP000005640");
//        new ObsoleteAccessionDatasetGenerator(config).generateDataset();
//    }
}
