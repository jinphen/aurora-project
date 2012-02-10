/*
 * Created on 2008-6-11
 */
package aurora.database.actions;

import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.ocm.OCManager;
import aurora.database.FetchDescriptor;
import aurora.database.IResultSetConsumer;
import aurora.database.service.BusinessModelService;
import aurora.database.service.BusinessModelServiceContext;
import aurora.database.service.DatabaseServiceFactory;

public class ModelQuery extends AbstractQueryAction {
 
    String                      model;    
    
    DatabaseServiceFactory      svcFactory;
    
    BusinessModelService        service;
    
    BusinessModelServiceContext serviceContext; 

    public ModelQuery( DatabaseServiceFactory  svcFactory, OCManager manager) {
        super(manager);
        this.svcFactory = svcFactory;
    }
    
    public BusinessModelService getService(){
        return service;
    }
    
    public DatabaseServiceFactory getServiceFactory(){
        return svcFactory;
    }

    /**
     * @return the model
     */
    public String getModel() {
        return model;
    }
    
    protected void doQuery( CompositeMap param, IResultSetConsumer consumer, FetchDescriptor desc )
        throws Exception
    {
        service.query(param, consumer, desc);
    }
    
    protected void prepare( CompositeMap context )
        throws Exception
    {
        if(model==null)
            throw new IllegalArgumentException("Must set 'model' property");
        service = svcFactory.getModelService(model, context);
        //service.setTrace(getTrace());
        serviceContext = (BusinessModelServiceContext)DynamicObject.cast(context, BusinessModelServiceContext.class);
    }
    
    protected void cleanUp( CompositeMap context ){
    }

    /**
     * @param model the model to set
     */
    public void setModel(String model) {
        this.model = model;
    }
/*
    public String getRootPath() {
        // TODO Auto-generated method stub
        String path =  super.getRootPath();
        if(path==null)
            return service.getBusinessModel().getBaseTable();
        else
            return path;
    }
*/
}
