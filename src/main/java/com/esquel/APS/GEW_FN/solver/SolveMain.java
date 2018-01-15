package com.esquel.APS.GEW_FN.solver;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;

import com.esquel.APS.GEW_FN.domain.Attribute;
import com.esquel.APS.GEW_FN.domain.Broken;
import com.esquel.APS.GEW_FN.domain.Constraint;
//import com.esquel.APS.GEW_FN.domain.DosageScope;
import com.esquel.APS.GEW_FN.domain.Lot;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.MachinePlanProcess;
import com.esquel.APS.GEW_FN.domain.MatchRule;
import com.esquel.APS.GEW_FN.domain.NonproductionInfo;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSummary;
import com.esquel.APS.GEW_FN.domain.PlanningResult;
import com.esquel.APS.GEW_FN.domain.ScoreSummary;
import com.esquel.APS.GEW_FN.domain.Shift;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.TaskCost;
import com.esquel.APS.GEW_FN.domain.TaskSort;
import com.esquel.APS.GEW_FN.domain.Word;
import com.esquel.APS.GEW_FN.domain.enums.EntityCategory;
import com.esquel.APS.GEW_FN.domain.enums.ExceptionLevel;
import com.esquel.APS.GEW_FN.domain.enums.Lot_Break_Type;
import com.esquel.APS.GEW_FN.domain.enums.PlanningCategory;
import com.esquel.APS.GEW_FN.domain.enums.TaskType_Location_In_Lot;
import com.esquel.APS.GEW_FN.solver.interactor.NonproductionAssertionInteractor;
import com.esquel.APS.GEW_FN.solver.interactor.TaskMachinePlanAssertionInteractor;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;
import com.esquel.APS.Helpers.ListUtils;
import com.esquel.APS.Helpers.XMLHelper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

/**
 * ��������������
 * 
 * @author ZhangKent
 *
 */
public final class SolveMain {
	// public static List<TaskInfo> _firstTaskOfMachinePlan = new
	// ArrayList<TaskInfo>();
	public static ScoreDirector<TaskAssignmentSolution> _scoreDirector;
	@Deprecated
	public static ScoreDirector<TaskAssignmentSolution> _workingScoreDirector;
	private static ScoreDirectorFactory<TaskAssignmentSolution> _scoreDirectorFactory;
	// private static HardSoftScoreHolder scoreHolder;
	public static List<Task> _taskListToPlan = new ArrayList<Task>();
	private static List<Task> _outstandTasks = new ArrayList<Task>();	// the tasks what its information is incompleteness.
//	private static List<Task> _conditionInsufficientTasks = new ArrayList<Task>();	// the task what there isn't feasible machine plan
	private static List<Task> _planningFailureTasks = new ArrayList<Task>();	// the task what broke hard constraint(s).
	private static List<Task> _taskListPlanned = new ArrayList<Task>();
	public static List<Lot> _lots = new ArrayList<Lot>();
	
	private static HashMap<Long, List<NonproductionTask>> _nonproductionTaskListMapping = new HashMap<Long, List<NonproductionTask>>();
	private static HashMap<Long, NonproductionTaskSummary> _nonproductionTaskSummaries = new HashMap<Long, NonproductionTaskSummary>();
	
	public static List<MachinePlan> _machinePlanList = new ArrayList<MachinePlan>();
	
	public static List<NonproductionTaskSet> _nonpoductionTaskSets = new ArrayList<NonproductionTaskSet>();
//	private static List<Constraint> _constraints = new ArrayList<Constraint>();
	public static TaskAssignmentSolution _pendingTaskAssignment = new TaskAssignmentSolution();
	public static TaskAssignmentSolution _solvedTaskAssignment = null;// = new
//	private static TaskAssignmentSolution _bestAssignmentSolution = null;
	
	private static List<NonproductionTask> _nonproductionTaskBroke = new ArrayList<NonproductionTask>();
	private static List<TaskCost> _taskCost = new ArrayList<TaskCost>();
	private static List<Broken> _brokens = new ArrayList<Broken>();
	private static List<TaskSort> _taskSorts = new ArrayList<TaskSort>();
	private static List<ScoreSummary> _scoreSummaries = new ArrayList<ScoreSummary>();
	private static Long _minutesSpent;

	
	private final static String CHINA_TIME_ZONE = "Asia/Shanghai";
	
	private final static int SCORE_LEVEL_HARD = 0;
	private final static int SCORE_LEVEL_MEDIUM = 1;
	private final static int SCORE_LEVEL_SOFT = 2;
	
	private final static String SCORE_TAG_HARD = "H";
	private final static String SCORE_TAG_MEDIUM = "M";
	private final static String SCORE_TAG_SOFT = "S";
	
