package aurora.presentation.component.std;

import uncertain.composite.CompositeMap;

public class VBox extends Box {
	
	protected int getRows(CompositeMap view){
		return UNLIMITED;
	}
	
	protected int getColumns(CompositeMap view){
		return 1;
	}
}
