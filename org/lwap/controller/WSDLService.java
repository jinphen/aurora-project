package org.lwap.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import aurora.service.ServiceContext;
import aurora.service.json.JSONServiceContext;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;
import uncertain.composite.CompositeUtil;
import uncertain.composite.JSONAdaptor;
import uncertain.composite.XMLOutputter;
import uncertain.event.EventModel;

public class WSDLService implements IController {
	HttpServletResponse response;
	ServiceContext service_context;
	String output;
	public static final String DEFAULT_WSDL_CONTENT_TYPE = "text/xml;charset=utf-8";
	public int detectAction(HttpServletRequest request, CompositeMap context) {
		return IController.ACTION_DETECTED;
	}

	public String getProcedureName() {
		return "org.lwap.controller.InvokeService";
	}

	public void setServiceInstance(MainService serviceInst) {
		response =serviceInst.getResponse();
	}

	public int preParseParameter(ServiceContext ct) throws Exception {
		service_context = ct;
		return EventModel.HANDLE_NORMAL;

	}

	public void onCreateSuccessResponse() throws IOException {
		response.setContentType(DEFAULT_WSDL_CONTENT_TYPE);
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		CompositeMap result=null;
		PrintWriter out = response.getWriter();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.write("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		out.write("<soap:Body>");
		if (output != null) {
            Object obj = service_context.getObjectContext().getObject(
                    output);
            if (!(obj instanceof CompositeMap))
                throw new IllegalArgumentException(
                        "Target for WSDL output is not instance of CompositeMap: "
                                + obj);
            result = (CompositeMap) obj;
        } else
            result = service_context.getModel();
        if (result != null) {
        	CompositeMap cmt = CompositeUtil.expand(result);
            XMLOutputter xo = new XMLOutputter();
            xo.setGenerateCdata(false);
          out.println(xo.toXML(cmt));
        }
        out.write("</soap:Body>");
		out.write("</soap:Envelope>");
	}

	public void onCreateFailResponse(ServiceContext context)
			throws IOException, JSONException {
		response.setContentType(DEFAULT_WSDL_CONTENT_TYPE);
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		PrintWriter out = response.getWriter();
		out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.write("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		out.write("<soap:Body>");
		out.write("<errorcode>");
    	out.write(context.getError().getString("code"));
		out.write("</errorcode>");
		out.write("<errormsg>");
		out.write(context.getError().getString("message"));
		out.write("</errormsg>");
		out.write("</soap:Body>");
		out.write("</soap:Envelope>");
		
//		out.write(((CompositeMap)context.getObjectContext().getObject("/error code")).toXML());
	}
    public String getOutput() {
        return output;
    }

    /**
     * @param output
     *            the output to set
     */
    public void setOutput(String output) {
        this.output = output;
    }
}
