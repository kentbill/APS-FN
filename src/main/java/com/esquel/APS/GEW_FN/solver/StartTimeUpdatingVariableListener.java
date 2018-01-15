package com.esquel.APS.GEW_FN.solver;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.esquel.APS.GEW_FN.Test.TaskInfo;
import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.NonproductionTaskSet;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
//import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.enums.GetTimeType;
import com.esquel.APS.GEW_FN.domain.enums.RuleDefinition;
import com.esquel.APS.Helpers.Common;

/*
 * 在排产过程中，通过本类中的方法，设置各个任务的开始及结束时间
 */
public class StartTimeUpdatingVariableListener implements VariableListener<Task> {

	//private List<Long> assessedTaskIDs = new ArrayList<Long>();
	//private List<Task> checkingTasks = null;

	/*
	 * @Override public boolean requiresUniqueEntityEvents(){ return true; }
	 */

	public void afterEntityAdded(ScoreDirector scoreDirector, Task task) {
		
	}

	public void afterEntityRemoved(ScoreDirector scoreDirector, Task task) {
	
	}

	public void afterVariableChanged(ScoreDirector scoreDirector, Task task) {
		this.replanAllTasksTime(scoreDirector);
	}

	/*
	private boolean isDeadLoopInLot(Task task) {

		if (checkingTasks.contains(task)) {
			return true;
		} else {
			checkingTasks.add(task);
		}

		if (task.getNextTask() != null) {
			isDeadLoopInMachinePlan(task.getNextTask());
		}

		if (task.getNextTaskInSameLot() != null) {
			isDeadLoopInLot(task.getNextTaskInSameLot());
		}

		return false;

	}

	private boolean isDeadLoopInMachinePlan(Task task) {
		if (checkingTasks.contains(task)) {
			return true;
		} else {
			checkingTasks.add(task);
		}

		if (task.getNextTaskInSameLot() != null) {
			isDeadLoopInLot(task.getNextTaskInSameLot());
		}

		if (task.getNextTask() != null) {
			isDeadLoopInMachinePlan(task.getNextTask());
		}

		return false;

	}
	*/
	

	public void beforeEntityAdded(ScoreDirector scoreDirector, Task task) {
		// System.out.println("beforeEntityAdded" + task.getId());
	}

	public void beforeEntityRemoved(ScoreDirector scoreDirector, Task task) {
		// System.out.println("beforeEntityRemoved" + task.getId());
	}

	public void beforeVariableChanged(ScoreDirector scoreDirector, Task task) {
	//	this.replanAllTasksTime(scoreDirector);
	}

	/**
	 * 对所有已排入机台计划的任务进行重新设定时间
	 * 
	 * @param scoreDirector
	 */
	private void replanAllTasksTime(ScoreDirector scoreDirector) {
		TaskAssignmentSolution assSolution = ((TaskAssignmentSolution) (scoreDirector.getWorkingSolution()));

		// 先对每个任务设定一个标识性的时间
	
		for (Task task : assSolution.getTaskList()) {
			scoreDirector.beforeVariableChanged(task, "planStartTimeL");
			task.setPlanStartTimeL(null);
			scoreDirector.afterVariableChanged(task, "planStartTimeL");
		}


		//取得每个MachinePlan的首个任务
		List<Task> firstTasks = assSolution.getMachinePlanList().stream().filter(x -> x.getNextTask() != null).map(MachinePlan::getNextTask).collect(Collectors.toList());

		// 从每个首任务开始，对其树进行更新开始时间
		for (Task task : firstTasks) {
			CommonFunctions.updateTaskStartTime(task, scoreDirector);
		}
		
	
	}
}
