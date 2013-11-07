/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author ganqing
 */
public class ConfigUtils {

    private static Properties properies;
    private static Properties ulianConfig;
    private static String rootDir;
    
    private static final String SYS_CONF_NAME = "sys.conf";
    private static String sysConfig = "";

    public static void init() {
        properies = new Properties();
        try {
            properies.load(ConfigUtils.class.getResourceAsStream("/config.properties"));
        } catch (IOException ex) {
        }

        String path = ConfigUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        File baseDir = new File(path).getParentFile().getParentFile().getParentFile();

        ulianConfig = new Properties();
        try {
            rootDir = baseDir.getCanonicalPath();
            sysConfig = rootDir + File.separator + "data" + File.separator + SYS_CONF_NAME;
            ulianConfig.load(new FileInputStream(sysConfig));
        } catch (IOException ex) {
        }
    }

    public static String getRootDir() {
        return rootDir;
    }
    
    public static String getBinDir() {
        return rootDir + File.separator + "bin";
    }
    
    public static String getDataDir() {
        return rootDir + File.separator + "data";
    }

    public static String get(String key) {
        return properies.getProperty(key);
    }

    public static long getLong(String key) {
        if (properies.containsKey(key)) {
            return Long.valueOf(properies.getProperty(key));
        }
        return 0;
    }
    
    public static long getLong(String key, long defaultValue) {
        if (properies.containsKey(key)) {
            return Long.valueOf(properies.getProperty(key));
        }
        return defaultValue;
    }

    public static String getUlianConfig(String key) {
        return ulianConfig.getProperty(key);
    }
    
    /**
     * 异步更新配置文件，需要等待下次启动生效
     * @param key
     * @param value 
     */
    public static void writeUlianConfigAsyn(String key, String value) {
        try {
            // 重新加载 以便不影响运行的程序
            Properties localProperies = new Properties();
            InputStream in = new FileInputStream(sysConfig);
            localProperies.load(in);
            localProperies.setProperty(key, value);
            
            OutputStream out = new FileOutputStream(sysConfig);
            localProperies.store(out, value);
        } catch (IOException ex) {
        }
    }
    
    public static String getCurrentPVersionDir() {
        return "v" + get("current.program.version");
    }
    
    public static String getCurrentDVersionDir() {
        return "v" + get("current.data.version");
    }
    
    public static String getTempDir() {
        String path = System.getProperty("java.io.tmpdir") + "ulian";
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();
        
        return path;
    }
}
