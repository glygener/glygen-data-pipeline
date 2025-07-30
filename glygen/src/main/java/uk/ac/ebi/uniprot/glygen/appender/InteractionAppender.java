package uk.ac.ebi.uniprot.glygen.appender;

import uk.ac.ebi.kraken.interfaces.uniprot.Organism;
import uk.ac.ebi.kraken.interfaces.uniprot.OrganismHost;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.uniprot.dataservice.client.Client;
import uk.ac.ebi.uniprot.dataservice.client.QueryResult;
import uk.ac.ebi.uniprot.dataservice.client.ServiceFactory;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtQueryBuilder;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtService;
import uk.ac.ebi.uniprot.dataservice.query.Query;
import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.model.GlygenDataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static uk.ac.ebi.uniprot.glygen.util.GlygenUtility.*;
import static uk.ac.ebi.uniprot.glygen.util.RdfUtility.*;

public class InteractionAppender implements DataAppender {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Model rdfModel;
    private Model outModel;
    private GlygenConfig config;

    public InteractionAppender(GlygenConfig config) {
        this.config=config;
    }

    @Override
    public void appendData(GlygenDataset dataset) {
        rdfModel = dataset.getRdfModel();
        outModel = dataset.getOutModel();
        Set<String> partSet = new HashSet<>();

        ResultSet resultSet = getInteractionFromRdf(rdfModel);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String accession = solution.getResource(PROTEIN).getURI();
            String interId = solution.getResource(INTERACTION).getURI();

            Resource interaction = outModel.createResource(interId);
            interaction.addProperty(RDF.type, outModel.createResource(UP_TYPE_INTERACTION));

            String[] arr = interId.substring(interId.lastIndexOf(CHAR_FORWARD_SLASH) + 1).split(DASH);
            boolean selfInt = arr[0].equals(arr[1]);
            interaction.addProperty(RDF.type, outModel.createResource(selfInt ?
                    UP_TYPE_SELF_INTERACTION : UP_TYPE_NON_SELF_INTERACTION));

            interaction.addProperty(outModel.createProperty(UP_XENO), solution.getLiteral(XENO));
            interaction.addProperty(outModel.createProperty(UP_EXPERIMENTS), solution.getLiteral(EXPERIMENTS));

            addParticipantResource(interaction, arr[0], partSet);

            if (!selfInt) {
                addParticipantResource(interaction, arr[1], partSet);
            }

            Resource protein = outModel.createResource(accession);
            protein.addProperty(outModel.createProperty(UP_INTERACTION), interaction);
        }
        updateParticipant(partSet);
        updateOrganismHost(dataset.getAccessionSet().iterator().next());
    }

    private void addParticipantResource(Resource interaction, String resId, Set<String> partSet) {
        partSet.add(resId);
        Resource participant = outModel.createResource(PARTICIPANT_PREFIX + resId);
        interaction.addProperty(outModel.createProperty(UP_PARTICIPANT), participant);
    }

    private void updateParticipant(Set<String> partSet) {
        logger.debug("Participant set count: {}", partSet.size());
        Set<String> missingTaxPartSet = new HashSet<>();
        ResultSet resultSet = getAllParticipantInfoFromRdf(rdfModel);
        Set<String> availPart = new HashSet<>();
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String partId = solution.getResource(PARTICIPANT).getURI();
            String key = partId.substring(partId.lastIndexOf(DASH)+1);

            if (partSet.contains(key)) {
                Resource participant = outModel.createResource(partId);
                participant.addProperty(RDF.type, outModel.createResource(UP_TYPE_PARTICIPANT));
                String intActVal = GlygenConfig.getSameAsAttributeForIntActId(key);
                if (intActVal.startsWith(solution.getResource(SAME_AS).getURI())) {
                    participant.addProperty(OWL.sameAs, outModel.createResource(intActVal));
                } else {
                    participant.addProperty(OWL.sameAs,
                            outModel.createResource(solution.getResource(SAME_AS).getURI()));
                }
                addLiteralIfExists(participant, solution, RDFS.label, LABEL);

                if (solution.contains(MNEMONIC)) {
                    participant.addProperty(outModel.createProperty(UP_MNEMONIC), solution.getLiteral(MNEMONIC));
                    participant.addProperty(outModel.createProperty(UP_ORGANISM), solution.getResource(ORGANISM));
                } else {
                    missingTaxPartSet.add(partId);
                }
                availPart.add(key);
            } else {
                logger.warn("{}: Not adding participant {}", key, partId);
            }
        }
        partSet.removeAll(availPart);
        addMissedParticipantNode(missingTaxPartSet, partSet);
        updateParticipantFromUniProtService(updateParticipantTaxForIsoforms(missingTaxPartSet));
        logger.debug("Missing taxon participant count: {}", missingTaxPartSet.size());
    }

    private void addMissedParticipantNode(Set<String> missingTaxPartSet, Set<String> partSet) {
        for (String partId : partSet) {
            Resource participant = outModel.createResource(PARTICIPANT_PREFIX + partId);
            participant.addProperty(RDF.type, outModel.createResource(UP_TYPE_PARTICIPANT));
            participant.addProperty(OWL.sameAs,
                    outModel.createResource(GlygenConfig.getSameAsAttributeForIntActId(partId)));
            missingTaxPartSet.add(PARTICIPANT_PREFIX + partId);
        }
    }

    private Map<String, List<Resource>> updateParticipantTaxForIsoforms(Set<String> missingTaxPartSet) {
        Map<String, List<Resource>> missingAccMap = new HashMap<>();

        for (String partId : missingTaxPartSet) {
            Resource participant = outModel.getResource(partId);
            Resource sameAs = participant.getPropertyResourceValue(OWL.sameAs);
            String acc = sameAs.getURI();
            if (sameAs.getURI().startsWith(ISOFORM_PREFIX)) {
                acc = acc.substring(acc.lastIndexOf(CHAR_FORWARD_SLASH) + 1, acc.indexOf(CHAR_DASH));

                ResultSet resultSet = getMnemonicAndTaxonFromRdf(rdfModel, PROTEIN_PREFIX + acc);
                if (resultSet.hasNext()) {
                    QuerySolution solution = resultSet.nextSolution();
                    if (solution.contains(MNEMONIC)) {
                        participant.addProperty(outModel.createProperty(UP_MNEMONIC), solution.getLiteral(MNEMONIC));
                        participant.addProperty(outModel.createProperty(UP_ORGANISM), solution.getResource(ORGANISM));
                        partId = null;
                    }
                }
            }
            if (partId != null) {
                if (sameAs.getURI().startsWith(PROTEIN_PREFIX)) {
                    acc = acc.substring(acc.lastIndexOf(CHAR_FORWARD_SLASH) + 1);
                }
                if (acc.indexOf(CHAR_DASH) > -1) {
                    acc = acc.substring(0, acc.indexOf(CHAR_DASH));
                }
                // ignore annotations for now - Apr 2020
                if (!acc.contains(ANNOTATION)) {
                    if (!missingAccMap.containsKey(acc)) {
                        missingAccMap.put(acc, new ArrayList<>());
                    }
                    missingAccMap.get(acc).add(participant);
                }
            }
        }
        return missingAccMap;
    }

    private void updateParticipantFromUniProtService(Map<String, List<Resource>> missingAccMap) {
        logger.debug("Number of accessions to get from UniProtService {}", missingAccMap.size());

        if (missingAccMap.isEmpty()) {
            return;
        }
        ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
        UniProtService uniProtService = serviceFactoryInstance.getUniProtQueryService();
        String[] accIds = missingAccMap.keySet().toArray(new String[0]);
        Set<String> obsSecAccIdSet = new HashSet<>();
        try {
            // start the service
            uniProtService.start();

            int cnt = 0;
            while (cnt < accIds.length) {
                int i = 0;
                Set<String> querySet = new HashSet<>();
                while (i++ < 200 && cnt < accIds.length) {
                    querySet.add(accIds[cnt++]);
                }
                Query query = UniProtQueryBuilder.accessions(querySet);
                QueryResult<UniProtEntry> searchResult = uniProtService.getEntries(query);

                Set<String> retrievedIds = new HashSet<>();
                while (searchResult.hasNext()) {
                    UniProtEntry entry = searchResult.next();
                    String accId = entry.getPrimaryUniProtAccession().getValue();
                    Resource organism =
                            outModel.createResource(TAXONOMY_PREFIX + entry.getNcbiTaxonomyIds().get(0).getValue());

                    updateOrganismInfo(organism, entry.getOrganism());

                    for (Resource participant : missingAccMap.get(accId)) {
                        participant.addProperty(outModel.createProperty(UP_MNEMONIC), entry.getUniProtId().getValue());
                        participant.addProperty(outModel.createProperty(UP_ORGANISM), organism);
                    }
                    retrievedIds.add(accId);
                }
                querySet.removeAll(retrievedIds);
                if (!querySet.isEmpty()) {
                    obsSecAccIdSet.addAll(querySet);
                }
            }
        } catch (ServiceException se) {
            logger.debug(Arrays.toString(se.getStackTrace()));
            throw new GlyGenException(se);
        } finally {
            // always remember to stop the service
            uniProtService.stop();
            logger.debug("UniProtService stopped.");
        }

        if (!obsSecAccIdSet.isEmpty()) {
            removeInteractionsHavingAccessionsWithMissingTaxInfo(obsSecAccIdSet, missingAccMap);
        }
    }

    private void updateOrganismInfo(Resource taxon, Organism entryOrg) {
        if (!taxon.hasProperty(RDF.type)) {
            taxon.addProperty(RDF.type, outModel.createResource(UP_TYPE_TAXON));
            taxon.addProperty(outModel.createProperty(UP_SCIENTIFIC_NAME),
                    entryOrg.getScientificName().getValue());
            taxon.addProperty(outModel.createProperty(UP_COMMON_NAME),
                    entryOrg.getCommonName().getValue());
        }
    }

    private void removeInteractionsHavingAccessionsWithMissingTaxInfo(Set<String> obsSecAccIdSet,
            Map<String, List<Resource>> missingAccMap) {
        logger.debug("Removing interactions having accessions: {}", obsSecAccIdSet);

        for (String acc : obsSecAccIdSet) {
            for (Resource participant : missingAccMap.get(acc)) {
                ResultSet resultSet = getInfoOfParticipantFromRdf(rdfModel, participant.getURI());
                participant.removeProperties();

                while (resultSet.hasNext()) {
                    QuerySolution solution = resultSet.nextSolution();

                    Resource protein = outModel.getResource(solution.getResource(PROTEIN).getURI());
                    Resource interaction = outModel.getResource(solution.getResource(INTERACTION).getURI());
                    interaction.removeProperties();

                    outModel.remove(protein, outModel.createProperty(UP_INTERACTION), interaction);
                }
            }
        }
    }

    private void updateOrganismHost(String accession) {
        ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
        UniProtService uniProtService = serviceFactoryInstance.getUniProtQueryService();
        try {
            // start the service
            uniProtService.start();

            // check if organismHost exists
            UniProtEntry entry = uniProtService.getEntry(accession.substring(
                    accession.lastIndexOf(CHAR_FORWARD_SLASH) + 1));

            Resource organism = outModel.createResource(TAXONOMY_PREFIX +
                    entry.getNcbiTaxonomyIds().get(0).getValue());
            updateOrganismInfo(organism, entry.getOrganism());

            List<OrganismHost> orgHosts = entry.getOrganismHosts();
            if (orgHosts != null && !orgHosts.isEmpty()) {
                for (OrganismHost orgHost : orgHosts) {
                    Resource host = outModel.createResource(TAXONOMY_PREFIX + orgHost.getNcbiTaxonomyId().getValue());
                    organism.addProperty(outModel.createProperty(UP_HOST), host);

                    updateOrganismInfo(host, orgHost.getOrganism());
                }
            }
        } catch (ServiceException se) {
            logger.debug(Arrays.toString(se.getStackTrace()));
            throw new GlyGenException(se);
        } finally {
            // always remember to stop the service
            uniProtService.stop();
            //logger.debug("UniProtService stopped.");
        }
    }
}

