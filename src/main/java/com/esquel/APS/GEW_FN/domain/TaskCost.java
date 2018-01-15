package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * The cost of a task in a solution, it represent a non-production and it's conditions
 * @author ZhangKent
 *
 */
@XStreamAlias("TaskCost")
public class TaskCost extends AbstractPersistable{
	private String nonproductionTask;		// the non-production information (id and name)
	private Long duration;					// duration of the non-production
	private Long cost;						// cost of the non-production
	private String previousTask;			// the previous task information (id and name)
	private String task;					// the current task information (id and name)
	public String getNonproductionTask() {
		return nonproductionTask;
	}
	public void setNonproductionTask(String nonproductionTask) {
		this.nonproductionTask = nonproductionTask;
	}
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	public Long getCost() {
		return cost;
	}
	public void setCost(Long cost) {
		this.cost = cost;
	}
	public String getPreviousTask() {
		return previousTask;
	}
	public void setPreviousTask(String previousTask) {
		this.previousTask = previousTask;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	public TaskCost(String nonproductionTask, Long duration, Long cost, String previousTask, String task) {
		this.nonproductionTask = nonproductionTask;
		this.duration = duration;
		this.cost = cost;
		this.previousTask = previousTask;
		this.task = task;
	}
	

	
}
