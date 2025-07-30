package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.createModelFromRdfFile;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getPrefLabelFromTissuesRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getTissuesFromRdf;

public class TissueAppender implements DataAppender {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Model tisModel;

    public TissueAppender(GlygenConfig config) {
        tisModel = createModelFromRdfFile(config.getTissues());
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        Model outModel = dataset.getOutModel();
        ResultSet resultSet = getTissuesFromRdf(dataset.getRdfModel());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String accession = solution.getResource(PROTEIN).getURI();
            String tissueId = solution.getResource(ISOLATED_FROM).getURI();
            String prefLabel = solution.contains(PREF_LABEL) ?
                    solution.getLiteral(PREF_LABEL).toString() : getPrefLabelFromTissuesRdf(tisModel, tissueId);

            if (prefLabel == null) {
                logger.error("PrefLabel not found for {}", tissueId);
                throw new GlyGenException("PrefLabel not found for " + tissueId);

            } else {
                Resource tissue = outModel.createResource(tissueId);
                tissue.addProperty(RDF.type, outModel.createResource(UP_TYPE_TISSUE));
                tissue.addProperty(RDFS.label, prefLabel);

                Resource protein = outModel.createResource(accession);
                protein.addProperty(outModel.createProperty(UP_ISOLATED_FROM), tissue);
            }
        }
    }

}