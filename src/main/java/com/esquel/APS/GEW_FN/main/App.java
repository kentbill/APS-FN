package com.esquel.APS.GEW_FN.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


import javax.xml.soap.SOAPException;

import com.esquel.APS.GEW_FN.domain.enums.RunningSystem;
import com.esquel.APS.GEW_FN.solver.SolveMain;
import com.esquel.APS.GEW_FN.ws_invoker.RemoteService;
import com.esquel.APS.GEW_FN.ws_invoker.WSHelper;
 

/**
 * 程序入口,在本程序中启动排产过程
 * 
 * @author ZhangKent
 *
 */
public class App {

	private static final String _DOWNLOAD_PlanningResult = "DOWNLOAD_PlanningResult";
	private static final String _NOTIFY_API = "EXCEPTION_NOTIFY";
	
	public static void main(String[] args) throws IOException {
		// testing。。。。
		
		//failueNotification("测试测试", "APS201712270987");
		//downloadResult("APS201712270987", "test");
		
		String jobID = "";
		Properties props = System.getProperties();
		String osName = props.getProperty("os.name");
		RunningSystem rSys = osName.contains("Windows") ? RunningSystem.WINDOWS : RunningSystem.LINUX;
		try {
			if(args == null || args.length == 0){
				System.out.println("Please input the parameter. \r v - get then planner version. \r [jobid]+[envrionment] - to run the planner");
				return;
			}
			
			// 获取版本
			if(args.length == 1){
				String getVersionPara = args[0];
				
				if(!getVersionPara.equals("v") && !getVersionPara.equals("version")){
					System.out.println("To get the version of planner, input  v or version as the parameter");
					return;
				}
				
			//	updateVersion();
				return;
			}
			
			// 启动排产
			if (args == null || args.length != 3) {
				System.out.println(new Date() + "The engine expected 2 parameters: jobID, callbackflag, but not pass.");
				return;
			}

			if (args[0] == null || args[0] == "" || args[1] == null || args[1] == "" || args[2] == null || args[2] == "") {
				System.out.println("The engine expected 2 parameters: jobID, callbackflag, but not pass.");
				return;
			}

			jobID = args[0];					// JobId
			String callbackflag = args[1];			// APS Webservice环境
			String planningTarget = args[2];	// 排产目标
			//List<String> planningTargets = parsePlanningTarget(planningTargetString);
			
			showCovert(jobID, callbackflag, planningTarget);

			SolveMain.StartSolve(jobID, planningTarget);

			if (rSys == RunningSystem.LINUX) {
				
				// 下载排产结果
				downloadResult(jobID, callbackflag);
				// 结束排产后，清理进程文件
				finishedPlaning(jobID);
			} else {
				System.out.println("Download operation applies for production environment only.");
				System.out.println("All planning operations done!");
			}
		} catch (Exception e) {
			e.printStackTrace();
	
			if (rSys == RunningSystem.LINUX) {
				String message = "";

				if (e.getMessage() == null) {
					message = e.toString();
					message += "|" + e.getStackTrace()[0].toString();
				} else {
					e.getMessage();
				}

				// 出现异常，调用通知接口
				failueNotification(message, jobID);
			} else {
				System.out.println("Notification applies for production environment only.");
			}
		}

	}
	/*
	private static List<String> parsePlanningTarget(String targetString){
		if(targetString == null || targetString == ""){
			return null;
		}
		
		List<String> targetList = new ArrayList<String>();
		
		targetString = targetString.trim();
		
		if(!targetString.contains(",")){
			targetList.add(targetString);
			return targetList;
		}
		
		String[] targets = targetString.split(",");
		for(int i = 0; i < targets.length; i++){
			targetList.add(targets[i]);
		}
		return targetList;
	}
	*/
	
	private static void updateVersion() throws IOException {
		System.out.println("test1");
		String versionNo = getVersion();
		String fileName = "ver.inf";
		
		System.out.println("versionNo:" + versionNo);

		// 若文件存在，则删除
		File file = new File(fileName);
		if (file.exists()) {
			file.delete();
		}

		file.createNewFile();
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(versionNo);
		out.flush();
		out.close();
	}
	
	private static void showCovert(String jobID, String callbackflag, String planningTarget){
		String covert = "";

		System.out.println(covert);
		System.out.println(new Date() + "Planning started, jobID:" + jobID + ", environment:" + callbackflag + ", planning target:" + planningTarget);
	}
	
	/**
	 * call a web service to download the planning result.
	 * @param jobID
	 */
	private static void downloadResult(String jobID, String callbackflag){
		System.out.println("Calling back download service...");
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("JobID", jobID);
		
		try {
			
			String serviceKey = _DOWNLOAD_PlanningResult + "_" + callbackflag;
			RemoteService rService = WSHelper.getService(params, serviceKey);
			System.out.println("The download URL is: " + rService.getFinalURL());
			WSHelper.callBackAPI(rService);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * call the web service to notify failure
	 * @param errMsg
	 * @param jobID
	 */
	private static void failueNotification(String errMsg, String jobID){
		System.out.println("Calling back notify service...");
		
		// 消息超过80的字符只取前面80个
		if(errMsg != null && errMsg.length() > 80){
			errMsg = errMsg.substring(0,  80 - 1);
		}
		
		// 对消息进行编码
		errMsg = URLEncoder.encode(errMsg);
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("JobID", jobID);
		params.put("remark", errMsg);
		
		try {
			String serviceKey = _NOTIFY_API;
			RemoteService rService = WSHelper.getService(params, serviceKey);
			System.out.println("The notifcation URL is: " + rService.getFinalURL());
			WSHelper.callBackAPI(rService);
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	/**
	 * delete the pid file after planning, the pid file saves the planning process ID what use to kill the process.
	 * @param jobID
	 */
	private static void finishedPlaning(String jobID){
		String cmdString = "./planning-finished.sh " + jobID;
		Runtime runTime = Runtime.getRuntime();
		try {
			runTime.exec(cmdString);
			System.out.println("The pid file [" + jobID + ".pid] was clear.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static String getVersion() throws FileNotFoundException, IOException{

		System.out.println("getVersion1");
		Properties properties = new Properties();
		try {
			properties.load(App.class.getResourceAsStream("/META-INF/MANIFEST.MF"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = properties.getProperty("Implementation-Version");
		System.out.println("properties:" + properties);
		System.out.println("version:" + version);
		return version;
		
	}

}
