package uk.ac.ebi.uniprot.glygen.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.Statement;

import uk.ac.ebi.uniprot.glygen.core.GlyGenException;

@Deprecated
public class GetEnsemblGeneInfo {
    public static void main (String args[]) throws Exception{
    }
    @Deprecated
    private static void flushToFile (Connection conn, String query, int taxId) throws Exception{
        Statement stmt = conn.createStatement();
        java.sql.ResultSet rs = stmt.executeQuery(query);
        char tab = '\t';
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("in/" + taxId + "_gene.tsv"))) {

            while (rs.next()) {
                writer.write(rs.getString(1) + tab + //gene_name
                        rs.getString(2) + tab +   //ensg_id
                        rs.getString(3) + tab +   //chromosome
                        rs.getString(4) + tab +   //seq_region_strand
                        rs.getLong(5) + tab +     //seq_region_start
                        rs.getLong(6));           //seq_region_end
                writer.newLine();
            }
            writer.flush();

        } catch (Exception ex) {
            throw new GlyGenException("Error creating obsolete accession dataset " + ex.toString());
        }
    }
}
