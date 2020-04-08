package com.sofa.metric.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "headers")
@XmlAccessorType(XmlAccessType.FIELD)
public class Headers {

	@XmlElement(name = "header")
	private List<Header> headers = null;

	public Headers() {

	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> sc) {
		this.headers = sc;
	}

	public boolean contains(String name) {
		if (headers != null) {
			for (Header header : headers)
				if (header.getName().equals(name))
					return true;
		}
		return false;
	}

}
