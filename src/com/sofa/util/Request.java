package com.sofa.util;

import java.lang.reflect.Method;

public class Request {

	private String path;
	// request attributes
	private Method method;
	private String body;
	private NoCaseMap metaData;
	
	/**
	 * Initializes the request with empty values
	 */
	public Request() {
		path = new String();
		body = new String();
		metaData = new NoCaseMap();
	}
	
	// path
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }

	// method
	public Method getMethod() { return method; }
	public void setMethod(Method method) { this.method = method;  }

	// body
	public String getBody() { return body; }
	public void setBody(String body) { this.body = body; }

	// meta data
	public NoCaseMap getMetaData() { return metaData; }
	public void setMetaData(NoCaseMap metaData) { this.metaData = metaData; }
}
