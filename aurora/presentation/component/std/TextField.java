package aurora.presentation.component.std;

import java.io.IOException;
import java.util.Map;

import aurora.presentation.BuildSession;
import aurora.presentation.ViewContext;

/**
 * 文本输入框
 * 
 * @version $Id: TextField.java v 1.0 2009-7-20 上午11:27:00 znjqolf Exp $
 * @author <a href="mailto:znjqolf@126.com">vincent</a>
 * 
 */
public class TextField extends InputField {	
	
	
	
	public void onCreateViewContent(BuildSession session, ViewContext context) throws IOException {
		super.onCreateViewContent(session, context);
		Map map = context.getMap();		
		map.put(PROPERTITY_CONFIG, getConfigString());
	}
}
