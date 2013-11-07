/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ganqing
 */
public class ClassLoaderUtils {
    public static List<URL> loadJarPath(String path) {
        List<File> files = new ArrayList<File>();
        List<URL> urls = new ArrayList<URL>();
        File lib = new File(path);
        loopFiles(lib, files);
        System.out.println("loadJarPath " + path);
        System.out.println("loadJarPath " + files.size());
        for (File file : files) {
            URL url = loadJarFile(file);
            if(null != url)
                urls.add(url);
        }
        return urls;
    }

    public static URL loadJarFile(File file) {
        try {
            System.err.println(file.toURI().toURL().getPath());
            return file.toURI().toURL();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void loopFiles(File file, List<File> files) {
        if (file.isDirectory()) {
            File[] tmps = file.listFiles();
            for (File tmp : tmps) {
                loopFiles(tmp, files);
            }
        } else {
            if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
                files.add(file);
            }
        }
    }
    
    public static URLClassLoader customCL() {
        List<URL> urls = loadJarPath(ConfigUtils.getRootDir() + File.separator + "bin" + File.separator + "lib");
        URLClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
        return cl;
    }
}
