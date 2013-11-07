/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.net;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FxFileDownload extends Task<File> {

    private final static Log log = LogFactory.getLog(FxFileDownload.class.getName());
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private HttpClient httpClient;
    private String remoteUrl;
    private File localFile;
    private int bufferSize;

    public FxFileDownload(String remoteUrl, File localFile) {
        this(new DefaultHttpClient(), remoteUrl, localFile, DEFAULT_BUFFER_SIZE);
    }

    public FxFileDownload(HttpClient httpClient, String remoteUrl, File localFile, int bufferSize) {
        this.httpClient = httpClient;
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = bufferSize;

        stateProperty().addListener(new ChangeListener<State>() {
            public void changed(ObservableValue<? extends State> source, State oldState, State newState) {
                if (newState.equals(State.SUCCEEDED)) {
                    onSuccess();
                } else if (newState.equals(State.FAILED)) {
                    onFailed();
                }
            }
        });
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public File getLocalFile() {
        return localFile;
    }

    protected File call() throws Exception {
        log.info(String.format("Downloading file %s to %s", remoteUrl, localFile.getAbsolutePath()));

        HttpGet httpGet = new HttpGet(this.remoteUrl);
        HttpResponse response = httpClient.execute(httpGet);
        InputStream remoteContentStream = response.getEntity().getContent();
        OutputStream localFileStream = null;
        try {
            long fileSize = response.getEntity().getContentLength();
            log.info(String.format("Size of file to download is %s", fileSize));

            File dir = localFile.getParentFile();
            dir.mkdirs();

            localFileStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[bufferSize];
            int sizeOfChunk;
            int amountComplete = 0;
            while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1) {
                localFileStream.write(buffer, 0, sizeOfChunk);
                amountComplete += sizeOfChunk;
                updateProgress(amountComplete, fileSize);
                log.info(String.format("Downloaded %s of %s bytes (%d) for file",
                        amountComplete, fileSize, (int) ((double) amountComplete / (double) fileSize * 100.0)));
            }
            log.info(String.format("Downloading of file %s to %s completed successfully", remoteUrl, localFile.getAbsolutePath()));
            return localFile;
        } finally {
            remoteContentStream.close();
            if (localFileStream != null) {
                localFileStream.close();
            }
        }
    }

    private void onFailed() {
        log.warn("File download failed: " + getException().getMessage(), getException());
    }

    private void onSuccess() {
    }
}
