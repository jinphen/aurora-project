package aurora.presentation.component.std.config;

import uncertain.composite.CompositeMap;

public class BoxConfig extends ComponentConfig {
	
	public static final String ROWS = "row";
	public static final String COLUMNS = "column";
	
	public static final String PROPERTITY_CELLPADDING = "cellpadding";
	public static final String PROPERTITY_CELLSPACING = "cellspacing";
	public static final String PROPERTITY_VALIDALIGN = "validalign";
	public static final String PROPERTITY_PADDING = "padding";
	public static final String PROPRRTITY_SHOWBORDER = "showborder";
	
	
	public int getRows() {
		return getInt(ROWS, -1);		
	}
	public void setRows(int rows){
		putInt(ROWS, rows);
	}
	
	public int getColumns() {
		return getInt(COLUMNS, -1);		
	}
	public void setColumns(int columns){
		putInt(COLUMNS, columns);
	}
	
	public int getCellpadding(){
		return getInt(PROPERTITY_CELLPADDING, 0);
	}
	public void setCellpadding(int padding){
		putInt(PROPERTITY_CELLPADDING, padding);
	}
	
	public int getCellspacing(){
		return getInt(PROPERTITY_CELLSPACING, 0);
	}
	public void setCellspacing(int spacing){
		putInt(PROPERTITY_CELLSPACING, spacing);
	}
	
	public int getPadding(){
		return getInt(PROPERTITY_PADDING, 0);
	}
	public void setPadding(int padding){
		putInt(PROPERTITY_PADDING, padding);
	}
	
	
	public void addChild(CompositeMap item){
		CompositeMap context = getObjectContext();
		context.addChild(item);
	}
	
	public Boolean isShowBorder(){
        return getBoolean(PROPRRTITY_SHOWBORDER);
    }
    public void setShowBorder(boolean show){
        putBoolean(PROPRRTITY_SHOWBORDER, show);
    }
	
}
