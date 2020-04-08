package com.sofa.util;

import java.util.HashMap;

/**
 * This map is used to count the number of put key
 */
public class UniqueMap extends HashMap<String, Integer> {
	private static final long serialVersionUID = 3587281231690906089L;

	@Override
	public Integer put(String key, Integer value) {
		if(containsKey(key.toLowerCase())) {
			value = get(key.toLowerCase()) + value;
		}
		return super.put(key.toLowerCase(), value);
	}
	
	public Integer put(String key) {
		return put(key, 1);
	}
	
	public Integer get(String key) {
       return super.get(key.toLowerCase());
    }
}

