package com.esquel.APS.GEW_FN.ws_invoker;

import java.util.List;

import com.esquel.APS.GEW_FN.domain.AbstractPersistable;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * remote service
 * @author ZhangKent
 *
 */
@XStreamAlias("RemoteService")
public class RemoteService extends AbstractPersistable{
	private String name;
	private String link;
	private String entrance;
	private String operation;
	private String returnTag_Success;
	private String returnTag_Notify;
	private String namespaceURI;
	private String failureMessage;
	private String successMessage;
	private List<WSParameter> parameters;
	
	private String finalURL = "";
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getEntrance() {
		return entrance;
	}

	public void setEntrance(String entrance) {
		this.entrance = entrance;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getReturnTag_Success() {
		return returnTag_Success;
	}

	public void setReturnTag_Success(String returnTag_Success) {
		this.returnTag_Success = returnTag_Success;
	}

	public String getReturnTag_Notify() {
		return returnTag_Notify;
	}

	public void setReturnTag_Notify(String returnTag_Notify) {
		this.returnTag_Notify = returnTag_Notify;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}


	public String getFailureMessage() {
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage) {
		this.failureMessage = failureMessage;
	}

	public String getSuccessMessage() {
		return successMessage;
	}

	public void setSuccessMessage(String successMessage) {
		this.successMessage = successMessage;
	}

	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}

	public List<WSParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<WSParameter> parameters) {
		this.parameters = parameters;
	}
	
	public String getFinalURL() {
		return finalURL;
	}

	public void setFinalURL(String finalURL) {
		this.finalURL = finalURL;
	}

	public RemoteService(String name, String link, String entrance, String operation, String returnTag_Success, String returnTag_Notify, String namespaceURI, String failureMessage, String successMessage, List<WSParameter> parameters) {
		this.name = name;
		this.link = link;
		this.entrance = entrance;
		this.operation = operation;
		this.returnTag_Success = returnTag_Success;
		this.returnTag_Notify = returnTag_Notify;
		this.namespaceURI = namespaceURI;
		this.failureMessage = failureMessage;
		this.successMessage = successMessage;
		this.parameters = parameters;
	}
	
	@Override
	public String toString() {
		return getClass().getName().replaceAll(".*\\.", "") + "-" + this.name;
	}
	
}
