package aurora.ide.meta.gef.editors.source.gen;

import org.eclipse.ui.views.properties.IPropertyDescriptor;

import uncertain.composite.CompositeMap;
import aurora.ide.meta.gef.editors.models.Dataset;

public class DatasetMap extends AbstractComponentMap {

	private Dataset c;

	public DatasetMap(Dataset c) {
		this.c = c;
	}
	public CompositeMap toCompositMap() {
		String type = c.getType();
		CompositeMap map = AuroraComponent2CompositMap.createChild(type);
		IPropertyDescriptor[] propertyDescriptors = c.getPropertyDescriptors();
		for (IPropertyDescriptor iPropertyDescriptor : propertyDescriptors) {
			Object id = iPropertyDescriptor.getId();

			boolean isKey = this.isCompositMapKey(id.toString());
			if (isKey) {
				Object value = c.getPropertyValue(id);
				if (value != null && !("".equals(value)))
					map.putString(id, value.toString());
			}
		}
		return map;
	}

	public boolean isCompositMapKey(String key) {
		return true;
	}

}
