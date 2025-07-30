package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class VariationHumanFetcher extends AbstractFileFetcher implements Callable<List<FileFetchStatus> > {
    public VariationHumanFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
       
    }
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    static {       
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9606&format=xml", "UP000005640_9606.dbSnp.xml" );         
    };

    public List<FileFetchStatus> fetch() {        
        logger.info("Fetch human variation xml data");      
        List<FileFetchStatus> results = new ArrayList<>();
        results.addAll(super.fetch(URL_TO_FILES, new GenericFileDownload("application/xml")));
        return results;
    }
    @Override
    public List<FileFetchStatus> call() throws Exception {
        return fetch();
    }
}
