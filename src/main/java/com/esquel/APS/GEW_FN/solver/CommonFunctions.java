package com.esquel.APS.GEW_FN.solver;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.valuerange.ValueRangeFactory;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSummary;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.enums.GetTimeType;
import com.esquel.APS.GEW_FN.domain.enums.TaskType_Location_In_Lot;
import com.esquel.APS.GEW_FN.domain.enums.TimeComparisonResultType;
import com.esquel.APS.Helpers.Common;

/**
 * Solving �����е�ͨ�ù���
 * 
 * @author ZhangKent
 *
 */
public class CommonFunctions {
	
	private static List<Long> assessedTaskIDs = new ArrayList<Long>();

	/**
	 * 
	 * @param task
	 * @param need_Interval
	 * @return
	 */
	public static Long getTaskStartTimeL(Task task) {

		Long startTime = 0L;

		// 当前任务不在机台中，也没有同批次的前一个任务
		if (task == null || (task.getPreviousStep() == null && task.getPrivousTaskInSameLot() == null)) {
			return startTime;
		}
		
		// 获得同一批次前置任务的结束时间
		Long previousEndTimeInSameLot = (	task.getPrivousTaskInSameLot() == null || 
											task.getPrivousTaskInSameLot().getPlanEndTimeL() == null || 
											task.getPrivousTaskInSameLot().getPlanEndTimeL() == 0L) ? 0L
						: task.getPrivousTaskInSameLot().getPlanEndTimeL();

		// 获得与当前任务在同一机台上的前一个任务的结束时间。
		Long previousEndTimeSameMachinePlan = 0L;
		
		// 若当前任务是所在机台的首个任务，则开始时间是机台的开始时间
		MachinePlan anchor = task.getMyAnchor();
		if (anchor != null) {
			previousEndTimeSameMachinePlan = anchor.getStartTimeL();
		} else {
			// if the anchor is null and the previous step (a task) is not null,
			// the planEndTime of previous step as the start time
			if(task.getPreviousStep() != null){
				Task previousTask = (Task) task.getPreviousStep();
				previousEndTimeSameMachinePlan = previousTask.getPlanEndTimeL();
			}
		}

		// 同批次前置任务时间为空，且同机台前任务为空
		if ((previousEndTimeInSameLot == null || previousEndTimeInSameLot == 0L) && (previousEndTimeSameMachinePlan == null || previousEndTimeSameMachinePlan == 0L)) {
			return startTime;
		}

		// 取同批次获得的时间与通过机台获得的时间中，较大者，即较后者作为上一个任务的结束时间
		Long previousEndTime = previousEndTimeInSameLot > previousEndTimeSameMachinePlan ? previousEndTimeInSameLot
				: previousEndTimeSameMachinePlan;

		// 若不存在上工序的时间，则取机台开始时间作为开始时间，否则取上工序结束时间作为开始时间
		startTime = (previousEndTime == null || previousEndTime == 0L) ? task.getMachinePlan().getStartTimeL() : previousEndTime;

		// 备份开始时间，以便进行进一步判断
		Long basicStartTime = startTime;

		// 若任务存在预计到达时间，则取预计到达时间及当前工序上述逻辑所得的开始时间中，较迟者
		if ( task.getArrivalTimeL() != null && task.getArrivalTimeL() != 0L) {
			startTime = startTime > task.getArrivalTimeL() ? startTime : task.getArrivalTimeL();
		} else {
			// 若工序存在准备时间，则在上述逻辑所得的开始时间基础上，加上准备时间
			// 准备时间只适用于批次中非首工序，因为批次首工序的预计到达时间已包含了等时间
			if ((task.getTaskLocation() == TaskType_Location_In_Lot.NORMAL || task.getTaskLocation() == TaskType_Location_In_Lot.SINK) && task.getPrepareTime() > 0L) {
				startTime = basicStartTime + task.getPrepareTime();
			}
		}

		// 若任务存在最大等待时间,则取最大等待时间与上述逻辑所得的开始时间中，较早者
		if (task.getMaxWaitingTime() > 0L) {
			Long maxIdle = basicStartTime + task.getMaxWaitingTime();
			startTime = startTime < maxIdle ? startTime : maxIdle;
		}

		// 若任务存在最小等待时间,则取最大等待时间与上述逻辑所得的开始时间中，较晚者
		if (task.getMinWaitingTime() > 0L) {
			Long minIdle = basicStartTime + task.getMinWaitingTime();
			startTime = startTime > minIdle ? startTime : minIdle;
		}

		// 加上非生产任务的时间
		Long nontProdTaskTotalCT = task.getNonproductionTaskTotalCT();
		
		// 两任务之间没有非生产任务,刚直接添加一个任务间隔时间
		if(nontProdTaskTotalCT == 0L){
			startTime += Common.INTERVAL_OF_TASK;
		}else{
			// 任务之间存在非生产任务，则添加两个时间间隔
			startTime += Common.INTERVAL_OF_TASK;
			startTime += nontProdTaskTotalCT;
		}
	
		return startTime;
	}

