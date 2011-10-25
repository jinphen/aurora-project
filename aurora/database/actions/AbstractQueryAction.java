/*
 * Created on 2008-6-11
 */
package aurora.database.actions;

import java.util.List;

import org.lwap.controller.WSDLBinaryProcessor;
import org.lwap.feature.ExcelResultSetProcessor;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.composite.transform.Transformer;
import uncertain.ocm.OCManager;
import uncertain.proc.AbstractDeferredEntry;
import uncertain.proc.ProcedureRunner;
import aurora.database.CompositeMapCreator;
import aurora.database.FetchDescriptor;
import aurora.database.IResultSetConsumer;
import aurora.database.service.ServiceOption;
import aurora.database.service.SqlServiceContext;

public abstract class AbstractQueryAction  extends AbstractDeferredEntry {
    
    String      mode = ServiceOption.MODE_FREE_QUERY;
    String      parameter;
    boolean     fetchAll = false;
    boolean     trace = false;
    boolean     autoCount = false;
    boolean     saveResultSet = false;
    boolean 	outputSOAP=false;

    Integer     pageSize;
    String      fieldNameCase = "unassigned";
    byte        fieldNameCaseValue = Character.UNASSIGNED;

    String      rootPath;
    String      recordName;
    List        transform_list;
    
    protected abstract void doQuery( CompositeMap param, IResultSetConsumer consumer, FetchDescriptor desc ) throws Exception ;
    
    protected abstract void prepare( CompositeMap context_map ) throws Exception ;
    
    protected abstract void cleanUp( CompositeMap context_map );
    
    public AbstractQueryAction( OCManager oc_manager ){
        super(oc_manager);
    }
    
    public void query( CompositeMap context_map ) throws Exception
    {
        super.doPopulate();
        prepare( context_map );
        SqlServiceContext context = (SqlServiceContext)DynamicObject.cast(context_map, SqlServiceContext.class);
        //context.setTrace(trace);
        
        ServiceOption option = ServiceOption.createInstance();
        option.setQueryMode(mode);
        option.setAutoCount(autoCount);
        context.setServiceOption(option);
        
        IResultSetConsumer consumer = null;
        CompositeMapCreator compositeCreator = null;
        try{
            // get parameter
            CompositeMap param = context.getCurrentParameter();
            if(parameter!=null){
                Object obj = param.getObject(parameter);
                if(obj!=null){
                    if(! (obj instanceof CompositeMap))
                        throw new IllegalArgumentException("query parameter should be instance of CompositeMap, but actually got "+obj.getClass().getName());
                    param = (CompositeMap)obj;                    
                }  
            }
            
            // page settings
            FetchDescriptor desc = FetchDescriptor.createFromParameter(context.getParameter());
            desc.setFetchAll(fetchAll);
            if(pageSize!=null)
                desc.setPageSize(pageSize.intValue());
            
            // ResultSet consumer
            consumer = (IResultSetConsumer)context.getInstanceOfType(IResultSetConsumer.class);            
            if(consumer==null){
                
                // -- zhoufan 2011-07-12
                // check if save resultset
                if(getSaveResultSet()){
                    consumer = new ExcelResultSetProcessor(context_map);                    
                }else if(getOutputSOAP()){
                	consumer = new WSDLBinaryProcessor(context_map,rootPath);   
                }else{
                    CompositeMap result = context.getModel();
                    if(rootPath!=null) result = result.createChildByTag(rootPath);
                    compositeCreator = new CompositeMapCreator(result);
                    consumer = compositeCreator;
                }
            }
            context.setResultsetConsumer(consumer);
            doQuery(param, consumer, desc);
            if( transform_list != null && compositeCreator != null ) 
                Transformer.doBatchTransform( compositeCreator.getCompositeMap(), transform_list );
        }finally{
            context.setServiceOption(null);
            context.setResultsetConsumer(null);
            cleanUp( context_map );
        }        
    }

    public void run(ProcedureRunner runner) throws Exception {
        query(runner.getContext());
    }

    /**
     * @return the mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * @return the parameter
     */
    public String getParameter() {
        return parameter;
    }

    /**
     * @param parameter the parameter to set
     */
    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    /**
     * @return the recordName
     */
    public String getRecordName() {
        return recordName;
    }

    /**
     * @param recordName the recordName to set
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    /**
     * @return the rootPath
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * @param rootPath the rootPath to set
     */
    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    public void addTransformList( CompositeMap tlist ){
        transform_list = tlist.getChilds();
    }

    /**
     * @return the fetchAll
     */
    public boolean getFetchAll() {
        return fetchAll;
    }

    /**
     * @param fetchAll the fetchAll to set
     */
    public void setFetchAll(boolean fetchAll) {
        this.fetchAll = fetchAll;
    }

    /**
     * @return the pageSize
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize the pageSize to set
     */
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @return the trace
     */
    public boolean getTrace() {
        return trace;
    }

    /**
     * @param trace the trace to set
     */
    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    /**
     * @return the autoCount
     */
    public boolean getAutoCount() {
        return autoCount;
    }

    /**
     * @param autoCount the autoCount to set
     */
    public void setAutoCount(boolean autoCount) {
        this.autoCount = autoCount;
    }

    public String getFieldNameCase() {
        return fieldNameCase;
    }

    public void setFieldNameCase(String fieldNameCase) {
        this.fieldNameCase = fieldNameCase;
        if("upper".equalsIgnoreCase(fieldNameCase))
            fieldNameCaseValue = Character.UPPERCASE_LETTER;
        else if("lower".equalsIgnoreCase(fieldNameCase))
            fieldNameCaseValue = Character.LOWERCASE_LETTER;
        else if("unassigned".equalsIgnoreCase(fieldNameCase))
            fieldNameCaseValue = Character.UNASSIGNED;            
    }
    
    protected byte getFieldNameCaseValue(){
        return fieldNameCaseValue;
    }
    
    public boolean getSaveResultSet() {
        return saveResultSet;
    }

    public void setSaveResultSet(boolean saveResultSet) {
        this.saveResultSet = saveResultSet;
    }
    
    public boolean getOutputSOAP(){
    	return this.outputSOAP;
    }
    
    public void setOutputSOAP(boolean outputSOAP){
    	this.outputSOAP=outputSOAP;
    }

}
