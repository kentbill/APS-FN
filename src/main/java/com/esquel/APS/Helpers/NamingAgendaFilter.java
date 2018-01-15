package com.esquel.APS.Helpers;

import org.drools.core.spi.Activation;
import org.kie.api.runtime.rule.AgendaFilter;
import org.kie.api.runtime.rule.Match;

/**
 * Rule 名称过滤器
 * 
 * @author ZhangKent
 *
 */
public class NamingAgendaFilter implements AgendaFilter {
	private String ruleName;

	public NamingAgendaFilter() {
	}

	public NamingAgendaFilter(String ruleName) {
		this.ruleName = ruleName;
	}

	public boolean accept(Match match) {
		String ruleName = match.getRule().getName();
		return ruleName.equals(this.ruleName);
	}
	
	 @Override
	    public String toString() {
	        return getClass().getName().replaceAll(".*\\.", "") + " [" + this.ruleName +  "]";
	    }

}
