package com.esquel.APS.GEW_FN.domain;

import java.util.Date;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("NonproductionInfo")
public class NonproductionInfo{
	private Long id;
	private Long nonproductionTaskID;
	private String name;
	private Date startTime;
	private Date endTime;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getNonproductionTaskID() {
		return nonproductionTaskID;
	}
	public void setNonproductionTaskID(Long nonproductionTaskID) {
		this.nonproductionTaskID = nonproductionTaskID;
	}
	
	public NonproductionInfo(NonproductionTask nonproductionTask) {
		if(nonproductionTask != null){
			this.id = nonproductionTask.getId();
			this.nonproductionTaskID = nonproductionTask.getNonproductionTaskID();
			this.name = nonproductionTask.getName();
			this.startTime = nonproductionTask.getPlanStartTime() == null ? null : (Date)nonproductionTask.getPlanStartTime().clone();
			this.endTime = (Date)nonproductionTask.getPlanEndTime().clone();
		}
	}
	
	
}
