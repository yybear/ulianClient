/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author ganqing
 */
public class MessageUtils {
    private static Properties error;
    
    static {
        error = new Properties();
        try {
            error.load(MessageUtils.class.getResourceAsStream("/errors.properties"));
            error.load(MessageUtils.class.getResourceAsStream("/message.properties"));
        } catch (IOException ingore) {
        }
    }
    
    public static String getMessage(String key) {
        return error.getProperty(key);
    }
    
    public static String getErrorMessage(int code) {
        return error.getProperty("error."+code);
    }
}
