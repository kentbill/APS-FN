package com.esquel.APS.GEW_FN.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.PlanningResult;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

public class PlanningResultTest {
	private static List<PlanningResult> resultList;
	//private static List<Task> taskList;
	private static List<MachinePlan> machinePlanList;

	public static void main(String[] args) {

		try {
			getObjectList();
			test();
			
		} catch (FileNotFoundException | APSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	private static void test() throws FileNotFoundException, APSException{
		
		// 根据结果集创建Task表列
		List<Task> tasks = new ArrayList<Task>();
		for(PlanningResult planningResult : resultList){
			Task task = new Task();
			task.setId(planningResult.getTaskID());
			task.setPlanStartTime(planningResult.getPlannedStartTime());
			task.setPlanStartTimeL(Common.getTime_Minute(task.getPlanStartTime().getTime()));
			MachinePlan macinePlan = machinePlanList.stream().filter(x -> x.getId().equals(planningResult.getMachinePlanID())).findAny().orElse(null);
			if(macinePlan == null){
				throw new APSException("任务对应的MachinePlan不存在");
			}
			
			// 缺少太多环境对象，无法继续
			
			task.setMachinePlan(macinePlan);
			tasks.add(task);
		}
		
		
		
		
		
		// 检查Task是否重复
		for(Task task : tasks){
			System.out.println(task.timeOverlap());
		}
	
	}
	
	
	private static void getObjectList() throws FileNotFoundException{
		Class<?>[] classes = new Class[]{PlanningResult.class, MachinePlan.class};
		XStream xs = new XStream();
		//TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, null));
		xs.alias(Common.getClassName(PlanningResult.class.getName()), PlanningResult.class);
		xs.alias(Common.getClassName(MachinePlan.class.getName()), MachinePlan.class);
		
		String fileName = "src\\main\\resources\\PlanningResult.xml";
		FileInputStream fis = new FileInputStream(fileName);
		resultList = (List<PlanningResult>) xs.fromXML(fis, new ArrayList<PlanningResult>());
		
		fileName = "src\\main\\resources\\MachinePlan.xml";
		fis = new FileInputStream(fileName);
		machinePlanList = (List<MachinePlan>) xs.fromXML(fis, new ArrayList<MachinePlan>());
		
	}


}
