package uk.ac.ebi.uniprot.glygen.appender;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getGeneAltLabelsFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getGeneLabelFromRdf;

/**
 * Class to append gene info to uniprot accession.
 */
public class GeneAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Model outModel;
    private final GlygenConfig config;
    private GlygenDataset dataset;

    private HashMap<String, HashSet<String>> genResMap;

    public GeneAppender(GlygenConfig config) {
        this.config = config;
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        this.dataset = dataset;
        this.outModel = dataset.getOutModel();

        genResMap = new HashMap<>();

        updateGeneName(SKOS.prefLabel, PREF_LABEL, true);
        updateGeneAltNames();
        updateGeneName(outModel.createProperty(UP_ORF_NAME), ORF_NAME, false);
        updateGeneCoordinates();
    }

    private void updateGeneName(Property rdfProp, String prop, boolean isPrefLabel) {
        ResultSet resultSet = getGeneLabelFromRdf(dataset.getRdfModel(), isPrefLabel);
        int count = 0;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String geneName = solution.getLiteral(prop).toString();

            Resource gene = outModel.createResource(solution.getResource(ENCODED_BY).getURI());
            gene.addProperty(RDF.type, outModel.createResource(UP_TYPE_GENE));
            gene.addProperty(rdfProp, geneName);

            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(outModel.createProperty(UP_ENCODED_BY), gene);

            if (!genResMap.containsKey(geneName)) {
                genResMap.put(geneName, new HashSet<>());
            }
            genResMap.get(geneName).add(gene.getURI());
            count++;
        }
        logger.info("Gene count{}, entries having gene: {}", genResMap.size(), count);
    }

    private void updateGeneAltNames() {
        ResultSet resultSet = getGeneAltLabelsFromRdf(dataset.getRdfModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            Resource geneName = outModel.createResource(solution.getResource(ENCODED_BY).getURI());
            geneName.addProperty(SKOS.altLabel, solution.getLiteral(ALT_LABEL));
        }
    }

    private void addGeneRange(Resource locus, long start, long end) {
        Resource geneRange = outModel.createResource(getUri(RANGE_PREFIX));
        geneRange.addProperty(RDF.type, outModel.createResource(FALDO_TYPE_REGION));
        geneRange.addProperty(outModel.createProperty(FALDO_BEGIN), dataset.createPosition(start));
        geneRange.addProperty(outModel.createProperty(FALDO_END), dataset.createPosition(end));

        locus.addProperty(outModel.createProperty(GLY_GENE_RANGE), geneRange);
    }

    private void updateGeneCoordinates() {

        if (config.getGeneInfo() == null)
            return;
        File file = new File(getAbsFileName(config.getGeneInfo()));
        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
            String line; int gCount = 0; int count = 0;
            while ((line = fileReader.readLine()) != null) {
                line = line.replace("\"", "");
                String[] result = line.split("\t");

                //gene_name, ensg_id, chromosome, strand (R or F), loc_start, loc_end
                String genName = result[0];
                if (genName == null || genName.trim().isEmpty()) {
                    genName = result[1];
                }
                if (genResMap.containsKey(genName)) {
                    gCount++;
                    Resource locus = outModel.createResource(ENSG_PREFIX + result[1]);
                    locus.addProperty(RDF.type, outModel.createResource(GLY_TYPE_GENE_LOCUS));
                    locus.addProperty(outModel.createProperty(GLY_CHROMOSOME),
                            outModel.createLiteral(result[2]));
                    locus.addProperty(outModel.createProperty(GLY_REVERSE_STRAND),
                            outModel.createTypedLiteral(result[3].equals("R")));

                    addGeneRange(locus, Long.parseLong(result[4]), Long.parseLong(result[5]));

                    for (String url : genResMap.get(genName)) {
                        outModel.createResource(url).addProperty(outModel.createProperty(GLY_HAS_LOCUS), locus);
                        count++;
                    }
                }
            }
            logger.info("Gene having chromosome: {}, entries having chromosome: {}", gCount, count);
        } catch (IOException ex) {
            throw new GlyGenException(ex);
        }
    }

}
