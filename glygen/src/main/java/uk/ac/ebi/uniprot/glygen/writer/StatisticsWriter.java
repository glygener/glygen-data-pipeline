package uk.ac.ebi.uniprot.glygen.writer;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.stats.Statistics;
import uk.ac.ebi.uniprot.glygen.stats.Statistics.StatKey;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to generate glygen dataset stats
 */
public class StatisticsWriter implements OutputWriter {
    private static final String COUNT = "count";
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String outputDir;
    public StatisticsWriter(String outputDir) {
        this.outputDir = outputDir;
    }
    private long getClassPredicateValueCount(Model model, String queryStr, StatKey key) {
        String query = String.format(queryStr, key.getPred(), key.getValue(), key.getClas());
        ResultSet resultSet = getResultSet(model, query);
        long count = 0;
        if (resultSet.hasNext()) {
            count = resultSet.next().getLiteral(COUNT).getLong();
        }
        return count;
    }

    private long getClassPredicateCount(Model model, String queryStr, StatKey key) {
        ResultSet resultSet = getResultSet(model, String.format(queryStr, key.getPred(), key.getClas()));
        long count = 0;
        if (resultSet.hasNext()) {
            count = resultSet.next().getLiteral(COUNT).getLong();
        }
        return count;
    }

    private long getCount(Model model, String queryStr) {
        ResultSet resultSet = getResultSet(model, queryStr);
        if (resultSet.hasNext()) {
            return resultSet.next().getLiteral(COUNT).getLong();
        }
        return 0;
    }

    private ResultSet getResultSet(Model model, String queryStr) {
        Query query = QueryFactory.create(queryStr);
        return QueryExecutionFactory.create(query, model).execSelect();
    }

    @Override
    public void writeOutput(GlygenConfig config, Model model) {
        logger.info("Start statistics");

        Statistics statistics = new Statistics();

        // class stats
        String cQuery = "SELECT ?c (count(?c) as ?count) " +
                "WHERE { ?s a ?c . filter (?c = <%s>) } group by ?c ";
        statistics.getClassMap().replaceAll((k, v) -> getCount(model, String.format(cQuery, k.getClas())));

        // predicate stats
        String pQuery = "SELECT ?p (count(?p) as ?count) WHERE { ?s ?p ?o . filter (?p = <%s>) } group by ?p ";
        statistics.getPredicateMap().replaceAll((k, v) -> getCount(model, String.format(pQuery, k.getPred())));

        // class-predicate stats
        String cpQuery = "SELECT (count(?s) as ?count) " +
                "WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?c . " +
                "       ?s <%s> ?o . filter (?c = <%s>) } ";
        statistics.getClassPredicateMap().replaceAll((c, v) -> getClassPredicateCount(model, cpQuery, c));

        // class-predicate-value stats
        String cpvQuery = "SELECT (count(?s) as ?count) " +
                "WHERE { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?c . " +
                "       ?s <%s> ?o . filter (?o = %s && ?c = <%s> ) } ";
        statistics.getClassPredicateValueMap().replaceAll((k, v) -> getClassPredicateValueCount(model, cpvQuery, k));
        String fullFilename = outputDir + File.separator + config.getStatisticsFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fullFilename))) {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(writer, statistics);
        } catch (IOException e) {
            logger.error("Error writing statistics ", e );
        }
        logger.info("End statistics");
    }
}