package com.esquel.APS.GEW_FN.domain;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import com.esquel.APS.GEW_FN.domain.enums.ExceptionLevel;
import com.esquel.APS.GEW_FN.domain.enums.TaskType_Location_In_Lot;
import com.esquel.APS.GEW_FN.solver.CommonFunctions;
import com.esquel.APS.GEW_FN.solver.StartTimeUpdatingVariableListener;
import com.esquel.APS.GEW_FN.solver.TaskAssignmentDifficultyComparator;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**MINIMIZE_NONPRODUCTION_TASK
 * The mapping object from Task.xml
 * Refers to the process of lot.
 * @author ZhangKent
 *
 */
@XStreamAlias("Task")
@PlanningEntity(difficultyComparatorClass = TaskAssignmentDifficultyComparator.class /*, 
movableEntitySelectionFilter = MovableTaskSelectionFilter.class */) 
public class Task extends Step  {

	static final long serialVersionUID = 1L;

	private Long lotId; // Lot id
	private String cardNos; // card Number(s)
	private String processCode; // process code of the task
	private Long priority; // priority of the task
	private Long sequence; // the sequence of task in a lot
	private Date deliveryTime; // delivery time of the ppo
	private Long maxWaitingTime; // the max waiting time of the process
	private Long minWaitingTime; // the min waiting time of the process
	private Long duration; // duration of the process, in minute
	private Long prepareTime; // prepare time of the process, in minute
	private Date arrivalTime; // the arrival time of the lot
	private Long nextTaskID; // the next task id of current task
	private String department;  // the department what current ppo belong to.
	private Task nextTaskInSameLot;	// the next task what refers to the next task in the same lot.
	private Task previousTaskInSameLot;		// the previous task what refers to the previous task in the same lot.
	private List<MachinePlan> usableMachinePlan;	// the MachinePlan ID list what current task code be assigned to.
	private List<Attribute> attributeRequests;  // the attribute list of current task.
	private Long gf_id;
	private List<NonproductionTask> nonProductionTaskList;
	private Date taskRequestFinishTime;	// the standard finish time of the task.
	private String taskOrderType;
	private Date fn_DeliveryTime;

//	private TaskMachinePlanBrokenType taskMachinePlanBroken;
	private APSException exception = null;
//	private Long nonproductionScore;
	
	private Lot lot;
	private TaskType_Location_In_Lot taskLocation;	// the location of the task in a lot.
	
	private Step previousStep;
	private MachinePlan machinePlan;
	
	private Date planStartTime;

	// 将参与运算的时间变量数值化
	private Long deliveryTimeL;
	private Long arrivalTimeL;
	private Long taskRequestFinishTimeL;
//	private Long duration_In_MilSec_L;
	private Long planStartTimeL;
	private Long fn_DeliveryTimeL;
//	private Long prepareTimeL;
//	private Long maxWaitingTimeL;
//	private Long minWaitingTimeL;
	
	private Long delayL;
	
	
	private TaskAssignmentSolution taskSchedule;

	//@CustomShadowVariable(variableListenerClass = StartTimeUpdatingVariableListener.class, sources = {@CustomShadowVariable.Source(variableName = "previousStep") })
	// 6.5.0 -> 7.2.0
	
	@PlanningVariable(valueRangeProviderRefs = { "machinePlanRange","taskRange" }, graphType = PlanningVariableGraphType.CHAINED)
	public Step getPreviousStep() {
		return this.previousStep;
	}

	public void setPreviousStep(Step previousStep) {
		this.previousStep = previousStep;
	}
	
	@AnchorShadowVariable(sourceVariableName = "previousStep")
	public MachinePlan getMachinePlan() {
		return this.machinePlan;
	}

	public void setMachinePlan(MachinePlan machinePlan) {
		this.machinePlan = machinePlan;
	}

	public java.util.Date getPlanStartTime() {
		return this.planStartTime;
	}
	
