package org.gmod.schema.cfg;

import org.gmod.schema.feature.Transcript;

import org.hibernate.cfg.Configuration;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;

public class OrganismHeirachy implements ApplicationContextAware {

    private String beanName;

    private ApplicationContext applicationContext;

    private Collection<Integer> ids;

    public void afterPropertiesSet() {
        ChadoSessionFactoryBean csfb = applicationContext.getBean('&' +beanName, ChadoSessionFactoryBean.class);
        Configuration cfg = csfb.getConfiguration();

        if (!(cfg instanceof ChadoAnnotationConfiguration)) {
            throw new IllegalArgumentException(
                    String.format("Configuration is '%s', not an instance of ChadoAnnotationConfiguration",
                            cfg.getClass()));
        }
        ChadoAnnotationConfiguration cac = (ChadoAnnotationConfiguration) cfg;
        ids = cac.getTypeIdsByClass(Transcript.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Collection<Integer> getIds() {
        return ids;
    }



}
