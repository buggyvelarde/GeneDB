package org.genedb.util;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

public class GeneDBFormattingConversionServiceFactoryBean extends
		FormattingConversionServiceFactoryBean {

	private Map<Class<?>, Formatter<?>> formatters;
	private static final Logger logger = Logger.getLogger(GeneDBFormattingConversionServiceFactoryBean.class);

	@Override
	protected void installFormatters(FormatterRegistry registry) {
		super.installFormatters(registry);
		logger.info("*** Registering ");
		for (Map.Entry<Class<?>, Formatter<?>> entry : formatters.entrySet()) {
			logger.info("*** Registering '"+entry.getKey()+"', '"+entry.getValue()+"'");
			registry.addFormatterForFieldType(entry.getKey(), entry.getValue());
		}
	}

	public void setFormatters(Map<Class<?>, Formatter<?>> formatters) {
		this.formatters = formatters;
	}



}
