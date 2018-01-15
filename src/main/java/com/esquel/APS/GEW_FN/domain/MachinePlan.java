package com.esquel.APS.GEW_FN.domain;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;

import com.esquel.APS.GEW_FN.domain.enums.ExceptionLevel;
import com.esquel.APS.GEW_FN.domain.enums.TaskMachinePlanBrokenType;
import com.esquel.APS.GEW_FN.solver.CommonFunctions;
import com.esquel.APS.GEW_FN.solver.ConditionAssertion;
import com.esquel.APS.GEW_FN.solver.SolveMain;
import com.esquel.APS.GEW_FN.solver.interactor.TaskMachinePlanAssertionInteractor;
import com.esquel.APS.Helpers.APSException;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * ��̨�ƻ���������ʾһ����̨��ĳһ��ʱ���ڵ���Ϣ
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("MachinePlan")
public class MachinePlan extends Step implements java.io.Serializable {

	static final long serialVersionUID = 1L;
//	private int id;
	private String machineCode;
	private String department;
	private Date startTime;
	private Date endTime;
	private String machineType;
	private String machineNO;
	
	private Long startTimeL;
	private Long endTimeL;
	
	private List<Shift> shifts; // 当前机台计划的所有班次
	
	private List<Task> tasks;

	private List<MachinePlanProcess> processes;

	
	private TaskMachinePlanAssertionInteractor interactor;
	
	private TaskAssignmentSolution taskSchedule;
	
	
	public TaskAssignmentSolution getTaskSchedule() {
		return taskSchedule;
	}

	public void setTaskSchedule(TaskAssignmentSolution taskSchedule) {
		this.taskSchedule = taskSchedule;
	}

	public String getMachineCode() {
		return machineCode;
	}

	public void setMachineCode(String machineCode) {
		this.machineCode = machineCode;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Long getStartTimeL() {
		return startTimeL;
	}

	public void setStartTimeL(Long startTimeL) {
		this.startTimeL = startTimeL;
	}

	public Long getEndTimeL() {
		return endTimeL;
	}

	public void setEndTimeL(Long endTimeL) {
		this.endTimeL = endTimeL;
	}

	public List<MachinePlanProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(List<MachinePlanProcess> processes) {
		this.processes = processes;
	}
	
	public List<Shift> getShifts() {
		return shifts;
	}

	public void setShifts(List<Shift> shifts) {
		this.shifts = shifts;
	}

	/*
	public List<Long> getShiftsEndTime() {
		return shiftsEndTime;
	}

	public void setShiftsEndTime(List<Long> shiftsEndTime) {
		this.shiftsEndTime = shiftsEndTime;
	}
*/
	/**
	 * @return the machineType
	 */
	public String getMachineType() {
		return machineType;
	}

	/**
	 * @param machineType the machineType to set
	 */
	public void setMachineType(String machineType) {
		this.machineType = machineType;
	}

	public String getMachineNO() {
		return machineNO;
	}

	public void setMachineNO(String machineNO) {
		this.machineNO = machineNO;
	}

	public TaskMachinePlanAssertionInteractor getInteractor() {
		return interactor;
	}

	public void setInteractor(TaskMachinePlanAssertionInteractor interactor) {
		this.interactor = interactor;
	}

	public MachinePlan() {
	}

	public MachinePlan(Long id, String machineCode, String department, Date startTime, Date endTime,
			List<MachinePlanProcess> processes /*
												 * , String conditionExpression
												 */) {
		this.id = id;
		this.machineCode = machineCode;
		this.department = department;
		this.startTime = startTime;
		this.endTime = endTime;
		this.processes = processes;
		// this.conditionExpression = conditionExpression;
		
	}

	@Override
	public Date getPlanEndTime() {
		
		return this.startTime;
	}
	
	@Override
	public Long getPlanEndTimeL(){
		return this.startTimeL;
	}
	
	
	
	public List<Task> getTasks() {
		return this.tasks;
	}
	
	public void initTaskList(List<Task> tasks){
		this.tasks =  tasks.stream().filter(x -> x.getMachinePlan() != null && x.getMachinePlan().equals(this)).collect(Collectors.toList());
	}


	
	public void appendTask(Task task){
		if(this.tasks == null){
			this.tasks = new ArrayList<Task>();
		}
		
		this.tasks.add(task);
	}
	
	public void removeTask(Task task){
		if(this.tasks == null || this.tasks.isEmpty()){
			return;
		}
		
		if(this.tasks.contains(task)){
			this.tasks.remove(task);
		}
	}
	

	/**
	 * 检查是否存在可处理指定任务的机台计划
	 * @param task
	 * @return
	 */
	public boolean checkCondition(Task task) {

		if (task == null) {
			return false;
		}
		
		List<Long> mpProcessIds = this.getProcessIdMatch(task);
		if (mpProcessIds == null || mpProcessIds.isEmpty()) {
		//	task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]对应的工序为[" + task.getProcessCode() + "], 没有可处理该工序的机台计划."));
			return false;
			//return TaskMachinePlanBrokenType.Broke_For_MachinePlan_Process_Not_Exist;
		}


		if (!this.checkDepartment(task)) {
		//	task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]对应所属部门为[" + task.getDepartment() + "], 没有可处理该工序且属于该部门的机台计划."));
			return false;
			 //return TaskMachinePlanBrokenType.Broke_For_Department_Not_Exist;
		}
		 
		if (task.getAttributeRequests() == null || task.getAttributeRequests().isEmpty()){
		//	task.setException(null);
			return true;
			//return TaskMachinePlanBrokenType.Not_Break;
		}

		if (!this.checkProcessCondition(task, mpProcessIds)) {
		//	task.setException(new APSException(task, task.getId(), ExceptionLevel.WARNING, "任务[" + task + "]没有可处理的机台计划。"));
			return false;
			//return TaskMachinePlanBrokenType.Broke_For_MachinePlan_Process_Condition_Not_Match;
		}

		return true;
	}

	
	private List<Long> getProcessIdMatch(Task task) {
		
		List<Long> mpProcessIds = new ArrayList<Long>();
		
		mpProcessIds = this.processes.stream().filter(x -> x.getCode().equals(task.getProcessCode()))
				.map(MachinePlanProcess::getId).collect(Collectors.toList());

		return mpProcessIds;
		
	}
	
	/**
	 * get the process code list which from this machine plan and could handle the particular task  
	 * @param task
	 * @return
	 */
	public List<String> getProcessCodeMatch(Task task){
		List<String> mpProcessCodes = new ArrayList<String>();
		
		mpProcessCodes = this.processes.stream().filter(x -> x.getCode().equals(task.getProcessCode()))
				.map(MachinePlanProcess::getCode).collect(Collectors.toList());

		return mpProcessCodes;
	}

	private boolean checkDepartment(Task task) {
		// ����̨�ƻ�����������Ϊ��,����ʾ�û�̨�������ţ���ʾ������FA����FG�����񶼿��Դ���
		if (this.department == null || this.department.equals("")) {
			return true;
		}

		// �������Ҫ����Ϊ�գ�����ʾ������Բ�����Ҫ��
		if (task.getDepartment() == null || task.getDepartment().equals("")) {
			return true;
		}

		return this.department.equals(task.getDepartment());
	}

	/**
	 * 
	 * @param task
	 * @param mpProcessIds
	 * @return
	 */
	private boolean checkProcessCondition(Task task, List<Long> mpProcessIds) {

		List<MatchRule> ruels = this.interactor.getRules().stream().filter(x -> mpProcessIds.contains(x.getId()))
				.collect(Collectors.toList());

		if (ruels == null || ruels.isEmpty())
			return true;
		
		// set the task object of the Interator object, and send to assert.
		this.interactor.setTask(task);
		
		// get the Assertion object, it's a singleton
		ConditionAssertion assertion = null;
		boolean matchCondition = false;
		assertion = ConditionAssertion.getInstance(null, null);
		
		// Check the interactor
		matchCondition = assertion.AssertTrue(this.interactor);
		
		// reset the task object of the interactor
		this.interactor.setTask(null);
		this.interactor.setAssertReuslt(false);
		
		return matchCondition;
		
	}

	@Override
	public List<Attribute> getAttributeRequests() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if (obj instanceof MachinePlan) {
			MachinePlan mp = (MachinePlan) obj;
			return (mp.getId().equals(this.id));
		} else {
			return super.equals(obj);
		}
	}
	
