package uk.ac.ebi.uniprot.glygen.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class ProteomeRdfFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    private static Map<String, String> OTHER_URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000005640%29", "UP000005640_9606_uniprot_proteome.rdf.gz" );              
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000000589%29", "UP000000589_10090_uniprot_proteome.rdf.gz" );             
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000002494%29", "UP000002494_10116_uniprot_proteome.rdf.gz" );              
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000008095%29", "UP000008095_11116_uniprot_proteome.rdf.gz" );              
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000000354%29", "UP000000354_694009_uniprot_proteome.rdf.gz" );             
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000000803%29", "UP000000803_7227_uniprot_proteome.rdf.gz" );              
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000002311%29", "UP000002311_559292_uniprot_proteome.rdf.gz" );              
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000002195%29", "UP000002195_44689_uniprot_proteome.rdf.gz" );             
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000464024%29", "UP000464024_2697049_uniprot_proteome.rdf.gz" );         
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000008227%29" , "UP000008227_9823_uniprot_proteome.rdf.gz" );         
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000000539%29", "UP000000539_9031_uniprot_proteome.rdf.gz" );         
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000006548%29", "UP000006548_3702_uniprot_proteome.rdf.gz" ); 
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000009136%29", "UP000009136_9913_uniprot_proteome.rdf.gz" );         
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP000000437%29", "UP000000437_7955_uniprot_proteome.rdf.gz" ); 
        
        URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?compressed=true&format=rdf&query=%28proteome%3AUP001108280%29", "UP001108280_10029_uniprot_proteome.rdf.gz" ); 
        
        
       OTHER_URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/P27958.rdf", "UP000000518_63746_uniprot_proteome.rdf" );            
    };
   
    public ProteomeRdfFetcher(String dataDirectory, boolean force) {
       super(dataDirectory, force);
    }
    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch Proteome rdf files");
        List<FileFetchStatus> results = new ArrayList<>();
        results.addAll(super.fetchAndUncompress(URL_TO_FILES, new GenericFileDownload("application/rdf+xml"), "gz"));
        results.addAll(super.fetch(OTHER_URL_TO_FILES, new GenericFileDownload("application/rdf+xml")));
        return results;
    }

}
