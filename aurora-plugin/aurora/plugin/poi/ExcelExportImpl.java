package aurora.plugin.poi;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.ss.usermodel.*;

import aurora.i18n.ILocalizedMessageProvider;
import aurora.plugin.export.MergedHeader;
import uncertain.composite.CompositeMap;

public class ExcelExportImpl {	
	ILocalizedMessageProvider localMsgProvider;	
	Workbook wb;
	
	CompositeMap dataModel;
	CompositeMap headerConfig;
	List<CompositeMap> headerList;
	final int numberLimit=65535;
	int headLevel;
	HSSFCellStyle headstyle;	
	public ExcelExportImpl(ILocalizedMessageProvider localMsgProvider){		
		this.localMsgProvider = localMsgProvider;
	}
	public void createExcel(CompositeMap dataModel,CompositeMap column_config,OutputStream os) throws Exception {
		this.dataModel = dataModel;
		headerConfig=(new MergedHeader(column_config)).conifg;		
		wb = new HSSFWorkbook();
		setCellStyle(wb);//设置列style
		createExcel();
		try{
			wb.write(os);
			os.flush();
		}catch (Exception e) {
			throw e;
		}finally{
			try{
				os.close();
			}catch (Exception e) {
				throw e;
			}	
		}
	}
	void setCellStyle(Workbook wb){
		headstyle=(HSSFCellStyle) wb.createCellStyle(); 
		headstyle.getFont(wb).setFontName("宋体");
		headstyle.getFont(wb).setFontHeightInPoints((short)12);
		headstyle=(HSSFCellStyle) wb.createCellStyle(); 
		HSSFFont headfont = (HSSFFont) wb.createFont(); 		
		headfont.setFontName("宋体");
		headfont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//加粗
		headfont.setFontHeightInPoints((short) 12);// 字体大小
		headstyle.setFont(headfont);  
	    headstyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中  
	    headstyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中	
	}
	short getExcelAlign(String align){
		short excelAlign=0;		
		if(align==null||"left".equalsIgnoreCase(align))
			excelAlign=HSSFCellStyle.ALIGN_LEFT;
		else if("right".equalsIgnoreCase(align))
			excelAlign=HSSFCellStyle.ALIGN_RIGHT;
		else if("center".equalsIgnoreCase(align))
			excelAlign=HSSFCellStyle.ALIGN_CENTER;
		return excelAlign;
	}
	void createExcel(){
		Sheet sheet = null;
		Iterator iterator=this.dataModel.getChildIterator();
		if(iterator!=null){		
			while(iterator.hasNext()){
				this.headLevel=0;		
				sheet = wb.createSheet();
				createExcelHeader(sheet);
				createExcelTable(sheet,iterator);				
			}
		}else{
			sheet = wb.createSheet();
			createExcelHeader(sheet);
		}
	}
	
	void createExcelHeader(Sheet sheet){		
		Row header=sheet.createRow(0);
		headerList=new LinkedList<CompositeMap>();
		generatExcelHead(headerConfig,sheet,header,-1);
	}
	
	void createExcelTable(Sheet sheet,Iterator iterator) {
		HSSFCellStyle columnstyle;
		boolean is_setwidth=false;
		this.headLevel++;
		sheet.createFreezePane(0,this.headLevel);// 冻结		
		int col=0;
		String text;
		Cell cell;	
		
		while (iterator.hasNext()) {
			if(this.headLevel==numberLimit){
				break;
			}
			CompositeMap object = (CompositeMap) iterator.next();
			if(!"record".equals(object.getName()))break;
			Row row=sheet.getRow(this.headLevel);
			if(row==null){
				row=sheet.createRow(this.headLevel);
			}
			Iterator it=this.headerList.iterator();
			while (it.hasNext()) {
				cell=row.createCell(col);
				CompositeMap record = (CompositeMap) it.next();				
				text=object.getString(record.getString("name"));					
				columnstyle=(HSSFCellStyle) wb.createCellStyle();
				columnstyle.setAlignment(getExcelAlign(record.getString("align")));
				cell.setCellType(Cell.CELL_TYPE_STRING);
				cell.setCellStyle(columnstyle);
				if(text!=null)
					cell.setCellValue(new HSSFRichTextString(text));					
				if(!is_setwidth){
					int width=record.getInt("width", 100);
					sheet.setColumnWidth(col, (short)(width*35.7));
				}
				col++;
			}
			is_setwidth=true;			
			col=0;			
			this.headLevel++;
		}
		
	}

	String getPrompt(String key){		
		String promptString=localMsgProvider.getMessage(key);
		promptString=promptString==null?key:promptString;
		return promptString;
	}
	
	
   /** 
 	*    合并单元格 
	*    第一个参数：第一个单元格的行数（从0开始） 
	*    第二个参数：第二个单元格的行数（从0开始） 
	*    第三个参数：第一个单元格的列数（从0开始） 
	*    第四个参数：第二个单元格的列数（从0开始） 
    */
	int generatExcelHead(CompositeMap columnConfigs,Sheet sheet,Row header,int col) {
		CompositeMap record;	
		Long span;
		int level;
		String title;
		int rownum=header.getRowNum();		
		Iterator iterator=columnConfigs.getChildIterator();
		if(iterator!=null){
			while (iterator.hasNext()) {
				col++;						
				record = (CompositeMap) iterator.next();
				title=getPrompt(record.getString("prompt"));
				Cell cell=header.createCell(col);		
				cell.setCellValue(new HSSFRichTextString(title));
				cell.setCellStyle(this.headstyle);	
				level=record.getInt("_level",0);
				if(this.headLevel==0)
					this.headLevel=level;
				Iterator it=record.getChildIterator();				
				if(it!=null){
					span=(Long)record.getObject("column/@_count");					
					CellRangeAddress range = new CellRangeAddress(rownum,rownum,col,col+span.intValue()-1);
					sheet.addMergedRegion(range);				
					while (it.hasNext()) {
						Row nextRow=sheet.getRow(rownum+1);
						if(nextRow==null)						
							nextRow=sheet.createRow(rownum+1);
						CompositeMap object = (CompositeMap) it.next();
						col=generatExcelHead(object,sheet,nextRow,col-1);
					}					
				}else{
					this.headerList.add(record);					
					if(level!=0){
						CellRangeAddress range = new CellRangeAddress(rownum,rownum+level,col,col);
						sheet.addMergedRegion(range);
					}
				}							
			}
		}
		return col;
	}	
}