	/**
	 * check the tasks what nearby, whether should add the non-production tasks
	 * between them.
	 * 
	 * @param currentTask
	 * @param previousTask
	 * @return - the non-production task list between previousTask and
	 *         currentTask
	 * @throws ScriptException
	 * @throws UnsupportedEncodingException
	 */
	public static List<NonproductionTask> getActiveNonProductionTasks(Task previousTask, Task currentTask, List<NonproductionTaskSet> nonpoductionTaskSets) {

		if (currentTask == null || previousTask == null)
			return null;

		// 非生产任务列表为空
		if (nonpoductionTaskSets == null || nonpoductionTaskSets.isEmpty()) {
			return null;
		}

		// 获得一个工序编号与当前任务的工序编号一致的非生产任务集对象
		NonproductionTaskSet nonProdTskSet = nonpoductionTaskSets.stream()
				.filter(x -> x.getProcessCode().equals(currentTask.getProcessCode())).findAny().orElse(null);

		if (nonProdTskSet == null) {
			return null;
		}

		// 对当前非生产任务集中的非生产任务进行检查，找出符合当前前后任务的子集
		// 创建一个空的非生产任务列表
		List<NonproductionTask> activeNonProdTaskObjList = new ArrayList<NonproductionTask>();
		
		// 前一个任务的结束时间后推10秒，作为首个非生产任务的开始时间，
		// 而非首个非生产任务的开始时间，则等于上一个非生产任务的结束时间后推10秒
		//Date nonProductionStartTime = Common.getTimeOffset(previousTask.getPlanEndTime(), Common.INTERVAL_OF_TASK, Calendar.SECOND);
		Long nonProductionStartTime = previousTask.getPlanEndTimeL() + Common.INTERVAL_OF_TASK;//   Common.getTimeOffset(previousTask.getPlanEndTime(), Common.INTERVAL_OF_TASK, Calendar.SECOND);

		Task preTask = (Task) previousTask;
		
		// 通过对每一个非生产任务进行判断，确定其是否适用于当前两个相邻任务的非生产任务
		for (NonproductionTask nonProductionTask : nonProdTskSet.getNonproductionTasks()) {
			// 过滤条件为空的非生产任务
			if (nonProductionTask == null || nonProductionTask.getConditionString() == null
					|| nonProductionTask.getConditionString().equals(""))
				continue;

			// 判断该非生产任务是否适用于当前任务
			if (!nonProductionTask.couldBeNonproductionTask(preTask, currentTask)) {
				continue;
			}
			
			// 创建一个新的非生产任务对象，因为生产任务对象列表中的对象，并没有开始时间及结束时间，仅可以作为模板。
			NonproductionTask newNonProductionTask = new NonproductionTask(nonProductionTask.getId(), nonProductionTask.getNonproductionTaskID(),
					nonProductionTask.getName(), nonProductionTask.getSequence(), nonProductionTask.getDuration(),
					nonProductionTask.getCost(), nonProductionTask.getScore(), nonProductionTask.getConditionString());

			// 设计该非生产任务的开始、结束时间
			newNonProductionTask.setPlanStartTimeL(nonProductionStartTime);
			newNonProductionTask.setNonproductionTaskSet(nonProductionTask.getNonproductionTaskSet());
			newNonProductionTask.setPreviousTask((Task)previousTask);
			newNonProductionTask.setCurrentTask(currentTask);
			
			// 将非生产任务加入到当前任务的非生产任务列表中。
			activeNonProdTaskObjList.add(newNonProductionTask);

			// 将非生产任务的开始时间设计为结束时间，以备下一个循环使用
			nonProductionStartTime = newNonProductionTask.getPlanEndTimeL();
			nonProductionStartTime += Common.INTERVAL_OF_TASK;
		}

		return activeNonProdTaskObjList;

	}

