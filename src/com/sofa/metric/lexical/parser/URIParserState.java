package com.sofa.metric.lexical.parser;

import net.didion.jwnl.JWNLException;

public class URIParserState {
	
	

	public enum State {
		// nodes processing
		PROCESS_NODE,
		// parameters processing
		PROCESS_PTAG,
		PROCESS_PVAL,
		// unchanged
		UNCHANGED,
		// error
		ERROR;
	}
	
	/**
	 * State processing method
	 * @param c : the current character
	 * @param core : a reference to the parser core
	 * @return the next state
	 * @throws JWNLException 
	 */
	public State processCharacter(char c, URIParserCore core) throws JWNLException {
		return State.ERROR;
	}
	
	public boolean characterIsNodeSeparator(char c) {
		if (c == '/' || c == '\\') return true;
		return false;
	}
	
	public boolean characterIsUpperCaseLetter(char c) {
		if (c >= 'A' && c <= 'Z') return true;
		return false;
	}
	
	public boolean characterIsParameterMark(char c) {
		if (c == '?') return true;
		return false;
	}
	
	public boolean characterIsParameterEqual(char c) {
		if (c == '=') return true;
		return false;
	}
	
	public boolean characterIsParameterAnd(char c) {
		if (c == '&') return true;
		return false;
	}
	
	public boolean characterIsAlphabetical(char c) {
		if ((c >= 'a' && c <= 'z') ||
			(c >= 'A' && c <= 'Z')) return true;
		return false;
	}
}
