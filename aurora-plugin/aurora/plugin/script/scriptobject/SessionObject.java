package aurora.plugin.script.scriptobject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import uncertain.composite.CompositeMap;
import aurora.application.action.HttpSessionCopy;
import aurora.application.action.HttpSessionOperate;
import aurora.javascript.Callable;
import aurora.javascript.Context;
import aurora.javascript.Function;
import aurora.javascript.Scriptable;
import aurora.javascript.ScriptableObject;
import aurora.service.ServiceInstance;
import aurora.service.http.HttpServiceInstance;

public class SessionObject extends ScriptableObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7934572854688154878L;
	public static final String CLASS_NAME = "Session";
	private CompositeMap context;
	private CompositeMap sessionMap;
	private CompositeMapObject sessionObject;
	private HttpSessionOperate hso;
	private HttpServletRequest request;

	public SessionObject() {
		super();
	}

	public SessionObject(CompositeMap ctx) {
		super();
		hso = new HttpSessionOperate();
		context = ctx;
		sessionMap = context.getChild("session");
	}

	public static SessionObject jsConstructor(Context cx, Object[] args,
			Function ctorObj, boolean inNewExpr) {
		return new SessionObject((CompositeMap) args[0]);
	}

	private void init() {
		if (request == null) {
			HttpServiceInstance svc = (HttpServiceInstance) ServiceInstance
					.getInstance(context);
			request = svc.getRequest();
		}
	}

	public CompositeMap getContext() {
		return context;
	}

	public void setContext(CompositeMap context) {
		this.context = context;
	}

	public void jsFunction_write(String target, String source) {
		init();
		hso.setTarget(target);
		hso.setSource(source);
		hso.writeSession(request, context);
	}

	public void jsFunction_writeValue(String target, Object value) {
		init();
		HttpSession session = request.getSession();
		session.setAttribute(target, value);
		context.putObject("/session/@" + target, value);
	}

	public void jsFunction_create() {
		init();
		request.getSession(true);
	}

	public void jsFunction_clear() {
		init();
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	public void jsFunction_copy() {
		init();
		HttpSession ses = request.getSession(false);
		HttpSessionCopy.copySession(context, ses);
	}

	public void initSession() {
		if (context == null)
			return;
		if (sessionMap == null)
			sessionMap = context.createChild("session");
		if (sessionObject == null) {
			sessionObject = (CompositeMapObject) ScriptUtil.newObject(this,
					CompositeMapObject.CLASS_NAME);
			sessionObject.setData(sessionMap);
		}
	}

	public Object jsFunction_get(String key) {
		initSession();
		return sessionObject.composite_get(key);
	}

	public void jsFunction_put(String key, Object value) {
		initSession();
		sessionObject.composite_put(key, value);
	}

	@Override
	public String getClassName() {
		return CLASS_NAME;
	}

	@Override
	public boolean has(String name, Scriptable start) {
		initSession();
		if (sessionMap != null && sessionMap.containsKey(name))
			return true;
		return super.has(name, start);
	}

	@Override
	public Object get(String name, Scriptable start) {
		initSession();
		Object o = sessionObject == null ? null : sessionObject
				.get(name, start);
		if (ScriptUtil.isValid(o))
			return o;
		return super.get(name, start);
	}

	@Override
	public void put(String name, Scriptable start, Object value) {
		initSession();
		if (sessionMap != null && !(value instanceof Callable))
			sessionMap.put(name, value);
		if (!isSealed())
			super.put(name, start, value);
	}

	public String jsFunction_toXML() {
		return sessionObject.jsFunction_toXML();
	}
}
