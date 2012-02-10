/*
 * MethodArgSizeFilter.java
 *
 * Created on 2002��1��8��, ����3:17
 */

package org.javautil.reflect;
import java.lang.reflect.Method;

import uncertain.util.reflect.MethodFilter;
/**
 *
 * @author  Administrator
 * @version 
 */
public class MethodArgSizeFilter implements MethodFilter {
    
    int size = 0;

    /** Creates new MethodArgSizeFilter */
    public MethodArgSizeFilter(int n) {
        size = n;
    }
    
    public boolean accepts(Class owner, Method m) {
        return m.getParameterTypes().length == size;
    }    
    

}
