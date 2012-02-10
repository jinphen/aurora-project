/*
 * Created on 2009-9-15 下午06:34:13
 * Author: Zhou Fan
 */
package aurora.application.config;

import uncertain.composite.CompositeMap;
import uncertain.composite.CompositeUtil;

public class ScreenConfig extends BaseServiceConfig {

    public static final String KEY_VIEW = "view";
    
    public static ScreenConfig createScreenConfig(CompositeMap map) {
        ScreenConfig cfg = new ScreenConfig();
        cfg.initialize(map);
        return cfg;
    }

/*
    protected CompositeMap getChildNotNull(String name) {
        CompositeMap child = object_context.getChild(name);
        if (child == null)
            child = object_context.createChild(name);
        return child;
    }
*/

    public CompositeMap getViewConfig() {
        return object_context.getChild(KEY_VIEW);
    }
    
    public CompositeMap getDataSetsConfig(){
        return CompositeUtil.findChild(object_context, "dataSets");
    }

}
