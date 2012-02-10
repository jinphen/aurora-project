package aurora.presentation.component.std;

import java.io.IOException;
import java.util.Map;

import uncertain.composite.CompositeMap;
import aurora.presentation.BuildSession;
import aurora.presentation.ViewContext;

public class Tree extends Component {
	
	public static final String PROPERTITY_DATASET = "dataset";
	public static final String PROPERTITY_RENDERER = "renderer";
	public static final String PROPERTITY_FIELD_ID = "idfield";
	public static final String PROPERTITY_SHOWCHECKBOX = "showcheckbox";
	public static final String PROPERTITY_FIELD_PARENT = "parentfield";
	public static final String PROPERTITY_FIELD_DISPLAY = "displayfield";
	private static final String DEFAULT_CLASS = "item-tree";
	
	protected String getDefaultClass(BuildSession session, ViewContext context){
		return DEFAULT_CLASS;
	}
	
	public void onPreparePageContent(BuildSession session, ViewContext context) throws IOException {
		super.onPreparePageContent(session, context);
		addStyleSheet(session, context, "tree/Tree.css");
		addJavaScript(session, context, "tree/Tree.js");
	}
	
	public void onCreateViewContent(BuildSession session, ViewContext context) throws IOException{
		super.onCreateViewContent(session, context);
		Map map = context.getMap();
		CompositeMap view = context.getView();

		String size = "";
		/** Width属�1�7�1�7**/
		String width = view.getString(PROPERTITY_WIDTH, "");
		if(!"".endsWith(width)) {
			size += "width:"+width+"px;";
			addConfig(PROPERTITY_WIDTH, width);
		}
		/** Height属�1�7�1�7**/
		String height = view.getString(PROPERTITY_HEIGHT, "");
		if(!"".endsWith(height)) {
			size += "height:"+height+"px;";
			addConfig(PROPERTITY_HEIGHT, height);
		}
		
		map.put("size", size);
		map.put(PROPERTITY_DATASET, view.getString(PROPERTITY_DATASET));
		addConfig(PROPERTITY_FIELD_DISPLAY, view.getString(PROPERTITY_FIELD_DISPLAY,"name"));
		addConfig(PROPERTITY_RENDERER, view.getString(PROPERTITY_RENDERER,""));
		addConfig(PROPERTITY_FIELD_ID, view.getString(PROPERTITY_FIELD_ID,"id"));
		addConfig(PROPERTITY_FIELD_PARENT, view.getString(PROPERTITY_FIELD_PARENT,"pid"));
		addConfig(PROPERTITY_SHOWCHECKBOX, new Boolean(view.getBoolean(PROPERTITY_SHOWCHECKBOX, false)));
		map.put(PROPERTITY_CONFIG, getConfigString());
	}
	
	
	
}
