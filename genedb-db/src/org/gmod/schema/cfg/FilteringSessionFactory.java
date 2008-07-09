package org.gmod.schema.cfg;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * This class wraps a Hibernate SessionFactory object, and delegates to it.
 * When a new session is created, the specified filters will be enabled for
 * that session. This makes it possible to have filters that are enabled by
 * default. Currently only unparameterised filters are supported: there is no
 * way to set filter parameters.
 * <p>
 * Although this class could be used on its own, it's currently used only
 * by the Spring class {@link ChadoSessionFactoryBean}, which wraps the
 * session factory it returns in a <code>FilteringSessionFactory</code>,
 * configured with whatever default filters were defined in the configuration.
 *
 * @author rh11
 *
 */
public class FilteringSessionFactory implements SessionFactory {
    private static final Logger logger = Logger.getLogger(FilteringSessionFactory.class);

    private SessionFactory underlyingSessionFactory;
    private Set<String> filters = new HashSet<String>();

    /**
     * Create a new <code>FilteringSessionFactory</code> that wraps
     * the supplied underlying session factory.
     * @param underlyingSessionFactory the session factory to wrap
     */
    public FilteringSessionFactory(SessionFactory underlyingSessionFactory) {
        this.underlyingSessionFactory = underlyingSessionFactory;
    }
    /**
     * Add a filter to the list of filters that are enabled by default.
     * @param filterName the name of the filter to add
     */
    public void addFilter(String filterName) {
        filters.add(filterName);
    }

    private Session enableFilters(Session session) {
        for (String filterName: filters) {
            logger.debug(String.format("Enabling filter '%s' on session", filterName));
            session.enableFilter(filterName);
        }
        return session;
    }

    /* openSession methods */
    public Session openSession() throws HibernateException {
        return enableFilters(underlyingSessionFactory.openSession());
    }
    public Session openSession(Connection connection, Interceptor interceptor) {
        return enableFilters(underlyingSessionFactory.openSession(connection, interceptor));
    }
    public Session openSession(Connection connection) {
        return enableFilters(underlyingSessionFactory.openSession(connection));
    }
    public Session openSession(Interceptor interceptor) throws HibernateException {
        return enableFilters(underlyingSessionFactory.openSession(interceptor));
    }

    /* Delegated methods */
    public void close() throws HibernateException {
        underlyingSessionFactory.close();
    }
    @SuppressWarnings("unchecked")
    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        underlyingSessionFactory.evict(persistentClass, id);
    }
    @SuppressWarnings("unchecked")
    public void evict(Class persistentClass) throws HibernateException {
        underlyingSessionFactory.evict(persistentClass);
    }
    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        underlyingSessionFactory.evictCollection(roleName, id);
    }
    public void evictCollection(String roleName) throws HibernateException {
        underlyingSessionFactory.evictCollection(roleName);
    }
    public void evictEntity(String entityName, Serializable id) throws HibernateException {
        underlyingSessionFactory.evictEntity(entityName, id);
    }
    public void evictEntity(String entityName) throws HibernateException {
        underlyingSessionFactory.evictEntity(entityName);
    }
    public void evictQueries() throws HibernateException {
        underlyingSessionFactory.evictQueries();
    }
    public void evictQueries(String cacheRegion) throws HibernateException {
        underlyingSessionFactory.evictQueries(cacheRegion);
    }
    @SuppressWarnings("unchecked")
    public Map getAllClassMetadata() throws HibernateException {
        return underlyingSessionFactory.getAllClassMetadata();
    }
    @SuppressWarnings("unchecked")
    public Map getAllCollectionMetadata() throws HibernateException {
        return underlyingSessionFactory.getAllCollectionMetadata();
    }
    @SuppressWarnings("unchecked")
    public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
        return underlyingSessionFactory.getClassMetadata(persistentClass);
    }
    public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
        return underlyingSessionFactory.getClassMetadata(entityName);
    }
    public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
        return underlyingSessionFactory.getCollectionMetadata(roleName);
    }
    public Session getCurrentSession() throws HibernateException {
        return underlyingSessionFactory.getCurrentSession();
    }
    @SuppressWarnings("unchecked")
    public Set getDefinedFilterNames() {
        return underlyingSessionFactory.getDefinedFilterNames();
    }
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return underlyingSessionFactory.getFilterDefinition(filterName);
    }
    public Reference getReference() throws NamingException {
        return underlyingSessionFactory.getReference();
    }
    public Statistics getStatistics() {
        return underlyingSessionFactory.getStatistics();
    }
    public boolean isClosed() {
        return underlyingSessionFactory.isClosed();
    }
    public StatelessSession openStatelessSession() {
        return underlyingSessionFactory.openStatelessSession();
    }
    public StatelessSession openStatelessSession(Connection connection) {
        return underlyingSessionFactory.openStatelessSession(connection);
    }

}
