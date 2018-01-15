package com.esquel.APS.Helpers;

import com.esquel.APS.GEW_FN.domain.enums.ExceptionLevel;

public class APSException extends Exception{
	private Long id;
	private ExceptionLevel level;
	private Object owner;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ExceptionLevel getLevel() {
		return level;
	}

	public void setLevel(ExceptionLevel level) {
		this.level = level;
	}

	public Object getOwner() {
		return owner;
	}

	public void setOwner(Object owner) {
		this.owner = owner;
	}

	public APSException(){}
	
	public APSException(String exMessage){
		super(exMessage);
	}
	
	/**
	 * 生成一个明细的异常对象
	 * @param owner 当前异常对应的对象
	 * @param id 异常的ID，通常是owner的ID
	 * @param level 异常级别，分为提示，警告与错误
	 * @param exMessage 异常信息
	 */
	public APSException(Object owner, Long id, ExceptionLevel level, String exMessage){
		super(exMessage);
		this.owner = owner;
		this.id = id;
		this.level = level;
	}
	
	@Override
	public String toString(){
		return this.id + "[" + this.level + "]:" + this.getMessage();
		
	}
}
