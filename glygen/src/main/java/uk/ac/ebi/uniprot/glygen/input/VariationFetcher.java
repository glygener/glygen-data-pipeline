package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VariationFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
//    private static Map<String, String> URL_TO_FILES = new HashMap<>();
//    
//    static {
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=3702&format=xml", "UP000006548_3702.dbSnp.xml" );             
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9031&format=xml", "UP000000539_9031.dbSnp.xml" );              
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=2697049&format=xml", "UP000464024_2697049.dbSnp.xml" );              
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=694009&format=xml", "UP000000354_694009.dbSnp.xml" );             
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=559292&format=xml", "UP000002311_559292.dbSnp.xml" );              
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=7227&format=xml", "UP000000803_7227.dbSnp.xml" );              
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9823&format=xml", "UP000008227_9823.dbSnp.xml" );             
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=44689&format=xml", "UP000002195_44689.dbSnp.xml" );              
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10116&format=xml", "UP000002494_10116.dbSnp.xml" );         
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=10090&format=xml", "UP000000589_10090.dbSnp.xml" );        
//        URL_TO_FILES.put("https://www.ebi.ac.uk/proteins/api/variation?offset=0&size=-1&taxid=9606&format=xml", "UP000005640_9606.dbSnp.xml" );  
//        
//    };

    public VariationFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
    }

    public List<FileFetchStatus> fetch() {
        logger.info("Fetch variation xml data");
        List<FileFetchStatus> results = new ArrayList<>();

        ExecutorService es = Executors.newFixedThreadPool(2);

        List<Callable<List<FileFetchStatus>>> fetchers = new ArrayList<>();
        fetchers.add(new VariationHumanFetcher(dataDirectory, force));
        fetchers.add(new VariationNonhumanFetcher(dataDirectory, force));
        try {
            List<Future<List<FileFetchStatus>>> futures = es.invokeAll(fetchers);
            for (Future<List<FileFetchStatus>> future : futures) {
                List<FileFetchStatus> is = future.get();
                results.addAll(is);

            }
            es.shutdown();
        } catch (Exception e) {
            logger.error("Variation xml fetch failed", e);
        }
        try {
            while (!es.isTerminated()) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {

        }
        return results;
    }

}
