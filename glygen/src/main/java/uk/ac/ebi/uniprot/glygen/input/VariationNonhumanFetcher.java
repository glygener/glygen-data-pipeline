package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class VariationNonhumanFetcher extends AbstractFileFetcher implements Callable<List<FileFetchStatus> > {
    public VariationNonhumanFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
       
    }
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=3702&format=xml", "UP000006548_3702.dbSnp.xml" );             
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9031&format=xml", "UP000000539_9031.dbSnp.xml" );              
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=2697049&format=xml", "UP000464024_2697049.dbSnp.xml" );              
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=694009&format=xml", "UP000000354_694009.dbSnp.xml" );             
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=559292&format=xml", "UP000002311_559292.dbSnp.xml" );              
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=7227&format=xml", "UP000000803_7227.dbSnp.xml" );              
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9823&format=xml", "UP000008227_9823.dbSnp.xml" );             
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=44689&format=xml", "UP000002195_44689.dbSnp.xml" );              
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10116&format=xml", "UP000002494_10116.dbSnp.xml" );         
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10090&format=xml", "UP000000589_10090.dbSnp.xml" );   
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9913&format=xml", "UP000009136_9913.dbSnp.xml" );   
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10029&format=xml", "UP001108280_10029.dbSnp.xml" );        
    };

    public List<FileFetchStatus> fetch() {        
        logger.info("Fetch non human variation xml data");      
        List<FileFetchStatus> results = new ArrayList<>();
        results.addAll(super.fetch(URL_TO_FILES, new GenericFileDownload("application/xml")));
        return results;
    }
    @Override
    public List<FileFetchStatus> call() throws Exception {
        return fetch();
    }
}
