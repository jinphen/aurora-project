package aurora.presentation.component.std;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.ocm.ISingleton;
import aurora.presentation.BuildSession;
import aurora.presentation.IViewBuilder;
import aurora.presentation.ViewContext;

public class Switch implements IViewBuilder, ISingleton {

	private static final String TEST_FIELD = "test";
	private static final String KEY_VALUE = "value";

	public void buildView(BuildSession session, ViewContext view_context) throws IOException, aurora.presentation.ViewCreationException {
		CompositeMap model = view_context.getModel();//.getParent();
		CompositeMap view = view_context.getView();
		
		if (model == null) return;
		String testField = view.getString(TEST_FIELD);
		if (testField == null) throw new aurora.presentation.ViewCreationException("selector: No test field specified");
		Object obj = model.getObject(testField);
		Iterator it = view.getChildIterator();
		if (it == null) throw new aurora.presentation.ViewCreationException("selector:No case found");

		Collection child_views = null;
		while (it.hasNext()) {
			CompositeMap child = (CompositeMap) it.next();
			Object test_value = child.get(KEY_VALUE);
			if ("null".equals(test_value) && obj == null) {
				child_views = child.getChilds();
				break;
			}else if(test_value==null){
			    child_views = child.getChilds();
			    break;
			}else {
//				if (obj == null) obj = testField;
				String vl = test_value.toString();

				if ("*".equals(vl))
					if (obj != null) {
						child_views = child.getChilds();
						break;
					}

				vl = TextParser.parse(vl, model);
				if(obj==null)
				    break;
				if (vl.equals(obj.toString())) {
					child_views = child.getChilds();
					break;
				}
			}
		}

		if (child_views != null)
		try {
			session.buildViews(model, child_views);
		} catch (Exception e) {
			throw new aurora.presentation.ViewCreationException(e.getMessage());
		}
	}

	public String[] getBuildSteps(ViewContext context) {
		return null;
	}
}
