package uk.ac.ebi.uniprot.glygen.input;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.util.DefaultFileDownload;

public class SupportDataRdfFetcher extends AbstractFileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static Map<String, String> URL_TO_FILES = new HashMap<>();
    
    static {
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/databases.rdf.xz", "databases.rdf.xz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/diseases.rdf.xz", "diseases.rdf.xz" );       
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/enzyme.rdf.xz", "enzyme.rdf.xz" );       
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/keywords.rdf.xz", "keywords.rdf.xz" );       
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/locations.rdf.xz" , "locations.rdf.xz" );      
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/tissues.rdf.xz", "tissues.rdf.xz" );
        URL_TO_FILES.put("https://ftp.uniprot.org/pub/databases/uniprot/current_release/rdf/go.owl.xz", "go.owl.xz" );

    };
    
    
    public SupportDataRdfFetcher(String dataDirectory, boolean force) {
       super(dataDirectory, force);
    }
    public List<FileFetchStatus> fetch() {        
        logger.info("Fetch supported data rdf files");
        return super.fetchAndUncompress(URL_TO_FILES,new DefaultFileDownload(), "xz");
    }
    
}
