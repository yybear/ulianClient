/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.dao.impl;

import cn.yeahstar.dao.EntityDao;
import cn.yeahstar.ex.DBException;
import cn.yeahstar.jdbc.DBManager;
import cn.yeahstar.util.GenericUtils;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ganqing
 */
public class DBUtilsEntityDao<E, PK extends Serializable> implements EntityDao<E, PK>{

    private static final Log logger = LogFactory.getLog(DBUtilsEntityDao.class);
    protected Class<E> entityClass;
    protected QueryRunner runner;

    public DBUtilsEntityDao() {
        this.entityClass = GenericUtils.getGenericParameter0(getClass());
        this.runner = DBManager.getQueryRunner();
    }

    public void deleteByPk(PK id) {
        this.deleteByPk(null, id);
    }
    
    /**
     * 事务操作下使用，以便对conn进行一些操作
     * @param conn
     * @param id 
     */
    public void deleteByPk(Connection conn, PK id) {
        String sql = "delete from " + getTableName() + " where id=?";
        try {
            if(conn == null)
                this.runner.update(sql, id);
            else 
                runner.update(conn, sql, id);
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error, id is %s", sql, id + ""), ex);
        }
    }

    public void batchDeleteByPK(Set<PK> ids) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void batchDeleteByPK(Connection conn, Set<PK> ids) {
        String sql = "delete from " + getTableName() + " where id=?";
        Object[][] params = new Object[ids.size()][1];
        int i = 0;
        for (PK id : ids) {
            params[i++][0] = id;
        }
        try {
            if(null == conn)
                this.runner.batch(sql, params);
            else 
                this.runner.batch(conn, sql, params);
        } catch (SQLException ex) {
            throw new DBException(String.format("excute %s error", sql), ex);
        }
    }
    
    public E get(Connection conn, PK id) {
        ResultSetHandler<E> rsh = new BeanHandler<E>(entityClass);
        E result = null;
        String sql = "select * from " + getTableName()
                + " where id=?";
        try {
            if(null == conn)
                result = runner.query(sql, rsh, new Object[]{id});
            else
                result = runner.query(conn, sql, rsh, new Object[]{id});
        } catch (SQLException ex) {
            logger.error(ex.getMessage(), ex);
            throw new DBException(String.format("excute %s error, id is %s", sql, id + ""), ex);
        }

        return result;
    }

    public E get(PK id) {
        return this.get(null, id);
    }

    public Long insert(String sql, Object... args) {
        Connection conn = DBManager.getConnection();
        Long id = this.insert(conn, sql, args);
        DbUtils.closeQuietly(conn);
        return id;
    }
    
