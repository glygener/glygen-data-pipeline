package uk.ac.ebi.uniprot.glygen.writer;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;
import uk.ac.ebi.uniprot.glygen.model.GlygenConfig;
import uk.ac.ebi.uniprot.glygen.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NTriplesWriter implements OutputWriter {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String outputDir;
    public NTriplesWriter(String outputDir) {
        this.outputDir = outputDir;
    }
    @Override
    public void writeOutput(GlygenConfig config, Model model)  {
        logger.info("Start NTriples output ");
        if (model.isEmpty()) {
            throw new GlyGenException("Out model is empty or invalid. No triples to write into NT output file.");
        }
        String fullFilename = outputDir + File.separator + config.getRdfOutput();
      
        try (FileOutputStream out = new FileOutputStream(fullFilename)) {
            RDFDataMgr.write(out, model, Lang.NTRIPLES);
            String targetFilename = fullFilename +".gz";
            FileUtils.compressGzip(fullFilename, targetFilename);
 
        } catch (IOException io) {
            logger.error("Exception writing NTriples output ", io);
            throw new GlyGenException(io);
        }
        try  {       
            String targetFilename = fullFilename +".gz";
            FileUtils.compressGzip(fullFilename, targetFilename);
            File file = new File(fullFilename);
            file.delete();
        } catch (IOException io) {
            logger.error("Compress file failed ", io);
            
        }
        
        logger.info("End NTriples output ");
    }
}