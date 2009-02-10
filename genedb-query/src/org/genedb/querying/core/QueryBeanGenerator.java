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

package org.genedb.querying.core;

import net.sf.cglib.beans.BeanGenerator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryBeanGenerator implements BeanFactoryPostProcessor,
        ApplicationContextAware {

    private ApplicationContext applicationContext;


    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory)
            throws BeansException {

        List<String> templateNames = new ArrayList<String>();

        Map<String, QueryTemplate> map = factory.getBeansOfType(QueryTemplate.class);

        for (Map.Entry<String, QueryTemplate> entry : map.entrySet()) {
            String beanName = entry.getKey();
            templateNames.add(beanName);
            QueryTemplate template = entry.getValue();

            BeanGenerator bg = new BeanGenerator();
            bg.setSuperclass(template.getBaseClass());
            List<String> paramNames = new ArrayList<String>();
            // Extend with properties
            for (Parameter parameter : template.getParams()) {
                bg.addProperty(parameter.name, parameter.getClass());
                paramNames.add(parameter.name);
            }
            Query bean = (Query) bg.create();
            Class<?> newClass = bean.getClass();

            BeanDefinitionBuilder bdb = BeanDefinitionBuilder.rootBeanDefinition(newClass);
            bdb.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            bdb.addPropertyValue("paramNames", paramNames.toArray(new String[]{}));
            template.processNewPrototype(bdb);

            // Store back as prototype bean
            BeanDefinition bd = bdb.getBeanDefinition();
            ((GenericApplicationContext)applicationContext).registerBeanDefinition(beanName, bd);

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
