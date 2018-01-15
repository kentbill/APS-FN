package com.esquel.APS.GEW_FN.solver;

import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;

public class MachinePlanSelectionFilter implements SelectionFilter<TaskAssignmentSolution, Task>{

	@Override
	public boolean accept(ScoreDirector<TaskAssignmentSolution> scoreDirector, Task selectedTask) {
		return selectedTask.assignedToFeasibleMachinePlan(true);
	}

}
