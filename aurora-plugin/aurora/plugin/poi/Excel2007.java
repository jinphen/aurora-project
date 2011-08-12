package aurora.plugin.poi;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Excel2007 {	
	private int rowLimit;
	
	private int colLimit;
	
	private String fileExtension; 
	
	private String mimeType;
	
	public Excel2007(){
		rowLimit = (int) Math.pow(2, 20); // 1,048,576
        colLimit = (int) Math.pow(2, 14); // 16,384
        fileExtension = ".xlsx";
        mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";       
	}
	
	public Workbook getNewWorkbook() {
        return new XSSFWorkbook();
    }
	
	public int getRowLimit() {
		return rowLimit;
	}

	public void setRowLimit(int rowLimit) {
		this.rowLimit = rowLimit;
	}

	public int getColLimit() {
		return colLimit;
	}

	public void setColLimit(int colLimit) {
		this.colLimit = colLimit;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