	/**
	 * update the start time of task, and its successor tasks
	 * 
	 * @param scoreDirector
	 * @param task
	 */
	/*
	public static void updateTaskStartTime(ScoreDirector scoreDirector, Task task) {
		if (task == null || task.getMachinePlan() == null)
			return;

		// update the plan start time of the task
		boolean updated = updatePlanStartTime(scoreDirector, task);
		if (updated == false)
			return;
		
		// if the task plan start time has been update, update its successor tasks 
		// a queue to save the successor of the task, including tasks in same
		// lot and tasks in same machine plan
		Queue<Task> uncheckedSuccessorQueue = new ArrayDeque<Task>();
		if (task.getNextTaskInSameLot() != null) {
			uncheckedSuccessorQueue.add(task.getNextTaskInSameLot());
		}
		if (task.getNextTask() != null) {
			uncheckedSuccessorQueue.add(task.getNextTask());
		}

		while (!uncheckedSuccessorQueue.isEmpty()) {
			Task currentTask = uncheckedSuccessorQueue.remove();
			updated = updatePlanStartTime(scoreDirector, currentTask);
			if (updated) {
				if (currentTask.getNextTaskInSameLot() != null
						&& uncheckedSuccessorQueue.contains(currentTask.getNextTaskInSameLot()) == false) {
					uncheckedSuccessorQueue.add(currentTask.getNextTaskInSameLot());
				}
				if (currentTask.getNextTask() != null
						&& uncheckedSuccessorQueue.contains(currentTask.getNextTask()) == false) {
					uncheckedSuccessorQueue.add(currentTask.getNextTask());
				}
			}
		}
	}

	private static boolean updatePlanStartTime(ScoreDirector scoreDirector, Task task) {
		// 若当前任务为空，或不在机台计划上，则退出
		if (task == null)
			return false;
	
		// 获得并更新任务的开始时间
		Date startTime = getTaskStartTime(task, true);

		if (startTime != null) {
			scoreDirector.beforeVariableChanged(task, "planStartTime");
			task.setPlanStartTime(startTime);
			// taskUpdated = true;
			scoreDirector.afterVariableChanged(task, "planStartTime");

			return true;
		} else {
			startTime = getTaskStartTime(task, true);
			return false;
		}
	}
	
	*/
	
	/*
	private static boolean updatePlanStartTimeL(ScoreDirector scoreDirector, Task task) {
		// 若当前任务为空，或不在机台计划上，则退出
		if (task == null)
			return false;
	
		// 获得并更新任务的开始时间
		Date startTime = getTaskStartTime(task, true);

		if (startTime != null) {
			scoreDirector.beforeVariableChanged(task, "planStartTime");
			task.setPlanStartTime(startTime);
			// taskUpdated = true;
			scoreDirector.afterVariableChanged(task, "planStartTime");

			return true;
		} else {
			startTime = getTaskStartTime(task, true);
			return false;
		}
	}
	*/

