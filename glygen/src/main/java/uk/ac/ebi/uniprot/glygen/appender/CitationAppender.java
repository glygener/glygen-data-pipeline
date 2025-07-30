package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.addLiteralIfExists;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getCitationAuthorsFromRdf;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.getJournalCitationsFromRdf;

public class CitationAppender implements DataAppender {
    private Model rdfModel;
    private Model outModel;

    @Override
    public void appendData(GlygenDataset dataset) {
        rdfModel = dataset.getRdfModel();
        outModel = dataset.getOutModel();
        updateCitations();
    }

    private void updateCitations() {
        ResultSet resultSet = getJournalCitationsFromRdf(rdfModel);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();

            Resource citation = outModel.createResource(solution.getResource(CITATION).getURI());
            citation.addProperty(RDF.type, outModel.createResource(UP_TYPE_JOURNAL_CITATION));
            citation.addProperty(outModel.createProperty(UP_TITLE), solution.getLiteral(TITLE).getString());

            addAuthors(citation);

            citation.addProperty(SKOS.exactMatch, solution.getResource(EXACT_MATCH));

            addLiteralIfExists(citation, solution, DCTerms.identifier, IDENTIFIER);

            citation.addProperty(outModel.createProperty(UP_DATE),
                    outModel.createTypedLiteral(solution.getLiteral(DATE), XSDDatatype.XSDgYear));
            citation.addProperty(outModel.createProperty(UP_NAME), solution.getLiteral(NAME).getString());
            citation.addProperty(outModel.createProperty(UP_VOLUME), solution.getLiteral(VOLUME).getString());
            citation.addProperty(outModel.createProperty(UP_PAGES), solution.getLiteral(PAGES).getString());

            Resource protein = outModel.createResource(solution.getResource(PROTEIN).getURI());
            protein.addProperty(outModel.createProperty(UP_CITATION), citation);
        }
    }

    private void addAuthors(Resource citation){
        ResultSet resultSet = getCitationAuthorsFromRdf(rdfModel, citation.getURI());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            citation.addProperty(outModel.createProperty(UP_AUTHOR), solution.getLiteral(AUTHOR).getString());
        }
    }

}

