package aurora.plugin.dataimport;

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.core.UncertainEngine;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

import aurora.database.service.SqlServiceContext;
import aurora.plugin.csv.CsvParse;
import aurora.plugin.poi.eventmodel.XLSParse;
import aurora.plugin.poi.usermodel.XLSXParse;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class ImportExcel extends AbstractEntry {
	public static final String XLS_KEY = ".xls";
	public static final String XLSX_KEY = ".xlsx";
	public static final String CSV_KEY = ".csv";
	public static final String TXT_KEY = ".txt";
	public String fileName;
	public String separator = ",";
	public String header_id;
	public String user_id = "${/session/@user_id}";
	public String template_code;
	public String job_id;
	public String attribute1;
	public String attribute2;
	public String attribute3;
	public String attribute4;
	public String attribute5;
	public String dataSourceName;
	UncertainEngine mUncertainEngine;
	Connection conn;
	String lineSql = "fnd_interface_load_pkg.ins_fnd_interface_lines(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	CallableStatement lineCstm = null;
	boolean is_first = true;

	public ImportExcel(UncertainEngine uncertainEngine) {
		mUncertainEngine = uncertainEngine;
	}

	public String getTemplate_code() {
		return template_code;
	}

	public void setTemplate_code(String template_code) {
		this.template_code = template_code;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	public void run(ProcedureRunner runner) throws Exception {
		CompositeMap context = runner.getContext();
		validatePara(context);
		HttpServiceInstance serviceInstance = (HttpServiceInstance) ServiceInstance
				.getInstance(context);
		SqlServiceContext sqlServiceContext = SqlServiceContext
				.createSqlServiceContext(context);

		conn = sqlServiceContext.getNamedConnection(dataSourceName);

		if (conn == null) {
			sqlServiceContext.initConnection(
					mUncertainEngine.getObjectRegistry(), dataSourceName);
			conn = sqlServiceContext.getNamedConnection(dataSourceName);
		}

		saveHead();

		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload up = new ServletFileUpload(factory);
		List items = up.parseRequest(serviceInstance.getRequest());
		Iterator i = items.iterator();
		try {
			while (i.hasNext()) {
				FileItem fileItem = (FileItem) i.next();
				if (!fileItem.isFormField()) {
					fileName = fileItem.getName();
					String suffix = fileName.substring(fileName
							.lastIndexOf("."));
					parseFile(fileItem.getInputStream(), suffix.toLowerCase(),
							this);
				}
			}
			lineCstm.executeBatch();
		} finally {
			if (this.lineCstm != null) {
				try {
					lineCstm.close();
				} catch (Exception e) {

				}
			}
		}
	}

	void saveHead() throws SQLException {
		CallableStatement cstm = null;
		String headSql = "fnd_interface_load_pkg.ins_fnd_interface_headers(?,?,?,?,?,?,?,?,?,?,?)";
		try {
			cstm = conn.prepareCall("{call " + headSql + "}");
			cstm.setLong(1, new Long(header_id));
			if (job_id == null)
				cstm.setNull(2, java.sql.Types.NUMERIC);
			else
				cstm.setLong(2, new Long(job_id));
			cstm.setString(3, "NEW");
			cstm.setString(4, user_id);
			cstm.setString(5, fileName);
			if (template_code == null)
				cstm.setNull(6, java.sql.Types.VARCHAR);
			else
				cstm.setString(6, template_code);
			if (attribute1 == null)
				cstm.setNull(7, java.sql.Types.VARCHAR);
			else
				cstm.setString(7, attribute1);
			if (attribute2 == null)
				cstm.setNull(8, java.sql.Types.VARCHAR);
			else
				cstm.setString(8, attribute2);
			if (attribute3 == null)
				cstm.setNull(9, java.sql.Types.VARCHAR);
			else
				cstm.setString(9, attribute3);
			if (attribute4 == null)
				cstm.setNull(10, java.sql.Types.VARCHAR);
			else
				cstm.setString(10, attribute4);
			if (attribute5 == null)
				cstm.setNull(11, java.sql.Types.VARCHAR);
			else
				cstm.setString(11, attribute5);
			cstm.execute();
		} finally {
			if (cstm != null)
				cstm.close();
		}
	}

	void parseFile(InputStream is, String suffix, ImportExcel importExcel)
			throws Exception {
		if (XLS_KEY.equals(suffix)) {
			XLSParse xlsParse = new XLSParse();
			xlsParse.parseFile(is, importExcel);
		}
		if (XLSX_KEY.equals(suffix)) {
			XLSXParse xlsxParse = new XLSXParse();
			xlsxParse.parseFile(is, importExcel);
		} else if (CSV_KEY.equals(suffix) || TXT_KEY.equals(suffix)) {
			if (separator == null)
				throw new IllegalArgumentException("separator is undefined");
			CsvParse cvsParser = new CsvParse();
			cvsParser.parseFile(is, importExcel);
		}
	}

	void validatePara(CompositeMap context) {
		header_id = TextParser.parse(header_id, context);
		if (header_id == null && "".equals(header_id))
			throw new IllegalArgumentException("header_id is undefined");
		user_id = TextParser.parse(user_id, context);
		if (user_id == null && "".equals(user_id))
			throw new IllegalArgumentException("user_id is undefined");
		template_code = TextParser.parse(template_code, context);
		job_id = TextParser.parse(job_id, context);
		attribute1 = TextParser.parse(attribute1, context);
		attribute2 = TextParser.parse(attribute2, context);
		attribute3 = TextParser.parse(attribute3, context);
		attribute4 = TextParser.parse(attribute4, context);
		attribute5 = TextParser.parse(attribute5, context);
	}

	public void saveLine(CompositeMap data, int rownum) throws SQLException {
		if (data.getLong("maxCell") == null)
			return;
		int maxcell = data.getInt("maxCell");
		boolean is_null = true;

		for (int i = 0; i < maxcell; i++) {
			String valueString = data.getString("C" + i);
			if (valueString != null && !"".equals(valueString)) {
				is_null = false;
				break;
			}
		}
		// 过滤空行
		if (is_null)
			return;
		if (is_first) {
			is_first = false;
			StringBuffer lineSql = new StringBuffer(
					"fnd_interface_load_pkg.ins_fnd_interface_lines(?,?,?,?,?,?,?");
			for (int i = 0; i < maxcell; i++) {
				lineSql.append(",?");
			}
			lineSql.append(")");
			this.lineCstm = conn.prepareCall("{call " + lineSql + "}");
		}

		lineCstm.setLong(1, new Long(header_id));
		lineCstm.setNull(2, java.sql.Types.VARCHAR);
		lineCstm.setNull(3, java.sql.Types.VARCHAR);
		lineCstm.setString(4, user_id);
		lineCstm.setLong(5, rownum);
		lineCstm.setNull(6, java.sql.Types.VARCHAR);
		lineCstm.setNull(7, java.sql.Types.NUMERIC);
		String valueString;
		for (int i = 0; i < maxcell; i++) {
			valueString = data.getString("C" + i);
			if (valueString == null)
				lineCstm.setNull(8 + i, java.sql.Types.VARCHAR);
			else
				lineCstm.setString(8 + i, valueString);
		}
		lineCstm.addBatch();		
	}

	public String getHeader_id() {
		return header_id;
	}

	public void setHeader_id(String header_id) {
		this.header_id = header_id;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getUser_id() {
		return user_id;
	}

	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	public String getJob_id() {
		return job_id;
	}

	public void setJob_id(String job_id) {
		this.job_id = job_id;
	}

	public String getAttribute1() {
		return attribute1;
	}

	public void setAttribute1(String attribute1) {
		this.attribute1 = attribute1;
	}

	public String getAttribute2() {
		return attribute2;
	}

	public void setAttribute2(String attribute2) {
		this.attribute2 = attribute2;
	}

	public String getAttribute3() {
		return attribute3;
	}

	public void setAttribute3(String attribute3) {
		this.attribute3 = attribute3;
	}

	public String getAttribute4() {
		return attribute4;
	}

	public void setAttribute4(String attribute4) {
		this.attribute4 = attribute4;
	}

	public String getAttribute5() {
		return attribute5;
	}

	public void setAttribute5(String attribute5) {
		this.attribute5 = attribute5;
	}
}
