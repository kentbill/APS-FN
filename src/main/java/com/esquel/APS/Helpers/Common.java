package com.esquel.APS.Helpers;

import java.io.File;
//import java.io.IOException;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Properties;
//import java.util.SimpleTimeZone;
import java.util.TimeZone;

//import com.esquel.APS.GEW_FN.domain.DosageScope;
//import com.esquel.APS.GEW_FN.domain.NonproductionTask;
/*
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderErrors;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import com.esquel.APS.GEW_FN.domain.Attribute;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
*/
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.Word;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.esquel.APS.GEW_FN.domain.enums.GetTimeType;
import com.esquel.APS.GEW_FN.domain.enums.RunningSystem;
import com.esquel.APS.GEW_FN.domain.enums.TimeComparisonResultType;
//import com.esquel.APS.GEW_FN.solver.CommonFunctions;
//import com.esquel.APS.GEW_FN.domain.Step;
//import com.esquel.APS.GEW_FN.solver.SolveMain;

public final class Common {
	private static final String _resources_Linux_Folder = "resources";
	private static final String _resources_Windows_Folder = "src" + File.separator + "main" + File.separator +"resources";
	
	private static final String _outputPath = "output";

	private static final String _machinPlanXML_FileName = "MachinePlan.xml";
	private static final String _taskXML_FileName = "Task.xml";
	private static final String _nonProductionTaskXML_FileName = "NonproductionTaskSet.xml";
	private static final String _solverConfigurationXML_FileName = "configuration.solver.xml";
	private static final String _constraintXML_FileName = "Constraint.xml";
	private static final String _wordXML_FileName = "Word.xml";
	//private static final String _dosageMappingXML_FileName = "DosageScope.xml";
	private static final String _remoteServiceXML_Filename = "RemoteService.xml";

	private static final String _planningRestultXML_FileName = "PlanningResult.xml";
	private static final String _runningLogXML_FileName = "RunningLog.xml";
	private static final String _taskCostXML_FileName = "TaskCost.xml";
	
	private static final String _taskSortXML_FileName = "TaskSort.xml";
	
	private static final String _TaskInfo_FileName = "TaskInfo.xml";
	
	private static final String _scoreSummaryXML_FileName = "ScoreSummaries.xml";
	
	private static final String _scoreDrlXML_FileName = "rules" + File.separator + "scoreDrl.xml";
	
	private static Long _planningStartTimeL = null;
	
	// the task id list determines that the task has been visit.
	private static List<Long> updatedTaskIDs = new ArrayList<Long>();
	
	private static List<Word> _words = null; // new ArrayList<Word>();
//	private static List<DosageScope> _dosageScopes = null;
	private static String _jobId;
	
	public static final Long INTERVAL_OF_TASK = 1L; // 间隔为1分钟
	
	private final static String CHINA_TIME_ZONE = "Asia/Shanghai";

	/**
	 * @param _words the _words to set
	 */
/*	
	public static void set_words(List<Word> _words) {
		
		Common._words = _words;
	}
*/

	/*
	public static Word get_word(String key){
		if(_words == null || _words.isEmpty()){
			return null;
		}
		
		return _words.stream().filter(x -> x.getKey().equals(key)).findAny().orElse(null);
	}
	*/

	public static String get_jobId() {
		return _jobId;
	}

	public static void set_jobId(String _jobId) {
		Common._jobId = _jobId;
	}

	public static Long get_planningStartTimeL() {
		return _planningStartTimeL;
	}

	public static void set_planningStartTimeL(Long _planningStartTimeL) {
		Common._planningStartTimeL = _planningStartTimeL;
	}

	/**
	 * �Ų�����ļ���
	 * @return
	 */
	public static final String getPlanningrestultxmlFilename() {
		return _planningRestultXML_FileName;
	}

	/**
	 * ��־�ļ���
	 * @return
	 */
	public static final String getRunninglogxmlFilename() {
		return _runningLogXML_FileName;
	}
	
	
	public static final String getTaskCostxmlFilename() {
		return _taskCostXML_FileName;
	}
	
	public static final String getTaskInfoxmlFileName(){
		return _TaskInfo_FileName;
	}
	
	public static final String getScoreSummaryxmlFileName(){
		return _scoreSummaryXML_FileName;
	}
	/**
	 * ����������֮���ʱ����,��λ:��
	 * @return
	 */
	/*
	public static final int getIntervalOfTask() {
		return _intervalOfTask;
	}
	*/
	

