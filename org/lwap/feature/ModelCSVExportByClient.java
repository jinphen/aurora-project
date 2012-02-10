/*
 * Created on 2006-4-7
 */
package org.lwap.feature;

import java.io.Writer;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lwap.controller.AbstractController;
import org.lwap.controller.IController;
import org.lwap.controller.MainService;
import org.lwap.mvc.DataBindingConvention;
import org.lwap.mvc.excel.ExcelDataTable;
import org.lwap.mvc.excel.ExcelExport;

import uncertain.composite.CompositeMap;
import uncertain.composite.CompositeUtil;
import uncertain.composite.DynamicObject;
import uncertain.core.UncertainEngine;
import uncertain.event.Configuration;
import uncertain.event.EventModel;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.proc.IFeature;
import uncertain.proc.ProcedureRunner;

/**
 * ModelCSVExport: Export data from model to CSV format
 * Usage: Add a line directly under <code><service></code>
 * &lt;model-csv-export /&gt; 
 * @author Zhou Fan
 * 
 */
public class ModelCSVExportByClient extends AbstractController implements IFeature {
    
	public static final String KEY_PROMPT = "Prompt";
	public static final String KEY_DATA_INDEX = "DataIndex";
	public static final String KEY_FILE_NAME = "file_name";
	String Parameter_name = "generate_csv";
	public static final String Column_config = "_column_config_";
	String Charset = "gbk";
    
    String	        mRootPath;
    CompositeMap	mQueryActionConfig;
    ILogger          mLogger;
    
    //String  query_name;
    boolean in_generate_state = false;
    
    boolean is_generate = false;
    
    public ModelCSVExportByClient(UncertainEngine engine){
        super(engine);
    }
    
    public int detectAction( HttpServletRequest request, CompositeMap context ){
        return IController.ACTION_NOT_DETECTED;
    }
/*
    public int detectAction( HttpServletRequest request, CompositeMap context ){
        String s = request.getParameter(Parameter_name);
        if("true".equalsIgnoreCase(s))
            in_generate_state = true;
        else
            in_generate_state = false;
        return IController.ACTION_NOT_DETECTED;
    }
  */  
    public void postPrepareService( ProcedureRunner runner ){
        if(ServiceInstance==null)
            ServiceInstance = MainService.getServiceInstance(runner.getContext());
        mLogger = LoggingContext.getLogger(runner.getContext(), "org.lwap.service");
        String s = ServiceInstance.getParameters().getString(Parameter_name);
        if("true".equalsIgnoreCase(s))
            in_generate_state = true;
        else
            in_generate_state = false;
    }
    
    /**
     * Called by framework to get proper procedure name to run
     * @return procedure name
     */
    public String getProcedureName(){
        /*
        if(in_generate_state)
            return ControllerProcedures.BASE_SERVICE;
        else
            return null;
        */
        return null;
    }
    
    public int attachTo(CompositeMap config, Configuration procConfig ){        
        mQueryActionConfig = config;
        mRootPath = config.getString("rootpath");
        if(mRootPath==null ){
            //mLogger.warning("[ModelCSVExportByClient]: 'rootpath' property must be set to a path to dataModel");
            //mLogger.warning(config.toXML());
            return IFeature.NO_FEATURE_INSTANCE;
        }
        else{
            return IFeature.NORMAL;
        }
       
    }
    
    /** disable result set paging */
    public void preDoAction(ProcedureRunner runner){
        if(!in_generate_state) return;
        mQueryActionConfig.putBoolean("fetchall", false);      
		mQueryActionConfig.putBoolean("saveresultset", true);	
		mQueryActionConfig.put("pagesize", new Integer(65535));	
    }
    
    public int preCreateSuccessResponse(ProcedureRunner runner) throws Exception {
    	mLogger.info("ModelCSVExportByClient.....");
        if(!in_generate_state){
            return EventModel.HANDLE_NORMAL;
        }
//   goto ExcelResultSetProcessor.java
//        CompositeMap model = ServiceInstance.getModel();
//        CompositeMap parameter = ServiceInstance.getParameters();
//        ResultSet resultSet = (ResultSet)model.get(mRootPath);
//        if(resultSet==null){
//            mLogger.warning("[ModelCSVExportByClient]: Can't get model from path "+mRootPath);
//            mLogger.warning("Context dump:" + runner.getContext().toXML());
//            return EventModel.HANDLE_NORMAL;
//        }
//        CompositeMap column_config = parameter.getChild(Column_config);
//        if(column_config==null) throw new IllegalArgumentException("Must pass "+column_config+" property to export CSV data");
//        
//        //print csv content to response
//        HttpServletResponse response = ServiceInstance.getResponse();    
//        response.setContentType("application/vnd.ms-excel;charset="+Charset);
//        String fileName=ServiceInstance.getParameters().getString(KEY_FILE_NAME);        
//        response.setHeader("Content-Disposition","attachment; filename=\"" + new String(fileName.getBytes("gb2312"),"ISO8859-1" )+ ".xls\"");        
//        Writer out = response.getWriter();        
//        Iterator it = column_config.getChildIterator();
//        if( it==null ) throw new IllegalArgumentException("No columns defined in "+column_config);
//        while(it.hasNext()){                
//            CompositeMap item = (CompositeMap)it.next();
//            String dataIndex = item.getString(KEY_DATA_INDEX);
//            String prompt = item.getString(KEY_PROMPT);
//            if(prompt!=null) prompt = ServiceInstance.getLocalizedString(prompt);
//            if(dataIndex==null) 
//                throw new IllegalArgumentException("Must specify '"+KEY_DATA_INDEX+"' property in column config "+item.toXML());
//            if(dataIndex.indexOf('@')>=0)
//                item.put(DataBindingConvention.KEY_DATAFIELD, dataIndex);
//            else
//                item.put(DataBindingConvention.KEY_DATAFIELD, "@"+dataIndex);
//            item.put(KEY_PROMPT, prompt);
//        }
////        ExcelDataTable dataTable = (ExcelDataTable)DynamicObject.cast(column_config, ExcelDataTable.class);
//        ExcelExport dataTable=(ExcelExport)DynamicObject.cast(column_config, ExcelExport.class);
////        dataTable.setModel(model);
//        dataTable.setResultSet(resultSet,model);
//        dataTable.setWriter(out);
//        dataTable.setCreateTableHead(true);
//        dataTable.printTable();        
        return EventModel.HANDLE_STOP;
    }
}
