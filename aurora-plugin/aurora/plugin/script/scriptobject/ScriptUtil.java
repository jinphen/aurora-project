package aurora.plugin.script.scriptobject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.script.ScriptException;

import uncertain.composite.CompositeMap;
import uncertain.ocm.IObjectRegistry;
import uncertain.proc.ProcedureRunner;
import aurora.javascript.Context;
import aurora.javascript.NativeArray;
import aurora.javascript.Scriptable;
import aurora.javascript.ScriptableObject;
import aurora.plugin.script.engine.AuroraScriptEngine;
import aurora.plugin.script.engine.InterruptException;
import aurora.plugin.script.engine.InterruptExceptionDescriptor;
import aurora.plugin.script.engine.ScriptExceptionDescriptor;
import aurora.service.exception.ExceptionDescriptorConfig;
import aurora.service.exception.IExceptionDescriptor;

public class ScriptUtil {
	public static Scriptable newObject(Scriptable scope, String clsName) {
		Context ctx = Context.getCurrentContext();
		Scriptable topScope = ScriptableObject.getTopLevelScope(scope);
		return ctx.newObject(topScope, clsName);
	}

	public static NativeArray newArray(Scriptable scope, int length) {
		Context ctx = Context.getCurrentContext();
		Scriptable topScope = ScriptableObject.getTopLevelScope(scope);
		return (NativeArray) ctx.newArray(topScope, length);
	}

	public static AuroraScriptEngine getEngine(CompositeMap context) {
		ScriptShareObject sso = getSso();
		if (sso == null)
			return null;
		return sso.getEngine();
	}

	public static IObjectRegistry getObjectRegistry(CompositeMap context) {
		ScriptShareObject sso = getSso();
		if (sso == null)
			return null;
		return sso.getObjectRegistry();
	}

	public static Object getInstanceOfType(String className) {
		CompositeMap map = getContext();
		try {
			Class<?> cls = Class.forName(className);
			IObjectRegistry ior = getObjectRegistry(map);
			if (ior == null)
				return null;
			return ior.getInstanceOfType(cls);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static CompositeMap getContext() {
		return (CompositeMap) Context.getCurrentContext().getThreadLocal(
				AuroraScriptEngine.KEY_SERVICE_CONTEXT);
	}

	public static ScriptShareObject getSso() {
		return (ScriptShareObject) getContext().get(AuroraScriptEngine.KEY_SSO);
	}

	public static ProcedureRunner getProcedureRunner() {
		CompositeMap ctx = getContext();
		ScriptShareObject sso = getSso();
		ProcedureRunner runner = sso.getProcedureRunner();
		if (runner == null) {
			runner = new ProcedureRunner();
			runner.setContext(ctx);
		}
		return runner;
	}

	public static boolean isValid(Object obj) {
		return !(obj == null || obj == Context.getUndefinedValue() || obj == Scriptable.NOT_FOUND);
	}

	public synchronized static final String loadAuroraCore() {
		try {
			InputStream is = AuroraScriptEngine.class
					.getResourceAsStream(AuroraScriptEngine.aurora_core_js);
			if (is != null) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(is));
				StringBuilder sb = new StringBuilder(1024);
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append('\n');
				}
				is.close();
				br.close();
				return sb.toString();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return "";
	}

	public static void registerExceptionHandle(IObjectRegistry or) {
		ExceptionDescriptorConfig excpDesc = (ExceptionDescriptorConfig) or
				.getInstanceOfType(IExceptionDescriptor.class);
		try {
			excpDesc.addExceptionDescriptor(createExceptionHandleItem(
					InterruptException.class,
					InterruptExceptionDescriptor.class));
			excpDesc.addExceptionDescriptor(createExceptionHandleItem(
					ScriptException.class, ScriptExceptionDescriptor.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static CompositeMap createExceptionHandleItem(Class<?> expClass,
			Class<?> handleClass) {
		String className = handleClass.getSimpleName();
		CompositeMap item = new CompositeMap(className);
		item.put("exception", expClass.getName());
		item.put("handleclass", handleClass.getName());
		item.put("xmlns", handleClass.getPackage().getName());
		return item;
	}

}
