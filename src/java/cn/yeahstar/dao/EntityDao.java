/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ganqing
 */
public interface EntityDao<E, PK extends Serializable> {
    public void deleteByPk(PK id);
    /**
     * 事务操作下使用，以便对conn进行一些操作
     * @param conn
     * @param id 
     */
    public void deleteByPk(Connection conn, PK id);
    
    public void batchDeleteByPK(Set<PK> ids);
    
    public void batchDeleteByPK(Connection conn, Set<PK> ids);
    
    public E get(Connection conn, PK id);

    public E get(PK id);

    public Long insert(String sql, Object... args);
    
    public Long insert(Connection conn, String sql, Object... args);

    public int update(String sql, Object... args);
    
    public int update(Connection conn, String sql, Object... args);

    public E query(String sql, Object... args);
    
    public E query(Connection conn, String sql, Object... args);
    
    public List<E> list(String sql, Object... args);
    
    public List<E> list(Connection conn, String sql, Object... args);
    
    public List<E> batchGet(Connection conn, Set<PK> ids);

    public List<E> batchGet(Set<PK> ids);
    
    public List<E> getAll(Connection conn);

    public List<E> getAll();    
    
    public long count(Connection conn, String sql, Object... args);
    
    public long count(String sql, Object... args);
    
    public List<E> getPage(Connection conn, String sql, long start, long limit, Object... args);
    
    public List<E> getPage(String sql, long start, long limit, Object... args);
}
