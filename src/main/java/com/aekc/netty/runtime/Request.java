package com.aekc.netty.runtime;

import java.io.Serializable;

public class Request implements Serializable{

	private String id ;
	private String name ;
	private String requestMessage ;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRequestMessage() {
		return requestMessage;
	}
	public void setRequestMessage(String requestMessage) {
		this.requestMessage = requestMessage;
	}


}
