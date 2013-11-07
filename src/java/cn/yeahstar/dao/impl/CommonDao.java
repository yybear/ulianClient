/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.dao.impl;

import cn.yeahstar.ex.DBException;
import cn.yeahstar.jdbc.DBManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ganqing
 */
public class CommonDao {
    private static final Log logger = LogFactory.getLog(CommonDao.class);
    
    private QueryRunner runner;
    
    public CommonDao() {
        runner = DBManager.getQueryRunner();
    }
    
    private ScalarHandler scalarHandler = new ScalarHandler() {  
        @Override  
        public Object handle(ResultSet rs) throws SQLException {  
            Object obj = super.handle(rs);  
            if (obj instanceof Number)  
                return ((Number) obj).intValue();  
            return obj;  
        }  
    };
    
    public int count(String sql) {
        try {
            return (Integer)runner.query(sql, scalarHandler);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public int count(String sql, Object... params) {
        try {
            return (Integer)runner.query(sql, scalarHandler, params);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public Map<String, Object> query(String sql, Object... args) {
        try {
            return runner.query(sql, new MapHandler(), args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public <E> E queryClass(Connection conn, String sql, Class<E> className, Object... args) {
        ResultSetHandler<E> rsh = new BeanHandler<E>(className);
        E result = null;
        try {
            if(null == conn)
                result = runner.query(sql, rsh, args);
            else
                result = runner.query(conn, sql, rsh, args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
        }

        return result;
    }
    
    public Map<String, Object> query(Connection conn, String sql, Object... args) {
        try {
            return runner.query(conn, sql, new MapHandler(), args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public int update(String sql, Object... args) {
        try {
            return runner.update(sql, args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public int update(Connection conn, String sql, Object... args) {
        try {
            return runner.update(conn, sql, args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public List list(String sql, Object... args) {
        try {
            return runner.query(sql, new MapListHandler(), args);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("query %s error", sql), ex);
        }
    }
    
    public String getEnvSetting(String key) {
        String sql = "select value from ulian_env_settings where key=?";
        
        Map<String, Object> res = this.query(sql, key);
        if(MapUtils.isNotEmpty(res))
            return res.get("value").toString();
        else
            return null;
    }
}
