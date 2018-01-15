package com.esquel.APS.GEW_FN.domain;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

import com.esquel.APS.GEW_FN.solver.ConditionAssertion;
import com.esquel.APS.GEW_FN.solver.interactor.NonproductionAssertionInteractor;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ��ʾһ��������������󣬱�����ÿ��������������ָ���Ĺ����е���������
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("NonproductionTask")
public class NonproductionTask extends AbstractPersistable{
	private static final long serialVersionUID = 1L;

	private String name;
	private Long duration;
	private Long cost;
	private Long score;
	private String conditionString;

	private Long sequence;
	private Date planStartTime;
	private Date planEndTime;
	
	private Task previousTask;
	private Task currentTask;
	
	
	
	private NonproductionTaskSet nonproductionTaskSet; 
	
	// 将时间相关的成员变成Long型
	//private Long durationL;
	private Long planStartTimeL;
	private Long planEndTimeL;
	
	// 对应APSNonProdTaskDefine.NonProdTaskDefineID
	private Long nonproductionTaskID;

	/**
	 * ��ǰ�������������ڽ���Drools�����жϵ�����
	 */
	private NonproductionAssertionInteractor interactor;

	/*
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	*/

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public Long getScore() {
		return score;
	}

	public void setScore(Long score) {
		this.score = score;
	}

	public String getConditionString() {
		return conditionString;
	}

	public void setConditionString(String conditionString) {
		this.conditionString = conditionString;
	}

	public Long getSequence() {
		return sequence;
	}

	public void setSequence(Long sequence) {
		this.sequence = sequence;
	}

	public Date getPlanStartTime() {
		return planStartTime;
	}

	public void setPlanStartTime(Date planStartTime) {
		this.planStartTime = planStartTime;
	}
	
/*
	public Long getDurationL() {
		return durationL;
	}

	public void setDurationL(Long durationL) {
		this.durationL = durationL;
	}
	*/

	public Long getPlanStartTimeL() {
		return planStartTimeL;
	}

	public void setPlanStartTimeL(Long planStartTimeL) {
		this.planStartTimeL = planStartTimeL;
		
		Long planEndTime = this.planStartTimeL + this.duration;
		this.planEndTimeL = planEndTime;
	}
	

	
	public Long getPlanEndTimeL() {
		if(this.planEndTimeL == null || this.planEndTimeL == 0L){
			if(this.planStartTime == null || this.planStartTimeL == 0L){
				return 0L;
			}else{
				this.planEndTimeL = this.planStartTimeL + this.duration;
			}	
		}
		return planEndTimeL;

	}

	public void setPlanEndTimeL(Long planEndTimeL) {
		this.planEndTimeL = planEndTimeL;
	}


	public void setPlanEndTime(Date planEndTime) {
		this.planEndTime = planEndTime;
	}


	public Date getPlanEndTime() {
		if(this.planStartTime == null){
			return null;
		}
		
		return Common.getTimeOffset(this.planStartTime, this.duration, Calendar.MINUTE);
	}

	/*
	public void setPlanEndTime(Date planEndTime) {
		this.planEndTime = planEndTime;
	}
	*/

	public NonproductionAssertionInteractor getInteractor() {
		return interactor;
	}

	public void setInteractor(NonproductionAssertionInteractor interactor) {
		this.interactor = interactor;
	}

	public NonproductionTaskSet getNonproductionTaskSet() {
		return nonproductionTaskSet;
	}

	public void setNonproductionTaskSet(NonproductionTaskSet nonproductionTaskSet) {
		this.nonproductionTaskSet = nonproductionTaskSet;
	}

	public Task getPreviousTask() {
		return previousTask;
	}

	public void setPreviousTask(Task previousTask) {
		this.previousTask = previousTask;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}
	
	/**
	 * 对应APSNonProdTaskDefine.NonProdTaskDefineID字段
	 * @return
	 */
	public Long getNonproductionTaskID() {
		return nonproductionTaskID;
	}

	/**
	 * 对应APSNonProdTaskDefine.NonProdTaskDefineID字段
	 * @param nonproductionTaskID
	 */
	public void setNonproductionTaskID(Long nonproductionTaskID) {
		this.nonproductionTaskID = nonproductionTaskID;
	}

	public NonproductionTask() {
	}

	/**
	 * 
	 * @param id
	 * @param name
	 * @param sequence
	 * @param duration
	 * @param cost
	 * @param score
	 * @param conditionString
	 */
	public NonproductionTask(Long id,Long nonproductionTaskID, String name, Long sequence, Long duration, Long cost, Long score,
			String conditionString) {
		super(id);
		this.nonproductionTaskID = nonproductionTaskID;
		this.name = name;
		this.sequence = sequence;
		this.duration = duration;
		this.cost = cost;
		this.score = score;
		this.conditionString = conditionString;
	}

	/**
	 * check if the non-production need to be insert between 2 tasks.
	 * @param previousTask
	 * @param currentTask
	 * @return
	 */
	public boolean couldBeNonproductionTask(Task previousTask, Task currentTask) {

		/*
		NonproductionAssertionInteractor assertInterator = new NonproductionAssertionInteractor(this.id, previousTask, currentTask, this.interactor.getRules());
		assertInterator.setNonproductionTask(this);
		*/
		
		this.interactor.setPreviousTask(previousTask);
		this.interactor.setCurrentTask(currentTask);
			
		// Check if the non-production need to be insert between previousTask and currentTask
		boolean matchCondition = false;
		ConditionAssertion assertion = ConditionAssertion.getInstance(null, null);
		matchCondition = assertion.AssertTrue(this.interactor);
		
		// reset the task pair of the interactor
		this.interactor.setPreviousTask(null);
		this.interactor.setCurrentTask(null);
		this.interactor.setAssertReuslt(false);
		
		return matchCondition;
	}

	 @Override
	    public String toString() {
	        return getClass().getName().replaceAll(".*\\.", "") + " [" + id + " - " + this.name + "]";
	    }
	 
}
