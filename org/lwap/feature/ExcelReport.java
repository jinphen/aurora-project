/*
 * Created on 2005-12-9
 */
package org.lwap.feature;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import org.lwap.application.NoPrivilegeException;
import org.lwap.controller.MainService;
import org.lwap.database.DBUtil;
import org.lwap.database.c3p0.C3P0NativeJdbcExtractor;
import org.lwap.database.c3p0.TestC3P0;
import org.lwap.database.oracle.BlobUtil;
import org.lwap.mvc.excel.ExcelDataTable;

import com.mchange.v2.c3p0.C3P0ProxyConnection;

import uncertain.composite.CompositeMap;
import uncertain.event.Configuration;
import uncertain.event.EventModel;
import uncertain.logging.ILogger;
import uncertain.proc.IFeature;
import uncertain.proc.ProcedureRunner;

/**
 * ExcelReport
 * @author Zhou Fan
 * 
 */
public class ExcelReport  implements IFeature{
    
    public static final String EXCEL_REPORT_SESSION = "excel_report_session";
    public static final String SQL_PREPARE_EXCEL_QUERY = "PrepareExcelQuery.data";
    public static final String SQL_EXCEL_REPORT_PARAM_QUERY = "ExcelReportParamQuery.data";
    public static final String SQL_GET_SID = "GetSessionIDFromESID.data";

    public static final String LOGGING_TOPIC = "org.lwap.feature.excelreport";

	public static final String KEY_IS_GENERATE_SCRIPT = "IS_GENERATE_SCRIPT";	

	public static final String KEY_REPORT_NAME = "REPORT_NAME";
	public static final String KEY_SESSION_ID_STRING = "SESSIONIDSTR";
	public static final String KEY_VIEW_ID = "ViewID";
	public static final String KEY_SESSION_ID = "session_id";
	public static final String KEY_URL_PARAM = "URL_PARAM";
	
	ILogger			logger;
	
    Object			session_id;
    String  		session_id_str;
    MainService 	service;
    CompositeMap	context;
    CompositeMap	report_config;
    
	String 		view_id;
	boolean 	is_generate_script = true; 
    // whether this report is called from external sources other than web application
    boolean     is_called_external = false;
    
    public void postParseParameter() throws Exception {
        
        if(!is_called_external) return;
        
        CompositeMap param = service.getParameters();
        HttpServletRequest request = service.getRequest();
        
        
        String sid = request.getParameter(KEY_SESSION_ID_STRING);
        if( sid != null) param.put(KEY_SESSION_ID_STRING, sid);
        param.put(KEY_REPORT_NAME,  service.getServiceName());
        
        
        view_id  = request.getParameter(KEY_VIEW_ID);
        is_generate_script = view_id == null;
        
        CompositeMap    param_def = new CompositeMap();
        service.databaseAccess(SQL_EXCEL_REPORT_PARAM_QUERY, param, param_def);
        
        CompositeMap tmpMap = param_def.getChild("SID");
        session_id =  tmpMap.get("SESSION_ID");
        if( tmpMap != null) param.put(KEY_SESSION_ID, session_id);
        
        tmpMap = param_def.getChild("param-list");
        if( tmpMap != null){
            Iterator it = tmpMap.getChildIterator();
            if( it == null) return;
            while( it.hasNext()){
                CompositeMap item = (CompositeMap)it.next();
                String name = item.getString("PARAM_NAME");
                String type = item.getString("DATA_TYPE");
                Object value = item.get(type +"_VALUE");
                param.put(name,value);
            }
        }
        
    }    
	
