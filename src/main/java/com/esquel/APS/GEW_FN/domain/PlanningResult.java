package com.esquel.APS.GEW_FN.domain;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.esquel.APS.GEW_FN.domain.enums.StepTypeInChain;
import com.esquel.APS.Helpers.Common;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.esquel.APS.GEW_FN.domain.NonproductionInfo;

/**
 * �Ų��������¼��ÿ�����񱻷��ɵĻ�̨�ƻ������俪ʼʱ�䡢����ʱ����Ϣ��
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("PlanningResult")
public class PlanningResult extends AbstractPersistable {

	private static final long serialVersionUID = 1L;

	private Long taskID;			// ����ID
	private Long machinePlanID;		// ��̨�ƻ�ID
	private Date plannedStartTime;	// �ƻ���ʼʱ��
	private Date plannedEndTime;	// �ƻ�����ʱ��
	//private List<Integer> nonProductionTaskIDs;	// �����������б�
	private List<NonproductionInfo> nonProductionTasks;
	private Long machineIden; // ��̨ID

	// ���³�Ա����������棬���ᱣ�浽�����APSTask_HavePlan�У����ڷ���
	private Long previousStepIDInChain;	// �ڵ�ǰ���������е�ǰһ�����ID��ǰһ�������������Ҳ�����ǻ�̨�ƻ���
	private Long previousStepType;		// ��ʶ��ǰ�����ǰһ�����������(1 - ��̨�ƻ��� 2 - ����)
	private Long nextTaskIDInChain;		// ��ǰ������������һ�����ID
	
	private Long nextTaskIDInSameLot;		// ͬһ���ε���һ������IID
	private Long previousTaskIDInSameLot;	// ͬһ���ε���һ������ID
	
	private Long lotId;


	/**
	 * @return the taskID
	 */
	public Long getTaskID() {
		return taskID;
	}

	/**
	 * @param taskID the taskID to set
	 */
	public void setTaskID(Long taskID) {
		this.taskID = taskID;
	}

	/**
	 * @return the machinePlanID
	 */
	public Long getMachinePlanID() {
		return machinePlanID;
	}

	/**
	 * @param machinePlanID the machinePlanID to set
	 */
	public void setMachinePlanID(Long machinePlanID) {
		this.machinePlanID = machinePlanID;
	}

	/**
	 * @return the plannedStartTime
	 */
	public Date getPlannedStartTime() {
		return plannedStartTime;
	}

	/**
	 * @param plannedStartTime the plannedStartTime to set
	 */
	public void setPlannedStartTime(Date plannedStartTime) {
		this.plannedStartTime = plannedStartTime;
	}

	/**
	 * @return the plannedEndTime
	 */
	public Date getPlannedEndTime() {
		return plannedEndTime;
	}

	/**
	 * @param plannedEndTime the plannedEndTime to set
	 */
	public void setPlannedEndTime(Date plannedEndTime) {
		this.plannedEndTime = plannedEndTime;
	}


	/**
	 * @return the machineIden
	 */
	public Long getMachineIden() {
		return machineIden;
	}

	/**
	 * @param machineIden the machineIden to set
	 */
	public void setMachineIden(Long machineIden) {
		this.machineIden = machineIden;
	}

	/**
	 * @return the previousStepIDInChain
	 */
	public Long getPreviousStepIDInChain() {
		return previousStepIDInChain;
	}

	/**
	 * @param previousStepIDInChain the previousStepIDInChain to set
	 */
	public void setPreviousStepIDInChain(Long previousStepIDInChain) {
		this.previousStepIDInChain = previousStepIDInChain;
	}

	/**
	 * @return the previousStepType
	 */
	public Long getPreviousStepType() {
		return previousStepType;
	}

	/**
	 * @param previousStepType the previousStepType to set
	 */
	public void setPreviousStepType(Long previousStepType) {
		this.previousStepType = previousStepType;
	}

	public Long getNextTaskIDInChain() {
		return nextTaskIDInChain;
	}

	public void setNextTaskIDInChain(Long nextTaskIDInChain) {
		this.nextTaskIDInChain = nextTaskIDInChain;
	}

	public Long getNextTaskIDInSameLot() {
		return nextTaskIDInSameLot;
	}

	public void setNextTaskIDInSameLot(Long nextTaskIDInSameLot) {
		this.nextTaskIDInSameLot = nextTaskIDInSameLot;
	}

	public Long getPreviousTaskIDInSameLot() {
		return previousTaskIDInSameLot;
	}

	public void setPreviousTaskIDInSameLot(Long previousTaskIDInSameLot) {
		this.previousTaskIDInSameLot = previousTaskIDInSameLot;
	}

	public Long getLotId() {
		return lotId;
	}

	public void setLotId(Long lotId) {
		this.lotId = lotId;
	}
	
	public List<NonproductionInfo> getNonProductionTasks() {
		return nonProductionTasks;
	}

	public void setNonProductionTasks(List<NonproductionInfo> nonProductionTasks) {
		this.nonProductionTasks = nonProductionTasks;
	}

	/**
	 * �޲ι��캯��
	 */
	public PlanningResult() {

	}

	/**
	 * ����һ��Task����ʵ��
	 * 
	 * @param task
	 */
	public PlanningResult(Task task) {
		if(task == null)
			return;
		
		this.taskID = task.getId();
		this.previousTaskIDInSameLot = task.getPrivousTaskInSameLot() == null ? 0L : task.getPrivousTaskInSameLot().getId();
		this.nextTaskIDInSameLot = task.getNextTaskInSameLot() == null ? 0L : task.getNextTaskInSameLot().getId();
		this.lotId = task.getLot() == null ? 0L: task.getLot().getId();
		if (task.getMachinePlan() == null) {
			this.machinePlanID = 0L;
		} else {
			
			this.machinePlanID = task.getMachinePlan().getId();
			this.nonProductionTasks = this.getNonproducitons(task);
			
			if (task.getPlanStartTimeL() != null && task.getPlanStartTimeL() != 0L) {
				this.plannedStartTime = Common.getFormatedDatetime(task.getPlanStartTime());
			}
			if (task.getPlanEndTimeL() != null && task.getPlanEndTimeL() != 0L) {
				this.plannedEndTime = Common.getFormatedDatetime(task.getPlanEndTime());
			}
			// ������������һ���������¼����һ�������ID
			if (task.getNextTask() != null) {
				this.nextTaskIDInChain = task.getNextTask().getId();
			}

			// ������������һ�����裬���¼����һ�������ID���������ͣ��п����ǻ�̨������֮һ)
			if (task.getPreviousStep() != null) {
				if (task.getPreviousStep() instanceof Task) {
					this.previousStepType = Long.valueOf(StepTypeInChain.TASK.ordinal());
					this.previousStepIDInChain = ((Task) task.getPreviousStep()).getId();
				} else {
					this.previousStepType = Long.valueOf(StepTypeInChain.MACHINE_PLAN.ordinal());
					this.previousStepIDInChain = ((MachinePlan) task.getPreviousStep()).getId();
				}
			}

			// ����MachineIden
			if ((task.getMachinePlan().getMachineCode() != null)
					&& (!task.getMachinePlan().getMachineCode().equals("")))
				this.machineIden = Long.valueOf(task.getMachinePlan().getMachineCode());

		}
	}

	/**
	 * ��ʽ��ʱ��
	 * 
	 * @param sourceDatetime
	 * @return
	 */
	private Date getFormatedDatetime(Date sourceDatetime) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String soureTimeString = sdf.format(sourceDatetime);
		ParsePosition pos = new ParsePosition(0);
		Date destTime = sdf.parse(soureTimeString, pos);

		return destTime;
	}
	
	private List<NonproductionInfo> getNonproducitons(Task task){
		if(task == null || task.getNonProductionTaskList() == null){
			return null;
		}
		
		List<NonproductionInfo> nonproductionInfos = new ArrayList<NonproductionInfo>();
		
		for(NonproductionTask nonproductionTask : task.getNonProductionTaskList()){
			NonproductionInfo NonproductionInfo = new NonproductionInfo(nonproductionTask );
			nonproductionInfos.add(NonproductionInfo);
		}
		
		return nonproductionInfos;
	}


	@Override
	public String toString() {
		return "PlanningResult [taskID=" + this.taskID + ", machinePlanID=" + this.machinePlanID + ", plannedStartTime="
				+ this.plannedStartTime + ",plannedEndTime=" + this.plannedEndTime + "]";
	}
}
