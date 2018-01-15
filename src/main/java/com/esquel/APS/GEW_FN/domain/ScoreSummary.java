package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ScoreSummary")
public class ScoreSummary extends AbstractPersistable implements Cloneable {
	private Long timeStamp;
	private Long hardScore;
	private Long mediumScore;
	private Long softScore;
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Long getHardScore() {
		return hardScore;
	}
	public void setHardScore(Long hardScore) {
		this.hardScore = hardScore;
	}
	
	public Long getMediumScore() {
		return mediumScore;
	}
	public void setMediumScore(Long mediumScore) {
		this.mediumScore = mediumScore;
	}
	public Long getSoftScore() {
		return softScore;
	}
	public void setSoftScore(Long softScore) {
		this.softScore = softScore;
	}
	public ScoreSummary(Long id, Long timeStamp, Long hardScore,Long mediumScore, Long softScore) {
		super(id);
		this.timeStamp = timeStamp;
		this.hardScore = hardScore;
		this.mediumScore = mediumScore;
		this.softScore = softScore;
	}
	
	/*
	@Override
	public int compareTo(ScoreSummary other) {
		if(this.timeStamp < other.getTimeStamp()){
			return -1;
		} else if (this.timeStamp > other.getTimeStamp()){
			return 1;
		} else{
			return 0;	
		}
		
	}
	*/
	
	public Object clone() {
		ScoreSummary n = null;
		try {
			n = (ScoreSummary) super.clone();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return n;
	}
	
	
}
