/*
 * Created on 2009-9-1
 */
package aurora.service;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.event.Configuration;
import uncertain.event.RuntimeContext;
import uncertain.proc.IProcedureManager;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;

public class ServiceInstance implements IService {

    public static final String LOGGING_TOPIC = "aurora.application";
    
    protected IProcedureManager   mProcManager;
    
    protected CompositeMap        mContextMap;
    protected ServiceContext      mServiceContext;
    protected ServiceController   mController;    

    protected Configuration       mRootConfig;
    protected Configuration       mConfig;
    protected CompositeMap        mConfigMap;
    protected ProcedureRunner     mRunner;  
    
    private Object[]            mEventArgs = { this };
    
    public static ServiceInstance getInstance( CompositeMap context ){
        return (ServiceInstance)context.get( RuntimeContext.getTypeKey(IService.class));
    }
    
    public ServiceInstance( String name, IProcedureManager proc_manager ){
       //mConfigMap = config_map;
       mProcManager = proc_manager;  
       CompositeMap context = new CompositeMap("context");
       setContextMap(context);
       setName(name);
    }
    
    
    /** Global participants can do service config population before it is parsed */
    void parseConfig()
    {
        if(mConfig!=null)
            mConfig.clear();
        if(mRootConfig!=null)
            try{
                mRootConfig.fireEvent("PrepareServiceConfig", mEventArgs);
            }catch(Exception ex){
                throw new RuntimeException("Error in event PopulateServiceConfig", ex);
            }
        mConfig = mProcManager.createConfig();
        mConfig.addParticipant(this);
        mConfig.loadConfig(mConfigMap);
        if(mRootConfig!=null)
            mConfig.setParent(mRootConfig);
    }

    public ServiceContext getServiceContext() {
        return mServiceContext;
    }

    public void setServiceContext(ServiceContext context) {
        mServiceContext = context;
    }
    
    void initProcedureRunner( Procedure proc ){
        mRunner = new ProcedureRunner();
        mRunner.setProcedure(proc);
        mRunner.setContext(mContextMap);
        mRunner.setConfiguration(mConfig);
    }

    public void invoke() 
        throws Exception
    {
        mProcManager.initContext( mContextMap );        
        mConfig.fireEvent("DetectProcedure", mEventArgs );
        while( mController.getContinueFlag()){
            String name = mController.getProcedureName();        
            if(name==null){
                //name=DEFAULT_RUNSCREEN_PROCEDURE;
                throw new IllegalStateException("No procedure name set in service context");
            }
            Procedure proc = mProcManager.loadProcedure(name);
            if(proc==null)
                throw new IllegalStateException("Can't load procedure "+name);
            initProcedureRunner(proc);
            mController.setContinueFlag(false);
            mRunner.run();
            if(mRunner.getException()!=null)
                throw new RuntimeException(mRunner.getException());
        };
    }

    public CompositeMap getContextMap() {
        return mContextMap;
    }

    public void setContextMap(CompositeMap contextMap) {
        mContextMap = contextMap;
        mServiceContext = (ServiceContext)DynamicObject.cast(contextMap, ServiceContext.class);
        mController = ServiceController.createServiceController(mContextMap);
        mServiceContext.setInstanceOfType(IService.class, this);
    }

    public CompositeMap getServiceConfigData() {
        return mConfigMap;
    }

    public void setServiceConfigData(CompositeMap configMap) {
        mConfigMap = configMap;
        parseConfig();
    }
    
    public void setName( String name ){
        mContextMap.put( "service_name", name );
    }
    
    public String getName(){
        return mContextMap.getString("service_name");
    }

    public Configuration getServiceConfig() {
        return mConfig;
    }
    
    protected void clearMap( CompositeMap data ){
        if(data!=null)
            data.clear();
    }
    
    public boolean isTraceOn(){
        if(mConfigMap==null)
            return true;
        return mConfigMap.getBoolean("trace", false );
    }
    
    public void clear(){
        mProcManager.destroyContext(mContextMap);
        clearMap(mContextMap);
        clearMap(mConfigMap);
        if(mConfig!=null)
            mConfig.clear();
    }

    public ServiceController getController() {
        return mController;
    }
    
    public ServiceOutputConfig getServiceOutputConfig(){
        CompositeMap child = mConfigMap == null? null:mConfigMap.getChild(ServiceOutputConfig.KEY_SERVICE_OUTPUT);
        if(child==null)
            return null;
        else
            return ServiceOutputConfig.getInstance(child);
    }

    /** Get/Set root Configuration instance that contains global participants */
    public Configuration getRootConfig() {
        return mRootConfig;
    }

    public void setRootConfig(Configuration rootConfig) {
        mRootConfig = rootConfig;
    }

}
