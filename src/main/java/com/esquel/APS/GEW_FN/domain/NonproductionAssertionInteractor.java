package com.esquel.APS.GEW_FN.domain;

import java.util.List;

/**
 * 在判断非生产任务时(通过Drools判断)，作为传入参数及输出结果的载体 派生自Interactor
 * 
 * @author ZhangKent
 *
 */
public class NonproductionAssertionInteractor extends Interactor {
	// 当判断两个任务之间是否需要添加非生产任务时，该属性设置为前任务。
	private Task previousTask = null;

	// 当判断两个任务之间是否需要添加非生产任务时，该属性设置为后任务。
	private Task nextTask = null;

	// 当前载体对应的非生产工序
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
