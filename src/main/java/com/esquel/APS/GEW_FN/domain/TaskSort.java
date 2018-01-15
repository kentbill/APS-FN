package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * indicate a field from task will be sort and the sort mode, 
 * @author ZhangKent
 *
 */
@XStreamAlias("TaskSort")
public class TaskSort extends AbstractPersistable{
	private String fieldName;
	private boolean sortMode;	// true - Ascending, false - descending 	
	
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public boolean getSortMode() {
		return sortMode;
	}

	public void setSortMode(boolean sortMode) {
		this.sortMode = sortMode;
	}
	

	public TaskSort(Long id, String fieldName, boolean sortMode) {
		super(id);
		this.fieldName = fieldName;
		this.sortMode = sortMode;
	}

	@Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + ":" + this.fieldName + "-" + this.sortMode;
    }
}
