/*
 * Created on 2005-5-24
 */
package uncertain.testcase.proc;

import junit.framework.TestCase;
import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.event.Configuration;
import uncertain.ocm.OCManager;
import uncertain.proc.ParticipantRegistry;
import uncertain.proc.Procedure;
import uncertain.proc.ProcedureRunner;

/**
 * ProcedureRunnerTest
 * @author Zhou Fan
 * 
 */
public class ProcedureRunnerTest extends TestCase {
    
    OCManager			oc_manager;
    Procedure			test_proc;
    CompositeMap		proc_config;
    //HandleManager       handle_manager;
    ParticipantTest2    participant;
    Configuration       config;
    CompositeLoader     loader = CompositeLoader.createInstanceForOCM("proc");
    
    public ProcedureRunnerTest(String n){
        super(n);
        oc_manager = new OCManager();
        //oc_manager.addListener(new LoggingListener());
        oc_manager.getClassRegistry().registerPackage("uncertain.proc");
        config = new Configuration( new ParticipantRegistry(), oc_manager);
    }
    
    public void loadProcedure(String procName) throws Exception {
        int id = procName.indexOf(".proc");
        if(id>0)
            procName = procName.substring(0,id);
        /*
        InputStream is = getClass().getClassLoader().getResourceAsStream("uncertain/testcase/proc/"+procName);
        assertNotNull(is);
        proc_config = OCManager.getDefaultCompositeLoader().loadFromStream(is);
        */
        proc_config = loader.loadFromClassPath(this.getClass().getPackage().getName()+"."+procName);
        assertNotNull(proc_config);
        test_proc = (Procedure)oc_manager.createObject(proc_config);
        
    }

    /*
     * Class under test for void run()
     */
    public void testRun() throws Exception {

        loadProcedure("ProcTest.proc");
        participant = new ParticipantTest2(oc_manager);
        config.addParticipant(participant);
        /*
        handle_manager = new HandleManager();        
        handle_manager.addParticipant(participant);        
        */
        assertNotNull(test_proc);
        assertNotNull(test_proc.getEntryList());
        //assertEquals(test_proc.getEntryList().size(),5);
        ProcedureRunner runner = new ProcedureRunner(test_proc);
        runner.addConfiguration(config);
        //assertTrue(runner.locateTo("ActionB1"));
        runner.run();
        assertEquals(participant.getPosition(),6);
        assertTrue(runner.getContext().get("RESULT")==participant);
    }

}
