package uk.ac.ebi.uniprot.glygen.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.input.FileFetchStatus.FetchStatus;
import uk.ac.ebi.uniprot.glygen.util.FileDownload;
import uk.ac.ebi.uniprot.glygen.util.FileUtils;

public abstract class AbstractFileFetcher implements FileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected final String dataDirectory;
    protected final boolean force;

    public AbstractFileFetcher(String dataDirectory, boolean force) {
        this.dataDirectory = dataDirectory;
        this.force = force;
    }

    protected List<FileFetchStatus> fetchAndUncompress(Map<String, String> urlToFile,
            FileDownload fileDownload,
            String fileExt) {

        List<FileFetchStatus> results = new ArrayList<>();
        int fileExtSize = fileExt.length() + 1;
        for (Map.Entry<String, String> entry : urlToFile.entrySet()) {
            String url = entry.getKey();
            String filename = entry.getValue();
            String fullFileName = dataDirectory + File.separator + filename;
            String uncompressed = filename.substring(0, filename.length() - fileExtSize);
            String uncompressedFilename = dataDirectory + File.separator + uncompressed;
            if (!force) {
                File file = new File(uncompressedFilename);
                if (file.exists()) {
                    logger.info("File: " + uncompressed + " is existed, not loading.");
                    results.add(new FileFetchStatus(url, uncompressed, FetchStatus.SUCCEEDED));
                    continue;
                }
            }
            boolean downloaded = fileDownload.download(url, fullFileName);
            if (downloaded) {
                boolean isUnCompressed = FileUtils.decompressFile(fullFileName, uncompressedFilename);
                if (isUnCompressed) {
                    logger.info("File: " + uncompressed + " successfully fetched and uncompressed.");
                    results.add(new FileFetchStatus(url, uncompressed, FetchStatus.SUCCEEDED));
                    FileUtils.deleteIfExist(fullFileName);
                } else {
                    logger.warn("File: " + filename + " fetched but not uncompressed.");
                    results.add(new FileFetchStatus(url, filename, FetchStatus.FETCHED_NOT_UNCOMPRESSED));
                }
            } else {
                logger.error("File: " + fullFileName + " failed to be fetched and uncompressed.");
                results.add(new FileFetchStatus(url, filename,FetchStatus.FAILED));
            }
        }
        return results;
    }
 
    protected List<FileFetchStatus> fetch(Map<String, String> urlToFile, FileDownload fileDownload) {
        List<FileFetchStatus> results = new ArrayList<>();

        for (Map.Entry<String, String> entry : urlToFile.entrySet()) {
            String url = entry.getKey();
            String filename = entry.getValue();
            String fullFileName = dataDirectory + File.separator + filename;

            if (!force) {
                File file = new File(fullFileName);
                if (file.exists()) {
                    logger.info("File: " + filename + " is existed, not loading.");
                    results.add(new FileFetchStatus(url, filename, FetchStatus.SUCCEEDED));
                    continue;
                }
            }
            boolean downloaded =fileDownload.download(url, fullFileName);
            if (downloaded) {

                logger.info("File: " + filename + " successfully fetched.");
                results.add(new FileFetchStatus(url, filename, FetchStatus.SUCCEEDED));
            } else {
                logger.error("File: " + fullFileName + " failed to be fetched.");
                results.add(new FileFetchStatus(url, filename, FetchStatus.FAILED));
            }
        }
        return results;
    }
    protected FileFetchStatus extractDataToFile(String inputFilename, String name, String filename) {
        String inputFile = dataDirectory + File.separator +inputFilename;
                
                
        String outputFile =dataDirectory + File.separator + filename;
        if(!force) {
            File file = new File(outputFile);
            if (file.exists()) {
                logger.info("File: " + filename + " is existed, not loading.");
                return new FileFetchStatus("", filename, FetchStatus.SUCCEEDED);             
            }
        }
        
        try (Writer writer =new BufferedWriter(new FileWriter(outputFile))){
            List<String> lines =Files.readAllLines(Paths.get(inputFile));
            List<String> validLines = lines.stream().filter(line->line.contains(name)).toList();
            for(String line: validLines) {
                writer.write(line +"\n");
            }
            writer.flush();
            
        }catch(IOException e) {
            logger.info("File: " + filename + " failed to create.");
            return new FileFetchStatus("", filename, FetchStatus.FAILED); 
        }
        logger.info("File: " + filename + "is created");
        return new FileFetchStatus("", filename,FetchStatus.SUCCEEDED);
    }
}
