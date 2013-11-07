/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.jdbc;

import cn.yeahstar.ex.DBException;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author ganqing
 */
public class DBUtilsTransactionHelper {
    public static ThreadLocal<Connection> container = new ThreadLocal<Connection>();

    public static ThreadLocal<Connection> getContainer() {
        return container;
    }
    
    public static void startTransacion() {
        Connection conn  = container.get(); // 取当前线程的connection
        if(null == conn) {
            conn = DBManager.getConnection();
            container.set(conn);
        }
        try {
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            throw new DBException(ex.getMessage(), ex);
        }
    }
    
    /**
     * 要先调用startTransaction，否则得到null
     * @return 
     */
    public static Connection getConnetion() {
        return container.get();
    }
    
    public static void commit() {
        Connection conn  = container.get(); // 取当前线程的connection
        if(null != conn) {
            try {
                conn.commit();
            } catch (SQLException ex) {
                throw new DBException(ex.getMessage(), ex);
            }
        }
    }
    
    public static void rollback() {
        Connection conn  = container.get(); // 取当前线程的connection
        if(null != conn) {
            try {
                DbUtils.rollback(conn);
            } catch (SQLException ex) {
                throw new DBException(ex.getMessage(), ex);
            }
        }
    }
    
    public static void close() {
        Connection conn  = container.get(); // 取当前线程的connection
        if(null != conn) {
            try {
                DbUtils.close(conn);
            } catch (SQLException ex) {
                throw new DBException(ex.getMessage(), ex);
            } finally {
                container.remove();
            }
        }
    }
}
