package aurora.plugin.export.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import uncertain.composite.CompositeMap;
import uncertain.composite.JSONAdaptor;
import uncertain.core.UncertainEngine;
import uncertain.exception.BuiltinExceptionFactory;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.util.LoggingUtil;
import aurora.application.action.DoDispatch;
import aurora.service.IService;
import aurora.service.ServiceContext;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceFactory;
import aurora.service.http.HttpServiceInstance;
import aurora.service.http.WebContextInit;
import aurora.service.validation.ErrorMessage;


//报表下载和删除等管理操作的网页访问
public class TaskReportServlet extends HttpServlet {

	private static final long serialVersionUID = -8531728996484927927L;
	public static final String DEFAULT_JSON_CONTENT_TYPE = "application/json;charset=utf-8";
	public final String KEY_CHARSET = "GBK";
	public final static String EXECL_2003_EXTENSION = ".xls";
	public final static String EXECL_2007_EXTENSION = ".xlsx";

	private HttpServiceFactory mServiceFactory;
	private IObjectRegistry registry;

	private String reportDir;
	private IProcedureManager procedureManager;
	private Procedure pre_service_proc;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = config.getServletContext();
		UncertainEngine uncertainEngine = WebContextInit.getUncertainEngine(context);
		if (uncertainEngine == null)
			throw new ServletException("Uncertain engine not initialized");

		registry = uncertainEngine.getObjectRegistry();
		if (registry == null)
			throw new ServletException("IObjectRegistry not initialized");

