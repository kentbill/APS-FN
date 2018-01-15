package com.esquel.APS.GEW_FN.solver;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;

public class MovableTaskSelectionFilter implements SelectionFilter<TaskAssignmentSolution, Task> {

	//private static List<Task> checkingTasks = null;
	//private static Queue<Task> nextTasks = null;
	
	@Override
	public boolean accept(ScoreDirector<TaskAssignmentSolution> scoreDirector, Task selectedTask) {
		
		// 若Task树出现死循环，则不能移动
		//return CommonFunctions.isDeadLoop(selectedTask) == false;
		return true;
	}
	
	/*
	private boolean isDeadLoop(Task startTask) {
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
	*/
	
	/*
	private boolean isDeadLoopInLot(Task task){
		
		if(checkingTasks.contains(task)){
			return true;
		}else{
			checkingTasks.add(task);
		}
		
		if(task.getNextTask() != null){
			isDeadLoopInMachinePlan(task.getNextTask());
		}
		
		if(task.getNextTaskInSameLot() != null){
			isDeadLoopInLot(task.getNextTaskInSameLot());
		}
		
		return false;
		
	}
	
	private boolean isDeadLoopInMachinePlan(Task task){
		if(checkingTasks.contains(task)){
			return true;
		}else{
			checkingTasks.add(task);
		}
		
		if(task.getNextTaskInSameLot() != null){
			isDeadLoopInLot(task.getNextTaskInSameLot());
		}
		
		if(task.getNextTask() != null){
			isDeadLoopInMachinePlan(task.getNextTask());
		}
		
		return false;
		
	}
	*/
}
