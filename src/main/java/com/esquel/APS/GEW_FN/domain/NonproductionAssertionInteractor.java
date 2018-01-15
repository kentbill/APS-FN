package com.esquel.APS.GEW_FN.domain;

import java.util.List;

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
	private Task nextTask = null;

	// ��ǰ�����Ӧ�ķ���������
	private NonproductionTask nonproductionTask;

	protected List<MatchRule> rules;

	public Task getPreviousTask() {
		return previousTask;
	}

	public void setPreviousTask(Task previousTask) {
		this.previousTask = previousTask;
	}

	public Task getNextTask() {
		return nextTask;
	}

	public void setNextTask(Task nextTask) {
		this.nextTask = nextTask;
	}

	public List<MatchRule> getRules() {
		return rules;
	}

	public void setRules(List<MatchRule> rules) {
		this.rules = rules;
	}

	public NonproductionTask getNonproductionTask() {
		return nonproductionTask;
	}

	public void setNonproductionTask(NonproductionTask nonproductionTask) {
		this.nonproductionTask = nonproductionTask;
	}

	public NonproductionAssertionInteractor() {
	}

	public NonproductionAssertionInteractor(int id, List<MatchRule> rules, NonproductionTask nonproductionTask) {
		this.id = id;
		this.rules = rules;
		this.nonproductionTask = nonproductionTask;

	}

	public NonproductionAssertionInteractor(int id, List<MatchRule> rules, Task previousTask, Task nextTask,
			NonproductionTask nonproductionTask) {
		this.id = id;
		this.rules = rules;
		this.previousTask = previousTask;
		this.nextTask = nextTask;
		this.nonproductionTask = nonproductionTask;

	}

	public NonproductionAssertionInteractor(int id, Task previousTask, Task nextTask,
			NonproductionTask nonproductionTask) {
		this.id = id;
		this.previousTask = previousTask;
		this.nextTask = nextTask;
		this.nonproductionTask = nonproductionTask;

	}

	public NonproductionAssertionInteractor(int id, Task previousTask, Task nextTask, List<MatchRule> rules) {
		this.id = id;
		this.previousTask = previousTask;
		this.nextTask = nextTask;
		this.rules = rules;
	}
}
