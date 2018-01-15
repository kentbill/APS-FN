package com.esquel.APS.GEW_FN.solver.interactor;

import java.util.List;

import com.esquel.APS.GEW_FN.domain.AbstractPersistable;
import com.esquel.APS.GEW_FN.domain.MatchRule;

/**
 * ͨ��Drools���ж�̬������֤ʱ��ʹ�ô���Ķ����������壬���ڶ�ָ����rule����ִ��ʱ����������������ؽ��
 * ���಻��ʵ�����������ܹ����������ʵ����.
 * 
 * @author ZhangKent
 *
 */
public abstract class Interactor extends AbstractPersistable{
	/**
	 * ��ǰ�����Ӧ��ʵ���ID
	 */
	//protected int id;
	protected List<MatchRule> rules;
	protected boolean assertReuslt = false;

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
