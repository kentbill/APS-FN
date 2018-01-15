package com.esquel.APS.GEW_FN.domain;

import com.esquel.APS.GEW_FN.domain.enums.PlanningCategory;

/**
 * �����࣬������������������������ʽ��ʵ�塣
 * 
 * @author ZhangKent
 *
 */
public class MatchRule extends AbstractPersistable{
	private static final String _machinePlan = "machinePlan";
	private static final String _nonproductionTask = "nonproductionTask";

	/**
	 * idΪ��ǰ�����Ӧ�Ķ����id, ����������MachinePlan��ΪMachinePlan��process��ID
	 * ������ΪNonproduction��ΪNonproduction��detail��id
	 */
	//private int id;
	
	/**
	 * ��ǰ�������ڵ�ʵ���Ψһ��ţ�������������Ҳ�������ַ���
	 */
	private Object parentId;
//	private String name;
	private String conditionString;
	private PlanningCategory category;

	/**
	 * parentIDΪ��ǰ���������ʵ��ĸ���ʵ���ID�� ����ǰ��������MachniePlan,��parentIDΪMachinePlan���ԣ�
	 * Nonproduction��Ȼ��
	 * 
	 * @return
	 */
	public Object getParentId() {
		return parentId;
	}

	public void setParentId(Object parentId) {
		this.parentId = parentId;
	}

	/**
	 * ��ù�����,��ʽΪ��[�������]_[parentId]_[id]
	 * 
	 * @return
	 */
	public String getName() {
		
		String prefix = category == PlanningCategory.NONPRODUCTION_TASK ? _nonproductionTask : _machinePlan;
		return prefix + "_" + this.parentId.toString() + "_" + this.id;
	}

	/*
	public void setName(String name) {
		this.name = name;
	}
	*/

	public String getConditionString() {
		return conditionString;
	}

	public void setConditionString(String conditionString) {
		this.conditionString = conditionString;
	}

	public MatchRule() {
	}

	public MatchRule( String conditionString, PlanningCategory category) {
	//	this.name = name;
		this.conditionString = conditionString;
		this.category = category;
	}

	public MatchRule(Long id, Object parentId, String conditionString, PlanningCategory category) {
		this.id = id;
		this.parentId = parentId;
		this.conditionString = conditionString;
		this.category = category;
	}
}
