package com.sofa.metric.lexical.parser;

public class URIParserStateProcessPVal extends URIParserState {

	@Override
	public State processCharacter(char c, URIParserCore core) {
		
		if (characterIsParameterAnd(c)) {
			core.newParameter();
			return State.PROCESS_PTAG;
		} else {
			core.updatePVal(c);
			return State.UNCHANGED;
		}
	}
}