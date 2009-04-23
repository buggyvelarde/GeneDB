package org.genedb.web.mvc.model;

import org.gmod.schema.cfg.ChadoSessionFactoryBean;
import org.hibernate.SessionFactory;

import java.util.Properties;

import javax.sql.DataSource;

public class ConfigurableGeneDBSessionFactoryBean {

    Properties properties;

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    String packagesToScan[];

    public void setPackagesToScan(String[] packagesToScan) {
        this.packagesToScan = packagesToScan;
    }


    public SessionFactory createFullTextSessionFactory(String indexDir, int batchSize) throws Exception {
        ChadoSessionFactoryBean csfb = new ChadoSessionFactoryBean();
        csfb.setDataSource(dataSource);
        csfb.setPackagesToScan(packagesToScan);

        Properties customised = new Properties(properties);
        customised.put("hibernate.search.worker.batch_size", batchSize);
        customised.put("hibernate.search.default.indexBase", indexDir);
        csfb.setProperties(customised);

        csfb.afterPropertiesSet();

        return (SessionFactory) csfb.getObject();
    }


//            <property name="listeners">
//                <map>
//                    <entry key="post-insert" value-ref="fullTextIndexEventListener" />
//                    <entry key="post-update" value-ref="fullTextIndexEventListener" />
//                    <entry key="post-delete" value-ref="fullTextIndexEventListener" />
//                </map>
//            <property>
}

