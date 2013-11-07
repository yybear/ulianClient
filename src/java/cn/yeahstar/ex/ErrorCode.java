/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.ex;

/**
 *
 * @author ganqing
 */
public class ErrorCode {
    public static final int OK = 0;
    /*____________________ 异常编号 ___________________*/
    public static final int SYS_ERROR = 1;
    public static final int SYS_NET_ERR = 2;
    public static final int SYS_DB_NOT_EXIST = 3;
    public static final int SYS_DB_VERSION_ERR=4;
    public static final int USER_ERR_NOT_EXSIT = 20;
    public static final int USER_ERR_PASSWD_WRONG = 21;
}
