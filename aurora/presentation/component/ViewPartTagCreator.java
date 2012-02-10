/*
 * Created on 2009-5-15
 */
package aurora.presentation.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uncertain.composite.CompositeMap;
import uncertain.util.template.EmptyTag;
import uncertain.util.template.ITagContent;
import uncertain.util.template.ITagCreator;
import aurora.presentation.BuildSession;
import aurora.presentation.ViewContext;

public class ViewPartTagCreator implements ITagCreator {
    
    static final EmptyTag EMPTY_TAG = new EmptyTag();
    
    Collection          mChilds;
    Map                 mContentMap;
    BuildSession        mBuildSession;
    ViewContext         mViewContext;
    
    public ViewPartTagCreator( BuildSession session, ViewContext context )
    {
        mBuildSession = session;
        mViewContext = context;
        CompositeMap view = mViewContext.getView();
        if( view==null )
            throw new RuntimeException("No view in ViewContext");
        addChilds(view.getChilds());
    }
    
    public void clear(){
        if(mContentMap!=null){
            mContentMap.clear();
            mContentMap = null;
        }
        mChilds = null;
    }    

    public void addChilds( Collection childs ){
        clear();
        if(childs==null) return;
        mContentMap = new HashMap();
        mChilds = childs;
        Iterator it = mChilds.iterator();
        while(it.hasNext()){
            CompositeMap item = (CompositeMap)it.next();
            String name = item.getString("name");
            if(name==null) 
                throw new ViewPartConfigError("view part doesn't has 'name' property:"+item.toXML());
            if(mContentMap.containsKey(name))
                throw new ViewPartConfigError("view part with name '"+name+"' already defined");
            mContentMap.put(name, item);
        }
    }
    
    public ITagContent createInstance(String name_space, String tag){
        if( mContentMap == null)
            return EMPTY_TAG;
        CompositeMap view = (CompositeMap)mContentMap.get(tag);
        if( view==null)
            return EMPTY_TAG;
        return new ViewPartTag( mBuildSession, mViewContext, view);
    }

}