	/**
	 * check if the sequence of the task is in a dead loop.
	 * @param task
	 * @return
	 */
/*	
	public static boolean isDeadLoop(Task task) {
		if (task == null || task.getMachinePlan() == null)
			return false;

		List<Task> tasks = new ArrayList<Task>();
		tasks.add(task);

		// get the next task in the same lot.
		Queue<Task> successorQueue = new ArrayDeque<Task>();
		if (task.getNextTaskInSameLot() != null) {
			successorQueue.add(task.getNextTaskInSameLot());
			tasks.add(task.getNextTaskInSameLot());
		}
		
		// get the next task in the same machine plan.
		if (task.getNextTask() != null) {
			successorQueue.add(task.getNextTask());
			tasks.add(task.getNextTask());
		}

		while (!successorQueue.isEmpty()) {
			Task currentTask = successorQueue.remove();

			// get the next task in the same lot.
			Task nextTaskInLot = currentTask.getNextTaskInSameLot();
			if (nextTaskInLot != null) {
				if (successorQueue.contains(nextTaskInLot) == false && tasks.contains(nextTaskInLot) == false) {
					successorQueue.add(nextTaskInLot);
					tasks.add(nextTaskInLot);
				} else {
					return true;
				}
			}

			// get the next task in the same machine plan.
			Task nextTaskInMachinePlan = currentTask.getNextTask();
			if (nextTaskInMachinePlan != null) {
				if (successorQueue.contains(nextTaskInMachinePlan) == false
						&& tasks.contains(nextTaskInMachinePlan) == false) {
					successorQueue.add(nextTaskInMachinePlan);
					tasks.add(nextTaskInMachinePlan);
				} else {
					return true;
				}
			}
		}

		return false;
	}
*/
	
	
	/**
	 * 判断两个TaskTassignment是否需要更新非生产任务,将所得的非生产任务返回
	 * @param previousAssignment
	 * @param currentAssignment
	 */
	/*
	public static List<NonproductionTask> getNonproductionTasks(Task previousTask, Task currentTask) {

		if (previousTask == null || currentTask == null){
			return null;
		}

		// 任务没有参数，表示没有生成非生产计划的条件，不存在非生产计划。
		if (previousTask.getAttributeRequests() == null || previousTask.getAttributeRequests().isEmpty() ||
				currentTask.getAttributeRequests() == null || currentTask.getAttributeRequests().isEmpty()
				){
			return null;
		}

		// 获得这两者之间的非生产任务列表,并将其设置为当前任务的非生产任务列表。
		List<NonproductionTask> nonProductionTasks = CommonFunctions.getActiveNonProductionTasks(previousTask, currentTask);
		
		return nonProductionTasks;
	}
	*/


	public static boolean isDeadLoop(Task startTask) {
		List<Task> checkingTasks = new ArrayList<Task>();
		Queue<Task> nextTaskQueue = new ArrayDeque<Task>();

		if (startTask.getNextTaskInSameLot() != null) {
			nextTaskQueue.add(startTask.getNextTaskInSameLot());
		}

		if (startTask.getNextTask() != null) {
			nextTaskQueue.add(startTask.getNextTask());
		}

		while (!nextTaskQueue.isEmpty()) {
			Task checkTask = nextTaskQueue.remove();
			checkingTasks.add(checkTask);

			if (checkingTasks.contains(startTask)) {
				return true;
			}

			if (checkTask.getNextTaskInSameLot() != null) {
				nextTaskQueue.add(checkTask.getNextTaskInSameLot());
			}

			if (checkTask.getNextTask() != null) {
				nextTaskQueue.add(checkTask.getNextTask());
			}

		}

		return false;

	}
	
	/**
	 * 以指定的任务作为起始任务，将它所有的后置任务及同机台的后续任务重新设定时间
	 * 
	 * @param task
	 */
	public static void updateTaskStartTime(Task task, ScoreDirector scoreDirector) {
		if (task == null || task.getMachinePlan() == null)
			return;

		// 记录访问过的Task
		assessedTaskIDs.clear();
		assessedTaskIDs.add(task.getId());
		
		
		/*
		// 取得任务的开始时间
		Long startTimeL = CommonFunctions.getTaskStartTimeL(task, true);

		if(startTimeL.equals(task.getPlanStartTimeL()) == false){
			scoreDirector.beforeVariableChanged(task, "planStartTimeL");
			task.setPlanStartTimeL(startTimeL);
			scoreDirector.afterVariableChanged(task, "planStartTimeL");
		//	scoreDirector.triggerVariableListeners();
		}
		*/
		
		updateStartTimeField(task, scoreDirector);
		
		if(task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L){
			return;
		}
		
		// 更新同一机台中的下工序
		if (task.getNextTask() != null) {
			updateTaskStartTimeInMachinePlan(task.getNextTask(), scoreDirector);
		}

		// 更新同一工序中的下工序
		if (task.getNextTaskInSameLot() != null) {
			updateTaskStartTimeInLot(task.getNextTaskInSameLot(), scoreDirector);
		}
		
	}

