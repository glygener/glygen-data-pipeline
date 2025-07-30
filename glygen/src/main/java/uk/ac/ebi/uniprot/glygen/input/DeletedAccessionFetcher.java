package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

public class DeletedAccessionFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    private static Map<String, String> OTHER_URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/sec_ac.txt" , "sec_ac.txt" );            
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/delac_sp.txt" , "delac_sp.txt" );            
                
        OTHER_URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/complete/docs/delac_tr.txt.gz", "delac_tr.txt.gz" );                    
      
    };
    
    public DeletedAccessionFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
     }
    
    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch deleted accession files");
        List<FileFetchStatus> result = new ArrayList<>();
        result.addAll(super.fetch(URL_TO_FILES, new DefaultFileDownload()));
        result.addAll(super.fetchAndUncompress(OTHER_URL_TO_FILES, new DefaultFileDownload(), "gz"));
        return result;
    }

}
