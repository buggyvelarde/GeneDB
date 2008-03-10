package org.genedb.db.loading;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class SqlUtils {
    
    public void executeSql(Reader r, DataSource ds, boolean includeDrop) throws IOException, SQLException {
        BufferedReader in = new BufferedReader(r);
        StringBuffer cmd = new StringBuffer();
        String line;
        while ((line=in.readLine())!=null) {
            cmd.append(line);
            if (cmd.toString().endsWith(";")) {
                String command = cmd.toString().substring(0, cmd.length()-1);
                cmd = new StringBuffer();
                //System.err.println("[EXEC] "+command);
                if (!includeDrop && command.startsWith("drop")) {
                    continue;
                }
                Statement st = ds.getConnection().createStatement();
                st.execute(command);
            }
        }
    }
    
}
