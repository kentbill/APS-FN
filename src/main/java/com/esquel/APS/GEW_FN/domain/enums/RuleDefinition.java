package com.esquel.APS.GEW_FN.domain.enums;

/**
 * �����壬����ʶ��ͬ�Ĺ�����ʹ�ò�ͬ���㷽��
 * 
 * @author ZhangKent
 *
 */
public enum RuleDefinition {
	/**
	 * ����Լ���������������ڵ������۷�
	 */
	DELIVERY_TIME, 
	/**
	 * ͬһ�����и�����ļӹ�����Լ��
	 */
	TASK_SEQUENCE_IN_SAME_LOT, 
	/**
	 * ���ȴ�ʱ��������������������ȴ�ʱ�䣨��λ�����ӣ���ÿ1���ӿ�1��
	 */
	MAX_WAITING_TIME,
	/**
	 * ��С������������
	 */
	MINIMIZE_NONPRODUCTION_TASK,
	/**
	 * ��������Ľ���ʱ���ڻ�̨�ƻ��Ľ���ʱ��֮ǰ
	 */
	CATCH_UP_MACHINE_PLAN,
	/**
	 * �׸������翪ʼ
	 */
	FIRST_TASK_START_EARLY,
	/**
	 * �����ų��������������ѭ��
	 * 
	 */
	AVOID_DEAD_LOOP,
	/**
	 * ������ͬһ���δ��ڶ����ͬ�������������񱻷�����ͬһ��̨�ƻ���ʱ��ȷ�����ǵķ�������빤�����һ��
	 */
	REPEAT_PROCESS_SEQ,
	/**
	 * the task finished before the request time
	 */
	CATCH_TASK_FIHISH_TIME,
	/**
	 * determines that a task was plan in a machine plan without out of the time scope, in other words, to avoid the task be plan in a unusable time span.
	 */
	ALL_TASK_IN_MACHINE_PLAN
}