    public Long insert(Connection conn, String sql, Object... args) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            return rs.next() ? rs.getLong(1) : -1;
        } catch (SQLException ex) {
            throw new DBException(String.format("excute %s error", sql), ex);
        } finally {
            try {
                DbUtils.close(rs);
                DbUtils.close(ps);
            } catch (SQLException ex) {
            }
        }
    }

    public int update(String sql, Object... args) {
        return update(null, sql, args);
    }
    
    public int update(Connection conn, String sql, Object... args) {
        int count = 0;
        try {
            if(null != conn)
                count = runner.update(conn, sql, args);
            else 
                count = runner.update(sql, args);
        } catch (SQLException e) {
            logger.error("This table can not changed !", e);
            throw new DBException(String.format("excute %s error", sql), e);
        }
        return count;
    }

    public E query(String sql, Object... args) {
        return this.query(null, sql, args);
    }
    
    public E query(Connection conn, String sql, Object... args) {
        ResultSetHandler<E> rsh = new BeanHandler<E>(entityClass);
        E result = null;
        try {
            if(null != conn)
                result = runner.query(conn, sql, rsh, args);
            else
                result = runner.query(sql, rsh, args);
        } catch (SQLException ex) {
            throw new DBException(String.format("excute %s error", sql), ex);
        }
        return result;
    }

    @Override
    public List<E> list(String sql, Object... args) {
        return list(null, sql, args);
    }

    @Override
    public List<E> list(Connection conn, String sql, Object... args) {
        ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(entityClass);
        List<E> result = null;
        try {
            if(null != conn)
                result = runner.query(conn, sql, rsh, args);
            else
                result = runner.query(sql, rsh, args);
        } catch (SQLException ex) {
            throw new DBException(String.format("excute %s error", sql), ex);
        }
        return result;
    }
    
    public List<E> batchGet(Connection conn, Set<PK> ids) {
        ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(entityClass);
        List<E> result = null;
        String tableName = getTableName(); 
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(getTableName()).append(" where id in(");
        int i = 0;
        for(PK id : ids) {
            if(i>0)
                sb.append(",");
            sb.append("?");
            i++;
        }
        sb.append(")");
        try {
            String sqlStr = sb.toString();
            Object[] idArray = ids.toArray();
            if(conn == null)
                result = runner.query(sqlStr, rsh, idArray);
            else
                result = runner.query(conn, sqlStr, rsh, idArray);
        } catch (SQLException e) {
            logger.error("Can not this query table " + getTableName(), e);
            throw new DBException(String.format("getAll %s", tableName), e);
        }
        return result;
    }

    public List<E> batchGet(Set<PK> ids) {
        return batchGet(null, ids);
    }
    
    public List<E> getAll(Connection conn) {
        ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(entityClass);
        List<E> result = null;
        String tableName = getTableName(); 
        try {
            String sql = "select * from " + tableName;
            if(conn == null)
                result = runner.query(sql, rsh);
            else 
                result = runner.query(conn, sql, rsh);
        } catch (SQLException e) {
            logger.error("Can not this query table " + tableName, e);
            throw new DBException(String.format("getAll %s", tableName), e);
        }
        return result;
    }

    public List<E> getAll() {
        return getAll(null);
    }
    
    private ScalarHandler scalarHandler = new ScalarHandler() {  
        @Override  
        public Object handle(ResultSet rs) throws SQLException {  
            Object obj = super.handle(rs);  
            if (obj instanceof Number)  
                return ((Number) obj).longValue();  
            return obj;  
        }  
    };
    
    public long count(Connection conn, String sql, Object... args) {
        long count = 0;
        try {
            if(null == args || args.length == 0) {
                if(null == conn)
                    count = (Long)runner.query(sql, scalarHandler);
                else 
                    count = (Long)runner.query(conn, sql, scalarHandler);
            } else {
                if(null == conn)
                    count = (Long)runner.query(sql, scalarHandler, args);
                else
                    count = (Long)runner.query(conn, sql, scalarHandler, args);
            }
        } catch (SQLException ex) {
            throw new DBException(String.format("excute %s error", sql), ex);
        }
        return count;
    }
    
    public long count(String sql, Object... args) {
        return count(null, sql, args);
    }
    
    public List<E> getPage(Connection conn, String sql, long start, long limit, Object... args) {
        ResultSetHandler<List<E>> rsh = new BeanListHandler<E>(entityClass);
        List<E> result = null;
        String tableName = getTableName(); 
        StringBuilder sb = new StringBuilder(sql);
        sb.append(" Limit ? Offset ?");
        try {
            Object[] args2 = new Object[0];
            String sqlStr = sb.toString();
            if(null == args || args.length == 0) {
                args2 = new Object[2];
                args2[0] = limit;
                args2[1] = start;
            } else {
                args2 = new Object[args.length + 2];
                for(int i = 0; i < args.length; i++) {
                    args2[i] = args[i];
                }
                args2[args.length] = limit;
                args2[args.length + 1] = start;
            }
            if(null == conn)
                result = runner.query(sqlStr, rsh, args2);
            else 
                result = runner.query(conn, sqlStr, rsh, args2);
        } catch (SQLException e) {
            logger.error("Can not this query table " + getTableName(), e);
            throw new DBException(String.format("getAll %s", tableName), e);
        }
        return result;
    }
    
    public List<E> getPage(String sql, long start, long limit, Object... args) {
        return getPage(null, sql, start, limit, args);
    }
    
    protected String getTableName() {
        return StringUtils.lowerCase(entityClass.getSimpleName());
    } 
}
