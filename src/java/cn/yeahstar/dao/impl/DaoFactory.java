/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.dao.impl;

import cn.yeahstar.dao.UlianUsersDao;

/**
 *
 * @author ganqing
 */
public class DaoFactory {
    private static DaoFactory factory = new DaoFactory();
    
    private UlianUsersDao ulianUsersDao = new UlianUsersDaoImpl();
    
    private CommonDao commonDao = new CommonDao();
    
    private DaoFactory() {}
    
    public static DaoFactory getInstance() {
        return factory;
    }

    public UlianUsersDao getUlianUsersDao() {
        return ulianUsersDao;
    }

    public CommonDao getCommonDao() {
        return commonDao;
    }
    
}
