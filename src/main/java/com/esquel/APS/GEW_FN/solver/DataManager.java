package com.esquel.APS.GEW_FN.solver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import com.esquel.APS.GEW_FN.domain.Attribute;
import com.esquel.APS.GEW_FN.domain.Lot;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.MachinePlanProcess;
import com.esquel.APS.GEW_FN.domain.MatchRule;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSummary;
import com.esquel.APS.GEW_FN.domain.Shift;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskSort;
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

public class DataManager {
	private static DataManager _instance;
	
	private List<MachinePlan> machinePlanList = new ArrayList<MachinePlan>();
	private List<Task> taskList = new ArrayList<Task>();
	private List<NonproductionTaskSet> nonpoductionTaskSets = new ArrayList<NonproductionTaskSet>();
	private List<TaskSort> taskSorts = new ArrayList<TaskSort>();
	private List<Lot> lots = new ArrayList<Lot>();
	private List<Task> outstandTasks = new ArrayList<Task>();	// the tasks what its information is incompleteness.
	private HashMap<Long, NonproductionTaskSummary> nonproductionTaskSummaries = new HashMap<Long, NonproductionTaskSummary>();
	private HashMap<Long, List<NonproductionTask>> nonproductionTaskListMapping = new HashMap<Long, List<NonproductionTask>>();
	private String planningTarget;

	public List<MachinePlan> getMachinePlanList() {
		return machinePlanList;
	}

	public List<Task> getTaskList() {
		return taskList;
	}

	public List<NonproductionTaskSet> getNonpoductionTaskSets() {
		return nonpoductionTaskSets;
	}

	public HashMap<Long, NonproductionTaskSummary> getNonproductionTaskSummaries() {
		return nonproductionTaskSummaries;
	}

	public String getPlanningTarget() {
		return planningTarget;
	}

	public HashMap<Long, List<NonproductionTask>> getNonproductionTaskListMapping() {
		return nonproductionTaskListMapping;
	}

	public static synchronized DataManager getInstance(String planningTarget){
		if(_instance == null){
			_instance = new DataManager();
			_instance.planningTarget = planningTarget;
			_instance.initData();
		}
		
		return _instance;
	}
	
