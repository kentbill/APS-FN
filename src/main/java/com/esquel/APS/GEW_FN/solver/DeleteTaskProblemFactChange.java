package com.esquel.APS.GEW_FN.solver;

import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import com.esquel.APS.GEW_FN.domain.AbstractPersistable;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;

public class DeleteTaskProblemFactChange  extends AbstractPersistable implements ProblemFactChange<TaskAssignmentSolution>{

	private final Task task;
	
	public DeleteTaskProblemFactChange(Task task){
		this.task = task;
	}
	
	@Override
	public void doChange(ScoreDirector<TaskAssignmentSolution> scoreDirector) {
		TaskAssignmentSolution workingSolution = scoreDirector.getWorkingSolution();
		
		
		
	}

}
