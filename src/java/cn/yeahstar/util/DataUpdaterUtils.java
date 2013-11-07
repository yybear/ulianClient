/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import cn.yeahstar.jdbc.DBManager;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数据更新，可能包含数据库新增sql和新增文件（页面，视频等）
 *
 * @author ganqing
 */
public class DataUpdaterUtils {

    private static Log logger = LogFactory.getLog(DataUpdaterUtils.class);

//    public static void update(String vesion) {
//        String dataDir = ConfigUtils.getRootDir() + File.separator + "data" + File.separator + "v" + vesion;
//        String sqlDir = dataDir + File.separator + ConfigUtils.get("sql.dir");
//        String fileDir = dataDir + File.separator + ConfigUtils.get("file.dir");
//
//        File fileFolder = new File(fileDir);
//
//        DBManager.updateSQL(sqlDir); // 更新数据库数据
//        if (fileFolder.exists()) {
//            try {
//                String destDir = ConfigUtils.getRootDir() + File.separator + "files";
//                FileUtils.copyDirectory(fileFolder, new File(destDir), true);
//            } catch (IOException ex) {
//                logger.error(ex);
//            }
//        }
//    }
    
    public static void update(String version) {
        String dataDir = ConfigUtils.getTempDir() + File.separator + Contants.D_VERSION + "_" 
                + version + File.separator + Contants.UNZIP_DIR;
        logger.debug("data folder is " + dataDir);
        
        String sqlDir = dataDir + File.separator + ConfigUtils.get("sql.dir");
        String fileDir = dataDir + File.separator + ConfigUtils.get("file.dir");

        File fileFolder = new File(fileDir);

        DBManager.updateSQLs(sqlDir); // 更新数据库数据
        if (fileFolder.exists()) {
            try {
                String destDir = ConfigUtils.getRootDir() + File.separator + ConfigUtils.get("file.dir");
                FileUtils.copyDirectory(fileFolder, new File(destDir), true);
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        
        try {
            logger.debug("clean temp data folder");
            FileUtils.forceDeleteOnExit(new File(dataDir).getParentFile());
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
