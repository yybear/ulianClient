/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.net;

import cn.yeahstar.util.Contants;
import cn.yeahstar.util.FileDownloadUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
 * 文件异步下载支持下载队列
 * @author ganqing
 */
public class FileDownloadAsyn {

    private static Log logger = LogFactory.getLog(FileDownloadAsyn.class);
    private List<String> remoteUrls = new ArrayList<String>();
    private List<String> localFiles = new ArrayList<String>();

    public void addItem(String remoteUrl, String localFile) {
        if (StringUtils.isNotBlank(localFile) && StringUtils.isNotBlank(remoteUrl)) {
            logger.debug(String.format("add url %s file %s to download list", remoteUrl, localFile));
            remoteUrls.add(remoteUrl);
            localFiles.add(localFile);
        }
    }

    public void cleanItems() {
        remoteUrls.clear();
        localFiles.clear();
    }

    public void downloadList() {
        for (int i = 0, size = remoteUrls.size(); i < size; i++) {
            String url = remoteUrls.get(i);
            String fileName = localFiles.get(i);

            DownloadTask task = new DownloadTask(url, new File(fileName));
            task.start();
        }
    }
    
    public void resumeSingle(String url, File file) {
        DownloadTask task = new DownloadTask(url, file);
        task.start();
    }

    class DownloadTask extends Thread {
        private String url;
        private File file;

        public DownloadTask(String url, File file) {
            this.file = file;
            this.url = url;
        }

        @Override
        public void run() {
            FileDownloadUtils.resume(url, file);
        }
    }
}
