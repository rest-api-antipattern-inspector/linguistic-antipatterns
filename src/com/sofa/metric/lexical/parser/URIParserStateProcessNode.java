package com.sofa.metric.lexical.parser;

import net.didion.jwnl.JWNLException;

public class URIParserStateProcessNode extends URIParserState {
	
	boolean upperCaseWord = false;
	
	@Override
	public State processCharacter(char c, URIParserCore core) throws JWNLException {
		
		if (characterIsNodeSeparator(c)) {
			// new node
			core.newNode();
			return State.PROCESS_NODE;
		} else if (characterIsParameterMark(c)) {
			// new node
			core.newNode();
			return State.PROCESS_PTAG;
		} else {
			/** Camel case checking */
			if (characterIsUpperCaseLetter(c)) {
				if (core.getCurrentValue().length() > 0 &&
						characterIsUpperCaseLetter(core.getCurrentValueLastChar())) {
					core.updateValue(c);
					upperCaseWord = true; // current processing word is in upper case
					return State.UNCHANGED;
					
				} else {
					// new word
					core.newValue();
					core.updateValue(c);
					return State.UNCHANGED;
				}
			} else {
				// alphabetical character
				if (characterIsAlphabetical(c)) {
					if (upperCaseWord) {
						// end of an upper case word
						char buffer = core.getCurrentValueLastChar();
						// the previous upper case letter was the beginning of a new word
						core.removeCurrentValueLastChar();
						// save the upper case word
						core.newValue();
						// update the new word
						core.updateValue(buffer);
						core.updateValue(c);
						upperCaseWord = false;
						return State.UNCHANGED;
					} else {
						// update current word
						core.updateValue(c);
						return State.UNCHANGED;
					}
				// non alphabetical character
				} else {
					// save the previous word
					if (core.getCurrentValue().length() > 0) {
						core.newValue();
					}
					return State.UNCHANGED;
				}
				
			}
		}
	}
}