	public static String getXMLPath(EntityCategory enCate, boolean folderOnly) {
	
		Properties props = System.getProperties(); // ���ϵͳ���Լ�
		String osName = props.getProperty("os.name"); // ����ϵͳ����
		RunningSystem rSys = osName.contains("Windows") ? RunningSystem.WINDOWS : RunningSystem.LINUX;

		String fileName = "";

		switch (enCate) {
		
		case MACHINE_PLAN: {
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _machinPlanXML_FileName;
			}
			break;
		}
		case TASK: {
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _taskXML_FileName;
			} 
			break;
		}
		case NONPRODUCTION_TASK: {
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _nonProductionTaskXML_FileName;
			}
			break;
		}
		case SOLVER_CONFIGURATION: {
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _solverConfigurationXML_FileName;
			} 
			break;
		}
		case PLANNING_RESULT: {
			//DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			
			//String timeStamp = df.format(new java.util.Date());
			fileName = _outputPath + File.separator + _jobId + File.separator;
			if (!folderOnly) {
				fileName +=_planningRestultXML_FileName;
				
			}
			break;
		}
		case RUNNING_LOG: {
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String timeStamp = df.format(new java.util.Date());

			fileName = _outputPath + File.separator + timeStamp + File.separator + _runningLogXML_FileName;
			break;
		}
		case CONSTRAINT:{
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _constraintXML_FileName;
			} 
			break;
		}
		case WORD:{
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _wordXML_FileName;
			}
			break;
		}
		case REMOTE_SERVICE:{
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _remoteServiceXML_Filename;
			} 
			break;
		}
		case TASK_SORT:{
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _taskSortXML_FileName;
			} 
			break;
		}
		case SCORE_DRL:{
			fileName = rSys == RunningSystem.WINDOWS ? _resources_Windows_Folder : _resources_Linux_Folder;
			if (!folderOnly) {
				fileName += File.separator + _scoreDrlXML_FileName;
			} 

			break;
		}
		case TASK_INFO_test:{
			
			if (folderOnly) {
				if (rSys == RunningSystem.WINDOWS) {
					fileName = _outputPath + "\\" + _jobId + "\\";
				} else {
					fileName = _outputPath + "/" + _jobId + "/";
				}
			} else {

				if (rSys == RunningSystem.WINDOWS) {
					fileName = _outputPath + "\\" + _jobId + "\\" + _TaskInfo_FileName;
				} else {
					fileName = _outputPath + "/" + _jobId + "/" + _TaskInfo_FileName;
				}
			}
			break;
			
		}
		
		
		default: {
			fileName = "";
		}
		}

		return fileName;

	}



	/**
	 * ����һ��ʱ�����ָ�������������õ���ʱ��
	 * 
	 * @param baseTime
	 *            - ��ʼʱ��
	 * @param offset
	 *            - ��Ҫƫ�Ƶ�λ��Calendar.SECOND(��), Calendar.Minute(����),
	 *            Calendar.HOUR(Сʱ), Calendar.DATE(��), Calendar.MONTH(��)
	 * @return - ����ʼʱ������ϣ����ƶ�offset���õ�ʱ��
	 */
	public static Date getTimeOffset(Date baseTime, Long offset, int field) {

		if (baseTime == null) {
			return null;
		}

		if (offset == 0) {
			return baseTime;
		}

		Date newStartTime = baseTime;

		// ��ƫ�����ǿգ�����baseTime�����ϼ���ƫ����
		if (offset > 0) {
			Calendar cStartTime = new GregorianCalendar();
			cStartTime.setTime(baseTime);
			cStartTime.add(field, offset.intValue());
			newStartTime = cStartTime.getTime();
		}

		return newStartTime;
	}

	/**
	 * �Ƚ�����ʱ�䣬��Earlier_Or_LaterֵΪLATERʱ�����ؽϳ��ߣ����򷵻ؽ�����
	 * 
	 * @param time1
	 *            - ��Ҫ�Ƚϵ�ʱ��1
	 * @param time2
	 *            - ��Ҫ�Ƚϵ�ʱ��2
	 * @param Earlier_Or_Later
	 *            �� ��ʾ�ٻ����ö�٣�EARLIER - ��ȡ�����ߣ� LATER - ��ȡ�ϳ���
	 * 
	 * @return
	 */
	public static Date getTimeByCompare(Date time1, Date time2, GetTimeType Earlier_Or_Later) {
		// �������Ϊ�յ����, ����һ��ֵΪ��
		if (time1 == null || time2 == null) {
			if (time1 != null) {
				return time1;
			}

			if (time2 != null) {
				return time2;
			}

			// ����ʱ����Ϊ�գ��򷵻ؿ�
			if (time1 == null && time2 == null) {
				return null;
			}
		}
		
		// ȡ�ý�����
		if(Earlier_Or_Later == GetTimeType.LATER){
			if(time1.getTime() > time2.getTime()){
				return time1;
			}else{
				return time2;
			}
		}else{
			// ȡ�ýϽ�����
			if(time1.getTime() > time2.getTime()){
				return time2;
			}else{
				return time1;
			}
		}
	}

	/**
	 * �Ƚ�����ʱ��Ĵ�С��
	 * 
	 * @param time1
	 * @param time2
	 * @return ǰ�߱Ⱥ�����(��time1��time2��)������TimeComparisonResultType.LATER��
	 *         ������ȷ���TimeComparisonResultType.EQUALS��
	 *         ���򷵻�TimeComparisonResultType.EARLIER
	 */
	public static TimeComparisonResultType laterOrEarlier(Date time1, Date time2) {
		// ��������Ϊ�յ����
		if (time1 == null || time2 == null) {
			return TimeComparisonResultType.INVALID_INPUT;
		}

		Calendar cTime1 = new GregorianCalendar();
		cTime1.setTime(time1);
		cTime1.set(Calendar.SECOND, 0);

		Calendar cTime2 = new GregorianCalendar();
		cTime2.setTime(time2);
		cTime2.set(Calendar.SECOND, 0);

		int compareResult = cTime1.compareTo(cTime2);
		
		TimeComparisonResultType tcrt = compareResult == 0 ? TimeComparisonResultType.EQUALS
				: (compareResult > 0 ? TimeComparisonResultType.LATER : TimeComparisonResultType.EARLIER);

		return tcrt;

	}

	/**
	 * ���1900-01-01 00:00:00��Ϊ�������ڱ�ʶ��
	 * 
	 * @return
	 */
	public static Date getNullDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(CHINA_TIME_ZONE));
		Date nullDate = null;

		try {
			nullDate = sdf.parse("1900-01-01 00:00:00");

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return nullDate;
	}

	/**
	 * ���㱻������ͬһMachinePlan�����������ʱ����
	 * 
	 * @param previousTask
	 *            - ǰһ������
	 * @param nextTask
	 *            - ��һ������
	 * @return - ��ÿ10����1�ֵĹ��򷵻ؼ��ֵ����������ȡ��, ��������������֮��������ٸ�10����
	 */
	public static int getTheGapBetweenTasks(Task previousTask, Task nextTask) {
		if (previousTask == null || // ǰ����Ϊ��
				previousTask.getPlanEndTimeL() == null ||
				previousTask.getPlanEndTimeL() == 0L || // ǰ����Ľ���ʱ��Ϊ��
				previousTask.getMachinePlan() == null || // ǰ����Ļ�̨�ƻ�Ϊ��
				nextTask == null || // ������Ϊ��
						nextTask.getPlanStartTimeL() == null ||
				nextTask.getPlanStartTimeL() == 0L || // ������Ŀ�ʼʱ��Ϊ��
				nextTask.getMachinePlan() == null || // ������Ļ�̨�ƻ�Ϊ��
				!previousTask.getMachinePlan().equals(nextTask.getMachinePlan()) // ǰ����������Ļ�̨�ƻ���Ϊͬһ��̨�ƻ�

		)
			return 0;

		Long previousTime = previousTask.getPlanEndTimeL();
		Long nextTime = nextTask.getPlanStartTimeL();

		// ����������ʱ��ķ�����
		//long gapMin = Math.abs(getTimeGap(previousTime, nextTime, Calendar.MINUTE));
		Long gapMinL = (previousTime - nextTime); // Math.abs(getTimeGap(previousTime, nextTime, Calendar.MINUTE));

		int gap10Min = (int) Math.ceil((double) gapMinL / 10L);

		return gap10Min;

	}


	/**
	 * ��ʽ��ʱ��
	 * 
	 * @param sourceDatetime
	 * @return
	 */
	public static Date getFormatedDatetime(Date sourceDatetime) {
		if(sourceDatetime == null){
			return null;
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String soureTimeString = sdf.format(sourceDatetime);
		ParsePosition pos = new ParsePosition(0);
		Date destTime = sdf.parse(soureTimeString, pos);

		return destTime;
	}
	
	/*
	@SuppressWarnings("null")
	public static Date getDatetime(Long timeSeed){
		if(timeSeed == null || timeSeed == 0L){
			return null;
		}
		
		Date newDate = new Date(timeSeed);
		newDate.setTime(timeSeed + newDate.getTimezoneOffset() * 60L * 1000L + 480L * 60L * 1000L);
		
		return newDate;
		 
	}
	*/

	/**
	 * ȡ��һ�����ı�׼��
	 * @param numericList
	 * @return
	 */
	public static int getStandardDevition(List<Integer> numericList)
	{
		// ȡ�ø�������ƽ��ֵ
		IntSummaryStatistics stats = numericList.stream().mapToInt((x) -> x).summaryStatistics();
		double average = stats.getAverage();
		
		// �����׼��
		double sum = 0;
		for (int i : numericList) {
			sum += Math.sqrt(((double) i - average) * (i - average));
		}
		// ��׼��ȡ��
		int sd = (int)(sum / numericList.size());
		
		return sd;
	}
	
	/**
	 * update the task start time, and
	 * update its following tasks's start time in the same lot, and
	 * update its following tasks's start time in the same machine plan.
	 * @param task
	 */
	/*
	public static void updateTaskStartTime(Task task){
		
		if(task == null || task.getMachinePlan() == null)
			return;
		
		// clear the updated task id list as the checking.
		updatedTaskIDs.clear();
		updatedTaskIDs.add(task.getId());
		
		Long startTime = CommonFunctions.getTaskStartTimeL(task);
		
		
		startTime += Common.INTERVAL_OF_TASK ; //Common.getTimeOffset(startTime, Common.INTERVAL_OF_TASK, Calendar.SECOND);
		
		task.setPlanStartTimeL(startTime);
		
		if(task.getNextTaskInSameLot()!= null){
			updateTaskStartTimeInLot(task.getNextTaskInSameLot());
		}
		
		if(task.getNextTask() != null){
			updateTaskStartTimeInMachinePlan(task.getNextTask());
		}
	}
	*/
	
	/**
	 * update the task start time in a lot start with parameter task.
	 * @param task
	 */
	/*
	private static void updateTaskStartTimeInLot(Task task){
	
		if(task == null || task.getMachinePlan() == null)
			return;
		
		if (updatedTaskIDs.contains(task.getId())) {
			return;
		} else {
			updatedTaskIDs.add(task.getId());
		}
		
		// get the start time of task
		Long startTime = CommonFunctions.getTaskStartTimeL(task);
		startTime += Common.INTERVAL_OF_TASK;
		
		scoreDirector.beforeVariableChanged(task, "planStartTimeL");
		task.setPlanStartTimeL(startTime);
		scoreDirector.afterVariableChanged(task, "planStartTimeL");
		
		// update the following task start time in same lot.
		if(task.getNextTaskInSameLot() != null){
			updateTaskStartTimeInLot(task.getNextTaskInSameLot());
		}
		
		// update the following task start time in same machine plan.
		if(task.getNextTask() != null){
			updateTaskStartTimeInMachinePlan(task.getNextTask());
		}
	}
	*/
	
	/**
	 * update the tasks in a machine plan what start with parameter task. 
	 * @param task
	 * 
	 */
	/*
	private static void updateTaskStartTimeInMachinePlan(Task task){

		if(task == null || task.getMachinePlan() == null)
			return;
		
		if (updatedTaskIDs.contains(task.getId())) {
			return;
		} else {
			updatedTaskIDs.add(task.getId());
		}
		
		Long startTime = CommonFunctions.getTaskStartTimeL(task);
		startTime += Common.INTERVAL_OF_TASK;
		
		scoreDirector.beforeVariableChanged(task, "planStartTimeL");
		task.setPlanStartTimeL(startTime);
		scoreDirector.afterVariableChanged(task, "planStartTimeL");
		
		if (task.getNextTask() != null) {
			updateTaskStartTimeInMachinePlan(task.getNextTask());
		}

		if (task.getNextTaskInSameLot() != null) {
			updateTaskStartTimeInLot(task.getNextTaskInSameLot());
		}
	}
	*/
	
	/**
	 * 从一个时间的getTime函数值，得到该时间是在第几分钟数
	 * @param timeL
	 * @return
	 */
	public static Long getTime_Minute(Long timeMinSecL){
		Long timeMinute = timeMinSecL / 1000 / 60;
		return timeMinute;
	}
	
	
	private static String getTimeStringByMinuteLong(Long timeMinL){
		if(timeMinL == null){
			return null;
		}
		// 转为毫秒
		long timeMilSec = timeMinL * 60 * 1000;
		Date date = new Date(timeMilSec);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(CHINA_TIME_ZONE));
		String dateStr = sdf.format(date);
		
		return dateStr;
	}
	
	/**
	 * 根据分钟数值，取得时间
	 * @param timeMinL : 时间的分钟数
	 * @return
	 */
	public static Date getTimeByMinuteLong(Long timeMinL){
		if(timeMinL == null){
			return null;
		}
		// 获得日期字符串
		String dateStr = getTimeStringByMinuteLong(timeMinL);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone(CHINA_TIME_ZONE));
		
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return date;
	}
	
	/**
	 * 获得类的名称（去除包路径)
	 * @param fullName
	 * @return
	 */
	public static String getClassName(String fullName){
		return fullName.replaceAll(".*\\.", "");
	}

}
