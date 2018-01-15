package com.esquel.APS.Helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.esquel.APS.GEW_FN.Test.TaskInfo;
import com.esquel.APS.GEW_FN.domain.Attribute;
import com.esquel.APS.GEW_FN.domain.Broken;
import com.esquel.APS.GEW_FN.domain.Constraint;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.MachinePlanProcess;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.PlanningResult;
import com.esquel.APS.GEW_FN.domain.ScoreDrl;
import com.esquel.APS.GEW_FN.domain.ScoreSummary;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskCost;
import com.esquel.APS.GEW_FN.domain.TaskSort;
import com.esquel.APS.GEW_FN.domain.Word;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

/**
 * �ṩ�˽�ָ�����󣨻�����б����л���XML�ķ���
 * 
 * @author ZhangKent
 *
 */
public final class XMLHelper {
	private final static String CHINA_TIME_ZONE = "Asia/Shanghai";
	
	private static List<MachinePlan> machinePlanList = null;
	private static List<Task> taskList = null;
	private static List<NonproductionTaskSet> nonproductionTaskSetList = null;
	private static List<Constraint> constraintList = null;
	private static List<Word> wordList = null;
	private static List<TaskSort> taskSortList = null; 
	private static List<ScoreDrl> scoreDrlList  = null;
	
	public static List<MachinePlan> getMachinePlanList() {
		return machinePlanList;
	}

	public static List<Task> getTaskList() {
		return taskList;
	}

	public static List<NonproductionTaskSet> getNonproductionTaskSetList() {
		return nonproductionTaskSetList;
	}

	public static List<Constraint> getConstraintList() {
		return constraintList;
	}

	public static List<Word> getWordList() {
		return wordList;
	}

	public static List<TaskSort> getTaskSortList() {
		return taskSortList;
	}

	public static List<ScoreDrl> getScoreDrlList() {
		return scoreDrlList;
	}

	/**
	 * 将结果保存到XML文件
	 * @param planningResults
	 * @param brokens
	 * @param taskCost
	 * @throws IOException
	 */
	public static void SaveResult(List<PlanningResult> planningResults, List<Broken> brokens, List<TaskCost> taskCost, List<ScoreSummary> scoreSummaries) throws IOException {

		String folderName = Common.getXMLPath(EntityCategory.PLANNING_RESULT, true);

		File folder = new File(folderName);

		// �ļ����Ƿ��Ѵ���
		if (folder.exists()) {
			System.out.println("Folder [" + folder + "] already exist.");
			return;
		}

		if (!folderName.endsWith(File.separator)) {
			folderName += File.separator;
		}

		if (!folder.mkdirs()) {
			System.out.println("Create folder [" + folderName + "], failue");
			return;
		}

		String planningResultXMLFile = folderName + Common.getPlanningrestultxmlFilename();
		String runningLogXMLFile = folderName + Common.getRunninglogxmlFilename();
		String taskCostXMLFile = folderName + Common.getTaskCostxmlFilename();
		String scoreSummaryXMLFile = folderName + Common.getScoreSummaryxmlFileName();

		savePlanningResult(planningResults, planningResultXMLFile);
		saveRunningLog(brokens, runningLogXMLFile);
		saveTaskCostXMLFile(taskCost, taskCostXMLFile);
		saveScoreSummaries(scoreSummaries, scoreSummaryXMLFile);
		
	}

