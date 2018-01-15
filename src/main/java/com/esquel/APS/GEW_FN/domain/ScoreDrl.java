package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ScoreDrl")
public class ScoreDrl extends AbstractPersistable{
	private String key;
	private String drlFileName;
	private String remark;
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDrlFileName() {
		return drlFileName;
	}
	public void setDrlFileName(String drlFileName) {
		this.drlFileName = drlFileName;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public ScoreDrl(String key, String drlFileName, String remark) {
		this.key = key;
		this.drlFileName = drlFileName;
		this.remark = remark;
	}
	
	@Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-" + this.key + " - " + this.remark;
    }
}
