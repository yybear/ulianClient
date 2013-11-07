/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 *
 * @author ganqing
 */
public class FileDownloadUtils {

    private static final Log logger = LogFactory.getLog(FileDownloadUtils.class);

    public static boolean resume(String url, File localFile) {
        boolean isComplete = false;
        int bufferSize = 1024;
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setContentCharset(params, Contants.UTF8);
        HttpClient httpClient = new DefaultHttpClient(params);
        if (localFile == null || StringUtils.isBlank(url)) {
            return false;
        }
        InputStream remoteContentStream = null;
        OutputStream localFileStream = null;
        RandomAccessFile tempFile = null;
        try {
            long size = 0;

            if (localFile.exists()) { // 文件已经存在
                size = localFile.length();
                tempFile = new RandomAccessFile(localFile, "rw");
            }
            HttpGet httpGet = new HttpGet(url);
            if (size > 0) {
                httpGet.addHeader("RANGE", "bytes=" + size);
            }
            HttpResponse response = httpClient.execute(httpGet);
            remoteContentStream = response.getEntity().getContent();
            localFileStream = null;

            long fileSize = response.getEntity().getContentLength();
            logger.info(String.format("localFile size if %s, romateFile Size is %s", size, fileSize));

            byte[] buffer = new byte[bufferSize];
            int sizeOfChunk;
            if (size == 0) {
                localFileStream = new FileOutputStream(localFile);
                while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1) {
                    localFileStream.write(buffer, 0, sizeOfChunk);
                }
            } else if (size < fileSize) {
                // 需要续传
                tempFile.seek(size);
                while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1) {
                    tempFile.write(buffer, 0, sizeOfChunk);
                }
            }

            size = localFile.length();
            if (size == fileSize) {
                // 下载完成
                isComplete = true;
            }
        } catch (IOException | IllegalStateException ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            IOUtils.closeQuietly(remoteContentStream);
            IOUtils.closeQuietly(localFileStream);
            try {
                if (tempFile != null) {
                    tempFile.close();
                }
            } catch (IOException ex) {
            }
        }
        return isComplete;
    }
}
