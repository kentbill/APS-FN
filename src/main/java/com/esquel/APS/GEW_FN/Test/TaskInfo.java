package com.esquel.APS.GEW_FN.Test;

import java.util.Date;

import com.esquel.APS.GEW_FN.domain.MachinePlan;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Task")
public class TaskInfo {
	private int id;
	private Date startTime;
	private Date endTime;
	private int machinePlanID;
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the startTime
	 */
	public Date getStartTime() {
		return startTime;
	}
	
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * @return the endTime
	 */
	public Date getEndTime() {
		return endTime;
	}
	
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public TaskInfo(int id, Date startTime, Date endTime, int machinePlanId) {
		super();
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.machinePlanID = machinePlanId;
	}
	
	
}
