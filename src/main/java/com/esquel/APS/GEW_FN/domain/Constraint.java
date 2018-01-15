package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Constraint")
public class Constraint  extends AbstractPersistable{
	/**
	 * Լ����
	 */
	private String name;
	/**
	 * Լ�����ͣ�H - ӲԼ���� S - ��Լ��
	 */
	private String scoreLevel;
	
	/**
	 * ��ֵ, ��Ϊ����Լ���ķ�ֵ�Ƕ�̬��������ģ�����Լ���ķ�ֵΪVariant
	 */
	private String score;

	/**
	 * @return Լ����
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param Լ����
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Լ�����ͣ�H - ӲԼ���� S - ��Լ��
	 */
	public String getType() {
		return scoreLevel;
	}

	/**
	 * @param Լ�����ͣ�H - ӲԼ���� S - ��Լ��
	 */
	public void setType(String type) {
		this.scoreLevel = type;
	}


	public Constraint(){
		
	}

	public Constraint(String name, String type, String score) {
		super();
		this.name = name;
		this.scoreLevel = type;
		this.score = score;
	}
	
	

}
