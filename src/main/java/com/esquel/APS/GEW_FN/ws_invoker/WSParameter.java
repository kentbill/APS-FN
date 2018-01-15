package com.esquel.APS.GEW_FN.ws_invoker;

import com.esquel.APS.GEW_FN.domain.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;
@XStreamAlias("WSParameter")
public class WSParameter extends AbstractPersistable{

	private String name;
	private String value;
	
	
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


	public WSParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}


	@Override
    public String toString() {
        return getClass().getName().replaceAll(".*\\.", "") + "-[" + this.name + ":" + this.value + "]";
    }
}
