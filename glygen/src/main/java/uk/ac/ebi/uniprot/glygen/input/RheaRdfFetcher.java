package uk.ac.ebi.uniprot.glygen.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

public class RheaRdfFetcher extends AbstractFileFetcher {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://ftp.expasy.org/databases/rhea/rdf/rhea.rdf.gz", "rhea.rdf.gz" );      
        
    };
   
    public RheaRdfFetcher(String dataDirectory, boolean force) {
       super(dataDirectory, force);
    }
    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch Rhea rdf files");
        return super.fetchAndUncompress(URL_TO_FILES, new DefaultFileDownload(), "gz");
    }

}
