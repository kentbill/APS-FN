package com.esquel.APS.GEW_FN.domain;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * ���������񼯣�������һ�����������õķ����������б��������������������������
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("NonproductionTaskSet")
public class NonproductionTaskSet extends AbstractPersistable {
	private static final long serialVersionUID = 1L;

	private String processCode;
	private List<NonproductionTask> nonproductionTasks;
	
	private Long processId;

	/**
	 * ��õ�ǰ�������������õĹ���
	 * 
	 * @return
	 */
	public String getProcessCode() {
		return processCode;
	}

	/**
	 * ���õ�ǰ�������������õĹ���
	 * 
	 * @param processCode
	 */
	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}

	/**
	 * ��õ�ǰ���������񼯵ķ����������б�
	 * 
	 * @return
	 */
	public List<NonproductionTask> getNonproductionTasks() {
		return nonproductionTasks;
	}
	
	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	/**
	 * ���õ�ǰ���������񼯵ķ����������б�
	 * 
	 * @param nonproductionTasks
	 */
	public void setNonproductionTasks(List<NonproductionTask> nonproductionTasks) {
		this.nonproductionTasks = nonproductionTasks;
	}

	public NonproductionTaskSet() {
	}

	public NonproductionTaskSet(String processCode, List<NonproductionTask> nonproductionTasks) {
		this.processCode = processCode;
		this.nonproductionTasks = nonproductionTasks;
	}
	
	
	@Override
	public String toString(){
		return getClass().getName().replaceAll(".*\\.", "") + "-" + processCode ;
	}
}
