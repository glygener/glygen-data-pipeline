package uk.ac.ebi.uniprot.glygen.core;

import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {

    private ConfigReader() {
    }

    public static List<GlygenConfig> getGlygenConfigList(InputStream stream, String baseDir) throws IOException {
        Map<String, GlygenConfig> map = new HashMap<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isEmpty() && !line.startsWith("#")) {
                String[] strs = line.split("=");
                String[] names = strs[0].split("\\.");
                if (!map.containsKey(names[0])) {
                    map.put(names[0], new GlygenConfig());
                }
                if(baseDir ==null || baseDir.isBlank())
                    setProperty(map.get(names[0]), names[1], strs[1]);
                else {
                    if(strs[1].startsWith("in/")) {
                        String value = baseDir +"/" + strs[1];
                        setProperty(map.get(names[0]), names[1], value);
                    }else {
                        setProperty(map.get(names[0]), names[1], strs[1]);
                    }
                }
            }
        }
        reader.close();

        // "0" is common properties, goes into all other configs
        map.remove("0");

        return new ArrayList<>(map.values());
    }

    private static void setProperty(GlygenConfig config, String key, String value) {
        switch(key.trim()) {
            case "tissues": GlygenConfig.setTissues(value); break;
            case "keywords": GlygenConfig.setKeywords(value); break;
            case "geneOntologies": GlygenConfig.setGeneOntologies(value); break;
            case "enzyme": GlygenConfig.setEnzyme(value); break;
            case "databases": GlygenConfig.setDatabases(value); break;
            case "diseases": GlygenConfig.setDiseases(value); break;
            case "locations": GlygenConfig.setLocations(value); break;
            case "rhea": GlygenConfig.setRhea(value); break;
            case "intact": GlygenConfig.setIntAct(value); break;
            case "canonicalMapOut": GlygenConfig.setCanonicalMapOut(value); break;
            case "dbXrefMapOut": GlygenConfig.setDbXrefMapOut(value); break;
            case "rdfOutput": GlygenConfig.setRdfOutput(value); break;
            case "statisticsFile": GlygenConfig.setStatisticsFile(value); break;
            case "dbSnpOut": GlygenConfig.setDbSnpOut(value); break;

            case "name": config.setName(value); break;
            case "dbDir": config.setDbDir(value); break;
            case "canonical": config.setCanonical(value); break;
            case "isoform": config.setIsoform(value); break;
            case "geneCoordinate": config.setGeneCoordinate(value); break;
            case "ensemblCds": config.setEnsemblCds(value); break;
            case "ensemblPeptide": config.setEnsemblPeptide(value); break;
            case "transcriptDb": config.setTranscriptDb(value); break;
            case "taxId": config.setTaxId(value); break;
            case "speciesTaxId": config.setSpeciesTaxId(value); break;
            case "upid": config.setUpid(value); break;
            case "dbSnp": config.setDbSnp(value); break;
            case "reactomeReactions": config.setReactomeReactions(value); break;
            case "geneInfo": config.setGeneInfo(value); break;
            case "source": config.setSource(value); break;
            default:
                throw new GlyGenException("Invalid property for Glygen config, " + key);
        }
    }

}
