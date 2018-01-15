package com.esquel.APS.GEW_FN.domain;

import java.util.List;
import java.util.Date;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

//import com.esquel.APS.GEW_FN.solver.MovableTaskSelectionFilter;
import com.esquel.APS.GEW_FN.solver.TaskAssignmentDifficultyComparator;

/**
 * �����࣬��ΪTask��MachinePlan�Ļ��࣬��Ϊ��ʱ����ʱ����ͬ��ʱ������п��ܳ��ֵ�������Ҳ���ܳ��ֵ��ǻ�̨�ƻ���
 * ��ˣ���Ҫһ���������̨�ƻ��Ĺ�ͬ���࣬��ʾʱ�����ϲ�ͬ��ʵ����
 */

@PlanningEntity(difficultyComparatorClass = TaskAssignmentDifficultyComparator.class /*,movableEntitySelectionFilter = MovableTaskSelectionFilter.class */) 
public abstract class Step extends AbstractPersistable {

	private Task nextTask;

	public Step() {
	}

	public Step(Task nextTask) {
		this.nextTask = nextTask;
	}

	/*
	@InverseRelationShadowVariable(sourceVariableName = "previousStep")
	public Task getNextTaskInSameMachinePlan() {
		return this.nextTask;
	}

	public void setNextTaskInSameMachinePlan(Task nextTask) {
		this.nextTask = nextTask;
	}
	*/

	@InverseRelationShadowVariable(sourceVariableName = "previousStep")
	public Task getNextTask() {
		return this.nextTask;
	}

	public void setNextTask(Task nextTask) {
		this.nextTask = nextTask;
	}

	public abstract Date getPlanEndTime();
	
	public abstract Long getPlanEndTimeL();

	public abstract List<Attribute> getAttributeRequests();

}