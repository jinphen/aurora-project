/*
 * Created on 2008-5-28
 */
package aurora.bm;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.util.StringSplitter;
import aurora.database.sql.StringConcatenater;

public class DataFilter extends DynamicObject {
    
    public static final String KEY_NAME = "name";    
    
    public static final String KEY_EXPRESSION = "expression";
    
    public static final String KEY_ENFORCE_ACTIONS = "enforceactions";
    
    public static final String KEY_DATA_FILTERS = "data-filters";
    
    public static DataFilter getInstance( CompositeMap map ){
        DataFilter filter = new DataFilter();
        filter.initialize(map);
        return filter;
    }
    
    public String getName(){
        return getString(KEY_NAME);
    }
    
    public void setName(String name){
        putString(KEY_NAME, name);
    }
    
    public String getExpression(){
        String exp = getString(KEY_EXPRESSION);
        if(exp==null)
            exp = getObjectContext().getText();
        return exp;
    }
    
    public void setExpression( String exp ){
        getObjectContext().setText(exp);
        //putString(KEY_EXPRESSION, exp);
    }
    
    public String[] getEnforceActions(){
        String s = getString(KEY_ENFORCE_ACTIONS);
        if(s==null) return null;
        return StringSplitter.splitToArray(s, ',', true);
    }
    
    public void setEnforceActions( String[] actions ){
        StringConcatenater sc = new StringConcatenater(",");
        for(int i=0; i<actions.length; i++)
            sc.append(actions[i]);
        putString(KEY_ENFORCE_ACTIONS, sc.getContent());
    }
}
