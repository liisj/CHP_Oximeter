package org.chp;

import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.util.PortalUtil;
import com.liferay.util.bridges.mvc.MVCPortlet;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Logger;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Oximeter extends MVCPortlet {

	private static DateFormat dateFormat = new SimpleDateFormat("[HH:mm:ss]");
	private static Logger logger = Logger.getLogger("InfoLogging");
	
	private static JSONObject requestToJSONObject(ResourceRequest request) {
		JSONObject result = new JSONObject();
		Enumeration<String> parametersE = request.getParameterNames();
		while (parametersE.hasMoreElements()) {
			String parameter = parametersE.nextElement();
			String value = request.getParameter(parameter);
			result.put(parameter, value);
		}
		
		return result;
	}
	
	private static void writeMessage(ResourceResponse response, JSONObject jsonObject) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonObject.toJSONString());	
	}

	private static void writeMessage(ResourceResponse response, JSONArray jsonArray) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/json;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, jsonArray.toJSONString());
	}

	private static void writeMessage(ResourceResponse response, String string) throws IOException {
		HttpServletResponse httpResponse = PortalUtil.getHttpServletResponse(response);
		httpResponse.setContentType("application/plain;charset=UTF-8");
		ServletResponseUtil.write(httpResponse, string);	
	}
	
	
 
	// DMA functions
	
	/**
	 * 
	 * @param request Ignored
	 * @param response Will contain a JSONArray, containing JSONObjects like:<br>
	 * category_id (int)<br>
	 * category_name (String)
	 * @throws PortletException
	 * @throws IOException
	 */
	
	public void getLastReadings(ResourceRequest request, ResourceResponse response)
			throws PortletException, IOException {
		
		JSONObject parameters = requestToJSONObject(request);
		
		JSONArray list;
		Connection con = null;
		try {
			con = DataBaseFunctions.getWebConnection();
			list = DataBaseFunctions.getLastReadings(con, parameters);
		} catch (SQLException e) {	
			JSONObject errorObject =  new JSONObject();
			errorObject.put("error", "Database");
			errorObject.put("details", e.getMessage());
			writeMessage(response,errorObject);
			return;
		} finally {
			try {con.close();} catch (SQLException e) {e.printStackTrace();}
		}
		
		writeMessage(response,list);
	}
	
		
	// Very necessary function, please don't delete anything in here
	@Override
    public void processAction(
            ActionRequest actionRequest, ActionResponse actionResponse)
        throws IOException, PortletException {
		
		System.out.println("processAction reached");
		
        PortletPreferences prefs = actionRequest.getPreferences();
        String actionName = actionRequest.getParameter("actionName");
        boolean switchJSP = false;
        
        if (actionName != null) {
        	
        	
        	if (actionName.equals("subCategories")) {
        		
        		String catId1 = actionRequest.getParameter("category_id");
                if (catId1 != null) {
                    actionRequest.setAttribute("category_id", catId1);
                }
        	}
        	else if (actionName.equals("materials")) {
        		System.out.println("materials");
        		String catId2 = actionRequest.getParameter("id");
                if (catId2 != null) {
                    actionRequest.setAttribute("mat_id", catId2);
                }
        	}
        	else if (actionName.equals("goToMaterial")) {
        		Map<String,String[]> params = actionRequest.getParameterMap();
        		for (String key : params.keySet()) {
        			System.out.println(key + ": " + params.get(key));
        		}
        	}
        	else if (actionName.equals("subQuestions")) {
        		String questionId = actionRequest.getParameter("question_id");
                if (questionId != null) {
                	actionRequest.setAttribute("question_id", questionId);
                }
                
                String title = actionRequest.getParameter("title");
                if (title != null) {
                	actionRequest.setAttribute("title", title);
                }
                
                // hackery
                String next = actionRequest.getParameter("question_id");
                if (next != null && next.equals("3")) {
                	System.out.println("switching...");
                	switchJSP = true;
                }
        	}
        }
        
        String jspPage = actionRequest.getParameter("jspPage");
        System.out.println("jspPage: " + jspPage);
        if (jspPage != null) {
        	System.out.println(jspPage);
        	actionResponse.setRenderParameter("jspPage", jspPage);
        }
        if (switchJSP) {
        	actionResponse.setRenderParameter("jspPage", "/html/newportlet2/diagnose.jsp");
        }
        
        super.processAction(actionRequest, actionResponse);
    }
	
	public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException {        
				
		String resourceID = request.getResourceID();
		 if ("getLastReadings".equals(resourceID)) {
			 getLastReadings(request, response);
		 }
		 
	}
}

