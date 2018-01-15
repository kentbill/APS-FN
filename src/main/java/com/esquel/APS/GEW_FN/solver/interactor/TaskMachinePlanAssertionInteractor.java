package com.esquel.APS.GEW_FN.solver.interactor;

import java.util.List;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.esquel.APS.GEW_FN.domain.MatchRule;
import com.esquel.APS.GEW_FN.domain.Task;

/**
 * ���ж�һ�������Ƿ���Է��䵽ָ����̨�ƻ���ʱ��ͨ��������������������������
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
	
	/*
	public TaskMachinePlanAssertionInteractor(int id, List<MatchRule> rules)
	{
		this.id = id;
		this.rules = rules;
	}
	*/
	
	public TaskMachinePlanAssertionInteractor(Long id, List<MatchRule> rules, MachinePlan machinePlan)
	{
		this.id = id;
		this.rules = rules;
		this.machinePlan = machinePlan;
	}
	
	public TaskMachinePlanAssertionInteractor(Long id, List<MatchRule> rules,MachinePlan machinePlan, Task task)
	{
		this.id = id;
		this.rules = rules;
		this.machinePlan = machinePlan;
		this.task = task;
	}
	
	/*
	public TaskMachinePlanAssertionInteractor(int id, MachinePlan machinePlan, Task task)
	{
		this.id = id;
		this.task = task;
		this.machinePlan = machinePlan;
	}
	*/
	
	@Override
    public String toString() {
		String toString = getClass().getName().replaceAll(".*\\.", "");
		if(this.task == null){
			toString = getClass().getName().replaceAll(".*\\.", "") + " [" + id + ":{NULL}/" + this.machinePlan.getId() + "," +  this.machinePlan.getMachineNO() + "]";
		}else{
			toString = getClass().getName().replaceAll(".*\\.", "") + " [" + id + ": " + this.task.getProcessCode() + "/" + this.machinePlan.getId() + "," + this.machinePlan.getMachineNO() + "]";
		}
		
        return toString;
    }

}
