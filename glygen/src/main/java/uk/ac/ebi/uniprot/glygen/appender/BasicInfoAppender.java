package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class BasicInfoAppender implements DataAppender {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void appendData(GlygenDataset dataset) {
        validateModel(dataset.getRdfModel());

        Model outModel = dataset.getOutModel();
        ResultSet resultSet = getAllProteinAccessionsFromRdf(dataset.getRdfModel());
        Set<String> accSet = new HashSet<>();

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(RDF.type, outModel.createResource(UP_TYPE_PROTEIN));
            protein.addProperty(outModel.createProperty(UP_VERSION),
                    outModel.createTypedLiteral(solution.getLiteral(VERSION).getInt()));
            protein.addProperty(outModel.createProperty(UP_CREATED),
                    outModel.createTypedLiteral(solution.getLiteral(CREATED).getValue(), XSDDatatype.XSDdate));
            protein.addProperty(outModel.createProperty(UP_MODIFIED),
                    outModel.createTypedLiteral(solution.getLiteral(MODIFIED).getValue(), XSDDatatype.XSDdate));
            protein.addProperty(outModel.createProperty(UP_MNEMONIC), solution.getLiteral(MNEMONIC));
            protein.addProperty(outModel.createProperty(UP_REVIEWED),
                    outModel.createTypedLiteral(solution.getLiteral(REVIEWED).getBoolean()));
            protein.addProperty(outModel.createProperty(UP_EXISTENCE), solution.getResource(EXISTENCE));
            protein.addProperty(outModel.createProperty(UP_ORGANISM), solution.getResource(ORGANISM));
            accSet.add(protein.getURI());
        }
        dataset.createAccessionMap(accSet);
    }

    private void validateModel(Model rdfModel) {
        String queryStr = "select (count(*) as ?count) {?p <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
                "<http://purl.uniprot.org/core/Protein> . }";
        if (getCountFromOutput(rdfModel, queryStr) == 0) {
            logger.error("Empty local TDB OR Invalid DB_DIR");
            throw new GlyGenException("Empty local TDB OR Invalid DB_DIR");
        }
    }

}
