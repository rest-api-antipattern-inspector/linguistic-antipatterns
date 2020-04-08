package com.sofa.metric.util;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "statuscodes")
@XmlAccessorType (XmlAccessType.FIELD)
public class StatusCodes {

	@XmlElement(name = "statuscode")
    private List<StatusCode> statuscodes = null;

	public List<StatusCode> getStatusCodes() {
		return statuscodes;
	}

	public void setStatusCodes(List<StatusCode> sc) {
		this.statuscodes = sc;
	}

	public boolean contains(StatusCode concat) {
		if(statuscodes != null) {
			for (StatusCode sc : statuscodes)
				if(sc.equals(concat))
					return true;
		}
		return false;
	}
	
	

	
}