    // get session_id & session_id_str
    public void onBeginService(ProcedureRunner runner) 
    throws Exception {
		
        context = runner.getContext();
		//logger = LoggingContext.getLogger(context, LOGGING_TOPIC);
        logger = runner.getLogger();

		service = MainService.getServiceInstance(context);
        session_id = context.getObject("/session/@session_id");
        session_id_str = service.getRequest().getParameter(KEY_SESSION_ID_STRING);
		CompositeMap params = service.getParameters(), model = service.getModel();
        is_generate_script = !ExcelDataTable.isGenerateData(service.getRequest());        
        		
		if(session_id==null){
		    logger.config("${/session/@session_id} is null");
		    if(session_id_str==null){
		        logger.config(KEY_SESSION_ID_STRING+" from parameter is null, throwing exception");
		        throw new NoPrivilegeException();
		    }
            if(is_generate_script){
                // requested from other source, parameters saved in database
                logger.config("set to generate script mode");
                is_called_external = true;
            }else{
                // requested from excel, get session_id from request parameter
                logger.config("set to generate report data mode");
    		    params = service.getParameters();
    		    params.put(KEY_SESSION_ID_STRING, session_id_str);
    		    logger.config("to invoke "+SQL_GET_SID);
    		    service.databaseAccess(SQL_GET_SID, params, model);
    		    session_id = model.getObject("EXCEL-REPORT-SESSION/@SESSION_ID");
                if(session_id==null){
                    logger.config("EXCEL-REPORT-SESSION/@SESSION_ID is null, can't get session_id, throwing exception");
                    throw new NoPrivilegeException();
                }
            }
            
		}
        /*
        else if(){
            
        }
        */
		//  requested from browser, get session_id from session
		else{
		    logger.config("request from browser, to invoke " + SQL_PREPARE_EXCEL_QUERY );
		    service.databaseAccess(SQL_PREPARE_EXCEL_QUERY, params, model );
		    Object o = model.getObject("EXCEL-REPORT-SESSION/@ENCODED_SESSION_ID");
		    if(o==null){
		        logger.warning("Can't get session_id from EXCEL-REPORT-SESSION/@ENCODED_SESSION_ID, throwing exception");		        
		        throw new IllegalStateException("Can't get encoded session id");
		    }
		    session_id_str = (String)o;	
		}

		service.getRequest().setAttribute(KEY_IS_GENERATE_SCRIPT, new Boolean(is_generate_script) );
    }
    
	public int onParseParameter(ProcedureRunner runner){
	    // don't parse parameter if in generate data mode
	    if(!is_generate_script){
	        return EventModel.HANDLE_STOP;
	    }
        return EventModel.HANDLE_NORMAL;
	}    
    
    public int attachTo(CompositeMap config, Configuration procConfig) {
        report_config = config;
        return IFeature.NORMAL;
    }	
	
	public static boolean isGenerateScript( HttpServletRequest request){
		Boolean b = (Boolean) request.getAttribute(KEY_IS_GENERATE_SCRIPT);
		if( b == null) return true;
		return b.booleanValue();
	}

	public int onCreateModel(ProcedureRunner runner) throws Exception {
        int result = EventModel.HANDLE_NO_SAME_SEQUENCE;
	    Connection conn = null;
	    Connection nativeConn=null;
		HttpServletRequest request = service.getRequest();
		MainService svc = MainService.getServiceInstance(runner.getContext());
		String excel_table = svc.getApplicationConfig().getString("excel-report-table", EXCEL_REPORT_SESSION);
		String method = request.getMethod();		
		try{
			conn = service.getConnection();
			nativeConn=conn;
			if(conn instanceof C3P0ProxyConnection){
	        	C3P0NativeJdbcExtractor nativeJdbcExtractor=new C3P0NativeJdbcExtractor();
	        	try {
	        		nativeConn=nativeJdbcExtractor.getNativeConnection(conn);
				} catch (Exception e) {
					throw new SQLException(e);			
				}			
	        }
			if( is_generate_script){
			    logger.config("to save model data into database");
				service.createModel();
				CompositeMap model = service.getModel();
				if( model == null) {
				    logger.warning("model is null, no data generated");
				    return result;
				}
				CompositeMap parent = model.getParent();
				model.setParent(null);				
				BlobUtil.saveObject(nativeConn,excel_table,"report_data","session_id=" + session_id,model);
				nativeConn.commit();
				model.setParent(parent);
				logger.config("model data saved in "+excel_table+", session_id="+session_id);
			} else{
			    logger.config("to fetch data from database and transport to client in CSV format");				
				CompositeMap model = 
				(CompositeMap)BlobUtil.loadObject(nativeConn,"select report_data from " + excel_table  + " s where s.session_id = " + session_id);
				if( model != null){
				    logger.config("data fetched from "+excel_table+".report_data, session_id="+session_id);
					CompositeMap context = service.getServiceContext();
					CompositeMap m = service.getModel();
					if(m==null)
					    context.addChild( model);
					else
					    context.replaceChild(m,model);
				}else{
				    logger.warning("model loaded from DB is null, session_id="+session_id);					    
				}

			}
		} finally {
			DBUtil.closeConnection(conn);
		}
        return result;
	}

	public void preCreateView(){
	    service.getServiceConfig().put("content-type",getContentType());
	    service.getRequest().setAttribute(KEY_SESSION_ID_STRING, session_id_str);
		}
	

	/**
	 * @see org.lwap.application.BaseService#getContentType()
	 */
	public String getContentType() {
		if( is_generate_script)
			return "text/html;charset=gbk";
		else
			return "text/plain;charset=gbk";
	}

    public ExcelReport() {
    }    
}
