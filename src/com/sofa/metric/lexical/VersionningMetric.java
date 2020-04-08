package com.sofa.metric.lexical;

public class VersionningMetric {
	
	// regex
	private static String api = "(api)";
	private static String version = "((v)|(ver)|(version))";
	private static String separator = "(.|;)";
	private static String number = "([0-9]{1,3})";
	
	// filter state
	public static enum State {
		STAND_BY, API_NODE
	}
	
	/**
	 * Looks for a version into the URI
	 * Return either the version string found or an empty string
	 * @param uri
	 * @return
	 */
	public static String filterVersion(String uri) {
		
		State state = State.STAND_BY;
		String buffer = new String();
		
		for (String node : uri.split("/")) {

			switch(state) {
			
			/** STAND BY ==================================================== */
			case STAND_BY:
				
				if (node.matches(api + "|" + version)) { // node is api or version
					state = State.API_NODE;
					buffer = node;
					
				} else if (node.matches(version + "{0,1}" + separator + "{0,1}" + number)) { // node is version
					return node;
				}
				
				break;
				
			/** API NODE ==================================================== */	
			case API_NODE:
				if (node.matches(version + "{0,1}" + separator + "{0,1}" + number)) { // node is version
					return buffer + "/" + node;
				}
				break;
			}
		}
		
		return new String();
	}
}
