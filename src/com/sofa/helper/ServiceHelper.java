package com.sofa.helper;

import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import com.sofa.metric.util.Form;


public class ServiceHelper {

	public static String getShortServiceName(Object service) {
		return service.toString().substring(0, service.toString().indexOf("Itf@") - 4);
	}
	
	public static Form checkForm(String resource) {
		if ((resource.startsWith("{")&& resource.endsWith("}"))||(resource.startsWith("[")||resource.endsWith("]"))) {
			return Form.json;
		}
		else if (resource.startsWith("<?xml version=\"1.0\"")) {
			if(resource.contains("<rdf:RDF"))
				return Form.rdf;
			return Form.xml;
		}
		else if(resource.contains("<html>")) {
			return Form.html;
		}
		return Form.unknow;

	}
	
	public static HTTPMethod getHTTPMethod(Method method) {
		if(method.isAnnotationPresent(GET.class))
			return HTTPMethod.GET;
		else if(method.isAnnotationPresent(POST.class))
			return HTTPMethod.POST;
		else if(method.isAnnotationPresent(PUT.class))
			return HTTPMethod.PUT;
		else if(method.isAnnotationPresent(DELETE.class))
			return HTTPMethod.DELETE;
		return null;
	}
	
	@XmlType(name = "method")
	@XmlEnum
	public enum HTTPMethod {
		@XmlEnumValue("GET")
		GET ("GET"),
		@XmlEnumValue("POST")
		POST ("POST"),
		@XmlEnumValue("PUT")
		PUT ("PUT"),
		@XmlEnumValue("DELETE")
		DELETE ("DELETE");

		@XmlElement(name = "m")
		private final String name;       

		private HTTPMethod(String s) {
			name = s;
		}

		public boolean equalsName(String otherName){
			return (otherName == null) ? false : name.equals(otherName);
		}
		
		public String toString(){
			return name;
		}
		
		public static HTTPMethod parseObst( String str ){
		       return valueOf( HTTPMethod.class, str );
		   }

		   public static String printObst( HTTPMethod f ){
		       return f.toString();
		   }
	}
	
	
}
