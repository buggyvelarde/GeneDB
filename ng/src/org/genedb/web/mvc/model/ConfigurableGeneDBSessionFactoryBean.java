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
        customised.put("hibernate.search.worker.batch_size", Integer.toString(batchSize));
        customised.put("hibernate.jdbc.batch_size", Integer.toString(batchSize));
        customised.put("hibernate.search.default.indexBase", indexDir);
        csfb.setProperties(customised);

        csfb.afterPropertiesSet();

        return (SessionFactory) csfb.getObject();
    }

}

