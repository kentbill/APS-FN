package com.esquel.APS.GEW_FN.domain;

import com.esquel.APS.Helpers.APSException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/*
 * A class defines the constrain brokens.
 */
@XStreamAlias("Broken")
public class Broken extends AbstractPersistable{
	private String ruleName;
	
	//private Integer machinePlanID;
	private String machinePlanName;
	
	private Long taskID;
	private String taskName;
	private String previousSetpName;
	private Long score;
	private String scoreLevel; // H - HardScore, M - MediumScore, S - SoftScroe
	private String exceptionMessage;


	public Long getTaskID() {
		return taskID;
	}

	public void setTaskID(Long taskID) {
		this.taskID = taskID;
	}

	public String getPreviousSetpName() {
		return previousSetpName;
	}

	public void setPreviousSetpName(String previousSetpName) {
		this.previousSetpName = previousSetpName;
	}

	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

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
	

	public String getPreviousStepName() {
		return previousSetpName;
	}

	public void setPreviousStepName(String previousStepName) {
		this.previousSetpName = previousStepName;
	}

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public String getExceptionMessage() {
		return exceptionMessage;
	}

	public void setExceptionMessage(String exceptionMessage) {
		this.exceptionMessage = exceptionMessage;
	}

	public Broken() {
	}
	
	

	/**
	 * @return the scoreLevel
	 */
	public final String getScoreLevel() {
		return scoreLevel;
	}

	/**
	 * @param scoreLevel the scoreLevel to set
	 */
	public final void setScoreLevel(String scoreLevel) {
		this.scoreLevel = scoreLevel;
	}

	public Broken(Long taskID, String ruleName, String machinePlanName, String taskName, String previousTaskName, Long score,  String socreLevel, String exceptionMessage) {
		this.taskID = taskID;
		this.ruleName = ruleName;
		this.machinePlanName = machinePlanName;
		this.taskName = taskName;
		this.previousSetpName = previousTaskName;
		this.score = score;
		this.scoreLevel = socreLevel;
		this.exceptionMessage = exceptionMessage;
	}
	
	
	@Override
	public String toString() {
		return getClass().getName().replaceAll(".*\\.", "") + "-" + this.ruleName + "-" + this.machinePlanName + "-" + this.taskName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if (obj instanceof Broken) {
			Broken broken = (Broken) obj;
			return (broken.ruleName.equals(this.ruleName) && broken.taskName.equals(this.taskName) && broken.machinePlanName.equals(this.machinePlanName) );
		} else {
			return super.equals(obj);
		}
	}
}