	public static void StartSolve(String jobId, String planningTarget) {
		try {

			// 设置当前Plan的JobId
			Common.set_jobId(jobId);
			
			// load and initialize objects
			//initializeData(jobId);
			DataManager dataManager = DataManager.getInstance(planningTarget);
			
			
			// plan the tasks in solution task list
			planTasks(dataManager);

		} catch (APSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// clear the invalid tasks after planning, the tasks what broke rule be labeled as invalid tasks.
		clearPlanningFailureTasks();
		
		updateTaskListOfMachinePlan();
		
		
		// calculate the duration and cost by non-production tasks.
		createIndexesOfPlanResult();
		
		// update the task's start time finally.
		updateStartTime();
		
//		checkTaskSequence();
		VerifyResult();
		
		// update the time fields from long fields.
		updateDatetimeFieldsToTime();
		
		// save and print out the planning results, including planning result and logs.
		savePlanningResult(jobId);
		
		/*
		if(_taskListPlanned.stream().filter(x -> x.getId() == 1939).findAny().orElse(null) == null){
			System.out.println("After clear, not exist");
		}else{
			System.out.println("After clear, exist");
		}
		*/
	}
	
	private static void VerifyResult(){
		List<String> verifyMsg = new ArrayList<String>();
		
		verifyMsg.addAll(PlanningVerification.checkMachinePlanMatching(_taskListPlanned));//rule "MachinePlan Task Compare"
		verifyMsg.addAll(PlanningVerification.checkArriavleTimeAndPlannedStartTime(_taskListPlanned)); // rule "Match arrival time"
		verifyMsg.addAll(PlanningVerification.checkTaskTimeOverlap(_taskListPlanned, _machinePlanList)); // rule "Avoid Tasks overlap"
		verifyMsg.addAll(PlanningVerification.checkTaskSequenceInLot(_taskListPlanned)); // rule "Task Sequence"
		verifyMsg.addAll(PlanningVerification.checkIfTaskEndTimeLaterThanMachinePlanEndTime(_taskListPlanned)); //rule "Later than machinePlan end time"
		verifyMsg.addAll(PlanningVerification.checkShaoMaoNonproductionTask(_machinePlanList));
		
		System.out.println("The following task is invalid:");
		
		for(String msg : verifyMsg){
			System.out.println(msg);
		}
		
	}
	
	/**
	 * loads and initializes the objects
	 * @throws APSException 
	 * @throws ParseException 
	 */
	public static void initializeData(String jobId) throws APSException, ParseException {
		
		// 设置当前Plan的JobId
		//Common.set_jobId(jobId);
		
		// Read data from xml files
		loadObjects();
	
		
		// do some initializations for task, machine plan and non-production
		initilizeObjects();
		
		
	//	SolutionPreInitialization.initializeTaskMachinePlan(_taskListToPlan);
		
	}
	
	/**
	 * load the data from xml files.
	 * 
	 */

	private static void loadObjects() {
		XMLHelper.loadObjects();
		_machinePlanList = XMLHelper.getMachinePlanList();
		_taskListToPlan = XMLHelper.getTaskList();
		_nonpoductionTaskSets = XMLHelper.getNonproductionTaskSetList();
//		_constraints = XMLHelper.getConstraintList();
//		Common.set_words(XMLHelper.getWordList());
		_taskSorts = XMLHelper.getTaskSortList();
	}

	/**
	 * Initialize the data, including tasks, process, machine plan....
	 * @throws APSException 
	 * @throws ParseException 
	 */
	private static void initilizeObjects() throws APSException, ParseException {
		
		

		// connect the non-production task and its set
		relateNonproductionTaskAndSet();
		
		// create the machine rules between tasks and machines
		createRuleAndInteractors();
				
		// Initialize the interactors for Task - MachinePlan and Nonproduction task checking
		initAssertionObject();

		// initialize the tasks.
		initTasks();
		
		// filter MacinePlan
		//filterMachinePlan();
	}
	
	/*
	private static void checkMachinePlan(){
		if(_machinePlanList == null || _machinePlanList.isEmpty()){
			return;
		}
		
		for(MachinePlan machinePlan : _machinePlanList){
			// 开始时间晚于结束时间
			if(machinePlan.)
		}
	}
	*/
	
	/**
	 * Create the non-production task and its set
	 */
	private static void relateNonproductionTaskAndSet(){
		for(NonproductionTaskSet nonproductionTaskSet : _nonpoductionTaskSets){
			for(NonproductionTask nonproductionTask : nonproductionTaskSet.getNonproductionTasks()){
				nonproductionTask.setNonproductionTaskSet(nonproductionTaskSet);
			}
		}
	}
	
	/**
	 * Initialize the Match rules for Task - MachinePlans and Nonproduction tasks
	 * @throws APSException 
	 */
	private static void createRuleAndInteractors() throws APSException {

		// machine plan match rules.
		initMachinePlanRuleAndInteractors();

		// non-production tasks set match rules
		initNonproductionRuleAndInteractors();

	}
	
	/**
	 * 生成机台计划与任务匹配的Rule与Interactor
	 * @throws APSException
	 */
	private static void initMachinePlanRuleAndInteractors() throws APSException {
		for (MachinePlan machinePlan : _machinePlanList) {

			if (machinePlan == null) {
				throw new APSException("机台计划列表中存在为空的机台计划");
			}

			if (machinePlan.getProcesses() == null || machinePlan.getProcesses().isEmpty()) {
				// throw new APSException("机台计划列表中存在对应工序为空的机台计划");
				continue;
			}

			List<MatchRule> rules = new ArrayList<MatchRule>();

			// create a match rule for each machine plan process
			for (MachinePlanProcess machinePlanProcess : machinePlan.getProcesses()) {
				// 若没有机台限制，则表示所有工序均可加工
				// String conditionString = "1 == 1";

				if (machinePlanProcess == null) {
					// conditionString =
					// machinePlanProcess.getconditionExpression();
					throw new APSException("机台计划[" + machinePlan + "]的适用工序为空.");
				}

				MatchRule matchRule = new MatchRule(machinePlanProcess.getId(), machinePlan.getId(),
						machinePlanProcess.getconditionExpression(), PlanningCategory.MACHINE_PLAN);
				rules.add(matchRule);
			}

			// create a interactor for each machine plan.
			TaskMachinePlanAssertionInteractor interactor = new TaskMachinePlanAssertionInteractor(machinePlan.getId(),
					rules, machinePlan);
			machinePlan.setInteractor(interactor);
		}
	}
	
	/**
	 * 初始化非生产任务的规则与Interactor
	 * @throws APSException 
	 */
	private static void initNonproductionRuleAndInteractors() throws APSException
	{
		for (NonproductionTaskSet nonproductionTaskSet : _nonpoductionTaskSets) {
			if (nonproductionTaskSet == null || nonproductionTaskSet.getNonproductionTasks().isEmpty()) {
				throw new APSException("非生产任务集中，存在生产任务为空的情况.");
			}

			for (NonproductionTask nonproductionTask : nonproductionTaskSet.getNonproductionTasks()) {

				// 若非生产任务没有设定条件，则表示所有情况均需要加入该非生产任务
				// String conditionString = "1 == 1";

				if (nonproductionTask.getConditionString() == null || nonproductionTask.equals("")) {
					// conditionString = nonproductionTask.getConditionString();
					throw new APSException("非生产任务[" + nonproductionTask + "]的生成条件为空.");
				}

				// create a match rule for non-production task
				List<MatchRule> rules = new ArrayList<MatchRule>();

				MatchRule matchRule = new MatchRule(nonproductionTask.getId(),
						(Object) nonproductionTaskSet.getProcessCode(), nonproductionTask.getConditionString(),
						PlanningCategory.NONPRODUCTION_TASK);

				rules.add(matchRule);

				// create a interactor for each non-production task.
				NonproductionAssertionInteractor interactor = new NonproductionAssertionInteractor(
						nonproductionTask.getId(), rules, nonproductionTask);
				nonproductionTask.setInteractor(interactor);

			}
		}
	}

	
	/**
	 * Initialize the Assertion instance(it's a singleton). to initialize the
	 * Assertion instance need to pass the assertion interactors, including
	 * machine plan and non-production interactors.
	 * 
	 * @throws APSException
	 * 
	 */
	private static void initAssertionObject() throws APSException {
		// get the interactors from Task - MachinePlan objects
		List<TaskMachinePlanAssertionInteractor> machinePlanInteractors = initAssertionForMachinePlan();

		// get the interactors from non-production task objects
		List<NonproductionAssertionInteractor> nonproductionInteractors = initAssertionForNonproductionTask();

		// Initialize the Assertion object.
		ConditionAssertion.getInstance(machinePlanInteractors, nonproductionInteractors);

	}

	private static List<TaskMachinePlanAssertionInteractor> initAssertionForMachinePlan() {

		List<TaskMachinePlanAssertionInteractor> machinePlanInteractors = new ArrayList<TaskMachinePlanAssertionInteractor>();

		for (MachinePlan machinePlan : _machinePlanList) {
			if (machinePlan.getInteractor() == null) {
				continue;
			}

			machinePlanInteractors.add(machinePlan.getInteractor());
		}

		return machinePlanInteractors;
	}

	private static List<NonproductionAssertionInteractor> initAssertionForNonproductionTask() throws APSException {
		List<NonproductionAssertionInteractor> nonproductionInteractors = new ArrayList<NonproductionAssertionInteractor>();
		for (NonproductionTaskSet nonproductionTaskSet : _nonpoductionTaskSets) {

			if (nonproductionTaskSet == null) {
				throw new APSException("非生产任务集[" + nonproductionTaskSet + "]为空");
			}

			if (nonproductionTaskSet.getNonproductionTasks() == null
					|| nonproductionTaskSet.getNonproductionTasks().isEmpty()) {
				throw new APSException("非生产任务集[" + nonproductionTaskSet + "]的非生产任务列表为空");
			}

			for (NonproductionTask nonproductoinTask : nonproductionTaskSet.getNonproductionTasks()) {
				if (nonproductoinTask.getInteractor() == null) {
					// throw new APSException("非生产任务[" + nonproductoinTask +
					// "]的Interactor为空");
					continue;
				}

				nonproductionInteractors.add(nonproductoinTask.getInteractor());
			}
		}

		return nonproductionInteractors;
	}

	/**
	 * Initialize the task, including:
	 *  1. reset arrival time; 
	 *  2. initialize and connect attributes; 
	 *  3. create lots by tasks;
	 *  4. create a chain with tasks which have the same lot;
	 *  5. find and collect the task which is incomplete;
	 *  6. find and collect the task which does not exist feasible machine plan;
	 *  7. remove the tasks from planning task list what collect by step 5 and 6;
	 *  8. initialize the machine plan for the tasks.   
	 * @throws APSException 
	 * @throws ParseException 
	 */
	private static void initTasks() throws APSException, ParseException {
		//1. Reset the time fields.
		resetFields();

		//2. connect the task and its attributes
		relateTaskAttribute();
		
		// create the lot list by task list, 
		createLots();
		
		// initialize the date time fields to long
		updateDatetimeFieldsToLong();
		
		//check if the machine plans is OK
		checkMachinePlan();
		
		// check the lots are there invalid cases.
		checkLot();
		
		//5. collect the tasks what invalid, save in _crashedTasks
		collectCrashedTasks();

		// find the machine plans for each task what could handle particular task.
		initMachinePlanOfTask();
		
		// initialize the end time of the shifts in the machine plan 
		initShifts();
		
		//List<Task> t1 = _taskListToPlan.stream().filter(x -> x.getId() == 509).collect(Collectors.toList());

		// the arrival time is later now than 2 days, remove it.
		collectFarTasks();

		// create the nonproduction task mapping list
		initNonproductionTaskSetInUse();

		// some tasks what are insufficient condition to plan, remove it from
		// the planning list.
		clearInvalidTasks();
		
		//List<Task> t2 = _taskListToPlan.stream().filter(x -> x.getId() == 509).collect(Collectors.toList());
		
		// sort task by custom rule
		sortTasks();

	}
	
	/**
	 * 检查不合的机计划
	 */
	private static void checkMachinePlan(){
		List<MachinePlan> invalidMachinePlans = new ArrayList<MachinePlan>();
		for(MachinePlan machinePlan : _machinePlanList){
			// 小于10分钟的机台计划不合理
			if(machinePlan.getEndTimeL() - machinePlan.getStartTimeL() < 10){
				invalidMachinePlans.add(machinePlan);
			}
		}
		
		_machinePlanList.removeAll(invalidMachinePlans);
	}

	/**
	 * 1. 对NonproductionTaskSet进行过滤，将没有工序使用的NonproductionTaskSet清除
	 * 2. 重设每个NonproductionTaskSet下的每个NonproduciotnTask的ID，ID为Set的ID加上Task的ID（字符连接）
	 * @return
	 */
	private static void initNonproductionTaskSetInUse() {
		// 找出工序中有用到的NonproductionTaskSet
		List<String> processCodes = _taskListToPlan.stream().map(Task::getProcessCode).distinct().collect(Collectors.toList());
		
		// 只取有工序用到的NonproductionTaskSet列表
		List<NonproductionTaskSet> nonProducttionTaskSets = _nonpoductionTaskSets.stream().filter(x -> processCodes.contains(x.getProcessCode())).collect(Collectors.toList());
		
		
		// 更新正在使用的NonproductionSet
		_nonpoductionTaskSets = nonProducttionTaskSets;
		
		
		// 创建前后任务之间的非生产任务
		createNonproductionTaskMapping();
		
	}
	
	
	/**
	 * 创建前后任务之间的非生产任务映射列表，分别有两个列表： 1. _nonproductionTaskSummaries：
	 * NonproductionTaskSummary的列表，表示两个任务之间，满足非生产任务时，表达这些非生产任务的信息归纳，分数、持续时间加总，名称连接。用于在Planning过程中进行分数与时间的计算。
	 * 2. _nonproductionTaskListMapping：
	 * 用一个hashmap来保存每两个任务之间可用的非生产任务列表。用于完成Planning之后进行非生产任务的分配。
	 */
	private static void createNonproductionTaskMapping() {

		// 对有可能排在同一机台下的所有任务，进行两两组合，并找出其适用的非生产任务ID列表
		for (MachinePlan machinePlan : _machinePlanList) {

			// 取得适用于指定机台计划的所有任务对应的TaskAssignment
			List<Task> taskMacthMachinePlan = _taskListToPlan.stream()
					.filter(x -> x.getUsableMachinePlan() != null && x.getUsableMachinePlan().isEmpty() == false && x.getUsableMachinePlan().contains(machinePlan)).collect(Collectors.toList());

			// 对每个任务的TaskAssignment取笛卡尔积，对其中每对元素进行双向检测
			for (Task leftTask : taskMacthMachinePlan) {
		
				for (Task rightTask : taskMacthMachinePlan) {
				/*
					if(leftTask.getId() == 1191 && rightTask.getId() == 1325){
						System.out.println("test");
					}
					
					if(rightTask.getId() == 1191 && leftTask.getId() == 1325){
						System.out.println("test");
					}
				*/
					// 正向检测
					createNonproductionTaskMappingAndSummaries(leftTask, rightTask);

					// 反向检测
					createNonproductionTaskMappingAndSummaries(rightTask, leftTask);
				}
			}
		}
	}
	
	/**
	 * 检查前后任务并生成NonproductionTaskMapping与NonproducitonTaskSummaries
	 * @param fromTA
	 * @param toTA
	 */
	private static void createNonproductionTaskMappingAndSummaries(Task fromTask, Task toTask){
		
		List<NonproductionTask> nonproductionTasksMatch = CommonFunctions.getActiveNonProductionTasks(fromTask, toTask, _nonpoductionTaskSets);
		if (nonproductionTasksMatch == null || nonproductionTasksMatch.isEmpty()){
			return;
		}
		
		// 生成前后任务的组合Key,用作NonproductionTask Mapping的ID
		Long tasksID_Key = Long.valueOf(fromTask.getId().toString() + toTask.getId().toString());
		
		// 生成非生产任务的Summary
		NonproductionTaskSummary nonproductionTaskSummary = new NonproductionTaskSummary();
		nonproductionTaskSummary.setId(tasksID_Key);

		// 对满足条件的NonproductionTask列表的Name,Score及Duration字段进行加总，并保存到NonproductionTaskSummary中去
		Long scoreSum = 0L;
		Long durationSum = 0L;
		String nonproducitonName = "";

		for (NonproductionTask nonprodTask : nonproductionTasksMatch) {
			scoreSum += nonprodTask.getScore();
			durationSum += nonprodTask.getDuration();
			if (nonproducitonName == "") {
				nonproducitonName = nonprodTask.getName();
			} else {
				nonproducitonName += ", " + nonprodTask.getName();
			}
		}
		
		Long interval = 0L;
		
		 // 多个非生产任务之间添加一个时间间隔，若只有一个非生产任务，则不需要添加
		if(nonproductionTasksMatch.size() > 1){
			interval = nonproductionTasksMatch.size() *  Common.INTERVAL_OF_TASK;
			interval -= Common.INTERVAL_OF_TASK; //去除最后一个间隔
		}
		durationSum += interval;
		
		nonproductionTaskSummary.setScore(scoreSum);
		nonproductionTaskSummary.setDuation(durationSum);
		nonproductionTaskSummary.setNonproductionNames(nonproducitonName);

		_nonproductionTaskSummaries.put(tasksID_Key, nonproductionTaskSummary);
		_nonproductionTaskListMapping.put(tasksID_Key, nonproductionTasksMatch);
	}


	
	/**
	 * Reset the time fields to null, including arrival time, delivery time, 
	 * due to the XML formating request, the value of a node must be a date time format, so fills the date 1900-01-01 00:00:00 while the arrival time is null
	 * @throws APSException 
	 */
	private static void resetFields() throws APSException {

		for (Task task : _taskListToPlan) {
		
			
			if (task.getArrivalTime() != null && task.getArrivalTime().equals(Common.getNullDate())) {
				task.setArrivalTime(null);
			}

			if (task.getDeliveryTime() != null && task.getDeliveryTime().equals(Common.getNullDate())) {
				task.setDeliveryTime(null);
			}

			if (task.getTaskRequestFinishTime() != null && task.getTaskRequestFinishTime().equals(Common.getNullDate())) {
				task.setTaskRequestFinishTime(null);
			}

			if (task.getDuration() == null || task.getDuration() <= 0L) {
				throw new APSException("所有任务的持续时间必须大于或等于1,但任务[" + task + "]的持续时间为空，或小于等于0");
			}

			if (task.getMaxWaitingTime() == null || task.getMaxWaitingTime() < 0L) {
				task.setMaxWaitingTime(0L);
			}

			if (task.getMinWaitingTime() == null || task.getMinWaitingTime() < 0L) {
				task.setMinWaitingTime(0L);
			}
			
			if(task.getNextTaskID() == null || task.getNextTaskID() < 0L){
				task.setNextTaskID(0L);
			}

			//task.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Not_Break);
			task.setException(null);
			
		}
	}
	
	
	
	/**
	 * Create the relation ship between task and its attribute
	 * @throws APSException 
	 */
	private static void relateTaskAttribute() throws APSException {
		// Initialize attributes(some attributes need to be create by logic)
		// initSpecialAttribute();
		for (Task task : _taskListToPlan) {

			/*
			 * if(task.getAttributeRequests() == null ||
			 * task.getAttributeRequests().isEmpty()){ throw new
			 * APSException("任务" + task + "的工艺参数列表为空"); }
			 */
			boolean invalid = false;
			for (Attribute attr : task.getAttributeRequests()) {
				
				if (attr == null) {
					invalid = true;
					break;
					//throw new APSException("任务" + task + "的工艺参数" + attr + "为空");
				}

				attr.setTask(task);
			}
			
			if(invalid){
				//task.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect);
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]的属性为空."));
			}
		}
	}
	

