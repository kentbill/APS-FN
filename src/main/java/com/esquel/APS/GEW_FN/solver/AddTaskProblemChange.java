package com.esquel.APS.GEW_FN.solver;

import java.util.ArrayList;

import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solver.ProblemFactChange;

import com.esquel.APS.GEW_FN.domain.AbstractPersistable;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;

/**
 * 添加任务到Workingsolution
 * @author ZhangKent
 *
 */
public class AddTaskProblemChange extends AbstractPersistable implements ProblemFactChange<TaskAssignmentSolution>{
	private final Task task;
	
	public AddTaskProblemChange(Task task){
		this.task = task;
	}

	@Override
	public void doChange(ScoreDirector<TaskAssignmentSolution> scoreDirector) {
		TaskAssignmentSolution workingSolution = scoreDirector.getWorkingSolution();
		
		workingSolution.setTaskList(new ArrayList<>(workingSolution.getTaskList()));
		
		
		scoreDirector.beforeProblemFactAdded(this.task);
		workingSolution.getTaskList().add(this.task);
		scoreDirector.afterProblemFactAdded(this.task);

		//scoreDirector.triggerVariableListeners();
		
	}
	
	
}