	/**
	 * get the work load of this machine plan, the the total duration of the task what assigned to this machine plan.
	 * @return
	 */

	@Deprecated
	public int getWorkLoad(){
		
		// get all tasks that was assigned to this machine plan.
		List<Task> myTasks =  this.getTasks();
		
		if(myTasks == null || myTasks.isEmpty())
			return 0;
	
		List<Long> durationList = myTasks.stream().map(Task::getDurationOfNonproduction).collect(Collectors.toList());
		LongSummaryStatistics stats = durationList.stream().mapToLong((x) -> x).summaryStatistics();
		int workLoad = (int)stats.getSum();
		
		return workLoad;
		
	}


	
	/**
	 * get the max task start time in this machine plan, that is the last task's start time in this machine plan
	 * 
	 * @return
	 */
	private Date getMaxStartTime() {
		Task lastTask = this.getLastTask();
		
		if(lastTask == null || lastTask.getPlanStartTimeL() == null || lastTask.getPlanStartTimeL() == 0L)
			return null;
		
		return lastTask.getPlanStartTime();
	}
	
	private Long getMaxStartTimeL() {
		Task lastTask = this.getLastTask();
		
		if(lastTask == null || lastTask.getPlanStartTimeL() == null || lastTask.getPlanStartTimeL() == 0L)
			return 0L;
		
		return lastTask.getPlanStartTimeL();
	}

	/**
	 * get the last Task what assigned to this machine plan
	 * 
	 * @return
	 */
	public Task getLastTask() {
		// get the whole task in this machine plan
		List<Task> myTasks = this.getTasks();

		if (myTasks == null || myTasks.isEmpty())
			return null;
		
		// the last task in machine plan is the task that machine plan is current machine plan and the nextTaskInSameMachinePlan is null
		Task lasTask = myTasks.stream().filter(x -> x.getNextTask() == null).findAny().orElse(null);
		
		return lasTask;
		
	}
	
	/**
	 * 判断一个机台计划中是否存在时间重叠的任务
	 * @return
	 */
	public boolean taskOverlap(){

		if(this.getNextTask() == null){
			return false;
		}

		List<Task> tasks = this.taskSchedule.getTaskList().stream().filter(x -> x.getMachinePlan() != null && x.getMachinePlan().equals(this)).collect(Collectors.toList());
		
		if(tasks == null || tasks.isEmpty() || tasks.size() == 1){
			return false;
		}
		
		
		return CommonFunctions.isOverlap(tasks);
		
	}
	
	
	@Override
	public String toString() {
		return getClass().getName().replaceAll(".*\\.", "") + " [" + this.id + "/" + this.machineNO + "]";
	}

}