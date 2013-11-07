/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.service;

import cn.yeahstar.ex.ServiceException;

/**
 *
 * @author SanYuan
 */
public class FavoriteService {
    
    private final static int ZERO = 0 ;
    
    public int createFavorite(int questionId){
    
        int favId = 0 ;

        if(ZERO >= questionId)
            throw new ServiceException("Question id error ") ;
        
        return favId ;
    }
    
     public boolean removeFavorite(int questionId){
    
        boolean bRslt = false ;

        if(ZERO >= questionId)
            throw new ServiceException("Question id error ") ;
        
        return bRslt ;
    }
}
