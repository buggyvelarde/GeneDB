package org.gmod.schema.utils;

import org.apache.log4j.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CvTermUtils {
    private static final Logger logger = Logger.getLogger(CvTermUtils.class);

    public static void checkCvTermPath(Connection conn) throws SQLException {
        PreparedStatement st = conn.prepareStatement(
            " select cv.name, count(cvtermpath.*)"
            +" from cv left join cvtermpath on cv.cv_id = cvtermpath.cv_id"
            +" where cv.name in ("
            +"       'biological_process'"
            +"     , 'molecular_function'"
            +"     , 'cellular_component'"
            +"     , 'sequence'"
            +" )"
            +" group by cv.name"
        );
        try {
            boolean addedToCvTermPath = false;
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String cvName = rs.getString(1);
                int count = rs.getInt(2);
                logger.debug(String.format("There are %d cvtermpath entries for '%s'", count, cvName));

                if (count == 0) {
                    populateCvTermPath(conn, cvName);
                    addedToCvTermPath = true;
                }
            }
            if (addedToCvTermPath)
                analyzeCvTermPath(conn);
        }
        finally {
            try {st.close(); conn.commit();} catch (SQLException e) {logger.error(e);}
        }
    }

    private static void populateCvTermPath(Connection conn, String cvName) throws SQLException {
        CallableStatement st = conn.prepareCall("{call fill_cvtermpath(?)}");
        try {
            st.setString(1, cvName);
            logger.info(String.format("Populating cvtermpath for cv '%s'", cvName));
            st.execute();
        }
        finally {
            try {st.close();} catch (SQLException e) {logger.error(e);}
        }
    }

    private static void analyzeCvTermPath(Connection conn) throws SQLException {
        logger.info("Analyzing cvtermpath table");
        Statement st = conn.createStatement();
        try {
            st.execute("analyze cvtermpath");
        }
        finally {
            try { st.close(); } catch (SQLException e) {logger.error(e);}
        }
    }

}
