package com.esquel.APS.GEW_FN.ws_invoker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.w3c.dom.Document;

import com.esquel.APS.GEW_FN.domain.Attribute;
import com.esquel.APS.GEW_FN.domain.Constraint;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.MachinePlanProcess;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.ScoreDrl;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskSort;
import com.esquel.APS.GEW_FN.domain.Word;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;


public class WSHelper {
	
	
	
	private static List<RemoteService> _remoteServices = new ArrayList<RemoteService>();
	private final static String CHINA_TIME_ZONE = "Asia/Shanghai";
	
	private static void initRemoteServices(){
		
		
		Class<?>[] classes = new Class[]{RemoteService.class, WSParameter.class};
		XStream xs = new XStream();
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));
		xs.alias(Common.getClassName(RemoteService.class.getName()), RemoteService.class);
		xs.autodetectAnnotations(true);
		FileInputStream fis;

		try {
			String fileName = Common.getXMLPath(EntityCategory.REMOTE_SERVICE, false);
			fis = new FileInputStream(fileName);
			_remoteServices = (List<RemoteService>) xs.fromXML(fis, new ArrayList<RemoteService>());
	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static RemoteService getService(String name){
		if(_remoteServices == null || _remoteServices.isEmpty()){
			initRemoteServices();
		}
		
		RemoteService rs = _remoteServices.stream().filter(x -> x.getName().equals(name)).findAny().orElse(null);
		if(rs == null){
			return null;
		}
		
		return rs;

	}
	
	public static RemoteService getService(Map<String, String> params, String serviceKey){
	
		RemoteService rService = getService(serviceKey);
		// initialize the parameter
		for (String pName : params.keySet()) {

			String pValue = params.get(pName);
			WSParameter param = rService.getParameters().stream().filter(x -> x.getName().equals(pName)).findAny()
					.orElse(null);
			if (param != null) {
				param.setValue(pValue);
			}
		}

		String url = rService.getLink() + "/" + rService.getOperation() + "?";
		
		// combine the parameter string
		String parramString = "";
		for(WSParameter param : rService.getParameters()){
			if(parramString != ""){
				parramString += "&";
			}
			
			parramString += param.getName() + "=" + param.getValue(); 
		}
		url += parramString;
		rService.setFinalURL(url);
		return rService;
	}
	
	public static void callBackAPI(RemoteService rService) throws IOException {
		
		List<String> resultLines = invokWS(rService);
		String sucess = resultLines.stream().filter(x -> x.contains(rService.getReturnTag_Notify())).findAny().orElse(null);
		System.out.println("Return message summary:" + sucess);
		String showMessage = sucess.contains(rService.getReturnTag_Success()) ? rService.getSuccessMessage() : rService.getFailureMessage();
		System.out.println(showMessage);
	}
	
	/**
	 * 调用接口并返回结果
	 * @param rService
	 * @return
	 * @throws IOException
	 */
	private static List<String> invokWS(RemoteService rService) throws IOException{
		URL getUrl = new URL(rService.getFinalURL());
		
		HttpURLConnection connection = (HttpURLConnection) getUrl.openConnection();
		connection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		List<String> lines = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {

			lines.add(line);
		}
		reader.close();;
		
		return lines;
	}
	

}
