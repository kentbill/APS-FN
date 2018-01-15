package com.esquel.APS.GEW_FN.domain;

import java.util.List;

/**
 * 通过Drools进行动态条件验证时，使用此类的对象作来载体，用于对指定的rule进行执行时，传入参数，并返回结果
 * 此类不可实例化，必须能过其子类进行实例化.
 * 
 * @author ZhangKent
 *
 */
public abstract class Interactor {
	/**
	 * 当前载体对应的实体的ID
	 */
	protected java.lang.Integer id;
	protected List<MatchRule> rules;
	protected boolean assertReuslt = false;

	/**
	 * 获得当前载体对应的实体的ID
	 * 
	 * @return
	 */
	public java.lang.Integer getId() {
		return id;
	}

	/**
	 * 设置当前载体对应的实体的ID
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
