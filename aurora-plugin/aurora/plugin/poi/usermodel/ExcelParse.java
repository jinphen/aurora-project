package aurora.plugin.poi.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import uncertain.composite.CompositeMap;

import aurora.plugin.dataimport.ImportExcel;

public class ExcelParse {
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public void parseFile(InputStream is, ImportExcel importProcessor,
			String suffix) throws IOException, SQLException {
		Workbook wb = null;
		if (ImportExcel.XLS_KEY.equalsIgnoreCase(suffix)) {
			wb = new HSSFWorkbook(is);
		} else if (ImportExcel.XLSX_KEY.equalsIgnoreCase(suffix)) {
			wb = new XSSFWorkbook(is);
		}
		Sheet sheet = null;
		for (int i = 0, l = wb.getNumberOfSheets(); i < l; i++) {
			sheet = wb.getSheetAt(i);
			parseFile(sheet, importProcessor);
		}
	}

	void parseFile(Sheet sheet, ImportExcel importProcessor)
			throws SQLException {
		Row row;
		Cell cell;
		CompositeMap record;
		String sheetName = sheet.getSheetName();
		int  l = sheet.getLastRowNum();
		int maxCellNum=sheet.getRow(0).getLastCellNum();
		System.out.println("导入文件sheet("+sheetName+")最后一行是："+(l+1));
		boolean is_write = false;
		boolean is_new=true;
		int indexCount=0;
		for (int i = 0; i <= l; i++) {
			row = sheet.getRow(i);
			if (row == null)
				break;
			record = new CompositeMap("record");
			record.putBoolean("is_new", is_new);
			is_new=false;
			is_write = false;			
			
			if(i==0){
				record.putString("sheetName", "sheetName");	
			}
			else
				record.putString("sheetName", sheetName);			
			for (int j = 0; j < maxCellNum; j++) {
				String value = null;
				cell = row.getCell(j);
				if (cell != null) {
					if (cell.getCellType() == Cell.CELL_TYPE_STRING)
						value = cell.getRichStringCellValue().toString();
					if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
						try {
							Double.parseDouble(cell.toString());
							if(cell.toString().endsWith(".0")){
								Long a=new Double(cell.getNumericCellValue()).longValue();
								value=a.toString();
							}else{
								value = Double.toString(cell.getNumericCellValue());
							}
						} catch (Exception e) {
							if (cell.getDateCellValue() != null)
								value = df.format(cell.getDateCellValue());
						}
					}
					if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN)
						value = Boolean.toString(cell.getBooleanCellValue());
					if (value != null && !"".equalsIgnoreCase(value)) {
						is_write = true;
						record.putString("C" + j, value);
						indexCount++;
					}
				}
			}
			if(i==0&&maxCellNum!=indexCount){
				maxCellNum=indexCount;
			}
			record.putInt("maxCell", maxCellNum);
			if (is_write)
				importProcessor.saveLine(record, i);
		}
	}
}
