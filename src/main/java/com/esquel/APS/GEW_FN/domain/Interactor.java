package com.esquel.APS.GEW_FN.domain;

import java.util.List;

/**
 * ͨ��Drools���ж�̬������֤ʱ��ʹ�ô���Ķ����������壬���ڶ�ָ����rule����ִ��ʱ����������������ؽ��
 * ���಻��ʵ�����������ܹ����������ʵ����.
 * 
 * @author ZhangKent
 *
 */
public abstract class Interactor {
	/**
	 * ��ǰ�����Ӧ��ʵ���ID
	 */
	protected java.lang.Integer id;
	protected List<MatchRule> rules;
	protected boolean assertReuslt = false;

	/**
	 * ��õ�ǰ�����Ӧ��ʵ���ID
	 * 
	 * @return
	 */
	public java.lang.Integer getId() {
		return id;
	}

	/**
	 * ���õ�ǰ�����Ӧ��ʵ���ID
	 * 
	 * @param id
	 */
	public void setId(java.lang.Integer id) {
		this.id = id;
	}

	public List<MatchRule> getRules() {
		return rules;
	}

	public void setRules(List<MatchRule> rules) {
		this.rules = rules;
	}

	public boolean isAssertReuslt() {
		return assertReuslt;
	}

	public void setAssertReuslt(boolean assertReuslt) {
		this.assertReuslt = assertReuslt;
	}
}
