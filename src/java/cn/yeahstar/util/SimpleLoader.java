/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;

/**
 *
 * @author ganqing
 */
public class SimpleLoader {

    ClassLoader classLoader = null;

    public SimpleLoader() {
        try {
            URL[] urls = new URL[1];
            URLStreamHandler streamHandler = null;
            File classPath = new File(ConfigUtils.getRootDir() + File.separator + "bin" + File.separator + "lib");
            String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString();
            urls[0] = new URL(null, repository, streamHandler);
            classLoader = new URLClassLoader(urls);
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}