	/**
	 * create the lots and the relation ship of Task - Lot
	 * @throws APSException 
	 */
	private static void createLots() throws APSException {
		// 检查是否有任务的Lot ID为空或为0
		List<Task> tasksNullLot = _taskListToPlan.stream().filter(x -> x.getLotId() == null || x.getLotId() == 0L).collect(Collectors.toList());
		if (tasksNullLot != null && (!tasksNullLot.isEmpty())) {
			throw new APSException("所有任务均应该存在一个非零的LotID,但以下任务的LotID为空或为零:\r" + tasksNullLot);
		}

		List<Long> lotIds = _taskListToPlan.stream().map(Task::getLotId).distinct().collect(Collectors.toList());
		for (Long lotId : lotIds) {
			
			List<Task> taskInLots = _taskListToPlan.stream().filter(x -> x.getLotId().equals(lotId))
					.collect(Collectors.toList());
			if (taskInLots == null || taskInLots.isEmpty()) {
				throw new APSException("所有的批次都应该至少有一个任务，但批次ID：[" + lotId + "]的任务为空.");
			}

			Lot lot = new Lot(lotId, taskInLots);

			_lots.add(lot);
		}
	}

	/**
	 * check if the attributes is duplicate
	 */
	private static boolean checkAttribute(Task task) {
		List<String> attrName = new ArrayList<String>();
		boolean duplicate = false;
		for (Attribute attribute : task.getAttributeRequests()) {
			if (attrName.contains(attribute.getName())) {
				duplicate = true;
				break;
			} else {
				attrName.add(attribute.getName());
			}
		}
		
		return duplicate;

	}
	
