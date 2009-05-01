package org.gmod.schema.cfg;

import org.hibernate.cfg.Environment;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.ConnectionProviderFactory;
import org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider;

import java.util.HashMap;
import java.util.Properties;

import javax.sql.DataSource;

/**
 * This SettingsFactory class is used by {@link ChadoAnnotationConfiguration}.
 * It makes it simpler to use the ChadoAnnotationConfiguration directly
 * from Hibernate (i.e. without Spring), by making it possible to configure
 * the Hibernate database connection to use the DataSource that
 * ChadoAnnotationConfiguration requires in any case.
 *
 * To make use of this facility, simply set the
 * <code>hibernate.connection.provider_class</code> configuration property
 * to <code>org.hibernate.ejb.connection.InjectedDataSourceConnectionProvider</code>.
 *
 * @author rh11
 *
 */
public class ChadoAnnotationSettingsFactory extends SettingsFactory {

    private DataSource dataSource;

    void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected ConnectionProvider createConnectionProvider(Properties properties) {
        String connectionProviderClass = properties.getProperty(Environment.CONNECTION_PROVIDER);
        if (InjectedDataSourceConnectionProvider.class.getName().equals(connectionProviderClass)) {
            return ConnectionProviderFactory.newConnectionProvider( properties,
                new HashMap<String,Object>() {
                {
                    put ("dataSource", dataSource);
                }
            });
        }

        return super.createConnectionProvider(properties);
    }

}
