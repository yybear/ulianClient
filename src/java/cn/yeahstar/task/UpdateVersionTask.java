/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.task;

import cn.yeahstar.net.FileDownloadAsyn;
import cn.yeahstar.net.NetHelper;
import cn.yeahstar.util.ConfigUtils;
import cn.yeahstar.util.Contants;
import static cn.yeahstar.util.Contants.D_VERSION;
import static cn.yeahstar.util.Contants.LATEST_FILE_URL;
import static cn.yeahstar.util.Contants.LATEST_VERSION_URL;
import static cn.yeahstar.util.Contants.P_VERSION;
import cn.yeahstar.util.FileDownloadUtils;
import cn.yeahstar.util.FileUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;

/**
 *
 * @author ganqing
 */
public class UpdateVersionTask extends TimerTask {

    private static final Log logger = LogFactory.getLog(UpdateVersionTask.class);

    @Override
    public void run() {
        logger.debug("UpdateVersionTask run");
        final String currentDVersion = ConfigUtils.getUlianConfig(D_VERSION);
        final String currentPVersion = ConfigUtils.getUlianConfig(P_VERSION);
        logger.debug(String.format("current Data version is %s, Program version is %s", currentDVersion, currentPVersion));

        // 读取服务器上最新的版本
        String url = ConfigUtils.get(LATEST_VERSION_URL);
        JsonNode json = NetHelper.getInstance().ping(url);
        if (null != json) {
            String latestDVersion = json.get(D_VERSION).getTextValue();
            String latestPVersion = json.get(P_VERSION).getTextValue();
            logger.debug(String.format("latest Data version is %s, Program version is %s", latestDVersion, latestPVersion));
            if (!currentDVersion.equals(latestDVersion)) {
                // 需要更新数据版本
                downloadLatest(latestDVersion, json.get(Contants.DATA_FILES).getTextValue(), D_VERSION);
            }
            if (!currentPVersion.equals(latestPVersion)) {
                // 需要更新数据版本
                downloadLatest(latestPVersion, json.get(Contants.PROGRAMS_FILES).getTextValue(), P_VERSION);
            }
        }
    }

    private void downloadLatest(String version, String fileName, String type) {
        String sysTemp = ConfigUtils.getTempDir();

        File zipFile = null;
        try {

            File tempDir = new File(sysTemp + File.separator + type + "_" + version);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            zipFile = new File(tempDir.getCanonicalPath() + File.separator + "Temp.zip");
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        if (zipFile == null) {
            return;
        }
        // TODO code 需要是U盘的序列号
        String url = ConfigUtils.get(LATEST_FILE_URL) + "/" + fileName + "/923203023";
        String distDir = ConfigUtils.getRootDir() + File.separator;
        if (type.equals(P_VERSION)) {
            distDir = distDir + "bin" + File.separator + "v" + version;
        } else {
            //distDir = distDir + "data" + File.separator + "v" + version;
            distDir = null;
        }

        String unzipDir = sysTemp + File.separator + type + "_" + version + File.separator + Contants.UNZIP_DIR;
        FileDownloadAsyn download = new FileDownloadAsyn();
        boolean isComplete = FileDownloadUtils.resume(url, zipFile);
        if (isComplete) {
            // 下载完成
            try {
                File srcfolder = new File(unzipDir);
                FileUtils.unzip(zipFile, unzipDir);

                logger.debug("target dir is " + distDir);

                // 解压完成  拷贝到distDir
                if (distDir != null) {
                    File distDirFile = new File(distDir);
                    if (FileUtils.getDirSize(srcfolder) < FileUtils.getFreeSpace(ConfigUtils.getRootDir())) {
                        // 检测磁盘空间
                        FileUtils.copyDirectory(srcfolder, distDirFile);
                        checkFile(distDir, version, type);

                        // 删除临时文件
                        File tempDir = zipFile.getParentFile();
                        try {
                            logger.debug("remove temp dir :" + tempDir.getCanonicalFile());
                            FileUtils.forceDeleteOnExit(tempDir);
                        } catch (IOException ex) {
                        }
                    }

                    // 最多保存几个版本
                    removeOlderVersion();
                }
            } catch (IOException ex) {
            }
        }
    }
    
    private void removeOlderVersion() throws IOException {
        String binDir = ConfigUtils.getRootDir() + File.separator + "bin";
        File bin = new File(binDir);
        File[] versionFiles = bin.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("v");
            }
        });
        if(versionFiles.length < 4)
            return;
        
        List<String> fileNames = new ArrayList<String>(versionFiles.length);
        for(File version : versionFiles) {
            fileNames.add(version.getName().substring(1));
        }
        FileUtils.sortVersionDirs(fileNames);
        for(int i =3; i < fileNames.size(); i++) {
            FileUtils.deleteDirectory(new File(binDir + File.separator + fileNames.get(i)));
        }
    }

    private void checkFile(String distDir, String type, String version) {
        File distDirFile = new File(distDir);
        if (distDirFile.exists() && distDirFile.isDirectory()) {
            File mainApp = null;
            try {
                mainApp = new File(distDirFile.getCanonicalPath() + File.separator + Contants.MAIN_APP);
            } catch (IOException ex) {
            }
            if (mainApp != null && mainApp.exists()) {
                // 完成后更改版本号
                ConfigUtils.writeUlianConfigAsyn(type, version);
            }
        }
    }
}
