package uk.ac.ebi.uniprot.glygen.input;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.uniprot.glygen.input.FileFetchStatus.FetchStatus;

@Deprecated
public class EnsemblGeneFetcher implements FileFetcher {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String dataDirectory;
    private final boolean force;
    private final String dbStr ="";

    public EnsemblGeneFetcher(String dataDirectory, boolean force) {
        this.dataDirectory = dataDirectory;
        this.force = force;
    }
    @Override
    @Deprecated
    public List<FileFetchStatus> fetch() {
        logger.info("Fetch Ensembl Gene.");
        List<FileFetchStatus> result =new ArrayList<>();
        return result;
    }
    FileFetchStatus fetch(Connection connection, Integer taxId, String sql) {
        String filename = taxId + "_gene.tsv";
        String fullFilename = dataDirectory + File.separator + filename;
        if (!force) {
            File file = new File(fullFilename);
            if (file.exists()) {
                logger.info("File: " + filename + " is existed, not loading.");
                return new FileFetchStatus("", filename, FetchStatus.SUCCEEDED);
            }
        }

        char tab = '\t';
        try ( PreparedStatement stmt = connection.prepareStatement(sql);
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullFilename))){
            stmt.setInt(1, taxId);
            ResultSet rs =stmt.executeQuery();
            while(rs.next()) {
                writer.write(rs.getString(1) + tab + //gene_name
                        rs.getString(2) + tab +   //ensg_id
                        rs.getString(3) + tab +   //chromosome
                        rs.getString(4) + tab +   //seq_region_strand
                        rs.getLong(5) + tab +     //seq_region_start
                        rs.getLong(6));           //seq_region_end
                writer.newLine();
            }
            writer.flush();
            rs.close();

        }catch(Exception e) {
            logger.warn("File: " + filename + " failed to fetch.");
            return new FileFetchStatus("", filename, FetchStatus.FAILED);
        }
        logger.warn("File: " + filename + " fetched successfully.");
       return new FileFetchStatus("", filename, FetchStatus.SUCCEEDED);

    }

}
