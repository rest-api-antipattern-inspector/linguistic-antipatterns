package com.sofa.util;

import java.util.HashMap;

/**
 * Specializes a HashMap to store values with a lower cased key
 */
public class NoCaseMap extends HashMap<String, String> {
	private static final long serialVersionUID = 3587281231690906089L;

	@Override
	public String put(String key, String value) {
		return super.put(key.toLowerCase(), value);
	}
	
	public String get(String key) {
       return super.get(key.toLowerCase());
    }
}
