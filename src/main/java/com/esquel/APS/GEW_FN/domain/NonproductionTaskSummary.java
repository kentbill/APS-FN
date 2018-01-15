package com.esquel.APS.GEW_FN.domain;

public class NonproductionTaskSummary extends AbstractPersistable{
	
	private Long score;
	private Long duation;
	
	private String nonproductionNames;
	
	public Long getScore() {
		return score;
	}
	public void setScore(Long score) {
		this.score = score;
	}
	
	/*
	public void appendScore(Long score){
		if(this.score == null){
			this.score = 0L;
		}
		
		this.score += score;
	}
	*/
	
	public Long getDuation() {
		return duation;
	}
	public void setDuation(Long duation) {
		this.duation = duation;
	}
	
	/*
	public void appendDuation(Long duation){
		if(this.duation == null){
			this.duation = 0L;
		}
		
		this.duation += duation;
	}
	*/
	
	/*
	public void appendNonproductionTaskName(String nonProductionName){
		if(this.nonproductionNames == null){
			this.nonproductionNames = "";
		}
		
		if(this.nonproductionNames == ""){
			this.nonproductionNames = nonProductionName;
		}else{
			this.nonproductionNames += ", " + nonProductionName;
		}
	}
	*/
	
	public String getNonproductionNames() {
		return nonproductionNames;
	}
	public void setNonproductionNames(String nonproductionNames) {
		this.nonproductionNames = nonproductionNames;
	}
	public NonproductionTaskSummary(){}

	public NonproductionTaskSummary(Long id, Long score, Long duation) {
		super(id);
		this.score = score;
		this.duation = duation;
	}

	@Override
    public String toString() {
        return this.id + " - " + this.nonproductionNames;
    }
}
