package uk.ac.ebi.uniprot.glygen.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class GenomicCoordinateFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put( "https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=9606&format=json", "UP000005640_9606.coordinates.json" );            
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=10090&format=json", "UP000000589_10090.coordinates.json" );             
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=10116&format=json" , "UP000002494_10116.coordinates.json" );        
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=7227&format=json", "UP000000803_7227.coordinates.json" );      
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=559292&format=json", "UP000002311_559292.coordinates.json" );            
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=9823&format=json", "UP000008227_9823.coordinates.json" );         
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=9031&format=json", "UP000000539_9031.coordinates.json" );         
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=44689&format=json" , "UP000002195_44689.coordinates.json" );         
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=3702&format=json" , "UP000006548_3702.coordinates.json" ); 
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=9913&format=json" , "UP000009136_9913.coordinates.json" ); 
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=7955&format=json" , "UP000000437_7955.coordinates.json" ); 
        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/coordinates?offset=0&size=-1&taxid=10029&format=json" , "UP001108280_10029.coordinates.json" ); 
        
    };

    
    public GenomicCoordinateFetcher(String dataDirectory, boolean force) {
       super(dataDirectory, force);
    }
    
    
    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch genomic coordinate to json files");
        return super.fetch(URL_TO_FILES, new GenericFileDownload( "application/json"));
    }

}
