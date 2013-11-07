/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static org.apache.commons.io.FileUtils.cleanDirectory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ganqing
 */
public class FileUtils extends org.apache.commons.io.FileUtils{
    
    private static final Log logger = LogFactory.getLog(FileUtils.class);
    
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    /**
     * 
     * @param zipFile
     * @param folder 解压的目录
     * @throws IOException 
     */
    private static void unzip(File zipFile, File folder) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        if(!folder.exists()) // 目录不在就创建 否则清理
            folder.mkdirs();
        else { 
            try {
                cleanDirectory(folder);
            } catch (IOException ignore) {
            }
        }

        ZipInputStream zis = null;
        try {
            String outputFolder = folder.getCanonicalPath();
            //get the zip file content
            zis = new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                logger.debug("file unzip : " + newFile.getAbsoluteFile());
                if(ze.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            
        } catch (IOException e) {
            throw e;
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                } catch (IOException ignore) {
                }
                IOUtils.closeQuietly(zis);
            }
        }
    }
    
    /**
     * 解压zip 文件
     */
    public static void unzip(File zipFile, String unzipDir) throws IOException {
        File folder = new File(unzipDir);
        unzip(zipFile, folder);
    }
    
    /**
     * 获取磁盘的剩余空间
     * @param root 
     */
    public static long getFreeSpace(String root) {
        File f = new File(root);
        return f.getFreeSpace();
    } 
    
    /**
     * 获取文件或者目录的所占空间大小
     * @param file
     * @return 
     */
    public static long getDirSize(File file) {     
        //判断文件是否存在     
        if (file.exists()) {     
            //如果是目录则递归计算其内容的总大小    
            if (file.isDirectory()) {     
                File[] children = file.listFiles();     
                long size = 0;     
                if(children != null) {
                    for (File f : children)     
                        size += getDirSize(f);     
                }
                return size;     
            } else {
                return  file.length();     
            }     
        } else {     
            return 0;     
        }     
    }
    
    public static void sortVersionDirs(List<String>fileNames) {
        Collections.sort(fileNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] o1s = StringUtils.split(o1, ".");
                String[] o2s = StringUtils.split(o2, ".");
                int flag = 0;
                for(int i = 0; i< o1s.length; i ++) {
                    int o1I = Integer.valueOf(o1s[i]);
                    int o2I = Integer.valueOf(o2s[i]);
                    if(o1I > o2I) {
                        flag = -1; break;
                    } else if(o1I < o2I) {
                        flag = 1; break;
                    }
                }
                return flag;
            }
        });
    }  
}
