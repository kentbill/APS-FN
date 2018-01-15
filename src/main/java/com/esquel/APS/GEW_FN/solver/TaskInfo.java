package com.esquel.APS.GEW_FN.solver;

import java.util.Date;
import java.util.List;

import com.esquel.APS.GEW_FN.domain.NonproductionInfo;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("TaskInfo")
public class TaskInfo {
	private String machinePlanName;
	private String taskName;
	private Date startTime;
	private Date endTime;
	private List<NonproductionInfo> nonproductionInfos;
	
	public String getMachinePlanName() {
		return machinePlanName;
	}
	public void setMachinePlanName(String machinePlanName) {
		this.machinePlanName = machinePlanName;
	}
	public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
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

	public List<NonproductionInfo> getNonproductionInfos() {
		return nonproductionInfos;
	}
	public void setNonproductionInfos(List<NonproductionInfo> nonproductionInfos) {
		this.nonproductionInfos = nonproductionInfos;
	}
	public TaskInfo(){}
	
	public TaskInfo(String machinePlanName, String taskName, Date startTime, Date endTime, List<NonproductionInfo> nonproductionInfos) {
		this.machinePlanName = machinePlanName;
		this.taskName = taskName;
		this.startTime = startTime;
		this.endTime = endTime;
		this.nonproductionInfos = nonproductionInfos;
	}
	


}
