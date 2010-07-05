package org.genedb.web.mvc.controller.download;

import org.springframework.expression.ParserContext;

class TemplatedParserContext implements ParserContext {

	@Override
	public boolean isTemplate() {
		return true;
	}

	@Override
	public String getExpressionSuffix() {
		return "}";
	}

	@Override
	public String getExpressionPrefix() {
		return "${";
	}
}