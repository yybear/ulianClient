/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import cn.yeahstar.net.FxFileDownload;
import cn.yeahstar.ui.MainWin;
import static cn.yeahstar.util.Contants.D_VERSION;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javafx.scene.web.WebEngine;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 用户界面utils，如弹窗等。
 *
 * @author ganqing
 */
public class JavaAppUtils {

    private static final Log logger = LogFactory.getLog(JavaAppUtils.class);

    /**
     * 弹窗
     *
     * @param onwer
     * @param title
     * @param msg
     */
    public static void alter(JFrame onwer, String title, String msg) {
        JDialog dialog = new JDialog(onwer, title);
        JLabel label = new JLabel(msg);
        dialog.add(label);
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        //dialog.setSize(100, 200);
        int w = dialog.getWidth();
        int h = dialog.getHeight();
        dialog.setLocation((width - w) / 2, (height - h) / 2);
        dialog.setAlwaysOnTop(true);
        dialog.setVisible(true);
    }

    /**
     * 重定向文件，如果没有http:和file开头则从page目录下读取文件.
     *
     * @param url
     * @param webEngine
     */
    public static void redirect(String url, WebEngine webEngine) {
        if (StringUtils.startsWith(url, "http:") || StringUtils.startsWith(url, "file:")) {
            webEngine.load(url);
        } else {
            webEngine.loadContent(getPageContent(url));
        }
    }

    /**
     * 下载文件
     *
     * @param remoteUrl
     * @throws IOException
     */
    public static void download(String remoteUrl, File localFile) throws IOException {
        /*FileChooser chooser = new FileChooser();
         File file = chooser.showSaveDialog(stage);
         if (file != null)
         {
         FileDownloadTask fileDownloadTask = new FileDownloadTask(remoteUrl, file);
         new Thread(fileDownloadTask).start();
         }*/
        FxFileDownload fileDownloadTask = new FxFileDownload(remoteUrl, localFile);
        new Thread(fileDownloadTask).start();
    }

    /**
     * 根据文件名称读取page下面的页面文件
     *
     * @param name
     * @return
     */
    public static String getPageContent(String name) {
        String page = "";
        String pagePath = ConfigUtils.getRootDir() + File.separator + "bin" + File.separator +
                ConfigUtils.getCurrentPVersionDir() + File.separator + "app" + File.separator + name;
        logger.debug(String.format("path is %s", pagePath));
        try {
            InputStream in = new FileInputStream(pagePath);
            page = IOUtils.toString(in, "utf-8");
        } catch (IOException ex) {
        }

        return page;
    }
}
