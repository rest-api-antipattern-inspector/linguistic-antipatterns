package com.sofa.metric.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.sofa.helper.Tools;

@XmlRootElement(name = "statuscode")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusCode {

	private int code;
	@XmlElement(name = "description")
	private String description;
	@XmlElement(name = "method")
	private List<String> methods;

	public StatusCode() {

	}

	public StatusCode(int code, String description, List<String> methods) {
		super();
		this.code = code;
		this.description = description;
		this.methods = methods;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getMethods() {
		return methods;
	}

	public void setMethods(List<String> methods) {
		this.methods = methods;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StatusCode) {
			StatusCode sc = (StatusCode)obj;
			
			/*if(Tools.lastDigits(this.code) == Tools.lastDigits(sc.code)) {
				System.out.println();
			}*/
			
			if((this.code == sc.code || Tools.firstDigit(this.code) == Tools.firstDigit(sc.code))
					&& (sc.description == null || this.description.equals(sc.description))
					&& this.methods.containsAll(sc.methods)) {
				return true;
			}
		}
		return false;
	}

}
