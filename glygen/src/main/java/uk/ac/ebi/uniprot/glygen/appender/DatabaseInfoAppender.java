package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.createModelFromRdfFile;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getDetailsFromDatabasesRdf;

/**
 * Class to add database info. NOTE: Add this as last appender so other appenders can add db uri's to dataSet first.
 */
public class DatabaseInfoAppender implements DataAppender {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Model dbModel;

    public DatabaseInfoAppender(GlygenConfig config) {
        dbModel = createModelFromRdfFile(config.getDatabases());
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        Model outModel = dataset.getOutModel();
        Set<String> set = dataset.getDbUriSet();
        Set<String> addedSet = new HashSet<>();
        ResultSet resultSet = getDetailsFromDatabasesRdf(dbModel);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String dbUri = solution.getResource(DATABASE).getURI();
            if (set.contains(dbUri)) {
                addedSet.add(dbUri);
                Resource database = outModel.createResource(dbUri);
                database.addProperty(RDF.type, outModel.createResource(UP_TYPE_DATABASE));
                database.addProperty(outModel.createProperty(UP_ABBREVIATION), solution.getLiteral(ABBREVIATION));
                database.addProperty(outModel.createProperty(UP_CATEGORY), solution.getLiteral(CATEGORY).getString());
                database.addProperty(outModel.createProperty(UP_URL_TEMPLATE), solution.getLiteral(URL_TEMPLATE));
            }
        }
        set.removeAll(addedSet);
        if (!set.isEmpty()) {
            logger.debug("Database details missing in databases.rdf : {}", set);
        }
    }

}