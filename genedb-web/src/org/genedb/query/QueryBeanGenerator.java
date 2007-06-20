/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.query;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueryBeanGenerator implements BeanFactoryPostProcessor,
        ApplicationContextAware {
    
    private ApplicationContext applicationContext;

    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
            throws BeansException {
        
        List<String> templateNames = new ArrayList<String>();
        
        Map<String, QueryTemplate> map = (Map<String, QueryTemplate>) factory.getBeansOfType(QueryTemplate.class);
        //Collection<QueryTemplate> templates = map.values().getClass().cast(Collection<QueryTemplate>.class);
        for (Map.Entry<String, QueryTemplate> entry : map.entrySet()) {
            templateNames.add(entry.getKey());
            QueryTemplate template = entry.getValue();
            Class baseClass = template.getBaseClass();
            // Extend with properties
            for (Parameter parameter : template.getParams()) {
                
            }
            
            template.processNewPrototype(clone());
            BeanDefinition bd;
            // Store back as prototype bean
            
        }
        for (String name : templateNames) {
            factory.destroyScopedBean(name);
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.applicationContext = applicationContext;
    }

}