	@CustomShadowVariable(variableListenerClass = StartTimeUpdatingVariableListener.class, 
			sources = {@PlanningVariableReference(variableName = "previousStep"), })
	public Long getPlanStartTimeL() {
		return planStartTimeL;
	}

	public void setPlanStartTime(java.util.Date planStartTime) {
		this.planStartTime = planStartTime;
	}

	@Override
	public Date getPlanEndTime() {
		if (this.planStartTime == null) {
			return null;
		}

		return Common.getTimeOffset(this.planStartTime, this.duration, Calendar.MINUTE);
	}
	
	@Override
	public Long getPlanEndTimeL(){
		if(this.planStartTimeL == null || this.planStartTimeL == 0L){
			return 0L;
		}
		
		return this.planStartTimeL + (Long)this.duration;
	}


	public Long getLotId() {
		return lotId;
	}

	public void setLotId(Long lotId) {
		this.lotId = lotId;
	}

	public Lot getLot() {
		return lot;
	}

	public void setLot(Lot lot) {
		this.lot = lot;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Task() {
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arriaveTime) {
		this.arrivalTime = arriaveTime;
	}

	public String getCardNos() {
		return cardNos;
	}

	public void setCardNos(String cardNos) {
		this.cardNos = cardNos;
	}

	public String getProcessCode() {
		return processCode;
	}

	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	public Long getSequence() {
		return sequence;
		
		
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public Date getDeliveryTime() {
		return deliveryTime;
	}

	public void setDeliveryTime(Date deliveryTime) {
		this.deliveryTime = deliveryTime;
	}

	/**
	 * 获得最大可等待时间，单位：分钟
	 */
	public Long getMaxWaitingTime() {
		return maxWaitingTime;
	}

		
	public final Task getNextTaskInSameLot() {
		return nextTaskInSameLot;
	}

	public void setNextTaskInSameLot(Task nextTaskInSameLot) {
		this.nextTaskInSameLot = nextTaskInSameLot;
	}
	
	public Task getPrivousTaskInSameLot() {
		return previousTaskInSameLot;
	}

	public void setPrivousTaskInSameLot(Task privousTaskInSameLot) {
		this.previousTaskInSameLot = privousTaskInSameLot;
	}

	public void setMaxWaitingTime(Long maxWaitingTime) {
		this.maxWaitingTime = maxWaitingTime;
	}

	public Long getMinWaitingTime() {
		return minWaitingTime;
	}

	public void setMinWaitingTime(Long minWaitingTime) {
		this.minWaitingTime = minWaitingTime;
	}

	public Long getDuration() {
		return duration;
	}
	
	/**
	 * 取得当前任务与前一步骤（可能是机台也可能是任务）的时间间隔（单位:5分钟）
	 * @return
	 */
	public Long getDelayL() {
		if(this.previousStep == null){
			return 0L;
		}
		
		Long priviousEndTimeL = 0L;
		if(this.previousStep instanceof MachinePlan){
			MachinePlan machinePlan = ((MachinePlan)(this.previousStep));
			priviousEndTimeL = machinePlan.getStartTimeL();
		}else{
			Task preTask = ((Task) (this.previousStep));
			if (preTask.getPlanEndTimeL() != 0L) {
				priviousEndTimeL = preTask.getPlanEndTimeL();
			}
		}
		
		if (this.planStartTimeL > priviousEndTimeL) {
			// 小于5份钟按5分钟数，扣1分
			
			Long timeSpan = this.planStartTimeL - priviousEndTimeL;
			if(timeSpan <= 5L){
				return 1L;
			}else{
				return (this.planStartTimeL - priviousEndTimeL) / 5L;
			}
			
			
		} else {
			return 0L;
		}
		
	}

	public void setDelayL(Long delayL) {
		this.delayL = delayL;
	}

	@Deprecated
	public Long getDurationOfNonproduction(){
		if(this.nonProductionTaskList == null || this.nonProductionTaskList.isEmpty()){
			return this.duration;
		}
		
		List<Long> durationList = this.nonProductionTaskList.stream().map(NonproductionTask::getDuration).collect(Collectors.toList());
		LongSummaryStatistics stats = durationList.stream().mapToLong((x) -> x).summaryStatistics();
		int nonproductionDuration = (int)stats.getSum();
		
		return this.duration + nonproductionDuration;
	}
	
	/*
	public Long getDurationOfNonproductionL(){
		if(this.nonProductionTaskList == null || this.nonProductionTaskList.isEmpty()){
			return this.duration;
		}
		
		List<Long> durationList = this.nonProductionTaskList.stream().map(NonproductionTask::getDuration).collect(Collectors.toList());
		LongSummaryStatistics stats = durationList.stream().mapToLong((x) -> x).summaryStatistics();
		Long nonproductionDuration = stats.getSum();
		
		return this.duration + nonproductionDuration;
	}
	*/

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public Long getPrepareTime() {
		return prepareTime;
	}

	public void setPrepareTime(Long prepareTime) {
		this.prepareTime = prepareTime;
	}

	public List<NonproductionTask> getNonProductionTaskList() {
		return this.nonProductionTaskList;
	}
	
	public void setNonProductionTaskList(List<NonproductionTask> nonProductionTaskList) {
		this.nonProductionTaskList = nonProductionTaskList;
	}

	public Long getGf_id() {
		return gf_id;
	}

	public void setGf_id(Long gf_id) {
		this.gf_id = gf_id;
	}
	
	public Date getTaskRequestFinishTime() {
		return taskRequestFinishTime;
	}

	public void setTaskRequestFinishTime(Date taskRequestFinishTime) {
		this.taskRequestFinishTime = taskRequestFinishTime;
	}

	public Date getFn_DeliveryTime() {
		return fn_DeliveryTime;
	}

	public void setFn_DeliveryTime(Date fn_DeliveryTime) {
		this.fn_DeliveryTime = fn_DeliveryTime;
	}

	public String getTaskOrderType() {
		return taskOrderType;
	}

	public void setTaskOrderType(String taskOrderType) {
		this.taskOrderType = taskOrderType;
	}

	@Override
	public List<Attribute> getAttributeRequests() {
		return attributeRequests;
	}

	public void setAttributeRequests(List<Attribute> attributeRequests) {
		this.attributeRequests = attributeRequests;
	}
	
	public void appednAttributeRequest(Attribute attributeRequest){
		if(this.attributeRequests == null){
			this.attributeRequests = new ArrayList<Attribute>();
		}
		
		this.attributeRequests.add(attributeRequest);
	}

	public Long getNextTaskID() {
		return nextTaskID;
	}

	public void setNextTaskID(Long nextTaskID) {
		this.nextTaskID = nextTaskID;
	}

	public Task getPreviousTaskInSameLot() {
		return previousTaskInSameLot;
	}

	
	public Long getDeliveryTimeL() {
		return deliveryTimeL;
	}

	public void setDeliveryTimeL(Long deliveryTimeL) {
		this.deliveryTimeL = deliveryTimeL;
	}

	public Long getArrivalTimeL() {
		/*
		// 若是SourcTask ，则直接取deliveryTimeL的值，否则需要根据当前批次的SourceTask的deliveryTimeL
		if (this.deliveryTimeL != null && this.deliveryTimeL != 0L) {
			return this.deliveryTimeL;
		}
		
		// 取当前任务所在批次的Source Task(首个任务)的到达时间
		return this.lot.getSourceTask().getArrivalTimeL();
		*/
		return this.arrivalTimeL;
	}

	public void setArrivalTimeL(Long arrivalTimeL) {
		
		this.arrivalTimeL = arrivalTimeL;
	}

	public Long getTaskRequestFinishTimeL() {
		return taskRequestFinishTimeL;
	}

	public void setTaskRequestFinishTimeL(Long taskRequestFinishTimeL) {
		this.taskRequestFinishTimeL = taskRequestFinishTimeL;
	}

	public void setPreviousTaskInSameLot(Task previousTaskInSameLot) {
		this.previousTaskInSameLot = previousTaskInSameLot;
	}

	public void setPlanStartTimeL(Long planStartTimeL) {
		this.planStartTimeL = planStartTimeL;
	}

	/**
	 * 获得每个任务（工序的后整交期），其中：
	 * 1. 尾任务（包括唯一任务）的后整交期为其自身的后整交期；
	 * 2. 非尾任务的后整交期，根据其所在的任务链（批次），由尾任务交期推算所得（计算任务的持续时间和等待时间)
	 * @return
	 */
	public Long getFn_DeliveryTimeL() {
		return this.fn_DeliveryTimeL;
	}
	
	public void initDeliveryTimeBySinkTask(){
		
	
		// 若尾任务的交期为空，则无法计算
		if(this.lot.getSinkTask() == null || this.lot.getSinkTask().getFn_DeliveryTimeL() == null || this.lot.getSinkTask().getFn_DeliveryTimeL() <= 0L){
			return;
		}
		
		Long timeSpan = 0L;
		
		// 从下一工序开始算
		Task currentTask = this.getNextTaskInSameLot();
		
		while(currentTask != null){
			timeSpan += currentTask.duration + currentTask.prepareTime;
			currentTask = currentTask.getNextTaskInSameLot();
		}
		
		// 尾任务的结束时间，减去timeSpan，即为当前工序的结束时间
		this.fn_DeliveryTimeL = this.lot.getSinkTask().fn_DeliveryTimeL - timeSpan;
		
	}

	public void setFn_DeliveryTimeL(Long fn_DeliveryTimeL) {
		this.fn_DeliveryTimeL = fn_DeliveryTimeL;
	}

	public List<MachinePlan> getUsableMachinePlan() {
		return usableMachinePlan;
	}

	public void setUsableMachinePlan(List<MachinePlan> avairableMachinePlan) {
		this.usableMachinePlan = avairableMachinePlan;
	}

	public TaskType_Location_In_Lot getTaskLocation() {
		return taskLocation;
	}

	public void setTaskLocation(TaskType_Location_In_Lot taskLocation) {
		this.taskLocation = taskLocation;
	}

	public void initAvailableMachinePlans(List<MachinePlan> machinePlans) {
		// get the machine plan(s) what only code handle the process what task requests.
		List<MachinePlan> machinePlanByProcessCode = machinePlans.stream().filter(x -> (!x.getProcessCodeMatch(this).isEmpty())).collect(Collectors.toList());

		// get the machine plan what checkCondition method result is Not_Break, base on the machine plan list what create by previous step.
		if (machinePlanByProcessCode != null && (!machinePlanByProcessCode.isEmpty())) {
			this.usableMachinePlan = machinePlanByProcessCode.stream().filter(x -> x.checkCondition(this) == true).collect(Collectors.toList());
			
			if(this.usableMachinePlan == null || this.usableMachinePlan.isEmpty()){
				this.setException(new APSException(this, this.getId(), ExceptionLevel.WARNING, "任务[" + this +"]不存在可处理此工序的机台计划." ));
				//this.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_MachinePlan_Process_Condition_Not_Match);
			}
		} else {
			this.setException(new APSException(this, this.getId(), ExceptionLevel.WARNING, "任务[" + this + "]的工序为[" + this.processCode + "], 当前没有可处理此工序的机台计划." ));
			//this.setTaskMachinePlanBroken(TaskMachinePlanBrokenType.Broke_For_MachinePlan_Process_Condition_Not_Match);
		}
	}

	/**
	 * Check if the task was assigned to the feasible machine plan
	 * @param includeNullMahcinePlan
	 * @return - True if the available machine plan list contains the machine plan which assigned to the task.
	 * 	
	 */
	public boolean assignedToFeasibleMachinePlan(boolean includeNullMahcinePlan){
		if(this.machinePlan == null)
			return includeNullMahcinePlan;
		
		if(this.usableMachinePlan == null || this.usableMachinePlan.isEmpty())
			return includeNullMahcinePlan;
		
		return this.usableMachinePlan.contains(this.machinePlan);
		
	}
	/*
	public TaskMachinePlanBrokenType getTaskMachinePlanBroken() {
		return taskMachinePlanBroken;
	}

	
	public void setTaskMachinePlanBroken(TaskMachinePlanBrokenType taskMachinePlanBroken) {
		this.taskMachinePlanBroken = taskMachinePlanBroken;
	}
	*/


	public TaskAssignmentSolution getTaskSchedule() {
		return taskSchedule;
	}

	public void setTaskSchedule(TaskAssignmentSolution taskSchedule) {
		this.taskSchedule = taskSchedule;
	}

	public APSException getException() {
		return exception;
	}

	public void setException(APSException exception) {
		this.exception = exception;
	}

	/**
	 * Gets the value by name of attribute
	 * 
	 * @param attributeName
	 * @return
	 */
	public String getAttributeValue(String attributeName) {
		
		if(attributeName == null || attributeName.equals(""))
			return "";
		
		if (this.attributeRequests == null || this.attributeRequests.isEmpty())
			return "";

		Attribute attr = this.attributeRequests.stream().filter(x -> x.getName().equals(attributeName)).findAny().orElse(null);

		if (attr == null) {
			return "";
		}
		
		String returnString = attr.getValue() == null ? "" :  attr.getValue().trim();
/*		
		if(attributeName=="Dosage_Factor" && Integer.parseInt(returnString) < 1000 ){
			System.out.println(returnString);
			
		}
*/
		return returnString;

	}
	
	/**
	 * Get the total Non-production task CT of the task
	 * 
	 * @return
	 */
	/*
	public int getNonProductionTaskListTotalCT() {
		int totalCT = 0;

		// if the non-production task list is null or empty of the task, return 0.
		if ((this.nonProductionTaskList == null) || (this.nonProductionTaskList.isEmpty())) {
			return totalCT;
		}

		List<Integer> cts = this.nonProductionTaskList.stream().map(NonproductionTask::getDuration).collect(Collectors.toList());
		
		IntSummaryStatistics stats = cts.stream().mapToInt((x) -> x).summaryStatistics();
		totalCT = (int)stats.getSum();
	
		// add the intervals 
		totalCT += this.nonProductionTaskList.size() * Common.INTERVAL_OF_TASK;
		
		return totalCT;
	}
	*/
	
	public Long getNonproductionTaskTotalCT(){

		// 获得NonproductionSummary对象(根据当前任务与前一任务的ID组合多Map表中获得)
		NonproductionTaskSummary nonprdTaskSumm = CommonFunctions.getNonProdTaskSummary(this);
		if(nonprdTaskSumm == null){
			return 0L;
		}
		
		return nonprdTaskSumm.getDuation();
	}
	
	public Long getShiftDelayScore1(){
		return 	this.fn_DeliveryTimeL - this.getPlanEndTimeL();
	}
	
	/**
	 * 若当前任务是批次中最后一个任务，则获得当前任务的批延迟。
	 * 批次延迟的计算方式：任务的后整交期与任务的计划结束时间比较，计划结束时间比后整交期延迟一个班次，则扣除1分
	 * @return
	 * @throws APSException 
	 */
	public Long getShiftDelayScore() throws APSException {
		// 仅对最后一个工序进行计算
		/*
		if (taskLocation != TaskType_Location_In_Lot.SINK && taskLocation != TaskType_Location_In_Lot.ONLY_ONE) {
			return 0L;
		}

		// 对于未有计划结束时间，没有后整交期，及计划结束时间比后整交期早的，均不扣分
		if (this.getPlanEndTimeL() == null || this.getPlanEndTimeL() == 0L 
				|| this.fn_DeliveryTimeL == null || this.fn_DeliveryTimeL == 0L 
				|| this.getPlanEndTimeL() < this.fn_DeliveryTimeL) {
			return 0L;
		}
		*/

		if (this.machinePlan == null || this.machinePlan.getShifts() == null
				|| this.machinePlan.getShifts().isEmpty()) {
			return 0L;
		}

		// 检查任务的交期与结束时间是否均在机台所有的班次之内，若是，则按这些班次的差异计算；
		// 否则通过时间差与标准班次时间计算相关的班次数（按第班8小时算)
		if (this.fn_DeliveryTimeL >= this.machinePlan.getShifts().get(0).getStartTime()
				&& this.fn_DeliveryTimeL <= this.machinePlan.getShifts().get(this.machinePlan.getShifts().size() - 1).getStartTime()
				&& this.getPlanEndTimeL() >= this.machinePlan.getShifts().get(0).getStartTime()
				&& this.getPlanEndTimeL() <= this.machinePlan.getShifts().get(this.machinePlan.getShifts().size() - 1)	.getStartTime()) {
			// 获得交期所在的班次
			Shift deliveryShift = this.machinePlan.getShifts().stream()
					.filter(x -> this.fn_DeliveryTimeL >= x.getStartTime() && this.fn_DeliveryTimeL <= x.getEndTime())
					.findAny().orElse(null);
			if (deliveryShift == null) {
				
				/*
				System.out.println("Task:" + this + ", startTime:" + Common.getTimeByMinuteLong(this.planStartTimeL) + ", endTime:" + Common.getTimeByMinuteLong(this.getPlanEndTimeL()) );
				System.out.println("MachinePlan:");
				System.out.println(this.machinePlan + ", StartTime:" + this.machinePlan.getStartTime() + ", endTime:" + this.machinePlan.getEndTime());
				
				System.out.println("Shifts:");
				for(Shift s : this.machinePlan.getShifts()){
					System.out.println(s);
				}
				*/
				
				throw new APSException("任务交期与机台计划的班次存在不合理的情况.");
			}

			// 获得结束时间所在的班次,
			Shift shiftPlanEndTime = this.machinePlan.getShifts().stream()
					.filter(x -> this.getPlanEndTimeL() >= x.getStartTime() && this.getPlanEndTimeL() <= x.getEndTime())
					.findAny().orElse(null);
			
			if (shiftPlanEndTime == null) {
				throw new APSException("任务计划结束时间与机台计划的班次存在不合理的情况.");
			}

			// 若交期所在班班比计划结束时间所在班次晚，则扣相差的班次数，否则不扣分
			if (deliveryShift.getId() > shiftPlanEndTime.getId()) {
				return deliveryShift.getId() - shiftPlanEndTime.getId();
			} else {
				return 0L;
			}

		} else {
			// 若计划结束时间比交期早，则不扣分，否则计算它们相差的班次，每班次扣1分
			if (this.getPlanEndTimeL() <= this.fn_DeliveryTimeL) {
				return 0L;
			} else {
				Long diffSpan = this.getPlanEndTimeL() - this.fn_DeliveryTimeL;
				double diffShift = (double) diffSpan / 60 / 8; // 计算两个时间相差的班次
				Long diffShiftL = (long) Math.ceil(diffShift); // 所得班次计算结果，向上取整
				return diffShiftL;
			}

		}
	}

	/**
	 * 取得计划开始时间与最近班次结束时间的间隔
	 * @return
	 */
/*
	public Long getClostToShiftEndSocre(){
		Long shiftEndTime = getTheNearestRightShiftEndTime();
		return this.getPlanEndTimeL() - shiftEndTime;
	}
*/
	
	/**
	 * 取得一个任务右则最接近的班次结束时间
	 * @return
	 */
/*
	private Long getTheNearestRightShiftEndTime(){
		if(this.machinePlan == null){
			return 0L;
		}
		
		List<Long> shitfEndTimes = this.machinePlan.getShiftsEndTime();
		// 对列表进行排序
		Collections.sort(shitfEndTimes);

		Long nearestShiftEndTime = shitfEndTimes.get(0);
		if(this.planStartTimeL <= nearestShiftEndTime){
			return nearestShiftEndTime;
		}
		
		for(Long shitfEndTime : shitfEndTimes){
			if(this.planStartTimeL < shitfEndTime){
				nearestShiftEndTime = shitfEndTime;
			}else{
				break;
			}
		}
		
		return nearestShiftEndTime;
	}
*/
	
	/**
	 * Gets the Machine Plan object if the task is the first task of the machine plan, otherwise null.
	 * @return
	 */
	public MachinePlan getMyAnchor(){
	
		if(this.previousStep == null || this.getMachinePlan() == null || (this.previousStep instanceof Task)){
			return null;
		}
		
		MachinePlan anchore = ((MachinePlan)this.previousStep);

		return anchore;
	}
	
	/**
	 * Returns true if this task is the first task of the machine plan, otherwise false.
	 * @return
	 */
	public boolean isSourceTask(){
		return (this.getMyAnchor() != null);
	}
	
	/**
	 * Returns true if this task is the last task of the machine plan. otherwise false.
	 * @return
	 */
	public boolean isSinkTask(){
		return (this.getNextTask() == null);
	}
	
	/**
	 * 获得当前任务的非生产任务分数，即当前任务所有的非生产任务分数总和
	 * @return
	 */
	public Long getNonproductionScore(){

		NonproductionTaskSummary nonprdTaskSumm = CommonFunctions.getNonProdTaskSummary(this);
		return nonprdTaskSumm == null ? 0L : nonprdTaskSumm.getScore();
	}
	
	/**
	 * 检查是否存在与当前任务时间冲突的任务,取得重叠分钟数作扣分数的基础
	 * @return
	 */
	public Long timeOverlap(){
		/*
		if(this.getId() == 84 || this.getId() == 363){
			System.out.println("test: particula task");
		}
		*/
		
		if(this.previousStep == null || this.machinePlan == null){
			return 0L;
		}
		
		List<Task> tasks = this.taskSchedule.getTaskList().stream().filter(x -> x.getMachinePlan() != null && x.getMachinePlan().equals(this.getMachinePlan())).collect(Collectors.toList());
		
		if(tasks == null || tasks.isEmpty() || tasks.size() == 1){
			return 0L;
		}
		
		Long overlapAmount = CommonFunctions.isOverlap(this,tasks);
		
		/*
		if(overlapAmount > 0L){
			System.out.println("Test: Overlap");
		}
		*/
		
		return overlapAmount;
	}
	
	/**
	 * 根据组合ID获得NonproductionTaskSummary对象
	 * @return
	 */
	private NonproductionTaskSummary getNonProdTaskSummary(){
		if(this.previousStep == null || (this.previousStep instanceof MachinePlan)){
			return null;
		}
		
		Task previousTask = ((Task)this.previousStep);
		
		// 根据前后任务的ID组合，从
		Long nonproductionTaskMappingID = Long.valueOf(previousTask.getId().toString() + this.getId().toString());
		// 根据前后任务的组合ID，从Mapping List中获得对应的Nonproduction Task Summary
		NonproductionTaskSummary nonprdTaskSumm = this.taskSchedule.getNonproductionMappingList().get(nonproductionTaskMappingID);
		return nonprdTaskSumm;
	}
	
	/**
	 * Override the equals method, equal if the ids are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Task) {
			Task t = (Task) obj;
			return (t.getId().equals(this.id));
		} else {
			return super.equals(obj);
		}
	}
	
	
	@Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + " [" + id + "-" + this.processCode + "]";
    }
	
	
	

}