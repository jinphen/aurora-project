package aurora.plugin.excelreport;

import java.util.HashMap;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uncertain.composite.CompositeMap;

public class ExcelFactory {
	Map<String, CellStyle> styles;
	public final String KEY_CONTENT = "content";
	public final String KEY_FORMULA = "formula";	

	public void createExcel(CompositeMap context, ExcelReport excelReport)
			throws Exception {
		if(excelReport.getSheets()==null)return;	
		Workbook wb = null;
		if (ExcelReport.KEY_EXCEL2007_SUFFIX.equalsIgnoreCase(excelReport.getFormat()))
			wb = new XSSFWorkbook();
		else
			wb = new HSSFWorkbook();		
		if(excelReport.getStyles()!=null)
			styles = createStyles(wb, excelReport);
		for (SheetWrap sheetObj : excelReport.getSheets()) {
			sheetObj.createSheet(wb, context, this);
		}
		wb.write(excelReport.getOutputStream());		
		excelReport.getOutputStream().close();
	}

	private Map<String, CellStyle> createStyles(Workbook wb,
			ExcelReport excelReport) {		
		Map<String, CellStyle> styles = new HashMap<String, CellStyle>();
		CellStyle style;		
		for (CellStyleWrap cellStyleObj : excelReport.getStyles()) {
			style = cellStyleObj.createStyle(wb);
			styles.put(cellStyleObj.getName(), style);
		}
		return styles;
	}

	public static Row createRow(Sheet sheet, int rownum) {
		Row row = sheet.getRow(rownum - 1);
		if (row == null)
			row = sheet.createRow(rownum - 1);
		return row;
	}
}
