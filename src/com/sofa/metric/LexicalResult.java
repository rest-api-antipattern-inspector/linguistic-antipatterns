package com.sofa.metric;

import java.lang.reflect.Method;


import com.sofa.util.Response;


public class LexicalResult implements Comparable<LexicalResult> {
	private Response response;
	private String informations;
	
	/**
	 * The reliability indicator is used to judge the reliability of the detection
	 * the value must be between 0 and 1
	 * @author Charlie Faucheux
	 */
	public static class Reliability {
		
		public static final double PERFECT = 1;
		public static final double REPROCESSED_READING = 0.25;
		public static final double AMBIGUOUS_DETECTION = 0.5;
		
		public static final Reliability defaultReliability = new Reliability();
		
		private double readingReliability;
		public void multiplyReadingReliability(double reliability) {
			if (reliability < 0) reliability = 0;
			readingReliability *= reliability;
		}
		public double getReadingReliability() {
			return readingReliability;
		}
		private double detectionReliability;
		public void multiplyDetectionReliability(double reliability) {
			if (reliability < 0) reliability = 0;
			detectionReliability *= reliability;
		}
		public double getDetectionReliability() {
			return detectionReliability;
		}
		
		/**
		 * Default values are 100% of reliability
		 */
		public Reliability() {
			readingReliability = 1;
			detectionReliability = 1;
		}
		
		/**
		 * The score is the reading reliability * detection reliability
		 * @return
		 */
		public double getValue() {
			return readingReliability * detectionReliability;
		}
	}
	private Reliability reliability;
	
	/**
	 * Creates a lexical result from a Response
	 * At a reliability of 1
	 * @param response
	 */
	public LexicalResult(Response response) {
		super();
		this.setResponse(response);
		// default reliability
		reliability = new Reliability();
	}
	
	/**
	 * Creates a lexical result from a Response with informations
	 * At a reliability of 1
	 * @param response
	 * @param informations
	 */
	public LexicalResult(Response response, String informations) {
		super();
		this.setResponse(response);
		this.setInformations(informations);
		// default reliability
		reliability = new Reliability();
	}
	
	/**
	 * Creates a lexical result from a Response with informations
	 * With the detection level
	 * @param response
	 * @param info
	 * @param level
	 */
	public LexicalResult(Response response, String info, Reliability reliability) {
		super();
		this.setResponse(response);
		this.setInformations(info);
		this.reliability = reliability;
	}
	
	/**
	 * @return the reliability of the detection
	 */
	public double getReliability() {
		return reliability.getValue();
	}

	public String getName() {
		return response.getRequest().getMethod().getDeclaringClass().getCanonicalName();
	}

	public Method getMethod() {
		return response.getRequest().getMethod();
	}
	
	public String getPath() {
		//if (response.getRequest().getMethod().isAnnotationPresent(Path.class)) {
		//	return response.getRequest().getMethod().getAnnotation(Path.class).value();
		//}
		return "/";
	}
	
	@Override
	public int compareTo(LexicalResult s) {
		return this.response.equals(s.getResponse()) ? 0 : -1;
		/*if (this.name.compareTo(s.name) == 0
				&& this.resource.compareTo(s.resource) == 0
				&& this.httpMethod.compareTo(s.httpMethod) == 0)
			return 0;
		else
			return (this.resource.compareTo(s.resource));*/
	}

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}

	public String getInformations() {
		return informations;
	}

	public void setInformations(String informations) {
		this.informations = informations;
	}
	
}
