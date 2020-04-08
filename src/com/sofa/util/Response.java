package com.sofa.util;

public class Response {
	
	// response attributes
	private Request request;
	private String body;
	private NoCaseMap metaData;
	private int status;
	
	/**
	 * Initializes the response with empty values
	 */
	public Response() {
		request = new Request();
		body = new String();
		metaData = new NoCaseMap();
		status = 0;
	}
	
	// body
	public String getBody() { return body; }
	public void setBody(String body) { this.body = body; }

	// meta data
	public NoCaseMap getMetaData() { return metaData; }
	public void setMetaData(NoCaseMap metaData) { this.metaData = metaData; }

	// status
	public int getStatus() { return status; }
	public void setStatus(int status) { this.status = status; }

	// request
	public Request getRequest() {
		if(this.request == null) this.request = new Request();
		return this.request;
	}
	public void setRequest(Request request) { this.request = request; }
}
