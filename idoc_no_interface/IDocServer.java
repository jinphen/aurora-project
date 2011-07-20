package aurora.plugin.sap.sync.idoc;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;

import uncertain.logging.ILogger;

import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDocHandlerFactory;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerTIDHandler;
public class IDocServer {
	public JCoIDocServer iDocServer;
	private String serverName;
	private DataBaseUtil dbUtil;
	public LinkedList syncFils = new LinkedList();
	public LinkedList backupFils = new LinkedList();
	private IDocServerInstance iDocServerInstance;
	private ILogger logger;
	private int server_id = -1;

	public IDocServer(IDocServerInstance iDocServerInstance, String serverName) {
		this.iDocServerInstance = iDocServerInstance;
		this.serverName = serverName;
	}
	public void start() {
		logger = iDocServerInstance.getLogger();
		log("get database connection for " + serverName);
		try {
			dbUtil = new DataBaseUtil(iDocServerInstance.getRegistry(), logger);
		} catch (ApplicationException e1) {
			handleException(e1);
		}
		try {
			// see provided examples of configuration files MYSERVER.jcoServer
			// and BCE.jcoDestination
			log("begin start IDocServer " + serverName + "...");
			iDocServer = JCoIDoc.getServer(serverName);
			log("get HistoryIdocs " + iDocServer.getProgramID());
			dbUtil.getHistoryIdocs(iDocServer.getProgramID(), syncFils);
		} catch (JCoException e) {
			handleException(serverName + " is not valid.", e);
		} catch (SQLException e) {
			handleException(" get HistoryIdocs " + iDocServer.getProgramID() + " failure!", e);
		} catch (ApplicationException e) {
			handleException(e);
		}
		MyIDocHandlerFactory idocHanlerFactory = new MyIDocHandlerFactory();
		iDocServer.setIDocHandlerFactory(idocHanlerFactory);
		iDocServer.setTIDHandler(new MyTidHandler());

		MyThrowableListener listener = new MyThrowableListener();
		iDocServer.addServerErrorListener(listener);
		iDocServer.addServerExceptionListener(listener);
		log("getConnectionCount is "+iDocServer.getConnectionCount() );
		if(iDocServer.getConnectionCount()==0){
			iDocServer.setConnectionCount(1);
		}
		try {
			server_id = dbUtil.registerSapServers(iDocServer);
			log("get server_id " + server_id);
			iDocServer.start();
			log("idocServer's status is " + iDocServer.getState());
			if (!JCoServerState.ALIVE.equals(iDocServer.getState())
					&& !JCoServerState.STARTED.equals(iDocServer.getState())) {
				log("unRegisterSapServers server_id " + server_id);
				if(dbUtil.getConnection()!=null&&!dbUtil.getConnection().isClosed()){
					dbUtil.unRegisterSapServers(server_id);
					dbUtil.dispose();
				}
				return;
			}
			log("start IDocSync thread.. ");
			IDocSync sync = new IDocSync(this);
			sync.start();
			log("start backup thread.. ");
			IDocBackup backup = new IDocBackup(this);
			backup.start();
		} catch (SQLException e) {
			handleException(e);
		}
		log("IDocServer " + serverName + " start finished.");
	}
	public DataBaseUtil getDbUtil() {
		return dbUtil;
	}
	public void setDbUtil(DataBaseUtil dbUtil) {
		this.dbUtil = dbUtil;
	}
	class MyIDocHandler implements JCoIDocHandler {
		public void handleRequest(JCoServerContext serverCtx, IDocDocumentList idocList) {
			FileOutputStream fos = null;
			OutputStreamWriter osw = null;
			try {
				IDocXMLProcessor xmlProcessor = JCoIDoc.getIDocFactory().getIDocXMLProcessor();
				String fileName=serverCtx.getTID() + "_idoc.xml";
				String filePath = iDocServerInstance.getIdocDir() + File.separator + fileName;
				log("receive idoc " + fileName);
				fos = new FileOutputStream(filePath);
				osw = new OutputStreamWriter(fos, "UTF8");
				xmlProcessor.render(idocList, osw, IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF);
				osw.flush();
				int idoc_id = dbUtil.addIdoc(server_id, filePath);
				log("add idoc_id " + idoc_id);
				addSyncFile(new IDocFile(filePath, idoc_id, server_id));
			} catch (Throwable thr) {
				handleException(thr);
			} finally {
				try {
					if (osw != null)
						osw.close();
					if (fos != null)
						fos.close();
				} catch (IOException e) {
					handleException(e);
				}
			}
		}
	}
	public synchronized void addSyncFile(IDocFile file) {
		syncFils.addLast(file);
	}
	public synchronized IDocFile getSyncFile() {
		if (syncFils.size() <= 0) {
			return null;
		}
		IDocFile file = (IDocFile) syncFils.getFirst();
		syncFils.remove(0);
		return file;
	}
	public synchronized void addBackupFile(IDocFile file) {
		backupFils.addLast(file);
	}
	public synchronized IDocFile getBckupFile() {
		if (backupFils.size() <= 0) {
			return null;
		}
		IDocFile file = (IDocFile) backupFils.getFirst();
		backupFils.remove(0);
		return file;
	}

