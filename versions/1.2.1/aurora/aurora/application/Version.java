/*
 * Created on 2006-5-25
 */
package aurora.application;

import java.lang.reflect.Method;

public class Version {
    
    static StringBuffer VERSION_STRING = new StringBuffer();
    
    static {
        VERSION_STRING.append(getMajorVersion()).append('.').append(getMinorVersion()).append('.').append(getBuild());
    }
    
    public static String getVersion(){
        return VERSION_STRING.toString();
    }
    
    public static int getMajorVersion(){
        return 1;
    }
    
    public static int getMinorVersion(){
        return 2;
    }
    
    public static int getBuild(){
        return 0;
    }

}