	/**
	 * Checks and collects the tasks that invalid. 
	 * 
	 */
	private static void collectCrashedTasks() {
		if (_taskListToPlan == null || _taskListToPlan.isEmpty())
			return;

		// check each task in the list
		for (Task task : _taskListToPlan) {

			boolean checkOk = checkTask(task);
			
			//TaskMachinePlanBrokenType brokenType = checkTask(task);
			//task.setTaskMachinePlanBroken(brokenType);

			// if the task is invalid, save in the list _crashedTasks
			if (!checkOk) {
				_outstandTasks.add(task);
			}
		}
	}
	
	/**
	 * 移除所到达时间比其机台的结束时间还晚的任务.
	 */
	private static void collectFarTasks(){
		
		for(Task task : _taskListToPlan){
			
			//if(task.getTaskMachinePlanBroken() != TaskMachinePlanBrokenType.Not_Break){
			// 原来已经有异常，无需进一步判断
			if(task.getException() != null){	
				continue;
			}
				
			if(task.getArrivalTimeL() == null || task.getArrivalTimeL() == 0L){
				continue;
			}
				
			if(task.getUsableMachinePlan() == null || task.getUsableMachinePlan().isEmpty()){
				continue;
			}
			
			// 取得该任务可用机台的最晚完成时间
			 List<Long> endTimeList = task.getUsableMachinePlan().stream().map(MachinePlan::getEndTimeL).collect(Collectors.toList());
			 LongSummaryStatistics stats = endTimeList.stream().mapToLong((x) -> x).summaryStatistics();
			 Long maxEndTimeL = stats.getMax();
			 
			 // 到达时间比可用机台中最近的结束时间晚，则不参与本次排产
			if(task.getArrivalTimeL() > maxEndTimeL){
				//task.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Arriavle_Time);
				task.setException(new APSException(task, task.getId(), ExceptionLevel.INFO, "任务[" + task + "]的预计到达时间不在其可用的机台计划时间段内."));
				_outstandTasks.add(task);
			}
		}
	}
	
	/**
	 * checks if a lot is invalid:
	 * 1. do the tasks in the lot make a dead loop ?
	 * 2. is there the only one source task and sink task in a lot ?
	 * 3. Are the tasks in a lot between source task and sink task connected to be a chain.
	 * 4. if there some task linked 2 lots. 
	 * 5. is there only one task have arrival time(Source) and only one task what its next taskID is 0 (Sink).
	 * @param lot
	 * @return
	 * @throws APSException 
	 */
	private static void checkLot() throws APSException{

		for (Lot lot : _lots) {
			
			if(!isSourceSinkTaskExist(lot)){
				//lot.setBreakType(Lot_Break_Type.SOURCE_TASK_NOT_EXIST);
				//lot.getTasks().get(0).setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect);
				setTaskExceptionOfLot(lot, Lot_Break_Type.SOURCE_TASK_NOT_EXIST);
				continue;
			}

			if (isTaskDeadLoop(lot)) {
				//lot.setBreakType(Lot_Break_Type.DEAD_LOOP);
				//lot.getTasks().get(0).setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect);
				setTaskExceptionOfLot(lot, Lot_Break_Type.DEAD_LOOP);
				continue;
				//throw new APSException("批次[" + lot + "]中的任务存在死循环情况。");
			}

			if (isChainBroke(lot)) {
				//lot.setBreakType(Lot_Break_Type.CHAIN_BROKE);
				//lot.getTasks().get(0).setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect);
				setTaskExceptionOfLot(lot, Lot_Break_Type.CHAIN_BROKE);
				continue;
				//throw new APSException("批次[" + lot + "]中的任务存在断链情况。");
				
			}
			
			if(isAllArrivalTimeEmpty(lot)){
				//lot.setBreakType(Lot_Break_Type.ALL_TAKS_HAS_NULL_ARRIVAL_TIME);
				//lot.getTasks().get(0).setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_Invalid_Task_Arrival_Time_Empty);
				setTaskExceptionOfLot(lot, Lot_Break_Type.ALL_TAKS_HAS_NULL_ARRIVAL_TIME);
				continue;
				//throw new APSException("批次[" + lot + "]中的所有任务，其预计到达时间均为空。");
			}

			lot.setBreakType(Lot_Break_Type.PERFECT);
		}
	}
	