	class MyIDocHandlerFactory implements JCoIDocHandlerFactory {
		private JCoIDocHandler handler = new MyIDocHandler();
		public JCoIDocHandler getIDocHandler(JCoIDocServerContext serverCtx) {
			return handler;
		}
		public JCoIDocHandler getIDocHandler() {
			return handler;
		}
	}

	class MyThrowableListener implements JCoServerErrorListener, JCoServerExceptionListener {

		public void serverErrorOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx, Error error) {
			handleException(">>> Error occured on " + server.getProgramID() + " connection " + connectionId, error);
		}
		public void serverExceptionOccurred(JCoServer server, String connectionId, JCoServerContextInfo ctx,
				Exception error) {
			handleException(">>> Exception occured on " + server.getProgramID() + " connection " + connectionId, error);
		}
	}

	class MyTidHandler implements JCoServerTIDHandler {
		public boolean checkTID(JCoServerContext serverCtx, String tid) {
			log("checkTID called for TID=" + tid);
			return true;
		}

		public void confirmTID(JCoServerContext serverCtx, String tid) {
			log("confirmTID called for TID=" + tid);
		}

		public void commit(JCoServerContext serverCtx, String tid) {
			log("commit called for TID=" + tid);
		}

		public void rollback(JCoServerContext serverCtx, String tid) {
			log("rollback called for TID=" + tid);
		}
	}
	public boolean isShutDown() {
		return !(JCoServerState.ALIVE.equals(iDocServer.getState()));
	}
	public boolean isFinished() {
		return syncFils.size() <= 0;
	}
	public void log(String message) {
		if (logger != null) {
			logger.info(message);
		} else {
			System.out.println(message);
		}
	}
	public void log(Throwable e) {
		if (logger != null) {
			logger.log(Level.SEVERE, "", e);
		} else {
			e.printStackTrace();
		}
	}
	public int getServerId() {
		return server_id;
	}
	public void setServerId(int serverId) {
		this.server_id = serverId;
	}
	public boolean isDeleteImmediately() {
		return iDocServerInstance.isDeleteImmediately();
	}
	public void handleException(String message) {
		handleException(message);
	}
	public void handleException(Throwable e) {
		handleException("", e);
	}
	public void handleException(String message, Throwable e) {
		if (message == null && e == null)
			return;
		if (logger != null) {
			logger.log(Level.SEVERE, message, e);
		}
		try {
			dbUtil.getConnection().rollback();
			dbUtil.getConnection().setAutoCommit(true);
			dbUtil.stopSapServers(server_id);
		} catch (SQLException e1) {
			log("reportException:exception " + e1 + " failed!");
		}
		if (JCoServerState.ALIVE.equals(iDocServer.getState()) || JCoServerState.STARTED.equals(iDocServer.getState())) {
			log("stop iDocServer ");
			iDocServer.stop();
		}
		log("close dbconnection ");
		try {
			dbUtil.dispose();
		} catch (SQLException e1) {
			log("dispose dbUtil " + e1 + " failed!");
		}
		log("...........shutdown "+serverName+" finished.............. ");
		if (e != null)
			throw new RuntimeException(message, e);
		else
			throw new RuntimeException(message);

	}
}