	private static void updateTaskStartTimeInLot(Task task, ScoreDirector scoreDirector) {
		// 若当前任务为空，或不在机台计划上，则退出
		if (task == null)
			return;

	
		// 记录访问过的任务，用于检测是否进入死循环
		if (assessedTaskIDs.contains(task.getId())) {
			return;
		} else {
			assessedTaskIDs.add(task.getId());
		}


		/*
		// 获得并更新任务的开始时间
		Long startTimeL = CommonFunctions.getTaskStartTimeL(task, true);
		
		if(startTimeL.equals(task.getPlanStartTimeL()) == false){
			scoreDirector.beforeVariableChanged(task, "planStartTimeL");
			task.setPlanStartTimeL(startTimeL);
			scoreDirector.afterVariableChanged(task, "planStartTimeL");
		}
		*/
		
		updateStartTimeField(task, scoreDirector);
		
		// 更新同批次中的下一个任务
		if (task.getNextTaskInSameLot() != null) {
			updateTaskStartTimeInLot(task.getNextTaskInSameLot(), scoreDirector);
		}
				
		// 更新同机台中的下一个任务
		if (task.getNextTask() != null) {
			updateTaskStartTimeInMachinePlan(task.getNextTask(), scoreDirector);
		}
	}
	

	/**
	 * 更新指定机台上的任务
	 * @param task
	 * @param scoreDirector
	 */
	private static void updateTaskStartTimeInMachinePlan(Task task, ScoreDirector scoreDirector) {
		if (task == null || task.getPreviousStep() == null)
			return;

	
		if (assessedTaskIDs.contains(task.getId())) {
			return;
		} else {
			assessedTaskIDs.add(task.getId());
		}
	
		/*
		Long startTimeL = CommonFunctions.getTaskStartTimeL(task, true);
		
		if(startTimeL.equals(task.getPlanStartTimeL()) == false){
			scoreDirector.beforeVariableChanged(task, "planStartTimeL");
			task.setPlanStartTimeL(startTimeL);
			scoreDirector.afterVariableChanged(task, "planStartTimeL");;
		}
		*/
		
		updateStartTimeField(task, scoreDirector);
		
		if (task.getNextTask() != null) {
			updateTaskStartTimeInMachinePlan(task.getNextTask(), scoreDirector);
		}
		
		if (task.getNextTaskInSameLot() != null) {
			updateTaskStartTimeInLot(task.getNextTaskInSameLot(), scoreDirector);
		}
	}
	
	/**
	 * 更新计划开始时间属性
	 * @param task
	 * @param scoreDirector
	 */
	private static void updateStartTimeField(Task task, ScoreDirector scoreDirector){
		/*
		if(task.getId() == 1071 && task.getPreviousStep() != null && (task.getPreviousStep() instanceof Task)){
			Task preTask = (Task)task.getPreviousStep();	
			if(preTask.getId() == 1051){
				System.out.println("test");
			}
		}
		*/

		// 获得并更新任务的开始时间
		Long startTimeL = CommonFunctions.getTaskStartTimeL(task);
		
		// 时间有差异则更新
		if(!startTimeL.equals(task.getPlanStartTimeL())){
			scoreDirector.beforeVariableChanged(task, "planStartTimeL");
			task.setPlanStartTimeL(startTimeL);
			scoreDirector.afterVariableChanged(task, "planStartTimeL");
		}
	}
	