	/**
	 * 更新异常批次及其任务的异常对象
	 * @param lot
	 * @param lotBreakType
	 */
	private static void setTaskExceptionOfLot(Lot lot, Lot_Break_Type lotBreakType){
		lot.setBreakType(lotBreakType);
		
		String lotMessage = "";
		String taskMessage = "";
		switch (lotBreakType) {
		case SOURCE_TASK_NOT_EXIST: {
			lotMessage = "批次[" + lot + "]没有起始或结束任务.";
			taskMessage = "任务[/TASK/]所在批次[" + lot + "]没有起始或结束任务.";
			break;
		}
		case DEAD_LOOP: {
			lotMessage = "批次[" + lot + "]中的任务存在死循环.";
			taskMessage = "任务[/TASK/]所在批次[" + lot + "]中的任务存在死循环.";
			break;
		}
		case CHAIN_BROKE: {
			lotMessage = "批次[" + lot + "]中的任务无法形成链.";
			taskMessage = "任务[/TASK/]所在批次[" + lot + "]中的任务无法形成链.";
			break;
		}
		case ALL_TAKS_HAS_NULL_ARRIVAL_TIME: {
			lotMessage = "批次[" + lot + "]中的所有任务均没有预计到达时间.";
			taskMessage = "任务[/TASK/]所在批次[" + lot + "]中的所有任务均没有预计到达时间.";
			break;
		}
		default: {
			lotMessage = "";
			break;
		}

		}
	
		lot.setException(new APSException(lot, lot.getId(), ExceptionLevel.WARNING, lotMessage));
		String taskMessageBackup = taskMessage;
		for(Task task : lot.getTasks()){
		//	task.setTaskMachinePlanBroken(taskMachinePlanBroken);
			
			String taskMessageInstance = taskMessage.replace("/TASK/", task.toString()); 
			task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, taskMessageInstance));
			taskMessage = taskMessageBackup;
		}
	}
	
	private static boolean isTaskDeadLoop(Lot lot) {

		List<Long> taskIDs = new ArrayList<Long>();

		if (lot.getTasks().size() <= 1) {
			return false;
		}

		Task task = lot.getSourceTask();

		while (task != null) {

			if (taskIDs.contains(task.getId())) {
				return true;
			} else {
				taskIDs.add(task.getId());
			}

			task = task.getNextTaskInSameLot();
		}

		return false;

	}

	/**
	 * Check if the chain in a lot is broke
	 * @param lot
	 * @return
	 */
	private static boolean isChainBroke(Lot lot) {
		if(lot.getTasks().size() == 1){
			return false;
		}
		
		return lot.getTasks().stream().filter(x -> x.getTaskLocation() != TaskType_Location_In_Lot.SINK && x.getNextTaskInSameLot() == null ).findAny().orElse(null) != null;
	}
	
	/**
	 *  All of the tasks's arrival time are empty
	 * @param lot
	 * @return
	 */
	private static boolean isAllArrivalTimeEmpty(Lot lot){
		if(lot.getTasks() == null || lot.getTasks().isEmpty()){ 
			return false;
		}

		// 不存在一个Arrival Time非空的任务
		return (lot.getTasks().stream().filter(x -> x.getArrivalTimeL() != null && x.getArrivalTimeL() > 0L).findAny().orElse(null) == null);
				
	}
	
	/**
	 * check if there are the source and sink tasks in a lot.
	 * @param lot
	 * @return
	 */
	private static boolean isSourceSinkTaskExist(Lot lot){
		
		return (lot.getSourceTask() != null) && (lot.getSinkTask() != null);
	}
	
	
	/**
	 * Checks if is the task is invalid.
	 * are the critical fields not null, including TaskContent, lot, processNo, orderDeliveryTime, Duration, current_department, Quantity, GF_ID, Job_NO..
	 * are the task id in list is unique.
	 * is the  delivery time is not null.
	 * is the Max waiting time longer than min waiting time.
	 * is the arrival time is not null of a source task.
	 * is the arrival time is null of a non-source task.
	 * 
	 * @param task
	 * @return
	 */
	//private static TaskMachinePlanBrokenType checkTask(Task task) {
	private static boolean checkTask(Task task) {
		
		
		// 0. are the critical fields not null, including TaskContent, lot, processNo, orderDeliveryTime, Attributes, Duration, current_department, Quantity, GF_ID, Job_NO
		String checkingResult = checkTaskBodyInformation(task);
		if(checkingResult != null){
			
			
			task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, checkingResult));
			return false;
			//return TaskMachinePlanBrokenType.Broke_For_Invalid_Information_No_Perfect;
		}

		// is the Max waiting time longer than min waiting time.
		if (task.getMaxWaitingTime() < task.getMinWaitingTime()){
			task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]的最大等时间比最小等待时间小。"));
			return false;
			//return TaskMachinePlanBrokenType.Broke_For_Invalid_Idle_Time;
		}
		
		if(checkAttribute(task)){
			task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]存在重复属性。"));
			return false;
			//return TaskMachinePlanBrokenType.Broke_For_Invalid_Task_Attribute_Duplicate;
		}
		
		return true;

	//	return TaskMachinePlanBrokenType.Not_Break;
	}
	
	/**
	 * checks if the fields of a task is complete
	 * 
	 * @param task
	 * @return
	 */
	private static String checkTaskBodyInformation(Task task) {
		// are the critical fields not null, including TaskContent, lot,
		// processNo, orderDeliveryTime, Duration, current_department, Quantity,
		// GF_ID, Job_NO
		if (task.getLot() == null) {
			return "任务[" + task + "]的批次为空";
		}

		if (task.getProcessCode() == null || task.getProcessCode().equals("")) {
			return "任务[" + task + "]的工序编号为空";
		}
		
		if (task.getDuration() == null || task.getDuration() == 0L) {
			return "任务[" + task + "]的持续时间空";
		}
		
		if (task.getDepartment()== null || task.getDepartment().equals("")) {
			return "任务[" + task + "]的生产部门为空";
		}
		
		if(task.getGf_id() == 0L){
			return "任务[" + task + "]的品名ID为空";
		}
		

		return null;
	}
	
	/**
	 * Collects the tasks what could not be send to plan, including the crashed tasks and there are not feasible machine plan tasks.  
	 */
	private static void clearInvalidTasks() {
		
		
		//List<Lot> lotsToRemove = new ArrayList<Lot>();
		System.out.println("");
		System.out.println("######################################################");
		System.out.println("Task: ");
		System.out.println("Total:" + _taskListToPlan.size() + " tasks, " + _lots.size() + " lots.");
		
		//List<Task> brokeTasks = _taskListToPlan.stream().filter(x -> x.getTaskMachinePlanBroken() != TaskMachinePlanBrokenType.Not_Break).collect(Collectors.toList());
		List<Task> brokeTasks = _taskListToPlan.stream().filter(x -> x.getException() != null).collect(Collectors.toList());
		List<Lot> brokeLots = brokeTasks.stream().map(Task::getLot).distinct().collect(Collectors.toList());
		
		
		//List<Task> t3 = brokeTasks.stream().filter(x -> x.getId() == 509).collect(Collectors.toList());
		
		System.out.println("Crashed.");
		System.out.println("Task(" + brokeTasks.size() + ")");
		for(Task task : brokeTasks){
			System.out.println(task + ":" + task.getException().getMessage());
		}
		
		System.out.println("");
		
		System.out.println("Lot("+ brokeLots.size() +"):" + brokeLots);
		
		List<Task> tasksToRemove = new ArrayList<Task>();
		for(Lot lot : brokeLots){
			tasksToRemove.addAll(lot.getTasks());
		}
		
		_taskListToPlan.removeAll(tasksToRemove);
		_lots.removeAll(brokeLots);
		
		System.out.println("Tasks to be plan: " + _taskListToPlan.size() + " tasks, " + _lots.size() + " lots, " + _machinePlanList.size() + " machinePlans");
		System.out.println("######################################################");
		System.out.println("");

	}
	
	private static void updateDatetimeFieldsToLong() throws APSException {

		// 初始化机台计划的时间型属性
		for (MachinePlan machinePlan : _machinePlanList) {
			
			if(machinePlan.getId() == 1989490){
				System.out.println("test");
			}
			
			if (machinePlan.getStartTime() == null || machinePlan.getStartTime().equals(Common.getNullDate())) {
				throw new APSException("所有机台计划均应该有开始时间，但机台计划[" + machinePlan + "] 的开始时间为空.");
			}

			if (machinePlan.getEndTime() == null || machinePlan.getEndTime().equals(Common.getNullDate())) {
				throw new APSException("所有机台计划均应该有结束时间，但机台计划[" + machinePlan + "] 的结束时间为空.");
			}

			if (machinePlan.getEndTime().getTime() <= machinePlan.getStartTime().getTime()) {
				throw new APSException("所有机台计划的开始时间均应早于其结束时间，但机台计划[" + machinePlan + "] 的结束时间比开始时间早，或相等.");
			}

			machinePlan.setStartTimeL(machinePlan.getStartTime() == null ? 0L
					: Common.getTime_Minute(machinePlan.getStartTime().getTime()));
			machinePlan.setEndTimeL(
					machinePlan.getEndTime() == null ? 0L : Common.getTime_Minute(machinePlan.getEndTime().getTime()));

		}

		// 初始化任务的时间类型
		for (Task task : _taskListToPlan) {
			if (task.getDeliveryTime() == null || task.getDeliveryTime().equals(Common.getNullDate())) {
				throw new APSException("所有任务的订单交期均不应该为空，但任务[" + task + "]的订单交期为空.");
			}

			task.setDeliveryTimeL(Common.getTime_Minute(task.getDeliveryTime().getTime()));

			if (task.getArrivalTime() == null || task.getArrivalTime().equals(Common.getNullDate())) {
				task.setArrivalTimeL(0L);
			} else {
				task.setArrivalTimeL(Common.getTime_Minute(task.getArrivalTime().getTime()));
			}

			if (task.getTaskRequestFinishTime() == null
					|| task.getTaskRequestFinishTime().equals(Common.getNullDate())) {
				task.setTaskRequestFinishTimeL(0L);
			} else {
				task.setTaskRequestFinishTimeL(Common.getTime_Minute(task.getTaskRequestFinishTime().getTime()));
			}
			
			/*
			if(task.getId() == 252){
				System.out.println("252");
			}
			*/

			if (task.getFn_DeliveryTime() == null || task.getFn_DeliveryTime().equals(Common.getNullDate())) {
				task.setFn_DeliveryTimeL(0L);
			} else {
				
				task.setFn_DeliveryTimeL(Common.getTime_Minute(task.getFn_DeliveryTime().getTime()));
			}
			
		}

		
		// 初始化任务的后整交期(限非尾任务，及非单独任务)
		for (Task task : _taskListToPlan) {
			if(task.getTaskLocation() != TaskType_Location_In_Lot.SINK  && task.getTaskLocation() != TaskType_Location_In_Lot.ONLY_ONE){
				task.initDeliveryTimeBySinkTask();
			}
		}
	}
	
	
	private static void updateDatetimeFieldsToTime(){
		

		for(Task task : _taskListPlanned){
			
			if (task.getPlanStartTimeL() != null && task.getPlanStartTimeL() != 0L) {
				task.setPlanStartTime(Common.getTimeByMinuteLong(task.getPlanStartTimeL()));
			}else{
				System.out.println("StartTime is null:" + task);
			}
			
			
			if(task.getNonProductionTaskList() == null || task.getNonProductionTaskList().isEmpty()){
				continue;
			}
			
			for(NonproductionTask nonproductionTask : task.getNonProductionTaskList()){
				nonproductionTask.setPlanStartTime(Common.getTimeByMinuteLong(nonproductionTask.getPlanStartTimeL()));
			}
		}
	}

	
	/**
	 * sort the task list by custom rules
	 */
	private static void sortTasks(){
		
		if(_taskSorts == null || _taskSorts.isEmpty()){
			return;
		}
		
		setTaskArrival(false);
		
		List<String> sortFieldList = _taskSorts.stream().map(TaskSort::getFieldName).collect(Collectors.toList());
		List<Boolean> sortModeList = _taskSorts.stream().map(TaskSort::getSortMode).collect(Collectors.toList());
		int size = sortFieldList.size();
		String[] sortFields = (String[])sortFieldList.toArray(new String[size]);
		Boolean[] sortModes = (Boolean[])sortModeList.toArray(new Boolean[size]);
		
		System.out.println(_taskListToPlan);
		ListUtils.sort(_taskListToPlan, sortFields, sortModes);
		System.out.println(_taskListToPlan);
	
		setTaskArrival(true);
	}
	
	/**
	 * set the all tasks arrival time as the particular value, null, or the lot arrival time(the source task's arrival time of the lot)
	 * @param toNull true: set the task arrival time to null beside the source task
	 */
	private static void setTaskArrival(boolean toNull){
		for(Lot lot : _lots){
			if(lot.isValid() == false){
				continue;
			}
			
			Task sourceTask = lot.getSourceTask();
			
			if(sourceTask == null){
				continue;
			}
			
			Date arrivaleTime = toNull ? null : lot.getSourceTask().getArrivalTime();
			Long arrivaleTimeL = toNull ? 0L :  lot.getSourceTask().getArrivalTimeL();
				
			for(Task task : lot.getTasks()){
				if(task.getTaskLocation() == TaskType_Location_In_Lot.SOURCE){
					continue;
				}
				
				task.setArrivalTime(arrivaleTime);
				task.setArrivalTimeL(arrivaleTimeL);
			}
		}
	}
	
	/**
	 * Initialize the available machine plan for tasks.
	 */
	private static void initMachinePlanOfTask(){
		
		for(Task task : _taskListToPlan){

			if(task.getException() != null){
				continue;
			}
			
			// find the usable machine plans from the whole machine plan list
			task.initAvailableMachinePlans(_machinePlanList);
		}

		// remove the machine plan what do not be use.
		removeMachinePlansNeverUsed();
	}
	
	
	/**
	 * 过滤没有被使用的机台
	 */
	private static void removeMachinePlansNeverUsed() {

		List<MachinePlan> machinePlanBeUse = new ArrayList<MachinePlan>();
		// 找出有任务在用的MachinePlan
		for (Task task : _taskListToPlan) {
			if (task.getException() != null
					|| task.getUsableMachinePlan() == null
					|| task.getUsableMachinePlan().isEmpty()) {
				continue;
			}

			// 对有存可用可机台计划，记录它的机台计划，这些机台计划将不被过滤
			for (MachinePlan machinePlan : task.getUsableMachinePlan()) {
				if (machinePlanBeUse.contains(machinePlan)) {
					continue;
				}

				machinePlanBeUse.add(machinePlan);
			}
		}

		// 只取用到的机台计划
		_machinePlanList = machinePlanBeUse;
	}
	
	/**
	 * 初始化机台班次，计算出每个班次的结束时间, 分以下步骤进行;
	 * 1. 识别出机台计划中存在的班次
	 * 2. 对每个班次计算出结束时间
	 * @throws ParseException 
	 *
	 */
	private static void initShifts() throws ParseException{
		for(MachinePlan machinePlan : _machinePlanList){
			
			/*
			if(machinePlan.getId() == 1988924){
				System.out.println("test");
			}
			*/
			
			
			List<Shift> shifts = getShifts(machinePlan.getStartTimeL(), machinePlan.getEndTimeL());
			machinePlan.setShifts(shifts);
		}
		
		/*
		System.out.println("Test--Test--Test--Test--Test--Test--Test--   Start!!!!");
		for(MachinePlan machinePlan : _machinePlanList){

			System.out.println(machinePlan + ", StarTime:" + machinePlan.getStartTime() + ",EndTime:" + machinePlan.getEndTime());
			System.out.println(machinePlan.getShifts());
			
		}
		System.out.println("Test--Test--Test--Test--Test--Test--Test--   End!!!!");
		*/
	}
	
	/**
	 * 根据机台计划的开始与结束时间，获得班次列表。
	 * 注意：若机台计划的起始时间，与三班固定的起始时间不同，则首个班次的开始时间以机台计划的开始时间为准；
	 * 		若机台计划若机台计划的结束时间，与三班的固定结束时间不同，则最后一个班次的结束时间，以机台计划的结束时间为准。
	 * 计算方法，先获得首个班次，再以该班次的结束时间作为下一班次的开始时间，往后推8小时作为下一班次的结束时间，以此类推，直到机台计划的结束时间；
	 * 	最后一个班次的结束时间，是以机台计划的结束时间为准。
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws ParseException 
	 */
	private static List<Shift> getShifts(Long startTime, Long endTime) throws ParseException{
		List<Shift> shifts = new ArrayList<Shift>();
		Long shiftId = 1L;
		Shift firstShift = getShift(shiftId, startTime, endTime);
		shifts.add(firstShift);
		
		// 若第一个班次的结束时间等于机台计划的结束时间，则表示机台计划只有一个班
		if(firstShift.getEndTime() == endTime){
			return shifts;
		}
		
		// 以第一个班次的结时间，每隔8个小时间作一个班
		Long startShiftEndTime = firstShift.getEndTime() + 1;// 第一个班次的结时间移通1分钟作为下一班次的开始时间
		Long shiftSpan = 60L * 8L; //时间以分钟数，需要转换为小时

		for(Long i = startShiftEndTime; i <= endTime; i += shiftSpan){
			shiftId++;
			Shift shift = getShift(shiftId, i, endTime);
			shifts.add(shift);
		}
	
		return shifts;
	}
	
	
	/**
	 * 以开始结束时间生成班次对象
	 * @param startTime
	 * @param machinePlanEndTime
	 * @return
	 * @throws ParseException
	 */
	private static Shift getShift(Long shiftId, Long startTime, Long machinePlanEndTime) throws ParseException{
		// 根据开始时间，获得该时间所在的班次，再获得该班次的结时间，结时间可能是该班次的结束时间07:00, 15:00或23:00, 也可能是机台计划的结束时间（机台计划时长短于一个班次）
		Date startDate = Common.getTimeByMinuteLong(startTime);
		// 取得日期的小时部分，判断是在7:00, 15:00, 23:00的哪个区间
		Calendar cal = new GregorianCalendar();
		cal.setTime(startDate);
		
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// sdf.setTimeZone(TimeZone.getTimeZone(CHINA_TIME_ZONE));

		 String endTimeHour = "";
		 
		if(hour < 7){ 
			endTimeHour = "06:59:00";
			
		}else if(hour >= 7 && hour < 15){
			endTimeHour = "14:59:00";
		}else if(hour >= 15 && hour < 23){
			endTimeHour = "22:59:00";
		} else{ // 23:00点到00:00的班次，结束时间为次日的07:00
			cal.add(cal.DATE, 1);
			endTimeHour = "06:59:00";
		}
		
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		month++; //获取的月份是以0开始的
		int date = cal.get(Calendar.DAY_OF_MONTH);
		String dateString = year + "-" + month + "-" + date + " " + endTimeHour;
		Date endTime = sdf.parse(dateString);
		
		Long endTimeL = Common.getTime_Minute(endTime.getTime());
		
		// 若班次的固定结束时间比机台计划还晚，则取机台计划的结束时间，作为班次的结束时间
		if(machinePlanEndTime != null){
			endTimeL = machinePlanEndTime < endTimeL ? machinePlanEndTime : endTimeL;	
		}
	
		// 以开始时间的分钟数为单位，创建班次
		Shift fistShift = new Shift(shiftId, startTime, endTimeL);
		
		return fistShift;
	}

	/**
	 * 获得一个机台计划的班次结束时间列表
	 * @param machinePlanStartTime
	 * @param machinePlanEndTime
	 * @return
	 */
	/*
	private static List<Long> getShiftEndTimeList(Long machinePlanStartTime, Long machinePlanEndTime){
		// 取得一个机台计划的首个班次结束时间
		Long firstShiftEndTime = getFistShiftEndTime(machinePlanStartTime);
		
		List<Long> shiftEndTimeList = new ArrayList<Long>();
		
		// 得出一个班次的分钟数
		Long shiftSpan = 60L * 8L; // 8个小时,480分钟
		// 从机台计划的首个班次结束时间开始，循环事个机台计划的时间，间隔为班次（480分钟).
		for(Long shiftEndTime = firstShiftEndTime; shiftEndTime <= machinePlanEndTime; shiftEndTime += shiftSpan){
			shiftEndTimeList.add(shiftEndTime);
		}
		
		return shiftEndTimeList;
	}
	*/
	
	/**
	 * 由起始时间计算，最接近的班次下班时间
	 * 目前三班的下班时间分别是7:00, 15:00, 23:00, 也就是找出指定时间内，右则最接近上述三个时间的时间值
	 * @param machinePlanStartTime
	 * @return
	 */
	/*
	private static Long getFistShiftEndTime(Long machinePlanStartTime){
		// 获得当前时间
		Date currentDate = Common.getTimeByMinuteLong(machinePlanStartTime);
		
		// 取得日期的小时部分，判断是在7:00, 15:00, 23:00的哪个区间
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentDate);
		
		// 取当前时间的年月日
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int date = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		
		
		int shiftHour = 7;
		Date shiftDate = null;
		Long shiftEndTime = null;
		if(hour >= 7 && hour < 15){ 
			// 7点到15点之间，早班，结时间为当天15点
			shiftHour = 15;			
		}else if(hour >= 15 && hour < 23){ 
			// 7点到15点之间，早班，结时间为当天23点
			shiftHour = 23;	
		}else{ // 剩下的为跨天班，由23点到第二天7点
			shiftHour = 7;	
			if(hour == 23 || (hour >= 0 && hour < 7)){
				date++; // 第二天
			}
		}
		
		cal.set(year, month, date, shiftHour, 0);
		shiftDate = cal.getTime();
		shiftEndTime = Common.getTime_Minute(shiftDate.getTime());
		
		return shiftEndTime;
	}
	*/
		
	
	private static void planTasks(DataManager dataManager) throws APSException {

		SolverManager solverManager = SolverManager.getInstance(dataManager);		

		// 设置Planning开始时间为当前时间
		Date today = new Date();
		long planingStartTime = Common.getTime_Minute(today.getTime());

		Common.set_planningStartTimeL(planingStartTime);
		
		//_solvedTaskAssignment = solverManager.startSolve();
		solverManager.startSolve();
		_solvedTaskAssignment = solverManager.getSolvedTaskAssignment();
		
		if (_solvedTaskAssignment == null) {
			System.out.println("排产失败，请联系系统管理员.");
			return;
		}
		
		_scoreSummaries = solverManager.getSocreSummaries();
		_minutesSpent = solverManager.getMinutesSpent();
		createScoreSummaries();
		
		_taskListPlanned = _solvedTaskAssignment.getTaskList();
		_machinePlanList = _solvedTaskAssignment.getMachinePlanList();
		_scoreDirector = solverManager.getScoreDirector();

		/*
		if (_taskListPlanned.stream().filter(x -> x.getId() == 1939).findAny().orElse(null) == null) {
			System.out.println("planTasks, not exist");
		} else {
			System.out.println("planTasks, exist");
		}
		*/
	}
	
	/**
	 * 创建一个每隔1分钟显示一个分值的序列
	 */
	private static void createScoreSummaries(){
		 List<ScoreSummary> scoreSummaries = new ArrayList<ScoreSummary>();
		
		for(Long i = 1L; i <= _minutesSpent; i++){
			
			ScoreSummary ss = getNearestScoreSummary(i);
			if(ss != null){
				ScoreSummary newSS = ((ScoreSummary)ss.clone());
				newSS.setTimeStamp(i);
				scoreSummaries.add(newSS);
			}
		}
		
		_scoreSummaries = scoreSummaries;
	}
	
	private static ScoreSummary getNearestScoreSummary(Long timeSpan){
		
		List<ScoreSummary> sSumms = _scoreSummaries.stream().filter(x -> x.getTimeStamp() <= timeSpan).collect(Collectors.toList());
		// 取得ID最大那个
		Long id = 0L;
		ScoreSummary targetSS = null;
		for(ScoreSummary socreSummary : sSumms){
			if(socreSummary.getId() >= id){
				targetSS = socreSummary;
				id = socreSummary.getId();
			}
		}
		
		return targetSS;
	}
	
	


	/**
	 * clear the tasks what broke rule(s)
	 */
	private static void clearPlanningFailureTasks() {

		for (Task task : _taskListPlanned) {
	
			if (task.getMachinePlan() == null) {
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]没有分派到任何机台."));
				
			} else if (task.assignedToFeasibleMachinePlan(false) == false) {
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]没有分派到任何机台."));
				
			} else if (ScoreCalculator.isTaskOutOfMachinePlan(task)) {
				task.setException(new APSException(task, task.getId(), ExceptionLevel.INFO, "任务[" + task + "]被分派到机台计划的指定时间段以外."));
				
			} else if (ScoreCalculator.startTimeEarlierThanPreviousEndTime(task)){
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]的计划开始时间较其前置任务的结束时间早."));
				
			} else if (ScoreCalculator.plannedStartTimeLaterThanArrivalTime(task)){
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]的计划开始时间较其预计到达时间早."));
	
			} else if (ScoreCalculator.timeOverlap(task)){
				task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]与其所在机台的其它伤时间有重叠."));
			}
			
			if (task.getException() != null && (task.getException().getLevel() == ExceptionLevel.WARNING || task.getException().getLevel() == ExceptionLevel.ERROR)) {
				_planningFailureTasks.add(task);
			}
		}

		// remove the planning failure tasks. collect the lots of the failure planning tasks
		List<Long> lotIDs = _planningFailureTasks.stream().map(Task::getLotId).distinct().collect(Collectors.toList());
		for(Long lotId : lotIDs){
			removeTaskPlanedByLot(lotId);
		}
	}
	
	/**
	 * 初始化MachinePlan的TaskList
	 */
	private static void updateTaskListOfMachinePlan(){
		for(MachinePlan machinePlan : _machinePlanList){
			machinePlan.initTaskList(_taskListPlanned);
		}
	}
	
		
	/**
	 * calculate the indexes of the planning result
	 */
	private static void createIndexesOfPlanResult(){
		for(NonproductionTask nonproduction : _nonproductionTaskBroke){
			TaskCost taskCost = new TaskCost(
					nonproduction.toString(), 
					nonproduction.getDuration(), 
					nonproduction.getCost(),
					nonproduction.getPreviousTask().toString(),
					nonproduction.getCurrentTask().toString());
			
			_taskCost.add(taskCost);
		}
	}
	
	/**
	 * re-plan the start time base on the sequence and relation ship what
	 * planned by Optaplanner. re-plan the times with 2 steps:
	 * 1. re-plan the start time of task.
	 * 2. base on the task start and end time, re-plan the non-production task's start time.
	 * 
	 * @param taskAssignmentSolution
	 */
	private static void updateStartTime() {
		try {

			updateStartTimeOfNonproductionTask();

		} catch (CloneNotSupportedException | APSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 1. 根据每个TaskAssignment的NonproductionTaskSummary更新对应Task的NonproductionTask列表
	 * 2. 根据每个任务的开始时间，更新每个NonproductionTask的开始时间
	 * @throws CloneNotSupportedException 
	 * @throws APSException 
	 */

	private static void updateStartTimeOfNonproductionTask() throws CloneNotSupportedException, APSException {
		
		for(Task task : _taskListPlanned){
			
		
			if(task.getPreviousStep() == null || (task.getPreviousStep() instanceof MachinePlan)){
				continue;
			}
			
			Task preTask = (Task) task.getPreviousStep();
			

			List<NonproductionTask> nonProdList = null;
			if(preTask != null){
				Long nonProdListKey = Long.valueOf(preTask.getId().toString() + task.getId().toString());
				nonProdList = _nonproductionTaskListMapping.get(nonProdListKey); 
			}
			
			// 根据组合ID,从HashMap中取回非生产任务列表
			if(nonProdList == null || nonProdList.isEmpty()){
				//throw new APSException("任务[" + taskAssignment.getTask() + "] 理应有非生产任务，但列表中不存在对应的非生产任务列表");
				continue;
			}

			// 取得当前任务的开始时间，非生产任务将以此时间往前排
			Long nonproductionTaskStartTime = task.getPlanStartTimeL();
			
			//nonproductionTaskStartTime -= Common.INTERVAL_OF_TASK;
			
			for(NonproductionTask nonproductionTask : nonProdList){
				//nonproductionTask.setNonproductionTaskID(nonproductionTaskID);
			//	-- 此处不需要克隆！！！因为nonProdList从Mapping中取出时，已经是唯一的!!!
				nonproductionTaskStartTime -= nonproductionTask.getDuration();
				nonproductionTaskStartTime -= Common.INTERVAL_OF_TASK; // 再往前推移1分钟
				nonproductionTask.setPlanStartTimeL(nonproductionTaskStartTime);
				//newNonProdTask.setPlanStartTimeL(nonproductionTaskStartTime);
				//task. .appendNonproductionTask(nonproductionTask);
			}
			
			task.setNonProductionTaskList(nonProdList);
		}
	}
	
	/**
	 * remove the tasks what was plan failure, by following 3 steps:
	 * 1. find the whole tasks what are in the same lot;
	 * 2. for each task which need to remove, re-connect the relation ship of previous task and post task;
	 * 3. remove the task.
	 * 
	 * @param lot
	 */
	private static void removeTaskPlanedByLot(Long lotId) {

		List<Task> taskOfLot = _taskListPlanned.stream().filter(x -> x.getLotId().equals(lotId)).collect(Collectors.toList());

		for (Task task : taskOfLot) {

			// get the previous step, it's a Task or a MacinePlan
			Step preStep = task.getPreviousStep();
						
			// get the next task
			Task nextTask = task.getNextTask();

			// the next task must be not null, otherwise it;s the last task in a
			// Machine Plan, need not to connect to previous task
			if (nextTask != null) {
				nextTask.setPreviousStep(preStep);
			}

			// the previous task must be not null, otherwise it's the first task in a machine plan
			if (preStep != null) {
				preStep.setNextTask(nextTask);
			}
		//	task.clearNonproductionTasks();
			_taskListPlanned.remove(task);
		}
	}
	
	/**
	 * save and print out the planning results, including task list and log
	 */
	private static void savePlanningResult(String jobId){

		// a list the save the planning results.
		List<PlanningResult> planningResults = new ArrayList<PlanningResult>();
		
		// create and save the planning results.
		for (Task task : _taskListPlanned) {
			PlanningResult planningResult = new PlanningResult(task);
			planningResults.add(planningResult);
		}
		
		// 对无法参与排产的任务，生成broken
		getBrokensForInvalidTasks();
		
		
		// 分析约束的违反情况，获得planning日志
		getRunningLog_Constraint();
		
		// Save the planning result to XML file
		try {
			XMLHelper.SaveResult(planningResults, _brokens, _taskCost, _scoreSummaries);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// output the running result to termination
		printResult(_solvedTaskAssignment, jobId);
		
		ShowResult();
		
	}
	
	private static void ShowResult(){
		List<TaskInfo> tis = new ArrayList<TaskInfo>();
		
		List<MachinePlan> machinePlans = _taskListPlanned.stream().map(Task::getMachinePlan).distinct().collect(Collectors.toList());
		for(MachinePlan machinePlan : machinePlans){
			Step step = machinePlan;
			while(step.getNextTask() != null){
				
				Task t = (Task)step.getNextTask();
				List<NonproductionInfo> nonInfos = null;
				if(step instanceof Task){
					Task pt = (Task)step;
					Long nonProdListKey = Long.valueOf(pt.getId().toString() + t.getId().toString());
					List<NonproductionTask> nonProdList = _nonproductionTaskListMapping.get(nonProdListKey); 
					
					if (nonProdList != null && nonProdList.isEmpty() == false) {
						nonInfos = new ArrayList<NonproductionInfo>();
						for (NonproductionTask nonPrdTask : nonProdList) {
							nonInfos.add(new  NonproductionInfo(nonPrdTask));
						}	
					}
				}
				
				TaskInfo ti = new TaskInfo(machinePlan.toString(), t.toString(), t.getPlanStartTime(), t.getPlanEndTime(), nonInfos);
				tis.add(ti);
				step = step.getNextTask();
			}
		}
		
		String folderName = Common.getXMLPath(EntityCategory.TASK_INFO_test, false);
		String checkXMLFile = folderName;
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(checkXMLFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Class<?>[] classes = new Class[]{PlanningResult.class};
		
		TimeZone zone = TimeZone.getTimeZone(CHINA_TIME_ZONE);
		XStream xs = new XStream();
		xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", null, zone));
		xs.alias("TaskInfo", TaskInfo.class);
		XStream.setupDefaultSecurity(xs);
		xs.allowTypes(classes);
		xs.toXML(tis, fos);
		
	}
	
	/**
	 * 为没有参与排产的任务添加Broken
	 */
	private static void getBrokensForInvalidTasks() {
		for (Task task : _outstandTasks) {

			Broken broken = new Broken(task.getId(), "无法参与排产", "", task.toString(), "", -1L, SCORE_TAG_HARD,
					task.getException().getMessage());

			if (!_brokens.contains(broken)) {
				_brokens.add(broken);

			}

		}
	}
	

	private static void getRunningLog_Constraint() {

		_scoreDirector.setWorkingSolution(_solvedTaskAssignment);

		List<ConstraintMatchTotal> constraintMatchTotalList = new ArrayList<ConstraintMatchTotal>(
				_scoreDirector.getConstraintMatchTotals());

		Collections.sort(constraintMatchTotalList);

		if (constraintMatchTotalList.size() <= 0)
			return;

		for (ConstraintMatchTotal constraintMatchTotal : constraintMatchTotalList) {

			String ruleName = constraintMatchTotal.getConstraintName();
		
			for (ConstraintMatch constraintMatch : constraintMatchTotal.getConstraintMatchSet()) {
			
				if(constraintMatch == null){
					continue;
				}
			
				Task task = null;
				MachinePlan machinePlan = null;

				for (Object o : constraintMatch.getJustificationList()) {
			
					if(o == null){
						continue;
					}
					
					if (o instanceof Task) {
						task = (Task) o;
					}

					if (task != null) {
						machinePlan = task.getMachinePlan();
					}

					if (o instanceof MachinePlan) {
						machinePlan = (MachinePlan) o;
					}
				}

				Number[] scoreNumbers = constraintMatch.getScore().toLevelNumbers();
				
				String scoreLevel = getScoreLevel(scoreNumbers);
				if (scoreLevel == "") {
					continue;
				}

				Long score = getScore(scoreNumbers);
				if (score == null) {
					continue;
				}

				if (task == null && machinePlan == null) {
					continue;
				}
				
				String previousStepName = "";
				if (task != null) {
					
					Step previousStep = task.getPreviousStep();
					if (previousStep != null) {
						if (previousStep instanceof MachinePlan) {
							previousStepName = ((MachinePlan) previousStep).toString();
						} else {
							previousStepName = ((Task) previousStep).toString();
						}
					}
				}
				
				if(task != null){
				
					task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "违反约束:" + ruleName));
				}

				Broken broken = new Broken(task == null ? 0L : task.getId(), ruleName,
						machinePlan == null ? "" : machinePlan.toString(), task == null ? "" : task.toString(),
						previousStepName, score, scoreLevel, task.getException().getMessage());

				if (!_brokens.contains(broken)) {
					_brokens.add(broken);

				}

			}
		}
	}
	
	/**
	 * 根据分数的分布情况判断分数的级另硬分数- H, 中间分数 - M, 软分数 - S 
	 * @param scoreNumbers
	 * @return
	 */
	private static String getScoreLevel(Number[] scoreNumbers){
		if(scoreNumbers == null || scoreNumbers.length == 0){
			return "";
		}
		
		if(scoreNumbers[SCORE_LEVEL_HARD].longValue() != 0L){
			return SCORE_TAG_HARD;
		}
		
		if(scoreNumbers[SCORE_LEVEL_MEDIUM].longValue() != 0L){
			return SCORE_TAG_MEDIUM;
		}
		
		if(scoreNumbers[SCORE_LEVEL_SOFT].longValue() != 0L){
			return SCORE_TAG_SOFT;
		}
		
		return "";
	}
	
	private static Long getScore(Number[] scoreNumbers){
		if(scoreNumbers == null || scoreNumbers.length == 0){
			return null;
		}
		
		if(scoreNumbers[SCORE_LEVEL_HARD].longValue() != 0L){
			return scoreNumbers[SCORE_LEVEL_HARD].longValue();
		}
		
		if(scoreNumbers[SCORE_LEVEL_MEDIUM].longValue() != 0L){
			return scoreNumbers[SCORE_LEVEL_MEDIUM].longValue();
		}
		
		if(scoreNumbers[SCORE_LEVEL_SOFT].longValue() != 0L){
			return scoreNumbers[SCORE_LEVEL_SOFT].longValue();
		}
		
		return null;
	}
	
	/**
	 * get the score level by rule name, it should be H(hard) of S(soft)
	 * @param ruleName
	 * @return
	 */
	/*
	private static String getScoreLevel(String ruleName) {
		String scoreLevel = _constraints.stream().filter(x -> x.getName().equals(ruleName)).map(Constraint::getType)
				.findAny().orElse("");

		return scoreLevel;
	}
	*/
	
	/**
	 * pint out the running result, including detail and summary 
	 * 
	 * @param taskAssignmentSolution
	 * @throws IOException
	 */
	private static void printResult(TaskAssignmentSolution taskAssignmentSolution, String jobId) {
		// Show the broken information
		if (_brokens == null || _brokens.isEmpty()) {
			System.out.println("All the rules were match!");

		} else {

			System.out.println("Following constrain(s) was beaked.");

			// 将Broken按名分组
			List<String> ruleNames = _brokens.stream().map(Broken::getRuleName).distinct().collect(Collectors.toList());
			for(String ruleName : ruleNames){
				List<Broken> brokensByName = _brokens.stream().filter(x -> x.getRuleName().equals(ruleName)).collect(Collectors.toList());
				List<Long> scoreList = brokensByName.stream().map(Broken::getScore).collect(Collectors.toList());
				LongSummaryStatistics stats = scoreList.stream().mapToLong((x) -> x).summaryStatistics();
				Long constraintScore = stats.getSum();
				
				System.out.println(ruleName + ", Leve:" + brokensByName.get(0).getScoreLevel() + ", Score:" + constraintScore);
				
				for (Broken brokenByName : brokensByName) {
					String msg = brokenByName.getTaskName() + " -> " + brokenByName.getMachinePlanName()
							+ ", Previous Step:" + brokenByName.getPreviousStepName() + ", score: " + brokenByName.getScore();
					System.out.println(msg);
				}
			}
		}

		System.out.println("\r\n");

		// Show the summary information
		List<Task> unassignedTasks = _taskListPlanned.stream().filter(x -> x.getMachinePlan() == null)
				.collect(Collectors.toList());
		List<Task> assignedTasks = _taskListPlanned.stream().filter(x -> x.getMachinePlan() != null)
				.collect(Collectors.toList());

		int invalidCount = _outstandTasks.size() + _planningFailureTasks.size();

		System.out.println("Planning finished:");
		System.out.println("Task assignment, Invalid:" + invalidCount + ", Uninitialized:" + unassignedTasks.size()
				+ ", Initialized:" + assignedTasks.size() + ", Total: " + _taskListToPlan.size());
		System.out.println("Transaction [" + jobId + "] done!");

	}
}
