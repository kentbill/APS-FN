package com.esquel.APS.GEW_FN.domain.enums;

/**
 * ʵ�����ͣ����ڽ���XML��Դ�ļ�ƥ�䣬Ŀǰ��:
 * ����(TASK), 
 * ��̨�ƻ�(MACHINE_PLAN),
 * ����������(NONPRODUCTION_TASK), 
 * ������(PLANNING_RESULT), 
 * ������־(RUNNING_LOG), 
 * Լ���б�(CONSTRAINT),
 * ��������(PROCESS_SEQUENCE)
 * 
 * @author ZhangKent
 *
 */
public enum EntityCategory {
	TASK, 
	MACHINE_PLAN, 
	NONPRODUCTION_TASK, 
	SOLVER_CONFIGURATION, 
	PLANNING_RESULT, 
	RUNNING_LOG, 
	CONSTRAINT,
	WORD,
	REMOTE_SERVICE,
	TASK_SORT,
	SCORE_DRL,
	TASK_INFO_test
}
