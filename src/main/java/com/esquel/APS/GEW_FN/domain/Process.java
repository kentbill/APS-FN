package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * �����࣬��������������Ϣ����Ϊϵͳ�д�������������򣬼���̨�Ŀ��ṩ������ˣ�����ͨ��ֻ���������ɾ���Ĺ�����
 * @author ZhangKent
 *
 */
@XStreamAlias("Process")
public abstract class Process extends AbstractPersistable{
	private static final long serialVersionUID = 1L;
	//private int id;
	private String code;
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Process() {

	}

	public Process(Long id, String code, String name) {
		super(id);
		this.code = code;
		this.name = name;
	}

	
}
