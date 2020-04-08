package com.sofa.metric.util;

public class Link {

	private String url;
	private String type;
	private String entityParent;

	public Link() {
		super();
	}

	public Link(String url) {
		super();
		this.url = url;
	}

	public Link(String url, String entityParent) {
		super();
		this.url = url;
		this.entityParent = entityParent;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getEntityParent() {
		return entityParent;
	}

	public void setEntityParent(String entityParent) {
		this.entityParent = entityParent;
	}

}
