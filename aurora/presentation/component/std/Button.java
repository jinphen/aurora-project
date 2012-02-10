package aurora.presentation.component.std;

import java.io.IOException;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.presentation.BuildSession;
import aurora.presentation.ViewContext;

public class Button extends Field {
	
	public static final String CLASSNAME_WRAP = "item-btn";
	public static final String PROPERTITY_TEXT = "text";
	public static final String PROPERTITY_ICON = "icon";
	public static final String BUTTON_STYLE = "btnstyle";
	public static final String BUTTON_CLASS = "btnclass";
	public static final String PROPERTITY_CLICK = "click";
	public static final String PROPERTITY_TITLE = "title";
	private static final int DEFAULT_HEIGHT = 17;
	private static final int DEFAULT_WIDTH = 60;
	
	protected int getDefaultWidth(){
		return DEFAULT_WIDTH;
	}
	
	protected int getDefaultHeight(){
		return DEFAULT_HEIGHT;
	}
	
	protected String getDefaultClass(BuildSession session, ViewContext context){
		CompositeMap view = context.getView();
		String text = view.getString(PROPERTITY_TEXT, "");
		String icon = view.getString(PROPERTITY_ICON, "");
		String wrapClass = CLASSNAME_WRAP;
		if(!"".equals(icon)){
			if(!"".equals(text)){
				wrapClass += " item-btn-icon-text";
			}else{
				wrapClass += " item-btn-icon";
			}
		}
		return wrapClass;
	}
	
	public void onCreateViewContent(BuildSession session, ViewContext context) throws IOException{
		super.onCreateViewContent(session, context);
		CompositeMap view = context.getView();
		Map map = context.getMap();
		String clickEvent = view.getString(PROPERTITY_CLICK, "");
		if(!"".equals(clickEvent)){
			addEvent(id, "click", clickEvent);
		}
		String text = view.getString(PROPERTITY_TEXT, "");
		String icon = view.getString(PROPERTITY_ICON, "");
		String btnstyle = view.getString(BUTTON_STYLE, "");
		if(!"".equals(icon)){
			if(!"null".equalsIgnoreCase(icon))btnstyle+="background-image:url("+icon+");";
		}
		map.put(PROPERTITY_EVENTS, esb.toString());
		map.put(PROPERTITY_TEXT, text);
		map.put(BUTTON_CLASS, view.getString(BUTTON_CLASS, ""));
		map.put(PROPERTITY_TITLE, view.getString(PROPERTITY_TITLE, ""));
		map.put(BUTTON_STYLE, btnstyle);
		map.put(PROPERTITY_CONFIG, getConfigString());
	}
}
