package uk.ac.ebi.uniprot.glygen;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.input.FileFetchStatus;
import uk.ac.ebi.uniprot.glygen.input.InputFileFetcher;
import uk.ac.ebi.uniprot.glygen.input.VariationHumanFetcher;


public class GlyGenToolMain {
    private static final Logger logger = LoggerFactory.getLogger(GlyGenToolMain.class);
    
    public static void main(String[] args) {
        if(args.length ==0) {
            System.err.println("Missing out put directory");
            System.exit(1);
        }
        String dataDirectory =args[0];
        boolean force =false;
        if(args.length==2) {
            force = args[1].equals("force");
        }
  
        List<FileFetchStatus> result = fetch(dataDirectory, force);
        System.out.println("Following files are successfully fetched.");
        int count =0;
        for(FileFetchStatus status: result) {
            if(status.status().equals(FileFetchStatus.FetchStatus.SUCCEEDED)) {
                System.out.println(status.filename());
                count ++;
            }
        }
        System.out.println("Total successful fetches: " + count);
        System.out.println("Following files are successfully fetched but failed to uncompress.");
        count=0;
        for(FileFetchStatus status: result) {
            if(status.status().equals(FileFetchStatus.FetchStatus.FETCHED_NOT_UNCOMPRESSED)) {
                System.out.println(status.filename());
                count ++;
            }
        }
        System.out.println("Total successful fetches but not uncompressed: " + count);
        count =0;
        System.out.println("Following files failed to fetch.");
        for(FileFetchStatus status: result) {
            if(status.status().equals(FileFetchStatus.FetchStatus.FAILED)) {
                System.out.println(status.filename() + " url: " + status.url());
                count++;
            }
        }
        System.out.println("Total failed fetches: " + count);
        System.out.println("You can re-run the application with non force, to refetched non successful files. or manually fetch them");
    }
    
    public static  List<FileFetchStatus> fetch(String dataDirectory, boolean force) {
        logger.info("Fetch variation xml data");
        List<FileFetchStatus> results = new ArrayList<>();

        ExecutorService es = Executors.newFixedThreadPool(2);

        List<Callable<List<FileFetchStatus>>> fetchers = new ArrayList<>();
        fetchers.add(new VariationHumanFetcher(dataDirectory, force));
        fetchers.add(new InputFileFetcher(dataDirectory, force));
        try {
            List<Future<List<FileFetchStatus>>> futures = es.invokeAll(fetchers);
            for (Future<List<FileFetchStatus>> future : futures) {
                List<FileFetchStatus> is = future.get();
                results.addAll(is);

            }
            es.shutdown();
        } catch (Exception e) {
            logger.error("fetch failed", e);
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
