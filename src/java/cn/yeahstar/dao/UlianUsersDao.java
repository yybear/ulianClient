/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.dao;

import cn.yeahstar.model.Ulian_Users;
import java.sql.Connection;
import java.util.List;

/**
 *
 * @author ganqing
 */
public interface UlianUsersDao {
    public Long insert(String sql, Object... args);
    
    public Long insert(Connection conn, String sql, Object... args);
    
    public Ulian_Users get(Long id);
    
    public Ulian_Users get(Connection conn, Long id);
    
    public long count(String sql, Object... args);
    
    public long count(Connection conn, String sql, Object... args);
    
    public List<Ulian_Users> getPage(String sql, long start, long limit, Object... args);
    
    public int update(String sql, Object... args);
}
