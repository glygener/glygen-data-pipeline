package uk.ac.ebi.uniprot.glygen.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;

public class FileUtils {

    public static void deleteIfExist(String filename) {
        File file = new File(filename);
        if(file.isFile())
            file.delete();
    }
    public static boolean decompressFile(String compressedFile, String newFile) {
        if(compressedFile.endsWith(".gz")){
            return decompressGZFile(compressedFile, newFile);
        }else if(compressedFile.endsWith(".xz")){
            return decompressXZFile(compressedFile, newFile);
        }else
            return false;
    }
    public static boolean decompressGZFile(String compressedFile, String newFile) {
        try (
            GZIPInputStream cis = new GZIPInputStream(new FileInputStream(compressedFile));
            FileOutputStream fos = new FileOutputStream(newFile);){
            byte[] buffer = new byte[16384];
            int len;
            while ((len = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
           
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }
    public static boolean decompressXZFile(String compressedFile, String newFile) {
        try (XZCompressorInputStream cis = new XZCompressorInputStream(new FileInputStream(compressedFile));
            FileOutputStream fos = new FileOutputStream(newFile);){
            byte[] buffer = new byte[16384];
            int len;
            while ((len = cis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
           
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }
    
    public static void compressGzip(String sourceFilename, String targetFilename) throws IOException {
        try (GZIPOutputStream gos = new GZIPOutputStream(
                                      new FileOutputStream(targetFilename));
             FileInputStream fis = new FileInputStream(sourceFilename)) {

            // copy file
            byte[] buffer = new byte[16384];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                gos.write(buffer, 0, len);
            }
        }
    }
}
