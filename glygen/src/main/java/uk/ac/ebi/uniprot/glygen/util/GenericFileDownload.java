package uk.ac.ebi.uniprot.glygen.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class GenericFileDownload implements FileDownload {
    public static final int BUFFER_SIZE = 16384;
    private final String acceptType;

    public GenericFileDownload(String acceptType) {
        this.acceptType = acceptType;
    }

    @Override
    public boolean download(String url, String filename) {
        int code;
        boolean isfileDownload;
        try (FileOutputStream out = new FileOutputStream(filename)) {
            code = restGet(url, acceptType, out);
            isfileDownload = (code == 200) || (code == 203);
        } catch (IOException e) {
            return false;
        }
        return isfileDownload;
    }

    private int restGet(String urlStr, String acceptType, FileOutputStream outStream) {
        HttpURLConnection httpConnection = null;
        try {
            URL url = new URL(urlStr);
            URLConnection connection = url.openConnection();
            httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestProperty("Content-Type", acceptType);
            int responseCode = httpConnection.getResponseCode();
            if ((responseCode != 200) && (responseCode != 203)) {
                return responseCode;
            }
            InputStream response = connection.getInputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = response.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            return responseCode;
        } catch (Exception e) {
            return 400;
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }
}
