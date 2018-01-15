package com.esquel.APS.GEW_FN.solver.interactor;

import java.util.List;

import com.esquel.APS.GEW_FN.domain.MatchRule;
import com.esquel.APS.GEW_FN.domain.NonproductionTask;
import com.esquel.APS.GEW_FN.domain.Task;

/**
 * ���жϷ���������ʱ(ͨ��Drools�ж�)����Ϊ��������������������� ������Interactor
 * 
 * @author ZhangKent
 *
 */
public class NonproductionAssertionInteractor extends Interactor {
	// ���ж���������֮���Ƿ���Ҫ��ӷ���������ʱ������������Ϊǰ����
	private Task previousTask = null;

	// ���ж���������֮���Ƿ���Ҫ��ӷ���������ʱ������������Ϊ������
	private Task currentTask = null;

	// ��ǰ�����Ӧ�ķ���������
	private NonproductionTask nonproductionTask;

	public Task getPreviousTask() {
		return previousTask;
	}

	public void setPreviousTask(Task previousTask) {
		this.previousTask = previousTask;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public NonproductionTask getNonproductionTask() {
		return nonproductionTask;
	}

	public void setNonproductionTask(NonproductionTask nonproductionTask) {
		this.nonproductionTask = nonproductionTask;
	}

	public NonproductionAssertionInteractor() {
	}

	public NonproductionAssertionInteractor(Long id, List<MatchRule> rules, NonproductionTask nonproductionTask) {
		this.id = id;
		this.rules = rules;
		this.nonproductionTask = nonproductionTask;

	}

	public NonproductionAssertionInteractor(Long id, Task previousTask, Task currentTask, List<MatchRule> rules) {
		this.id = id;
		this.previousTask = previousTask;
		this.currentTask = currentTask;
		this.rules = rules;
	}
	
	 @Override
	    public String toString() {
		 
		 if (this.nonproductionTask != null){
	        return getClass().getName().replaceAll(".*\\.", "") + " [" + id + "-" +  this.nonproductionTask.getName() + "]";
		 }else{
			 return getClass().getName().replaceAll(".*\\.", "") + "-" + id;
		 }
	    }
	 
}
