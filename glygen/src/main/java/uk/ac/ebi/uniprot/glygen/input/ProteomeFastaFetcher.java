package uk.ac.ebi.uniprot.glygen.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;
import uk.ac.ebi.uniprot.glygen.util.GenericFileDownload;

public class ProteomeFastaFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    private static Map<String, String> OTHER_URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000005640/UP000005640_9606_additional.fasta.gz", "UP000005640_9606_additional.fasta.gz" );            
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000005640/UP000005640_9606.fasta.gz", "UP000005640_9606.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000589/UP000000589_10090_additional.fasta.gz", "UP000000589_10090_additional.fasta.gz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000589/UP000000589_10090.fasta.gz", "UP000000589_10090.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002494/UP000002494_10116_additional.fasta.gz", "UP000002494_10116_additional.fasta.gz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002494/UP000002494_10116.fasta.gz", "UP000002494_10116.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000803/UP000000803_7227_additional.fasta.gz", "UP000000803_7227_additional.fasta.gz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000803/UP000000803_7227.fasta.gz", "UP000000803_7227.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002311/UP000002311_559292_additional.fasta.gz", "UP000002311_559292_additional.fasta.gz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002311/UP000002311_559292.fasta.gz", "UP000002311_559292.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002195/UP000002195_44689_additional.fasta.gz", "UP000002195_44689_additional.fasta.gz" );  
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000002195/UP000002195_44689.fasta.gz", "UP000002195_44689.fasta.gz" );     
        
   //     URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Viruses/UP000000518/UP000000518_63746.fasta.gz" , "UP000000518_63746.fasta.gz" );      
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Viruses/UP000000354/UP000000354_694009.fasta.gz", "UP000000354_694009.fasta.gz" );  
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Viruses/UP000464024/UP000464024_2697049.fasta.gz", "UP000464024_2697049.fasta.gz" );  
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000008227/UP000008227_9823_additional.fasta.gz", "UP000008227_9823_additional.fasta.gz" );  
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000008227/UP000008227_9823.fasta.gz", "UP000008227_9823.fasta.gz" );  
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000539/UP000000539_9031_additional.fasta.gz", "UP000000539_9031_additional.fasta.gz" );  
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000539/UP000000539_9031.fasta.gz", "UP000000539_9031.fasta.gz" );  
        
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000006548/UP000006548_3702_additional.fasta.gz", "UP000006548_3702_additional.fasta.gz" );  
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000006548/UP000006548_3702.fasta.gz", "UP000006548_3702.fasta.gz" );  
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000009136/UP000009136_9913.fasta.gz", "UP000009136_9913.fasta.gz" ); 
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000009136/UP000009136_9913_additional.fasta.gz", "UP000009136_9913_additional.fasta.gz" ); 
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000437/UP000000437_7955.fasta.gz", "UP000000437_7955.fasta.gz" ); 
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP000000437/UP000000437_7955_additional.fasta.gz", "UP000000437_7955_additional.fasta.gz" ); 
        
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP001108280/UP001108280_10029.fasta.gz", "UP001108280_10029.fasta.gz" ); 
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/knowledgebase/reference_proteomes/Eukaryota/UP001108280/UP001108280_10029_additional.fasta.gz", "UP001108280_10029_additional.fasta.gz" ); 
        
        
        OTHER_URL_TO_FILES.put( "https://rest.uniprot.org/uniprotkb/stream?format=fasta&query=accession%3AP26662", "UP000008095_11116.fasta" );  
        OTHER_URL_TO_FILES.put("https://rest.uniprot.org/uniprotkb/stream?format=fasta&query=%28%28xref%3Aproteomes-UP000000518%29+NOT+%28accession%3AP0C045%29%29", "UP000000518_63746.fasta" );  
        
    };
    
 /**
    ## download as fasta(canonical)
    curl "https://rest.uniprot.org/uniprotkb/stream?format=fasta&query=accession%3AP26662" > UP000008095_11116.fasta

***/    
    public ProteomeFastaFetcher(String dataDirectory, boolean force) {
        super(dataDirectory, force);
    }
    @Override
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch fasta files");
        List<FileFetchStatus>  result= super.fetchAndUncompress(URL_TO_FILES, new DefaultFileDownload(), "xz");
        
        List<FileFetchStatus>  result2= super.fetch(OTHER_URL_TO_FILES,new GenericFileDownload("text/plain"));
        
        result.addAll(result2);
        return result;
    }

}
