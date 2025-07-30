package uk.ac.ebi.uniprot.glygen.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ReactomeReactionFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    private static Map<String, String> NAME_TO_FILES = new HashMap<>();

    static {
        URL_TO_FILES.put("https://reactome.org/download/current/UniProt2ReactomeReactions.txt", "UniProt2ReactomeReactions.txt");

        NAME_TO_FILES.put("Homo sapiens", "homo_reactions.txt");
        NAME_TO_FILES.put("Mus musculus", "mus_reactions.txt");
        NAME_TO_FILES.put("Rattus norvegicus", "rattus_reactions.txt");
        NAME_TO_FILES.put("Drosophila melanogaster", "drosophila_reactions.txt");
        NAME_TO_FILES.put("Saccharomyces cerevisiae", "saccharomyces_reactions.txt");
        NAME_TO_FILES.put("Dictyostelium discoideum", "dictyostelium_reactions.txt");
        NAME_TO_FILES.put("Sus scrofa", "sus_reactions.txt");
        NAME_TO_FILES.put("Gallus gallus", "gallus_reactions.txt");
        NAME_TO_FILES.put("Bos taurus", "bos_reactions.txt");
        NAME_TO_FILES.put("Danio rerio", "danio_reactions.txt");
        NAME_TO_FILES.put("Cricetulus griseus", "cricetulus_reactions.txt");

    }

    public ReactomeReactionFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
    }


    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch reactome reaction data");
        List<FileFetchStatus> result = new ArrayList<>();

        result.addAll(super.fetch(URL_TO_FILES, new DefaultFileDownload()));
        for (Map.Entry<String, String> entry : NAME_TO_FILES.entrySet()) {
            String name = entry.getKey();
            String filename = entry.getValue();
            result.add(extractDataToFile("UniProt2ReactomeReactions.txt", name, filename));
        }

        return result;
    }


}