	private void initData(){
		loadData();
		
		try {
			preprocessData();
		} catch (APSException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void loadData(){
		XMLHelper.loadObjects();
		machinePlanList = XMLHelper.getMachinePlanList();
		taskList = XMLHelper.getTaskList();
		nonpoductionTaskSets = XMLHelper.getNonproductionTaskSetList();
//		_constraints = XMLHelper.getConstraintList();
	//	Common.set_words(XMLHelper.getWordList());
		taskSorts = XMLHelper.getTaskSortList();
	}
	
	
	private void preprocessData() throws APSException, ParseException {

		// connect the non-production task and its set
		relateNonproductionTaskAndSet();

		// create the machine rules between tasks and machines
		createRuleAndInteractors();

		// Initialize the interactors for Task - MachinePlan and Nonproduction
		// task checking
		initAssertionObject();

		// initialize the tasks.
		initTasks();

		// filter MacinePlan
		// filterMachinePlan();
	}
	
	
	private void relateNonproductionTaskAndSet(){
		for(NonproductionTaskSet nonproductionTaskSet : nonpoductionTaskSets){
			for(NonproductionTask nonproductionTask : nonproductionTaskSet.getNonproductionTasks()){
				nonproductionTask.setNonproductionTaskSet(nonproductionTaskSet);
			}
		}
	}
	
	
	
	private void createRuleAndInteractors() throws APSException{

		// machine plan match rules.
		initMachinePlanRuleAndInteractors();

		// non-production tasks set match rules
		initNonproductionRuleAndInteractors();

	}
	
	/**
	 * 生成机台计划与任务匹配的Rule与Interactor
	 * @throws APSException
	 */
	private void initMachinePlanRuleAndInteractors() throws APSException {
		for (MachinePlan machinePlan : machinePlanList) {

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
	private void initNonproductionRuleAndInteractors() throws APSException
	{
		for (NonproductionTaskSet nonproductionTaskSet : nonpoductionTaskSets) {
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
	private void initAssertionObject() throws APSException {
		// get the interactors from Task - MachinePlan objects
		List<TaskMachinePlanAssertionInteractor> machinePlanInteractors = initAssertionForMachinePlan();

		// get the interactors from non-production task objects
		List<NonproductionAssertionInteractor> nonproductionInteractors = initAssertionForNonproductionTask();

		// Initialize the Assertion object.
		ConditionAssertion.getInstance(machinePlanInteractors, nonproductionInteractors);

	}
	
	private List<TaskMachinePlanAssertionInteractor> initAssertionForMachinePlan() {

		List<TaskMachinePlanAssertionInteractor> machinePlanInteractors = new ArrayList<TaskMachinePlanAssertionInteractor>();

		for (MachinePlan machinePlan : machinePlanList) {
			if (machinePlan.getInteractor() == null) {
				continue;
			}

			machinePlanInteractors.add(machinePlan.getInteractor());
		}

		return machinePlanInteractors;
	}

	private List<NonproductionAssertionInteractor> initAssertionForNonproductionTask() throws APSException {
		List<NonproductionAssertionInteractor> nonproductionInteractors = new ArrayList<NonproductionAssertionInteractor>();
		for (NonproductionTaskSet nonproductionTaskSet : nonpoductionTaskSets) {

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

	private void initTasks() throws APSException, ParseException {
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
	 * Reset the time fields to null, including arrival time, delivery time, 
	 * due to the XML formating request, the value of a node must be a date time format, so fills the date 1900-01-01 00:00:00 while the arrival time is null
	 * @throws APSException 
	 */
	private void resetFields() throws APSException {

		for (Task task : taskList) {
		
			
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
	private void relateTaskAttribute() throws APSException {
		// Initialize attributes(some attributes need to be create by logic)
		// initSpecialAttribute();
		for (Task task : taskList) {

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
	private void createLots() throws APSException {
		// 检查是否有任务的Lot ID为空或为0
		List<Task> tasksNullLot = taskList.stream().filter(x -> x.getLotId() == null || x.getLotId() == 0L).collect(Collectors.toList());
		if (tasksNullLot != null && (!tasksNullLot.isEmpty())) {
			throw new APSException("所有任务均应该存在一个非零的LotID,但以下任务的LotID为空或为零:\r" + tasksNullLot);
		}

		List<Long> lotIds = taskList.stream().map(Task::getLotId).distinct().collect(Collectors.toList());
		for (Long lotId : lotIds) {
			
			List<Task> taskInLots = taskList.stream().filter(x -> x.getLotId().equals(lotId))
					.collect(Collectors.toList());
			if (taskInLots == null || taskInLots.isEmpty()) {
				throw new APSException("所有的批次都应该至少有一个任务，但批次ID：[" + lotId + "]的任务为空.");
			}

			Lot lot = new Lot(lotId, taskInLots);

			lots.add(lot);
		}
	}


	private void updateDatetimeFieldsToLong() throws APSException {

		// 初始化机台计划的时间型属性
		for (MachinePlan machinePlan : machinePlanList) {
			
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
		for (Task task : taskList) {
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
		for (Task task : taskList) {
			if(task.getTaskLocation() != TaskType_Location_In_Lot.SINK  && task.getTaskLocation() != TaskType_Location_In_Lot.ONLY_ONE){
				task.initDeliveryTimeBySinkTask();
			}
		}
	}
	
	/**
	 * 检查不合的机台计划
	 */
	private void checkMachinePlan(){
		List<MachinePlan> invalidMachinePlans = new ArrayList<MachinePlan>();
		for(MachinePlan machinePlan : machinePlanList){
			// 小于10分钟的机台计划不合理
			if(machinePlan.getEndTimeL() - machinePlan.getStartTimeL() < 10){
				invalidMachinePlans.add(machinePlan);
			}
		}
		
		machinePlanList.removeAll(invalidMachinePlans);
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
	private void checkLot() throws APSException{

		for (Lot lot : lots) {
			
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
	
	private boolean isTaskDeadLoop(Lot lot) {

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
	private boolean isChainBroke(Lot lot) {
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
	private boolean isAllArrivalTimeEmpty(Lot lot){
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
	private boolean isSourceSinkTaskExist(Lot lot){
		
		return (lot.getSourceTask() != null) && (lot.getSinkTask() != null);
	}
	
	/**
	 * 更新异常批次及其任务的异常对象
	 * @param lot
	 * @param lotBreakType
	 */
	private void setTaskExceptionOfLot(Lot lot, Lot_Break_Type lotBreakType){
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
	private boolean checkTask(Task task) {
		
		
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

	}
	
	/**
	 * checks if the fields of a task is complete
	 * 
	 * @param task
	 * @return
	 */
	private String checkTaskBodyInformation(Task task) {
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

		if (task.getDepartment() == null || task.getDepartment().equals("")) {
			return "任务[" + task + "]的生产部门为空";
		}

		if (task.getGf_id() == 0L) {
			return "任务[" + task + "]的品名ID为空";
		}

		return null;
	}

	/**
	 * check if the attributes is duplicate
	 */
	private boolean checkAttribute(Task task) {
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
	private void collectCrashedTasks() {
		if (taskList == null || taskList.isEmpty())
			return;

		// check each task in the list
		for (Task task : taskList) {

			boolean checkOk = checkTask(task);
			
			//TaskMachinePlanBrokenType brokenType = checkTask(task);
			//task.setTaskMachinePlanBroken(brokenType);

			// if the task is invalid, save in the list _crashedTasks
			if (!checkOk) {
				outstandTasks.add(task);
			}
		}
	}
	
	
	/**
	 * Initialize the available machine plan for tasks.
	 */
	private void initMachinePlanOfTask(){
		
		for(Task task : taskList){

			if(task.getException() != null){
				continue;
			}
			
			// find the usable machine plans from the whole machine plan list
			task.initAvailableMachinePlans(machinePlanList);
		}

		// remove the machine plan what do not be use.
		removeMachinePlansNeverUsed();
	}
	
	
	/**
	 * 过滤没有被使用的机台
	 */
	private void removeMachinePlansNeverUsed() {

		List<MachinePlan> machinePlanBeUse = new ArrayList<MachinePlan>();
		// 找出有任务在用的MachinePlan
		for (Task task : taskList) {
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
		machinePlanList = machinePlanBeUse;
	}
	
	
	/**
	 * 初始化机台班次，计算出每个班次的结束时间, 分以下步骤进行;
	 * 1. 识别出机台计划中存在的班次
	 * 2. 对每个班次计算出结束时间
	 * @throws ParseException 
	 *
	 */
	private void initShifts() throws ParseException{
		for(MachinePlan machinePlan : machinePlanList){
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
	private List<Shift> getShifts(Long startTime, Long endTime) throws ParseException{
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
	private Shift getShift(Long shiftId, Long startTime, Long machinePlanEndTime) throws ParseException{
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
	 * 移除所到达时间比其机台的结束时间还晚的任务.
	 */
	private void collectFarTasks(){
		
		for(Task task : taskList){
			
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
				outstandTasks.add(task);
			}
		}
	}
	
	/**
	 * 1. 对NonproductionTaskSet进行过滤，将没有工序使用的NonproductionTaskSet清除
	 * 2. 重设每个NonproductionTaskSet下的每个NonproduciotnTask的ID，ID为Set的ID加上Task的ID（字符连接）
	 * @return
	 */
	private void initNonproductionTaskSetInUse() {
		// 找出工序中有用到的NonproductionTaskSet
		List<String> processCodes = taskList.stream().map(Task::getProcessCode).distinct().collect(Collectors.toList());
		
		// 只取有工序用到的NonproductionTaskSet列表
		List<NonproductionTaskSet> nonProducttionTaskSets = nonpoductionTaskSets.stream().filter(x -> processCodes.contains(x.getProcessCode())).collect(Collectors.toList());
		
		
		// 更新正在使用的NonproductionSet
		nonpoductionTaskSets = nonProducttionTaskSets;
		
		
		// 创建前后任务之间的非生产任务
		createNonproductionTaskMapping();
		
	}
	
	/**
	 * 创建前后任务之间的非生产任务映射列表，分别有两个列表： 1. _nonproductionTaskSummaries：
	 * NonproductionTaskSummary的列表，表示两个任务之间，满足非生产任务时，表达这些非生产任务的信息归纳，分数、持续时间加总，名称连接。用于在Planning过程中进行分数与时间的计算。
	 * 2. _nonproductionTaskListMapping：
	 * 用一个hashmap来保存每两个任务之间可用的非生产任务列表。用于完成Planning之后进行非生产任务的分配。
	 */
	private void createNonproductionTaskMapping() {

		// 对有可能排在同一机台下的所有任务，进行两两组合，并找出其适用的非生产任务ID列表
		for (MachinePlan machinePlan : machinePlanList) {

			// 取得适用于指定机台计划的所有任务对应的TaskAssignment
			List<Task> taskMacthMachinePlan = taskList.stream()
					.filter(x -> x.getUsableMachinePlan() != null && x.getUsableMachinePlan().isEmpty() == false && x.getUsableMachinePlan().contains(machinePlan)).collect(Collectors.toList());

			// 对每个任务的TaskAssignment取笛卡尔积，对其中每对元素进行双向检测
			for (Task leftTask : taskMacthMachinePlan) {
		
				for (Task rightTask : taskMacthMachinePlan) {
					
					if(leftTask.equals(rightTask)){
						continue;
					}
				
					if(leftTask.getId() == 1 && rightTask.getId() == 440){
						System.out.println("test");
					}
					
					if(rightTask.getId() == 1 && leftTask.getId() == 440){
						System.out.println("test");
					}
				
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
	private void createNonproductionTaskMappingAndSummaries(Task fromTask, Task toTask){
		
		List<NonproductionTask> nonproductionTasksMatch = CommonFunctions.getActiveNonProductionTasks(fromTask, toTask, nonpoductionTaskSets);
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

		nonproductionTaskSummaries.put(tasksID_Key, nonproductionTaskSummary);
		nonproductionTaskListMapping.put(tasksID_Key, nonproductionTasksMatch);
	}

	
	/**
	 * Collects the tasks what could not be send to plan, including the crashed tasks and there are not feasible machine plan tasks.  
	 */
	private void clearInvalidTasks() {
		
		
		//List<Lot> lotsToRemove = new ArrayList<Lot>();
		//System.out.println("");
		//System.out.println("######################################################");
		//System.out.println("Task: ");
		//System.out.println("Total:" + _taskList.size() + " tasks, " + _lots.size() + " lots.");
		
		//List<Task> brokeTasks = _taskListToPlan.stream().filter(x -> x.getTaskMachinePlanBroken() != TaskMachinePlanBrokenType.Not_Break).collect(Collectors.toList());
		List<Task> brokeTasks = taskList.stream().filter(x -> x.getException() != null).collect(Collectors.toList());
		List<Lot> brokeLots = brokeTasks.stream().map(Task::getLot).distinct().collect(Collectors.toList());
		
		
		//List<Task> t3 = brokeTasks.stream().filter(x -> x.getId() == 509).collect(Collectors.toList());
		
		//System.out.println("Crashed.");
		//System.out.println("Task(" + brokeTasks.size() + ")");
		//for(Task task : brokeTasks){
		//	System.out.println(task + ":" + task.getException().getMessage());
		//}
		
		//System.out.println("");
		
		//System.out.println("Lot("+ brokeLots.size() +"):" + brokeLots);
		
		List<Task> tasksToRemove = new ArrayList<Task>();
		for(Lot lot : brokeLots){
			tasksToRemove.addAll(lot.getTasks());
		}
		
		taskList.removeAll(tasksToRemove);
		lots.removeAll(brokeLots);
		
		//System.out.println("Tasks to be plan: " + _taskList.size() + " tasks, " + _lots.size() + " lots, " + _machinePlanList.size() + " machinePlans");
		//System.out.println("######################################################");
		//System.out.println("");

	}
	/**
	 * sort the task list by custom rules
	 */
	private void sortTasks(){
		
		if(taskSorts == null || taskSorts.isEmpty()){
			return;
		}
		
		setTaskArrival(false);
		
		List<String> sortFieldList = taskSorts.stream().map(TaskSort::getFieldName).collect(Collectors.toList());
		List<Boolean> sortModeList = taskSorts.stream().map(TaskSort::getSortMode).collect(Collectors.toList());
		int size = sortFieldList.size();
		String[] sortFields = (String[])sortFieldList.toArray(new String[size]);
		Boolean[] sortModes = (Boolean[])sortModeList.toArray(new Boolean[size]);
		
		System.out.println(taskList);
		ListUtils.sort(taskList, sortFields, sortModes);
		System.out.println(taskList);
	
		setTaskArrival(true);
	}
	
	/**
	 * set the all tasks arrival time as the particular value, null, or the lot arrival time(the source task's arrival time of the lot)
	 * @param toNull true: set the task arrival time to null beside the source task
	 */
	private void setTaskArrival(boolean toNull){
		for(Lot lot : lots){
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
}
