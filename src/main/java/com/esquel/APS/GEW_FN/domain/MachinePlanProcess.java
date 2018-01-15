package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ��̨�ƻ����򣬱�ʾĳһ��̨�ƻ����Դ���Ĺ��� ������Process��
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("MachinePlanProcess")
public class MachinePlanProcess extends Process {

	private static final long serialVersionUID = 1L;

	private String conditionExpression;

	public String getconditionExpression() {
		return conditionExpression;
	}

	public void setconditionExpression(String conditionExpression) {
		this.conditionExpression = conditionExpression;
	}

	public MachinePlanProcess() {

	}

	public MachinePlanProcess(Long id, String code, String name, String conditionExpression) {
		super(id, code, name);
		this.conditionExpression = conditionExpression;
	}

	/**
	 * ���һ�������Ƿ��ʺϷ��ڱ���̨�ƻ�������Ҫ��������飺 1.
	 * ��鹤���Ƿ��ڱ�������,���Ƚ�Task��processCode�뵱ǰ�����Code�Ƿ�һ�� 2. ����һ����ͨ�����򼰽����������ʽ��顣
	 */
	public boolean conditionsMatch(Task task) // throws ScriptException
	{
		// ���������Ĺ�����Ϣ������������false
		if ((task == null) || (task.getProcessCode() == null) || (task.getProcessCode().equals(""))) {
			return false;
		}

		// ����һ�£�ֱ�ӷ���false
		if (!task.getProcessCode().equals(this.getCode())) {
			return false;
		}

		return true;

		// ���һ���������(��Ϊ��̬���ʽ����δ��ɣ���ʱ�����й��ղ����ĶԱ�)
		// return this.TestConditionScript(task.getAttributeRequests());

	}

}