	/**
	 * 保存运行结果
	 * 
	 * @param planningResults
	 * @throws IOException
	 */
	private static void savePlanningResult(List<PlanningResult> planningResults, String resutlXML) throws IOException {

		FileOutputStream fos = new FileOutputStream(resutlXML);
		Class<?>[] classes = new Class[]{PlanningResult.class};
		
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream xs = new XStream();
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));
		xs.alias("PlanningResult", PlanningResult.class);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.toXML(planningResults, fos);
	}

	/**
	 * 保存运行日志，日志中记录了违反约束的情况
	 * 
	 * @param brokens
	 */
	private static void saveRunningLog(List<Broken> brokens, String runningLogXML) throws IOException {
		FileOutputStream fos = new FileOutputStream(runningLogXML);
		
		Class<?>[] classes = new Class[]{Broken.class};
		XStream xs = new XStream();
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));

		xs.alias("Broken", Broken.class);
		xs.toXML(brokens, fos);

	}
	
	private static void saveTaskCostXMLFile(List<TaskCost> taskCostList , String taskCostXML) throws FileNotFoundException{
		FileOutputStream fos = new FileOutputStream(taskCostXML);
		
		Class<?>[] classes = new Class[]{TaskCost.class};
		XStream xs = new XStream();
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));

		xs.alias("TaskCost", TaskCost.class);
		xs.toXML(taskCostList, fos);
	}
	
	private static void saveScoreSummaries(List<ScoreSummary> ScoreSummary, String scoreSummaryXML) throws FileNotFoundException{
		FileOutputStream fos = new FileOutputStream(scoreSummaryXML);
		
		Class<?>[] classes = new Class[]{ScoreSummary.class};
		XStream xs = new XStream();
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));
		xs.alias(Common.getClassName(ScoreSummary.class.getName()), ScoreSummary.class);
		xs.toXML(ScoreSummary, fos);
	}
	
	public static void SaveTaskInfo(List<TaskInfo> taskInfos) throws IOException{
		
		String folderName = Common.getXMLPath(EntityCategory.TASK_INFO_test, false);
		
		FileOutputStream fos = new FileOutputStream(folderName);

		// ����XML Stream
		XStream xs = new XStream();
		xs.alias("TaskInfo", Broken.class);
		xs.toXML(taskInfos, fos);
	}
	

	public static void loadObjects() {
		Class<?>[] classes = new Class[]{
				MachinePlan.class, 
				MachinePlanProcess.class,
				Task.class, 
				Attribute.class, 
				NonproductionTaskSet.class,
				NonproductionTask.class, 
				Constraint.class, 
				Word.class, 
				TaskSort.class,
				ScoreDrl.class};
		XStream xs = new XStream();
		//TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, null));
		
		xs.alias(Common.getClassName(MachinePlan.class.getName()), MachinePlan.class);
		xs.alias(Common.getClassName(Task.class.getName()), Task.class);
		xs.alias(Common.getClassName(NonproductionTaskSet.class.getName()), NonproductionTaskSet.class);
		xs.alias(Common.getClassName(Constraint.class.getName()), Constraint.class);
		xs.alias(Common.getClassName(Word.class.getName()), Word.class);
		xs.alias(Common.getClassName(TaskSort.class.getName()), TaskSort.class);
		xs.alias(Common.getClassName(ScoreDrl.class.getName()), ScoreDrl.class);

		xs.autodetectAnnotations(true);
		FileInputStream fis;

		try {

			String fileName = Common.getXMLPath(EntityCategory.MACHINE_PLAN, false);
			fis = new FileInputStream(fileName);
			machinePlanList = (List<MachinePlan>) xs.fromXML(fis, new ArrayList<MachinePlan>());
			

			fileName = Common.getXMLPath(EntityCategory.TASK, false);
			fis = new FileInputStream(fileName);
			taskList = (List<Task>) xs.fromXML(fis, new ArrayList<Task>());
			
			//List<Task> taskTest = taskList.stream().filter(x -> x.getProcessCode().equals("016") || x.getProcessCode().equals("040") || x.getProcessCode().equals("066")).collect(Collectors.toList());
			
			
			fileName = Common.getXMLPath(EntityCategory.NONPRODUCTION_TASK, false);
			fis = new FileInputStream(fileName);
			nonproductionTaskSetList = (List<NonproductionTaskSet>) xs.fromXML(fis, new ArrayList<NonproductionTaskSet>());

			fileName = Common.getXMLPath(EntityCategory.CONSTRAINT, false);
			fis = new FileInputStream(fileName);
			constraintList = (List<Constraint>) xs.fromXML(fis, new ArrayList<Constraint>());
			
			fileName = Common.getXMLPath(EntityCategory.WORD, false);
			fis = new FileInputStream(fileName);
			wordList = (List<Word>) xs.fromXML(fis, new ArrayList<Word>());
			// Save the word list in Common
		//	Common.set_words(words);
			
			// get sort field list
			fileName = Common.getXMLPath(EntityCategory.TASK_SORT, false);
			fis = new FileInputStream(fileName);
			taskSortList = (List<TaskSort>) xs.fromXML(fis, new ArrayList<TaskSort>());
			
			fileName = Common.getXMLPath(EntityCategory.SCORE_DRL, false);
			fis = new FileInputStream(fileName);
			scoreDrlList = (List<ScoreDrl>) xs.fromXML(fis, new ArrayList<ScoreDrl>());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
		}
	}
	
	

}
