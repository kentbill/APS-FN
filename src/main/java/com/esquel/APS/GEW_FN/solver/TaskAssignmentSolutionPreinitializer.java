package com.esquel.APS.GEW_FN.solver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.optaplanner.core.impl.phase.custom.AbstractCustomPhaseCommand;
import org.optaplanner.core.impl.score.director.ScoreDirector;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.Step;
import com.esquel.APS.GEW_FN.domain.Task;
import com.esquel.APS.GEW_FN.domain.TaskAssignmentSolution;
import com.esquel.APS.GEW_FN.domain.enums.RuleDefinition;
import com.esquel.APS.GEW_FN.domain.enums.TaskMachinePlanBrokenType;
import com.esquel.APS.GEW_FN.domain.enums.TimeComparisonResultType;
import com.esquel.APS.Helpers.Common;

public class TaskAssignmentSolutionPreinitializer extends AbstractCustomPhaseCommand<TaskAssignmentSolution> {

	@Override
	public void changeWorkingSolution(ScoreDirector<TaskAssignmentSolution> scoreDirector) {
		TaskAssignmentSolution solution = scoreDirector.getWorkingSolution();
		this.initializeTaskMachinePlan(scoreDirector, solution);
	}
	
	/**
	 * 
	 * @param scoreDirector
	 * @param solution
	 */
	private void initializeTaskMachinePlan(ScoreDirector<TaskAssignmentSolution> scoreDirector, TaskAssignmentSolution solution){
		for(Task task : solution.getTaskList()){
			if(task == null)
				continue;
			
			List<MachinePlan> machinePlans = task.getUsableMachinePlan();
			
			if(machinePlans == null || machinePlans.isEmpty())
				continue;
			
			if(task.getMachinePlan() == null){
				// update machine plan, 
				
				MachinePlan machinePlan = this.getBestMachinePlan(task, machinePlans);
				scoreDirector.beforeVariableChanged(task,"previousStep");
				//scoreDirector.beforeVariableChanged(task,"machinePlan");
				task.setMachinePlan(machinePlan);
				//scoreDirector.afterVariableChanged(task, "machinePlan");
				
				
				//List<Task> ts = machinePlan.getTasks();
				
				Step preStep = machinePlan.getLastTask() == null ? machinePlan : machinePlan.getLastTask();
				
				task.setPreviousStep(preStep);
				//scoreDirector.afterVariableChanged(task, "previousStep");
				
				//scoreDirector.beforeVariableChanged(task,"nextTask");
				preStep.setNextTask(task);
				//scoreDirector.afterVariableChanged(task, "nextTask");
				
				task.setNonProductionTaskList(this.getNonproductionTasks(task.getPreviousStep(), task));
				
				//scoreDirector.beforeVariableChanged(task,"planStartTime");
				task.setPlanStartTimeL(CommonFunctions.getTaskStartTimeL(task));
				//scoreDirector.afterVariableChanged(task, "planStartTime");
				
				scoreDirector.afterVariableChanged(task,"previousStep");
				
				scoreDirector.triggerVariableListeners();
				
				machinePlan.appendTask(task);
			}
		}
		
	//	this.TestPlanning(solution.getTaskList());
		
		
	}
	
	/**
	 * gets the best machine plan from the usable machine plan list
	 * @param machinePlans
	 */
	@Deprecated
	private MachinePlan getBestMachinePlan(Task task, List<MachinePlan> machinePlans){
		// get the usable machine plan, some machine plan have full if all task was assigned to only one machine plan
		List<MachinePlan> usableMachinePlan = new ArrayList<MachinePlan>();
		for(MachinePlan machinePlan : machinePlans){
			task.setMachinePlan(machinePlan);
			task.setPreviousStep(machinePlan.getLastTask() == null ? machinePlan : machinePlan.getLastTask());
			Long dateTest = CommonFunctions.getTaskStartTimeL(task);
			
			if(dateTest < machinePlan.getEndTimeL()){
				usableMachinePlan.add(machinePlan);
			}
			/*
			if(Common.laterOrEarlier(dateTest, machinePlan.getEndTime()) == TimeComparisonResultType.EARLIER){
				usableMachinePlan.add(machinePlan);
			}
			*/
		}
		task.setMachinePlan(null);
		task.setPreviousStep(null);
		
		// update the usable machine plan list of a task.
		task.setUsableMachinePlan(usableMachinePlan);
		
		MachinePlan minWorloadMachinePlan = task.getUsableMachinePlan().stream().min((mp1, mp2) -> (mp1.getWorkLoad() - mp2.getWorkLoad())).get();
		
		return minWorloadMachinePlan;
		
	}
	
	
	
	private List<NonproductionTask> getNonproductionTasks(Step previousStep, Task task){
		return null;
		/*
		if(previousStep == null || task == null || (previousStep instanceof MachinePlan)){
			return null;
		}
		
		return CommonFunctions.getActiveNonProductionTasks((Task) previousStep, task);
		*/
	}
	
	
}
