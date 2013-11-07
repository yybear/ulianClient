/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.ex;

/**
 *
 * @author ganqing
 */
public class DBException extends RuntimeException {
    public DBException(String msg) {
        super(msg);
    }
    
    public DBException(String msg, Throwable e) {
        super(msg, e);
    }
}
