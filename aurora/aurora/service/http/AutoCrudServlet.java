/*
 * Created on 2009-9-9 下午01:53:12
 * Author: Zhou Fan
 */
package aurora.service.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import uncertain.composite.CompositeMap;
import aurora.application.config.ScreenConfig;
import aurora.bm.BusinessModel;
import aurora.database.actions.config.ActionConfigManager;
import aurora.database.actions.config.ModelQueryConfig;
import aurora.database.service.ServiceOption;
import aurora.service.IService;
import aurora.service.ServiceContext;
import aurora.service.controller.ControllerProcedures;

public class AutoCrudServlet extends AbstractAutoServiceServlet {
    
    public static final String KEY_UPDATE_PASSED_FIELD_ONLY = "_"+ServiceOption.KEY_UPDATE_PASSED_FIELD_ONLY;
    
    private void prepareUpdateAction(ServiceContext context, CompositeMap action_config ){
        boolean is_update_passed_fields = true;
        if("false".equalsIgnoreCase(context.getParameter().getString(KEY_UPDATE_PASSED_FIELD_ONLY)))
            is_update_passed_fields = false;
        action_config.putBoolean(ServiceOption.KEY_UPDATE_PASSED_FIELD_ONLY, is_update_passed_fields);
        
    }
    
    protected void populateService(HttpServletRequest request,
            HttpServletResponse response, IService service) throws Exception {
        
        HttpServiceInstance svc = (HttpServiceInstance) service;
        svc.getController().setProcedureName(
                ControllerProcedures.INVOKE_SERVICE);
        AutoCrudServiceContext crudSvcContext = AutoCrudServiceContext.createAutoCrudServiceContext(svc.getContextMap());
        String uri = request.getRequestURI();
//        if(uri.startsWith("/")){
//            uri = uri.substring(1);
//        }
//        String[] args = uri.split("/");
//        if (args.length < 4)
//            throw new ServletException("Invalid request format");
//        int start_index = args[0].length() == 0 ? 1 : 0;
//        String object_name = args[start_index + 2];
//        String operation_name = args[start_index + 3];
        
        String[] args = uri.split("/");
		if (args.length < 4) {
			throw new ServletException("Invalid request format");
		}
		int start_index = 0;
		for (int i = 0; i < args.length; i++) {
			String tmp = args[i];
			if ("autocrud".equals(tmp)) {
				start_index = i;
				break;
			}
		}
		String object_name = args[(start_index + 1)];
		String operation_name = args[(start_index + 2)];
        
        BusinessModel bm = mDatabaseServiceFactory.getModelFactory().getModelForRead(object_name);
        if(bm==null)
            throw new ServletException("Can't load model:"+object_name);

        CompositeMap service_config = (CompositeMap) mServiceConfig.clone();
        svc.setName(object_name + "_" + operation_name);
        ScreenConfig screen = ScreenConfig.createScreenConfig(service_config);
        CompositeMap action_config = null;
        /*
        // begin check access
        Configuration config = super.getGlobalServiceConfig();
        if(config!=null){
            Object[] bm_args = new Object[]{ msvc.getBusinessModel(), action_name, svc };
            config.fireEvent(E_CheckBMAccess.EVENT_NAME, bm_args);
        }
        // end
        */
        if ("query".equals(operation_name)) {
            ModelQueryConfig mq = ActionConfigManager
                    .createModelQuery(object_name);
            mq.setParameters(svc.getServiceContext().getParameter());
            action_config = mq.getObjectContext();
            CompositeMap service_output = service_config.getChild("service-output");
            service_output.put("output", "/model/"+mq.getRootPath());
        } else if("update".equals(operation_name)){
            action_config = ActionConfigManager.createModelUpdate(object_name);
            prepareUpdateAction(svc.getServiceContext(), action_config);
        } else if("insert".equals(operation_name)){
            action_config = ActionConfigManager.createModelInsert(object_name);
        } else if("delete".equals(operation_name)){
            action_config = ActionConfigManager.createModelDelete(object_name);
        } else if("batch_update".equals(operation_name)){
            action_config = ActionConfigManager.createModelBatchUpdate(object_name);
            prepareUpdateAction(svc.getServiceContext(), action_config);
        } else if("execute".equals(operation_name)){
            action_config = ActionConfigManager.createModelAction("model-execute", object_name);
        }
        else
            throw new ServletException("Unknown command:"+operation_name);
        screen.getInitProcedureConfig().addChild(0, action_config);
        svc.setServiceConfigData(service_config);
        
        // set autocrud service context flag
        crudSvcContext.setAutoCrudService(true);
        crudSvcContext.setRequestedBM(object_name);
        crudSvcContext.setRequestedOperation(operation_name);
    }

}
