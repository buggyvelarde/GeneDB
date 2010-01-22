package org.genedb.util;

import java.util.Map;

import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

public class GeneDBFormattingConversionServiceFactoryBean extends
		FormattingConversionServiceFactoryBean {

	private Map<Class<?>, Formatter<?>> formatters;

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		System.err.println("*** Registering ");
		for (Map.Entry<Class<?>, Formatter<?>> entry : formatters.entrySet()) {
			System.err.println("*** Registering '"+entry.getKey()+"', '"+entry.getValue()+"'");
			registry.addFormatterForFieldType(entry.getKey(), entry.getValue());
		}
	}

	public void setFormatters(Map<Class<?>, Formatter<?>> formatters) {
		this.formatters = formatters;
	}



}
