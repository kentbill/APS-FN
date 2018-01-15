package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author ZhangKent
 *
 */
@XStreamAlias("Attribute")
public class Attribute extends AbstractPersistable {

	private static final long serialVersionUID = 1L;

	// private Card card;
	// private TaskProcess process;
	private Task task;
	private String name;
	private String value;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Attribute() {

	}

	public Attribute(Task task, String name, String value) {
		super();
		this.task = task;
		this.name = name;
		this.value = value;
	}

	public Attribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	@Override
	public String toString() {
		return getClass().getName().replaceAll(".*\\.", "") + " - [" +  this.name + ":" + this.value + "]";
	}

}
