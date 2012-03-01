/*
 * Created on 2008-5-8
 */
package aurora.testcase.database;

import java.sql.Connection;
import java.util.logging.Level;

import javax.sql.DataSource;

import junit.framework.TestCase;
import uncertain.composite.CompositeMap;
import uncertain.composite.DynamicObject;
import uncertain.core.UncertainEngine;
import uncertain.event.Configuration;
import uncertain.logging.LoggerProvider;
import uncertain.logging.LoggingContext;
import uncertain.ocm.IObjectRegistry;
import aurora.database.profile.DatabaseFactory;
import aurora.database.profile.DatabaseProfile;
import aurora.database.profile.IDatabaseFactory;
import aurora.database.profile.ISqlBuilderRegistry;
import aurora.database.service.BusinessModelServiceContext;
import aurora.database.service.DatabaseServiceFactory;
import aurora.database.service.SqlServiceContext;

public abstract class AbstractModelServiceTest extends TestCase {

    protected ConnectionProvider cp;
    protected Connection conn;
    protected UncertainEngine uncertainEngine;
    protected DatabaseServiceFactory svcFactory;
    DatabaseFactory     databaseFactory;

    /**
     * @param arg0
     */
    public AbstractModelServiceTest(String arg0) {
        super(arg0);

    }
    
    protected void createConnection()
        throws Exception
    {
        cp = new ConnectionProvider();
        conn = cp.getConnection();        
    }

    protected void setUp() throws Exception {
        super.setUp();
        createConnection();
        uncertainEngine = new UncertainEngine();
        uncertainEngine.initialize(new CompositeMap());
        IObjectRegistry reg = uncertainEngine.getObjectRegistry();
        reg.registerInstance(DataSource.class, cp);

        databaseFactory = new DatabaseFactory();
        DatabaseProfile prof = new DatabaseProfile("SQL92");
        databaseFactory.addDatabaseProfile(prof);
        databaseFactory.setDefaultDatabase("SQL92");
        assertNotNull(databaseFactory.getDefaultDatabaseProfile());
        ISqlBuilderRegistry sqlreg2 = databaseFactory.getDefaultSqlBuilderRegistry();
        assertNotNull(sqlreg2);
        reg.registerInstance(IDatabaseFactory.class, databaseFactory);
       
        //ServiceInitiator sinit = new ServiceInitiator(uncertainEngine);
        svcFactory = new DatabaseServiceFactory(uncertainEngine);
        reg.registerInstance(svcFactory);
        /*
        svcFactory = (DatabaseServiceFactory) reg
                .getInstanceOfType(DatabaseServiceFactory.class);
        */
        assertNotNull(svcFactory);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (conn != null)
            conn.close();
    }
    
    protected void initConfiguration( Configuration config ){
        
    }

    BusinessModelServiceContext createContext() {
        Configuration rootConfig = uncertainEngine.createConfig();
        initConfiguration(rootConfig);
        CompositeMap context = new CompositeMap("root");
        BusinessModelServiceContext bc = (BusinessModelServiceContext) DynamicObject
                .cast(context, BusinessModelServiceContext.class);
        bc.setConfig(rootConfig);
        bc.setConnection(conn);
        LoggerProvider lp = LoggerProvider.createInstance(Level.FINE,
                System.out);
        LoggingContext.setLoggerProvider(context, lp);
        SqlServiceContext sc = SqlServiceContext
                .createSqlServiceContext(context);
        sc.setTrace(true);
        return bc;
    }
       
}