	/**
	 * 检查一个任务是否与其所在机台的其它任务存在重叠
	 * @param task
	 * @return
	 */
	public static boolean timeOverlap(Task task){
		if(task.getMachinePlan() == null || task.getMachinePlan().getTasks() == null || task.getMachinePlan().getTasks().isEmpty()){
			return false;
		}
		
		CountableValueRange<Long> rangeTask = ValueRangeFactory.createLongValueRange(task.getPlanStartTimeL(), task.getPlanEndTimeL());
		List<Long> totalTimes = getLongList(rangeTask);
		
		for(Task taskInMachinePlan : task.getMachinePlan().getTasks()){
			rangeTask = ValueRangeFactory.createLongValueRange(taskInMachinePlan.getPlanStartTimeL(), taskInMachinePlan.getPlanEndTimeL());
			List<Long> times = getLongList(rangeTask);
			
			if(totalTimes.stream().filter(x -> times.contains(x)).findAny().orElse(null) == null){
				totalTimes.addAll(times);
			}else{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 检查指定的任务是否与指定列表中的任务存在时间重叠，若存在，则返回重叠量（分钟数）
	 * @param checkingTask
	 * @param tasks
	 * @return
	 */
	public static Long isOverlap(Task checkingTask, List<Task> tasks) {
		if (checkingTask == null || checkingTask.getPlanStartTimeL() == null || checkingTask.getPlanStartTimeL() == 0L
				|| checkingTask.getPlanEndTimeL() == null || checkingTask.getPlanEndTimeL() == 0L || tasks == null || tasks.isEmpty()) {
			return 0L;
		}
		
		// 被测试任务的开始时间应该为该任务的开始时间，往前移非生产任务的时间
		Long checkingTaskStartTime =  getTaskStartTime(checkingTask);
		Long checkingTaskEndTime = checkingTask.getPlanEndTimeL();
		
		
		Long overlapTotal = 0L;
		for(Task task : tasks){
			// 自己不算
			if(task.equals(checkingTask)){
				continue;
			}
			
			if(task.getPlanEndTimeL() == null || task.getPlanEndTimeL() == 0L){
				continue;
			}
			
			Long taskStartTime = getTaskStartTime(task);
			Long taskEndTime = task.getPlanEndTimeL();
			
			//以下Task为列表中与checkTask有重叠的任务，有以下四种重叠情况
			/* 在重叠任务的首尾以内
			 -----------------------------------
			|	Task	     				 	|
			 -----------------------------------
					 ---------------------
					| Checking Task		  |
					 ---------------------
			*/
			if(isBetween(checkingTaskStartTime, taskStartTime, taskEndTime) && 
					isBetween(checkingTaskEndTime, taskStartTime, taskEndTime)
					){
				Long overlap = checkingTaskEndTime - checkingTaskStartTime + 1;
				overlapTotal += overlap;
				// 出现此情况，将不会再存在以下情况的可能。
				continue;
			}
			
			
			/* 包含了重叠任务
					 -------------------
					|	Task	        |
					 -------------------
			 -----------------------------------
			| Checking Task					    |
			 -----------------------------------
			*/
			if(isBetween(taskStartTime, checkingTaskStartTime, checkingTaskEndTime) && 
					isBetween(taskEndTime, checkingTaskStartTime, taskEndTime)
					){
				Long overlap = taskEndTime - taskStartTime + 1;
				overlapTotal += overlap;
				// 出现此情况，将不会再存在以下情况的可能。
				continue;
			}
			
			
			/* 前段与其它任务重叠
			 --------------------
			|	Task		     |
			 --------------------
					 -----------------
					| Checking Task   |
					 -----------------
			*/
			if(isBetween(checkingTaskStartTime, taskStartTime, taskEndTime)){
				Long overlap = taskEndTime - checkingTaskStartTime + 1;
				overlapTotal += overlap;
			}
			
			/* 后段与其它任务重叠
					 --------------------
					|	Task		     |
					 --------------------
			 -----------------
			| Checking Task	  |
			 -----------------
			*/
			if(isBetween(checkingTaskEndTime, taskStartTime, taskEndTime)){
				Long overlap = checkingTaskEndTime - taskStartTime + 1;
				overlapTotal += overlap;
			}
		}
		
		return overlapTotal;
	}
	
	/**
	 * 获得一个任务的开始时间，需要判断是否存在非生产任务，若存在，则需要往前移动相应的时间
	 * @param task
	 * @return
	 */
	private static Long getTaskStartTime(Task task){
		
		Long startTime = task.getPlanStartTimeL();
		// 添加该任务的非生产任务时间段
		NonproductionTaskSummary nonproductionSummy = CommonFunctions.getNonProdTaskSummary(task);
		if(nonproductionSummy != null){
			
			// 非生产任务的结束时间前移非生产任务的持续时间，得到非生产任务的开始时间
			startTime = task.getPlanStartTimeL()  -  nonproductionSummy.getDuation();
		}
		
		return startTime;
	}
	
	private static boolean isBetween(Long check, Long from, Long to){
		return (check >= from && check <= to);
	}

	/**
	 * 检查任务列表中的时间是否有重叠
	 * @param tasks
	 * @return
	 */
	public static boolean isOverlap(List<Task> tasks){
		if(tasks == null || tasks.isEmpty() || tasks.size() == 1){
			return false;
		}
		
		List<Long> timeList = new ArrayList<Long>();
		for(Task task : tasks){
			if(task.getPlanStartTimeL() == null || task.getPlanStartTimeL() == 0L){
				continue;
			}
				
			Long startTime = getTaskStartTime(task);
			
			CountableValueRange<Long> taskRange = ValueRangeFactory.createLongValueRange(startTime, task.getPlanEndTimeL());
			List<Long> timesOfTask =  getLongList(taskRange);
			// 对这些时间数值范围进入重叠判断，一旦发现有数值重叠，即表示有时间重叠
			if(timesOfTask.stream().filter(x -> timeList.contains(x)).findAny().orElse(null) != null){
				return true;
			}
			
			timeList.addAll(timesOfTask);
		}
		
		return false;
		
	}
	
	public static NonproductionTaskSummary getNonProdTaskSummary(Task task){
		if(task.getPreviousStep() == null || (task.getPreviousStep() instanceof MachinePlan)){
			return null;
		}
		
		Task previousTask = ((Task)task.getPreviousStep());
		
		// 根据前后任务的ID组合，从
		Long nonproductionTaskMappingID = Long.valueOf(previousTask.getId().toString() + task.getId().toString());
		// 根据前后任务的组合ID，从Mapping List中获得对应的Nonproduction Task Summary
		NonproductionTaskSummary nonprdTaskSumm = task.getTaskSchedule().getNonproductionMappingList().get(nonproductionTaskMappingID);
		return nonprdTaskSumm;
	}
	
	/*
	// 检查两个任务是否有时间重叠
	public static boolean isOverlap(Task taskLeft, Task taskRight){
		
		if(taskLeft.getPlanEndTimeL() == null || taskLeft.getPlanEndTimeL() == 0 || taskRight.getPlanEndTimeL() == null || taskRight.getPlanEndTimeL() == 0){
			return false;
		}
		
		if(taskLeft.equals(taskRight)){
			return false;
		}
		
		CountableValueRange<Long> range1 = ValueRangeFactory.createLongValueRange(taskLeft.getPlanStartTimeL(), taskLeft.getPlanEndTimeL());
		CountableValueRange<Long> range2 = ValueRangeFactory.createLongValueRange(taskRight.getPlanStartTimeL(), taskRight.getPlanEndTimeL());
		
		List<Long> list1 = getLongList(range1);
		List<Long> list2 = getLongList(range2);
		
		return list1.stream().filter(x -> list2.contains(x)).findAny().orElse(null) != null;
		
	}
	*/

	
	private static List<Long> getLongList(CountableValueRange<Long> range) {

		if(range == null || range.isEmpty()){
			return null;
		}
		
		List<Long> longList = new ArrayList<Long>();
		Iterator<Long> itrTG = range.createOriginalIterator();
		
		if(itrTG == null){
			return null;
		}
		
		while (itrTG.hasNext()) {
			Long lTG = itrTG.next();

			if (!longList.contains(lTG)) {
				longList.add(lTG);
			}
		}

		return longList;
	}
	
	

}