		mServiceFactory = (HttpServiceFactory) registry.getInstanceOfType(HttpServiceFactory.class);
		if (mServiceFactory == null)
			throw new ServletException("No ServiceFactory instance registered in UncertainEngine");
		procedureManager = (IProcedureManager) registry.getInstanceOfType(IProcedureManager.class);
		if (procedureManager == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IProcedureManager.class, this.getClass().getName());
		IReportTask reportTaskConfig = (IReportTask) registry.getInstanceOfType(IReportTask.class);
		if (reportTaskConfig == null)
			throw BuiltinExceptionFactory.createInstanceNotFoundException(null, IReportTask.class, this.getClass().getCanonicalName());
		reportDir = reportTaskConfig.getReportDir();
		CompositeMap accessChecker = reportTaskConfig.getAccessChecker();
		pre_service_proc = procedureManager.createProcedure(accessChecker);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String fileName = request.getParameter("fileName");
		if (fileName == null) {
			HttpServiceInstance svc = mServiceFactory.createHttpService("emptyFileName", request, response, this);
			ErrorMessage message = new ErrorMessage(null, "This parameter 'fileName' is null, please check it.", null);
			svc.getServiceContext().setError(message.getObjectContext());
			onCreateFailResponse(response, svc.getContextMap(), null);
			return;
		}
		String fileNameDesc = request.getParameter("fileNameDesc");
		if (fileNameDesc == null)
			fileNameDesc = fileName;
		HttpServiceInstance svc = mServiceFactory.createHttpService(fileName, request, response, this);
		try {
			boolean is_success = checkPreService(fileName, svc);
			if (is_success) {
				String operation = getOperation(request);
				File reportFile = new File(reportDir, fileName);
				if ("download".equals(operation)) {
					downLoad(response, svc, reportFile, fileNameDesc);
				} else if ("delete".equals(operation)) {
					delete(response, svc, reportFile);
				} else {
					ErrorMessage message = new ErrorMessage(null, "This operation:" + operation + " is not support!", null);
					svc.getServiceContext().setError(message.getObjectContext());
					onCreateFailResponse(response, svc.getContextMap(), null);
				}
			} else {
				onCreateFailResponse(response, svc.getContextMap(), null);
			}
		} finally {
			svc.release();
			cleanUp(svc);
		}
	}

	protected boolean checkPreService(String fileName, HttpServiceInstance svc) {
		if (svc == null)
			throw new IllegalArgumentException("HttpServiceInstance can not be null");
		if (!validateFileExtension(fileName)) {
			ErrorMessage message = new ErrorMessage(null, "This file '" + fileName + "' is not an report file!", null);
			svc.getServiceContext().setError(message.getObjectContext());
			return false;
		}
		boolean is_success = true;
		if (pre_service_proc != null) {
			try {
				is_success = svc.invoke(pre_service_proc);
			} catch (Exception e) {
				is_success = false;
			}
		}
		if (svc.getServiceContext().hasError())
			is_success = false;
		return is_success;
	}

	// just for excel
	private boolean validateFileExtension(String fileName) {
		if (fileName == null)
			return false;
		return fileName.toLowerCase().endsWith(EXECL_2003_EXTENSION) || fileName.toLowerCase().endsWith(EXECL_2007_EXTENSION);
	}

	protected String getOperation(HttpServletRequest request) {
		if (request == null)
			throw new IllegalArgumentException("HttpServletRequest can not be null");
		String uri = request.getRequestURI();
		int begin = uri.lastIndexOf("/") + 1;
		String operation = uri.substring(begin);
		return operation;
	}

	protected void cleanUp(IService svc) {
		((ServiceInstance) svc).clear();
	}

	protected void downLoad(HttpServletResponse response, HttpServiceInstance svc, File file, String fileNameDesc) throws IOException {
		if (svc == null)
			throw new IllegalArgumentException("HttpServiceInstance can not be null");
		if (!file.exists()) {
			ErrorMessage message = new ErrorMessage(null, "This file not exits or has been removed, please check it!", null);
			svc.getServiceContext().setError(message.getObjectContext());
			onCreateFailResponse(response, svc.getContextMap(), null);
			return;
		}
		// just for excel
		response.setContentType("application/vnd.ms-excel");
		// response.setCharacterEncoding(KEY_CHARSET);
		String isoFileNameDesc = new String(fileNameDesc.getBytes(), "ISO-8859-1");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + isoFileNameDesc + "\"");

		OutputStream os = response.getOutputStream();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] bbuf = new byte[1024];
			int length;
			while ((length = fis.read(bbuf)) != -1) {
				os.write(bbuf, 0, length);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (os != null) {
				try {
					os.flush();
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void delete(HttpServletResponse response, HttpServiceInstance svc, File file) throws IOException {
		if (svc == null)
			throw new IllegalArgumentException("HttpServiceInstance can not be null");
		ILogger logger = LoggingContext.getLogger(svc.getContextMap(), this.getClass().getCanonicalName());
		if (file.exists()) {
			boolean is_success = file.delete();
			if (!is_success) {
				ErrorMessage message = new ErrorMessage(null, "Can not delete this file!", null);
				svc.getServiceContext().setError(message.getObjectContext());
				onCreateFailResponse(response, svc.getContextMap(), null);
				return;
			}
		}
		JSONObject json = new JSONObject();
		try {
			json.put("success", "true");
			PrintWriter out = svc.getResponse().getWriter();
			json.write(out);
		} catch (JSONException e) {
			LoggingUtil.logException(e, logger);
			ErrorMessage message = new ErrorMessage(null, e.getMessage(), null);
			svc.getServiceContext().setError(message.getObjectContext());
			onCreateFailResponse(response, svc.getContextMap(), e);
		}
	}

	public void onCreateFailResponse(HttpServletResponse response, CompositeMap context, Throwable thr) throws IOException {
		prepareResponse(response);
		ServiceContext servieContext = ServiceContext.createServiceContext(context);
		ILogger logger = LoggingContext.getLogger(servieContext.getObjectContext(), this.getClass().getCanonicalName());
		if (thr != null)
			LoggingUtil.logException(thr, logger);
		String url = context.getString("dispatch_url");
		if (url != null) {
			DoDispatch dispath = new DoDispatch();
			try {
				dispath.onDoDispatch(servieContext);
			} catch (Exception e) {
				LoggingUtil.logException(e, logger);
			}
		} else {
			try {
				JSONObject json = new JSONObject();
				json.put("success", false);
				CompositeMap error_map = servieContext.getError();
				if (error_map != null) {
					JSONObject err = JSONAdaptor.toJSONObject(error_map);
					json.put("error", err);
				}
				PrintWriter out = response.getWriter();
				json.write(out);
			} catch (JSONException e) {
				LoggingUtil.logException(e, logger);
			}
		}
	}

	private void prepareResponse(HttpServletResponse response) {
		response.setContentType(DEFAULT_JSON_CONTENT_TYPE);
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		// response.setHeader("Pragma", "no-cache");
		// ie https
		response.setHeader("pragma", "public");
		response.setHeader("Expires", "0");
	}
}
