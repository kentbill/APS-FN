package com.esquel.APS.GEW_FN.domain;

import java.util.HashMap;
import java.util.List;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.drools.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

//import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
//import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
//import org.optaplanner.persistence.xstream.api.score.buildin.hardsoft.HardSoftScoreXStreamConverter;

/**
 * the problem
 */
@PlanningSolution
public class TaskAssignmentSolution  extends AbstractPersistable{


	private static final long serialVersionUID = 1L;

	private HardMediumSoftLongScore score;

	private List<MachinePlan> machinePlanList;
	private List<Task> taskList;
	private HashMap<Long, NonproductionTaskSummary> nonproductionMappingList;
	

	@PlanningScore
	public HardMediumSoftLongScore getScore() {
		return score;
	}

	public void setScore(HardMediumSoftLongScore score) {
		this.score = score;
	}

	@ProblemFactCollectionProperty
	@ValueRangeProvider(id = "machinePlanRange")
	public List<MachinePlan> getMachinePlanList()	{
		
		return this.machinePlanList;
	}

	public void setMachinePlanList(List<MachinePlan> machineList)	{
		for(MachinePlan machinePlan : machineList){
			machinePlan.setTaskSchedule(this);
		}
		
		this.machinePlanList = machineList;
	}
	
	@PlanningEntityCollectionProperty
	@ValueRangeProvider(id = "taskRange")
	public List<Task> getTaskList()	{
		return this.taskList;
	}

	public void setTaskList(List<Task> taskList)	{
		
		for(Task task : taskList){
			task.setTaskSchedule(this);
		}
		
		this.taskList = taskList;
	}

	public TaskAssignmentSolution()	{
	}
	
	public TaskAssignmentSolution(List<MachinePlan> machinePlanList, List<Task> taskList)	{
		this.machinePlanList = machinePlanList;
		this.taskList = taskList;
	}

	public HashMap<Long, NonproductionTaskSummary> getNonproductionMappingList() {
		return nonproductionMappingList;
	}

	public void setNonproductionMappingList(HashMap<Long, NonproductionTaskSummary> nonproductionMappingList) {
		this.nonproductionMappingList = nonproductionMappingList;
	}
	
	
}