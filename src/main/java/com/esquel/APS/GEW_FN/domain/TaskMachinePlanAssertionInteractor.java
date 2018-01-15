package com.esquel.APS.GEW_FN.domain;

import java.util.List;

/**
 * 在判断一个任务是否可以分配到指定机台计划的时候，通过本截体对象传入参数，并输出结果
 * @author ZhangKent
 *
 */
public class TaskMachinePlanAssertionInteractor extends Interactor
{
	private Task task = null;
	private MachinePlan machinePlan = null;
	
	public Task getTask()
	{
		return task;
	}
	public void setTask(Task task)
	{
		this.task = task;
	}
	public MachinePlan getMachinePlan()
	{
		return machinePlan;
	}
	public void setMachinePlan(MachinePlan machinePlan)
	{
		this.machinePlan = machinePlan;
	}
	
	public TaskMachinePlanAssertionInteractor(){}
	
	public TaskMachinePlanAssertionInteractor(int id, List<MatchRule> rules)
	{
		this.id = id;
		this.rules = rules;
	}
	
	public TaskMachinePlanAssertionInteractor(int id, List<MatchRule> rules, MachinePlan machinePlan)
	{
		this.id = id;
		this.rules = rules;
		this.machinePlan = machinePlan;
	}
	
	public TaskMachinePlanAssertionInteractor(int id, List<MatchRule> rules,MachinePlan machinePlan, Task task)
	{
		this.id = id;
		this.rules = rules;
		this.machinePlan = machinePlan;
		this.task = task;
	}
	
	public TaskMachinePlanAssertionInteractor(int id, MachinePlan machinePlan, Task task)
	{
		this.id = id;
		this.task = task;
		this.machinePlan = machinePlan;
	}

}
