package org.genedb.db.adhoc;

import org.apache.log4j.PropertyConfigurator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

/**
 * Issue an ad hoc HQL query against the database.
 *
 * @author rh11
 *
 */
public class Query {

    private static void initLog4J(boolean verbose) {
        URL url = Query.class.getResource(
            verbose ? "/log4j.query-verbose.properties" : "/log4j.query.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    public static void main(String[] args) throws IOException {
        boolean commit = false;
        boolean verbose = false;
        for (String arg: args) {
            if (arg.equals("--commit")) {
                commit = true;
            } else if (arg.equals("--verbose")) {
                verbose = true;
            } else {
                throw new IllegalArgumentException("Unrecognised argument: " + arg);
            }
        }

        initLog4J(verbose);

        Reader stdin = new InputStreamReader(System.in);
        char[] buf = new char[4096];
        StringBuilder queries = new StringBuilder();
        int n;
        while (-1 < (n = stdin.read(buf))) {
            queries.append(buf, 0, n);
        }

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("Adhoc.xml");
        Query q = applicationContext.getBean("adhoc-query", Query.class);
        q.rollback = !commit;

        try {
            q.execute(queries.toString());
        } catch (ForceRollback e) {
            System.out.println("ROLLBACK");
            return;
        }
        System.out.println("COMMIT");
    }

    @Resource(name="sessionFactory")
    private SessionFactory sessionFactory;

    private boolean rollback = true;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional(rollbackFor=ForceRollback.class)
    public void execute(String queries) throws ForceRollback {
        for (String query: queries.split(";\\s+")) {
            executeSingleQuery(query.trim());
        }
        if (rollback) {
            throw new ForceRollback();
        }
    }

    private void executeSingleQuery(String query) {
        if (query == null) {
            throw new NullPointerException("Supplied query is null");
        }
        if (query.length() == 0) {
            return;
        }

        Session session = SessionFactoryUtils.getSession(sessionFactory, false);
        org.hibernate.Query q = session.createQuery(query);

        if (query.matches("(?is)delete\\b.*")) {
            executeDelete(q);
        } else {
            executeSelect(q);
        }
    }

    private void executeSelect(org.hibernate.Query q) {
        q.setMaxResults(50);
        List<?> results = q.list();
        for (Object o: results) {
            String stringValue = stringValueOf(o);

            System.out.printf("  %s%n", stringValue);
        }
        System.out.printf("-- %n%d results%n", results.size());
    }

    private void executeDelete(org.hibernate.Query q) {
        int numDeleted = q.executeUpdate();
        System.out.printf("-- %d rows deleted%n", numDeleted);
    }

    /**
     * Get the string value of an object, stringifying it nicely
     * even if it's an array.
     *
     * @param o the object
     * @return the string value
     */
    private String stringValueOf(Object o) {
        String stringValue = o.toString();
        if (o instanceof Object[]) {
            stringValue = Arrays.toString((Object[]) o);
        } else if (o instanceof int[]) {
            stringValue = Arrays.toString((int[]) o);
        } else if (o instanceof long[]) {
            stringValue = Arrays.toString((long[]) o);
        } else if (o instanceof short[]) {
            stringValue = Arrays.toString((short[]) o);
        } else if (o instanceof byte[]) {
            stringValue = Arrays.toString((byte[]) o);
        } else if (o instanceof char[]) {
            stringValue = Arrays.toString((char[]) o);
        }
        return stringValue;
    }
}

class ForceRollback extends Exception { }
