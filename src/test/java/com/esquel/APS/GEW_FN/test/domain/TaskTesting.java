package com.esquel.APS.GEW_FN.test.domain;

import java.util.List;

import com.esquel.APS.GEW_FN.domain.Lot;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.solver.SolveMain;

public class TaskTesting {

	public void TaskValidationTesting() {
	//	SolveMain.initializeData("APS20171005");

		checkTasks(SolveMain._taskListToPlan, SolveMain._lots);
	}

	/**
	 * Test the tasks, checked if the information is valid.
	 * 
	 * @param tasks
	 */
	private void checkTasks(List<Task> tasks, List<Lot> lots) {
		if (tasks == null || tasks.isEmpty()) {
			System.out.println("The task list is empty.");
			return;
		}

		// every task has a lot
		for (Task task : tasks) {
			if (task.getLot() == null) {
				System.out.println("The task [" + task + "] has not a lot.");
			}
		}

		// the source task (the first task in a lot) must have the non-null
		// arrival time
		for (Task task : tasks) {
			if (tasks.stream().filter(x -> x.getNextTaskID() == task.getId()).findAny().orElse(null) == null) {
				if (task.getArrivalTimeL() == null || task.getArrivalTimeL() == 0L) {
					System.out.println("The source task [" + task + "], arrival time is null.");
				}
			}
		}

		// for the non-source task, its arrival time must be null
		for (Task task : tasks) {
			if (tasks.stream().filter(x -> x.getNextTaskID() == task.getId()).findAny().orElse(null) != null) {
				if (task.getArrivalTimeL() == null || task.getArrivalTimeL() != 0L) {
					System.out.println("The non-source task [" + task + "], arrival time is non-null.");
				}
			}
		}
		
		// check if the taskId - nextTaskId forms a loop.
		for (Task task : tasks) {
			
		}
	}

}
