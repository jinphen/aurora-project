/** Perform a JCO function call in CreateModel step
 *  Created on 2006-6-14
 */
package org.lwap.sapplugin;

import java.sql.Array;
import java.sql.Connection;
import java.util.logging.Level;

import org.lwap.controller.MainService;

import uncertain.composite.CompositeMap;
import uncertain.composite.TextParser;
import uncertain.core.ConfigurationError;
import uncertain.logging.ILogger;
import uncertain.logging.LoggingContext;
import uncertain.proc.AbstractEntry;
import uncertain.proc.ProcedureRunner;

import com.sap.mw.jco.IFunctionTemplate;
import com.sap.mw.jco.IRepository;
import com.sap.mw.jco.JCO;
import com.sap.mw.jco.JCO.ParameterList;

public class JcoInvoke extends AbstractEntry {
    
	SapConfig           sapConfig;
    ILogger              logger;

    public Parameter[]  Parameters;
    public Table[]      Tables;
    public Structure[]  Structures;
    public String       Function;
    public String       Sid;
    public String       Return_target;
    public boolean      Dump = false;
  
    public JcoInvoke(SapConfig config){
    	sapConfig = config;
//        System.out.println(this+" constructed ");
    }
    
    public String toString(){
        CompositeMap invoke = new CompositeMap("jco","org.lwap.sapplugin","jco-invoke");
        invoke.put("function", Function);
        invoke.put("return_target", Return_target);
        invoke.put("dump", new Boolean(Dump));

        if(Parameters!=null){
            CompositeMap params = invoke.createChild("parameters");
            for(int i=0; i<Parameters.length; i++)
                params.addChild(Parameters[i].toCompositeMap());
        }

        if(Tables!=null){
            CompositeMap tables = invoke.createChild("tables");
            for(int i=0; i<Tables.length; i++)
                tables.addChild(Tables[i].toCompositeMap());
        }
        return invoke.toXML();
    }
    
