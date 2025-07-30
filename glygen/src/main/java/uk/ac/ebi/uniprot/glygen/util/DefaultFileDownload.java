package uk.ac.ebi.uniprot.glygen.util;

import uk.ac.ebi.kraken.util.net.HttpWrapper;
import uk.ac.ebi.kraken.util.net.HttpWrapper.DownloadResult;

public class DefaultFileDownload implements FileDownload {
    private final HttpWrapper http = new HttpWrapper();

    @Override
    public boolean download(String url, String filename) {
        DownloadResult result = http.download3Retries(url, filename);
        return result.isFileDownloaded;

    }

}
