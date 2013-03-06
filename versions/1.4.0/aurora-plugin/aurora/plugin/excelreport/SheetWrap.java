package aurora.plugin.excelreport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;

public class SheetWrap {
	String name;
	boolean displayGridlines=true;
	DynamicContent dynamicContent;
	CellData[] staticContent;

	private int offsetRowIndex = 0;

	private Sheet excelSheet;
	ExcelFactory excelFactory;
	int totalCount = -1;

	public void createSheet(ExcelFactory excelFactory) {
		this.excelSheet = excelFactory.getWorkbook().createSheet(this.getName());
		this.excelSheet.setDisplayGridlines(this.displayGridlines);
		this.excelFactory = excelFactory;
		if (this.getDynamicContent() != null)
			this.offsetRowIndex = this.getDynamicContent().createContent(excelFactory, this.excelSheet);
		if (this.getStaticContent() != null)
			createStaticContent(excelFactory.getContext());
	}

	void createStaticContent(CompositeMap context) {
		int rowIndex;
		int colIndex;

		Row row;
		Cell cell;
		CellStyle cellStyle;
		
		for (CellData cellConfig : this.getStaticContent()) {
			if (cellConfig.getOffset()) {
				rowIndex = this.offsetRowIndex + cellConfig.getRow();
			} else {
				rowIndex = cellConfig.getRow();
			}

			row = ExcelFactory.createRow(this.excelSheet, rowIndex);
			colIndex = CellReference.convertColStringToIndex(cellConfig
					.getCell());
			cell = row.createCell(colIndex);
			cellStyle = this.excelFactory.getStyle(cellConfig.getStyleName());

			if (ExcelFactory.isNotNull(cellStyle)) {
				cell.setCellStyle(cellStyle);
			}
			if (cellConfig.getRange() != null) {
				this.excelSheet.addMergedRegion(CellRangeAddress
						.valueOf(cellConfig.getRange()));
			}
			if (CellData.KEY_FORMULA.equals(cellConfig.getType())) {
				cell.setCellFormula(cellConfig.getValue());
			} else {
				String value = cellConfig.getValue();
				this.excelFactory.setCellValue(cell,
						TextParser.parse(value, context),
						cellConfig.getDataType());
			}
		}
	}	

	public DynamicContent getDynamicContent() {
		return dynamicContent;
	}

	public void addDynamicContent(DynamicContent dynamicContent) {
		this.dynamicContent = dynamicContent;
	}

	public CellData[] getStaticContent() {
		return staticContent;
	}

	public void setStaticContent(CellData[] staticContent) {
		this.staticContent = staticContent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean getDisplayGridlines() {
		return displayGridlines;
	}

	public void setDisplayGridlines(boolean displayGridlines) {
		this.displayGridlines = displayGridlines;
	}	
	
}