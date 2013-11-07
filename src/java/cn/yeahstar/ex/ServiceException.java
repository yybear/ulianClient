/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.ex;

import cn.yeahstar.util.MessageUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author SanYuan
 */
public class ServiceException extends RuntimeException {

    private int code = -1;
    private Object args[];
    
    public ServiceException(String msg) {
        super(msg);
    }

    public ServiceException(int code) {
        super();
        this.code = code;
    }

    public ServiceException(int code, Object[] args) {
        super();
        this.code = code;
        this.args = args;
    }

    @Override
    public String getMessage() {
        String msg = MessageUtils.getErrorMessage(this.code);
        if(StringUtils.isBlank(msg)) {
            if(args != null) {
                msg = String.format(msg, args);
            }
        } else {
            msg = super.getMessage();
        }
        return msg;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
