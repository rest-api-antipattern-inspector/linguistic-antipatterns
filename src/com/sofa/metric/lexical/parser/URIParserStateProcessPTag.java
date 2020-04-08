package com.sofa.metric.lexical.parser;

public class URIParserStateProcessPTag extends URIParserState {

	@Override
	public State processCharacter(char c, URIParserCore core) {
		
		if (characterIsParameterEqual(c)) {
			return State.PROCESS_PVAL;
		} else {
			core.updatePTag(c);
			return State.UNCHANGED;
		}
	}
}