    public void run(ProcedureRunner runner) throws Exception {       	
        CompositeMap context = runner.getContext();
        Sid=TextParser.parse(Sid, context);
        InstanceConfig sapInstance=null;
        if(Sid==null){
        	sapInstance=sapConfig.getSapInstance();
        	if(sapInstance==null)
        		throw new IllegalArgumentException("jco-invoke: sid attribute is null");  
        }else{
        	sapInstance=sapConfig.getSapInstance(Sid);
        }
    	
        logger = LoggingContext.getLogger(context, "org.lwap.plugin.sap");
        logger.config("jco-invoke");
        logger.config("===================================");
        logger.log(Level.CONFIG, "config:{0}", new Object[]{this} );
        MainService  service = MainService.getServiceInstance(context.getRoot());
        CompositeMap target = null;
        CompositeMap model = null;
        if(service!=null) model = service.getModel();
        else  model = context.getRoot().getChild("model");
        if(model==null) model = context.getRoot().createChild("model");
        if(Return_target!=null) {
            String t = TextParser.parse(Return_target, context);
            target = (CompositeMap)model.getObject(t);
            if(target==null) target = model.createChildByTag(t);            
        }
        
        IRepository repository= sapInstance.getRepository();
        JCO.Client client = null;
        try {
            // Get a function template from the repository
            IFunctionTemplate ftemplate = repository.getFunctionTemplate(Function);
            logger.info("function template:"+Function);
            // if the function definition was found in backend system
            if(ftemplate != null) {

                // Create a function from the template
                JCO.Function function = ftemplate.getFunction();

                // Get a client from the pool
                // client = JCO.getClient(sapInstance.SID);
                client = sapInstance.getClient();
                
                logger.config("connected to "+sapInstance.SERVER_IP+":"+sapInstance.SID);
                
                JCO.ParameterList input  = function.getImportParameterList();
                JCO.ParameterList output = function.getExportParameterList();
  
                //  String s_client  = input.getStructure("CLIENT");
                // JCO.Structure s_client=null;
                // input.setValue(sapInstance.SAP_CLIENT,"CLIENT");
                if(Parameters!=null)
                for(int i=0; i<Parameters.length; i++){
                    Parameter param = Parameters[i];
                    if(param.Return_field==null){
                        Object o = param.Source_field==null ? param.Value : context.getObject(param.Source_field);
                        String value = o==null?"":o.toString();                                         
                        input.setValue(value,param.Name);
                        logger.log(Level.CONFIG, "parameter {0} -> {1}", new Object[]{ param.Name, value});
                    }
                }
                if(Structures!=null){
                	for(int i=0;i<Structures.length;i++){
                		Structure structure=Structures[i];
                		structure.setLogger(logger);
                		if(structure.isImport()){
                			JCO.Structure stc=structure.getJCOStructure(input);   
                			structure.fillJCOStructure(stc, context);                			
                			input.setValue(stc, structure.Name);                			
                		}
                	}                    
                }
                // Set import table
                if(Tables!=null){
                    ParameterList list = function.getTableParameterList();
                    for(int i=0; i<Tables.length; i++)
                    {
                        Table table = Tables[i];
                        table.setLogger(logger);
                        if(table.isImport()){
                           JCO.Table tbl = table.getJCOTable(list);
                           Object o = context.getObject(table.Source_field);
                           logger.config("transfer import table "+table.Name+" from '"+table.Source_field+"':" + o);
                           if(o instanceof Array)
                        	   table.fillJCOTable(tbl,(Array)o);
                    	   if(o instanceof CompositeMap)
                    		   table.fillJCOTable(tbl, (CompositeMap)context);                                                     
                        }                        
                    }
                }
                
                // Call the remote system and retrieve return value
                logger.config("call function " + Function);
                client.execute(function);

                if(Parameters!=null){
	                for(int i=0; i<Parameters.length; i++){
	                    Parameter param = Parameters[i];
	                    if(param.Return_field!=null){
	                        if(target==null) throw new ConfigurationError("<jco-invoke>:must set 'return_target' attribute if there is return field");
	                        String vl = output.getString(param.Name);
	                        if(vl==null && !param.Nullable) throw new IllegalArgumentException("jco-invoke: return field "+param.Name+" is null");
	                        String f = TextParser.parse(param.Return_field,context);
	                        target.putObject(f, vl);
	                            logger.config("return: "+param.Name+ "=" + vl + " -> "+f);
	                    }
	                }
                }
                if(Structures!=null){
                	for(int i=0;i<Structures.length;i++){
                		Structure structure=Structures[i];
                		structure.setLogger(logger);
                		if(structure.isImport())continue;
                		if(structure.Target==null) throw new ConfigurationError("Must set 'target' attribute for Structures "+structure.Name);
            			JCO.Structure stc=structure.getJCOStructure(output);
            			CompositeMap result = (CompositeMap)context.getObject(structure.Target);
                        if(result==null) result = context.createChildByTag(structure.Target);
            			structure.fillCompositeMap(stc, result); 
                	}
                }
                // Get export tables
                if(Tables!=null){
                    ParameterList list = function.getTableParameterList();
                    if(list==null) throw new IllegalArgumentException("Function '"+Function+"' doesn't return tables");                    
                    for(int i=0; i<Tables.length; i++){
                        Table table = Tables[i];
                        if(table.isImport()) continue;
                        if(table.Target==null) throw new ConfigurationError("Must set 'target' attribute for table "+table.Name);
                        table.setLogger(logger);
                        JCO.Table records = table.getJCOTable(list);                        
                        // Fetch as CompositeMap
                        if(table.isFetchTypeMap()){
                            CompositeMap result = (CompositeMap)context.getObject(table.Target);
                            if(result==null) result = context.createChildByTag(table.Target);
                            table.fillCompositeMap(records, result);
                                int rc = 0;
                                if(result.getChilds()!=null) rc = result.getChilds().size();
                                logger.config("loading export table "+table.Name+" into path '"+table.Target+"', total " + rc + " record(s)");
                        }
                        // Fetch as Array
                        else if(table.isFetchTypeArray()){
                            Connection conn = MainService.getConnection(context);
                            Array array = table.fillArray(records, conn);
                            context.putObject(table.Target, array, true);

                                int rc = 0;                                
                                Object[] r = (Object[])array.getArray();
                                if(r!=null) rc = r.length;
                                logger.config("loading export table "+table.Name+" as " + array +", total " + rc + " record(s)");
                        }
                        else throw new ConfigurationError("Unknown fetch_type for export table:"+table.Fetch_type);
                    }                        
                } 
                // finish
                logger.config("jco invoke finished");
            }
            else {
                throw new IllegalArgumentException("Function '"+Function+"' not found in SAP system.");
            }
        } finally {
            JCO.releaseClient(client);
        }
        
    }
    
}
