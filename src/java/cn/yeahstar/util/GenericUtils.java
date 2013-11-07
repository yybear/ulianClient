/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.yeahstar.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author ganqing
 */
public class GenericUtils {
    public static Class getGenericParameter(Class clazz, int index) {
        Type genType = clazz.getGenericSuperclass();
        if (genType instanceof ParameterizedType) {
            Type param = ((ParameterizedType) genType).getActualTypeArguments()[index];
            if (param instanceof Class) {
                return (Class) param;
            }
        }
        return null;
    }

    public static Class getGenericParameter0(Class clazz) {
        return getGenericParameter(clazz, 0);
    }
}
