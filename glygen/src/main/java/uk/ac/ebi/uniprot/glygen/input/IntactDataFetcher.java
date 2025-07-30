package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

public class IntactDataFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
   
    static {
        URL_TO_FILES.put("https://ftp.ebi.ac.uk/pub/databases/intact/current/various/intact.fasta", "intact.fasta" );            
    } 
    public IntactDataFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
     }
     @Override
     public List<FileFetchStatus> fetch() {
         logger.info("Fetch Intact fasta file");
         List<FileFetchStatus> result =new ArrayList<>();
         
         result.addAll (super.fetch(URL_TO_FILES, new DefaultFileDownload()));
         result.add(extractDataToFile("intact.fasta", "INTACT", "intAct.txt"));
         
         
         return result;
     }
}
