package org.genedb.db.loading;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

    /**
     * Allows for configuration of individual bean property values from a property resource,
     * i.e. a properties file. Useful for custom config files targetted at system
     * administrators that override bean properties configured in the application context.
     *
     * <p>2 concrete implementations are provided in the distribution:
     * <ul>
     * <li>PropertyOverrideConfigurer for "beanName.property=value" style overriding
     * (<i>pushing</i> values from a properties file into bean definitions)
     * <li>PropertyPlaceholderConfigurer for replacing "${...}" placeholders
     * (<i>pulling</i> values from a properties file into bean definitions)
     * </ul>
     *
     * <p>Property values can be converted after reading them in, through overriding
     * the <code>convertPropertyValue</code> method. For example, encrypted values
     * can be detected and decrypted accordingly before processing them.
     *
     * @author Juergen Hoeller/Adrian Tivey
     * @since 2006/05/20
     * @see PropertyOverrideConfigurer
     * @see PropertyPlaceholderConfigurer
     * @see #convertPropertyValue
     */

    public class PropertyOverrideConfigurerByCoding implements BeanFactoryPostProcessor, Ordered {

	protected final Log logger = LogFactory.getLog(getClass());
	
    	private int order = Integer.MAX_VALUE;  // default: same as non-Ordered
    	private static final String DEFAULT_HOLDING_CLASS_NAME = "org.genedb.db.loading.PropertyOverrideHolder";
    	
    	private String holdingClassName = DEFAULT_HOLDING_CLASS_NAME;
    	
    	private String key;

    	
    	@Required
    	public void setKey(String key) {
	    this.key = key;
	}

	public void setOrder(int order) {
    	  this.order = order;
    	}

    	public int getOrder() {
    	  return order;
    	}


    	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    	    Properties mergedProps = retrieveProperties();

    	    // Convert the merged properties, if necessary.
    	    convertProperties(mergedProps);
    	    
    	    // Let the subclass process the properties.
    	    processProperties(beanFactory, mergedProps);

    	}
    	
    	

    	private Properties retrieveProperties() {
	    Properties properties = null;
	    try {
		Class holdingClass = ClassUtils.forName(holdingClassName);
		Method m = ClassUtils.getStaticMethod(holdingClass, "getProperties", new Class[]{String.class});
		properties = (Properties) m.invoke(null, new Object[]{key});
	    }
	    catch (IllegalArgumentException exp) {
		// TODO Auto-generated catch block
		exp.printStackTrace();
	    }
	    catch (IllegalAccessException exp) {
		// TODO Auto-generated catch block
		exp.printStackTrace();
	    }
	    catch (InvocationTargetException exp) {
		// TODO Auto-generated catch block
		exp.printStackTrace();
	    }
	    catch (ClassNotFoundException exp) {
		// TODO Auto-generated catch block
		exp.printStackTrace();
	    }
	    return properties;
	}

	/**
    	 * Convert the given merged properties, converting property values
    	 * if necessary. The result will then be processed.
    	 * <p>Default implementation will invoke <code>convertPropertyValue</code>
    	 * for each property value, replacing the original with the converted value.
    	 * @see #convertPropertyValue
    	 * @see #processProperties
    	 */
    	protected void convertProperties(Properties props) {
    		Enumeration propertyNames = props.propertyNames();
    		while (propertyNames.hasMoreElements()) {
    			String propertyName = (String) propertyNames.nextElement();
    			String propertyValue = props.getProperty(propertyName);
    			String convertedValue = convertPropertyValue(propertyValue);
    			if (!ObjectUtils.nullSafeEquals(propertyValue, convertedValue)) {
    				props.setProperty(propertyName, convertedValue);
    			}
    		}
    	}

    	/**
    	 * Convert the given property value from the properties source
    	 * to the value that should be applied.
    	 * <p>Default implementation simply returns the original value.
    	 * Can be overridden in subclasses, for example to detect
    	 * encrypted values and decrypt them accordingly.
    	 * @param originalValue the original value from the properties source
    	 * (properties file or local "properties")
    	 * @return the converted value, to be used for processing
    	 * @see #setProperties
    	 * @see #setLocations
    	 * @see #setLocation
    	 */
    	protected String convertPropertyValue(String originalValue) {
    		return originalValue;
    	}



    /**
     * A property resource configurer that overrides bean property values in an application
     * context definition. It <i>pushes</i> values from a properties file into bean definitions.
     *
     * <p>Configuration lines are expected to be of the following form:
     *
     * <pre>
     * beanName.property=value</pre>
     *
     * Example properties file:
     *
     * <pre>
     * dataSource.driverClassName=com.mysql.jdbc.Driver
     * dataSource.url=jdbc:mysql:mydb</pre>
     *
     * In contrast to PropertyPlaceholderConfigurer, the original definition can have default
     * values or no values at all for such bean properties. If an overriding properties file does
     * not have an entry for a certain bean property, the default context definition is used.
     *
     * <p>Note that the context definition <i>is not</i> aware of being overridden;
     * so this is not immediately obvious when looking at the XML definition file.
     *
     * <p>In case of multiple PropertyOverrideConfigurers that define different values for
     * the same bean property, the <i>last</i> one will win (due to the overriding mechanism).
     *
     * <p>Property values can be converted after reading them in, through overriding
     * the <code>convertPropertyValue</code> method. For example, encrypted values
     * can be detected and decrypted accordingly before processing them.
     *
     * @author Juergen Hoeller
     * @author Rod Johnson
     * @since 12.03.2003
     * @see #convertPropertyValue
     * @see PropertyPlaceholderConfigurer
     */

    	public static final String DEFAULT_BEAN_NAME_SEPARATOR = ".";


    	private String beanNameSeparator = DEFAULT_BEAN_NAME_SEPARATOR;

    	private boolean ignoreInvalidKeys = false;

    	/** Contains names of beans that have overrides */
    	private Set<String> beanNames = Collections.synchronizedSet(new HashSet<String>());


    	/**
    	 * Set the separator to expect between bean name and property path.
    	 * Default is a dot (".").
    	 */
    	public void setBeanNameSeparator(String beanNameSeparator) {
    		this.beanNameSeparator = beanNameSeparator;
    	}

    	/**
    	 * Set whether to ignore invalid keys. Default is "false".
    	 * <p>If you ignore invalid keys, keys that do not follow the
    	 * 'beanName.property' format will just be logged as warning.
    	 * This allows to have arbitrary other keys in a properties file.
    	 */
    	public void setIgnoreInvalidKeys(boolean ignoreInvalidKeys) {
    		this.ignoreInvalidKeys = ignoreInvalidKeys;
    	}


    	protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props)
    			throws BeansException {

    		for (Enumeration names = props.propertyNames(); names.hasMoreElements();) {
    			String localKey = (String) names.nextElement();
    			try {
    				processKey(beanFactory, localKey, props.getProperty(localKey));
    			}
    			catch (BeansException ex) {
    				String msg = "Could not process key '" + localKey + "' in PropertyOverrideConfigurer";
    				if (!this.ignoreInvalidKeys) {
    					throw new BeanInitializationException(msg, ex);
    				}
    				if (logger.isDebugEnabled()) {
    					logger.debug(msg, ex);
    				}
    			}
    		}
    	}

    	/**
    	 * Process the given key as 'beanName.property' entry.
    	 */
    	protected void processKey(ConfigurableListableBeanFactory factory, String localKey, String value)
    			throws BeansException {

    		int separatorIndex = localKey.indexOf(this.beanNameSeparator);
    		if (separatorIndex == -1) {
    			throw new BeanInitializationException("Invalid key '" + localKey +
    					"': expected 'beanName" + this.beanNameSeparator + "property'");
    		}
    		String beanName = localKey.substring(0, separatorIndex);
    		String beanProperty = localKey.substring(separatorIndex+1);
    		this.beanNames.add(beanName);
    		applyPropertyValue(factory, beanName, beanProperty, value);
    		if (logger.isDebugEnabled()) {
    			logger.debug("Property '" + localKey + "' set to value [" + value + "]");
    		}
    	}

    	/**
    	 * Apply the given property value to the corresponding bean.
    	 */
    	protected void applyPropertyValue(
    	    ConfigurableListableBeanFactory factory, String beanName, String property, String value) {

    		BeanDefinition bd = factory.getBeanDefinition(beanName);
    		bd.getPropertyValues().addPropertyValue(property, value);
    	}


    	/**
    	 * Were there overrides for this bean?
    	 * Only valid after processing has occurred at least once.
    	 * @param beanName name of the bean to query status for
    	 * @return whether there were property overrides for
    	 * the named bean
    	 */
    	public boolean hasPropertyOverridesFor(String beanName) {
    		return this.beanNames.contains(beanName);
    	}

	public void setHoldingClassName(String holdingClassName) {
	    this.holdingClassName = holdingClassName;
	}

    }

