package org.genedb.db.test.tools;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.net.URL;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Copy part of a PostgreSQL Chado database into an HSQLDB file
 * that can be used for testing.
 *
 * @author rh11
 *
 */
public class BuildTestDatabase {
    //private static final String SCHEMA = "public";
    private static final Logger logger = Logger.getLogger(org.genedb.db.test.tools.BuildTestDatabase.class);

    /**
     * Usage: java BuildTestDatabase <source URL> <username> <password> <target name> [<organism ID>]
     * <p>
     * Copies the schema and non-organism-specific data from the supplied database into a specified
     * HSQLDB file. If an organism ID is supplied, the data for that organism are also copied.
     *
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        if (args.length != 4 && args.length != 5) {
            throw new IllegalArgumentException(
                    "Usage: java BuildTestDatabase [--only-schema] <source URL> <username> <password> <target name> [<organism ID>]");
        }

        boolean onlySchema = args[0].equals("--only-schema");
        int argBase = onlySchema ? 1 : 0;

        String sourceUrl = args[argBase + 0];
        String sourceUsername = args[argBase + 1];
        String sourcePassword = args[argBase + 2];
        String targetDatabaseName = args[argBase + 3];

        int organismId = -1;
        if (!onlySchema && args.length > 4) {
            organismId = Integer.parseInt(args[4]);
        }

        URL url = BuildTestDatabase.class.getResource("/log4j.test.properties");
        if (url == null) {
            throw new RuntimeException("Could not find classpath resource /log4j.test.properties");
        }
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);

        Class.forName("org.postgresql.Driver");
        Class.forName("org.hsqldb.jdbcDriver");
        Connection source = DriverManager.getConnection(sourceUrl, sourceUsername, sourcePassword);
        Connection target = DriverManager.getConnection("jdbc:hsqldb:file:test-data/hsqldb/" + targetDatabaseName);
        source.setReadOnly(true);

        BuildTestDatabase buildTestDatabase = new BuildTestDatabase(source, target, organismId);
        buildTestDatabase.copySchema(true); //!onlySchema);

        //Build the Audit schema
        buildTestDatabase.createSchema("audit");
        buildTestDatabase.copySchema("audit", true);

        if (!onlySchema) {
            buildTestDatabase.copyPublicSchemaData();
        }
        target.createStatement().execute("shutdown");
        target.close();
    }

    private Connection source, target;
    private DatabaseMetaData sourceMeta;
    private final String organismIdString;
    private BuildTestDatabase(Connection source, Connection target, int organismId) throws SQLException {
        this.source = source;
        this.target = target;
        this.sourceMeta = source.getMetaData();
        if (organismId >= 0) {
            this.organismIdString = String.valueOf(organismId);
        } else {
            this.organismIdString = null;
        }
    }

    private void copyPublicSchemaData() throws SQLException {
        for (String tableName: "cv cvterm db organism phylotree".split("\\s+")) {
            copyTableData(tableName, null);
        }

        copyTableData("public", "phylonode", null, "phylonode_id"); // We need parent nodes before children
        copyTableData("public", "dbxref", "dbxref_id in (select dbxref_id from cvterm)");
        copyTableData("public", "pub",    "uniquename = 'null'");

        if (organismIdString != null) {
            copyOrganismData();
        }
    }

    private void copyOrganismData() throws SQLException {
        copyTableData("pub", "uniquename <> 'null'");

        copyTableData("dbxref", "dbxref_id in (" +
            " select feature.dbxref_id from feature where organism_id = " + organismIdString
            +"   union"
            +" select feature_dbxref.dbxref_id"
            +" from feature"
            +" join feature_dbxref using (feature_id)"
            +" where feature.organism_id = " + organismIdString
            +"   union"
            +" select pub_dbxref.dbxref_id"
            +" from feature"
            +" join feature_pub using (feature_id)"
            +" join pub_dbxref using (pub_id)"
            +" where feature.organism_id = " + organismIdString
            +")");

        copyTableData("feature", "organism_id = " + organismIdString);
        for (String tableName: "featureloc featureprop feature_cvterm feature_dbxref feature_pub".split("\\s+")) {
            copyTableData(tableName, "feature_id in (" +
                    " select feature_id from feature" +
                    " where organism_id = " + organismIdString + ")");
        }
        copyTableData("feature_cvtermprop", "feature_cvterm_id in ("
            +" select feature_cvterm.feature_cvterm_id"
            +" from feature_cvterm"
            +" join feature using (feature_id)"
            +" where feature.organism_id = " + organismIdString
            +")");

        copyTableData("feature_relationship",
            "subject_id in (select feature_id from feature where organism_id = " + organismIdString + ")"
            +" and object_id in (select feature_id from feature where organism_id = " + organismIdString + ")");
        copyTableData("feature_relationshipprop",
            "feature_relationship_id in ("
            +" select feature_relationship.feature_relationship_id"
            +" from feature_relationship"
            +" join feature s on feature_relationship.subject_id = s.feature_id"
            +" join feature o on feature_relationship.object_id  = o.feature_id"
            +" where s.organism_id = " + organismIdString
            +"   and o.organism_id = " + organismIdString
            +")");

        copyTableData("pub_dbxref", "pub_id in ("
            +" select feature_pub.pub_id"
            +" from feature"
            +" join feature_pub using (feature_id)"
            +" where feature.organism_id = " + organismIdString
            +")");

        copyTableData("analysis", "analysis_id in ("
            +" select analysis.analysis_id "
            +" from analysis"
            +" join analysisfeature on analysis.analysis_id = analysisfeature.analysis_id"
            +" join feature on feature.feature_id = analysisfeature.feature_id"
            +" where organism_id = " + organismIdString
            +")");

        copyTableData("analysisfeature", "feature_id in ("
            +" select feature_id "
            +" from feature"
            +" where organism_id = " + organismIdString
            +")");

    }

    private void copyTableData(String tableName, String condition) throws SQLException {
        copyTableData("public", tableName, condition);
    }

    private void copyTableData(String schema, String tableName, String condition) throws SQLException {
        copyTableData(schema, tableName, condition, null);
    }

    private void copyTableData(String schema, String tableName, String condition,  String order) throws SQLException {
        copyTableData(schema, tableName, condition, order, true);
    }

    private void copyTableData(String schema, String tableName, String condition, String order, boolean batch) throws SQLException {
        logger.info(String.format("Copying contents of table '%s' (%s)", tableName, condition));

        String selectStatement = "select * from \""+tableName+"\"";
        if (condition != null) {
            selectStatement += " where (" + condition + ")";
        }
        if (order != null) {
            selectStatement += " order by " + order;
        }

        Statement sourceSt = source.createStatement();
        ResultSet rs = sourceSt.executeQuery(selectStatement);
        ResultSetMetaData rsMeta = rs.getMetaData();
        int numberOfColumns = rsMeta.getColumnCount();
        String[] columnNames = getColumnNames(rsMeta);

        String insertStatement = insertStatement(schema, tableName, rsMeta);
        logger.trace(String.format("Preparing insert (%d cols): %s", numberOfColumns, insertStatement));
        PreparedStatement targetSt = target.prepareStatement(insertStatement);
        int numberOfRows = 0;
        while (rs.next()) {
            numberOfRows++;
            for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
                String columnName = columnNames[columnIndex - 1];
                Object value = rs.getObject(columnIndex);
                logger.trace(String.format("[%s@%d].%s = '%s'", tableName, numberOfRows, columnName, value));
                targetSt.setObject(columnIndex, value);
            }
            if (batch) {
                targetSt.addBatch();
                logger.trace("");
            } else {
                targetSt.executeUpdate();
                logger.trace("!");
            }
        }

        if (batch) {
            executeBatch(targetSt, String.format("Copy of '%s'", tableName));
            logger.info(String.format("Inserted %d rows", numberOfRows));
        }
        targetSt.close();
        sourceSt.close();
    }

    private void executeBatch(PreparedStatement st, String description) throws SQLException {
        logger.trace("Executing batch insert");
        try {
            int[] insertResults = st.executeBatch();
            logger.debug(String.format("Batch execution returned %d results", insertResults.length));
            for (int r: insertResults) {
                if (r <= 0) {
                    logger.warn("Insert failed");
                }
            }
        }
        catch (BatchUpdateException e) {
            /* getUpdateCounts() will return EITHER counts for the successes before the first failure,
             * OR results for all elements of the batch, with a failure code for the failed ones. Which
             * of these it does is driver-dependent. So we first look for a failure code, and if we don't
             * find one we assume that we just have the results for the successes.
             */
            int[] updateCounts = e.getUpdateCounts();
            boolean failureFound = false;
            for (int i=0; i < updateCounts.length; i++) {
                if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                    failureFound = true;
                    logger.error(String.format("%s failed on item #%d", description, i + 1));

                    Throwable t = e;
                    do {
                        logger.error("Caused by", t);
                    } while (null != (t = t.getCause()));
                }
            }
            if (!failureFound) {
                logger.error(String.format("%s failed on item #%d", description, updateCounts.length + 1));
                Throwable t = e;
                do {
                    logger.error("Caused by", t);
                } while (null != (t = t.getCause()));
            }

            throw e;
        }
    }

    private String insertStatement (String schema, String tableName, ResultSetMetaData rsMeta) throws SQLException {
        StringBuilder columns = new StringBuilder();
        StringBuilder placeholders = new StringBuilder();

        for (int columnIndex = 1; columnIndex <= rsMeta.getColumnCount(); columnIndex++) {
            String columnName = rsMeta.getColumnName(columnIndex);
            if (columnIndex > 1) {
                columns.append(", ");
                placeholders.append(", ");
            }
            columns.append(columnName);
            placeholders.append('?');
        }

        return String.format("insert into %s.%s (%s) values (%s)",
            schema, tableName, columns, placeholders);
    }

    private String[] getColumnNames(ResultSetMetaData rsMeta) throws SQLException {
        int numberOfColumns = rsMeta.getColumnCount();
        String[] columnNames = new String[numberOfColumns];
        for (int columnIndex = 1; columnIndex <= numberOfColumns; columnIndex++) {
            columnNames[columnIndex - 1] = rsMeta.getColumnName(columnIndex);
        }
        return columnNames;
    }


    private void createSchema(String schema)throws SQLException{
        Statement st = target.createStatement();
        String createStatement = String.format("create schema %s authorization dba", schema);
        logger.trace(createStatement);
        st.execute(createStatement);
        st.close();
        logger.debug(String.format("Schema %s successfully created", schema));
    }

    private void copySchema(boolean createCached) throws SQLException {
        copySchema("public", createCached);
    }

    private void copySchema(String schema, boolean createCached) throws SQLException {
        Set<String> tableNames = new HashSet<String>();
        ResultSet tables = sourceMeta.getTables(null, schema, null, new String[] {"TABLE", "SEQUENCE"});
        while (tables.next()) {
            String tableName = tables.getString("TABLE_NAME");
            String tableType = tables.getString("TABLE_TYPE");

            if (tableType.equals("TABLE")) {
                tableNames.add(tableName);
                createTable(schema, tableName, createCached);
            } else if (tableType.equals("SEQUENCE")) {
                createSequence(schema, tableName);
            } else {
                throw new RuntimeException(
                    String.format("Encountered table '%s' of unrecognised type '%s'", tableName, tableType));
            }
        }
        tables.close();

        for (String tableName: tableNames) {
            createForeignKeys(schema, tableName);
            createIndices(schema, tableName);
            createCheckConstraints(schema, tableName);
        }
    }

    private void createTable(String schema, String tableName, boolean createCached)
            throws SQLException {
        logger.info(String.format("Creating table \"%s\" in schema \"%s\"", tableName, schema));
        Statement st = target.createStatement();
        st.execute(String.format("drop table %s.%s if exists cascade", schema, tableName));
        st.close();

        st = target.createStatement();
        String createStatement = createStatementForTable(schema, tableName, createCached);
        logger.trace(createStatement);
        st.execute(createStatement);
        st.close();
    }

    private void createSequence(String schema, String sequenceName) throws SQLException {
        int nextval = getNextVal(String.format("%s.%s", schema, sequenceName));
        logger.info(String.format("Creating sequence '%s.%s' with starting value %d", schema, sequenceName, nextval));
        Statement statement = target.createStatement();
        statement.execute(String.format("create sequence %s.%s start with %d", schema, sequenceName, nextval));
        statement.close();
    }

    private int getNextVal(String sequenceName) throws SQLException {
        PreparedStatement st = source.prepareStatement("select nextval(?::regclass)");
        try {
            st.setString(1, sequenceName);
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            try { st.close(); } catch (SQLException e) { logger.error(e); }
        }
    }

    private void createForeignKeys(String schema, String tableName) throws SQLException {
        ResultSet rs = sourceMeta.getImportedKeys(null, schema, tableName);
        String currentConstraint = null;
        String targetTable = null;
        StringBuilder sourceColumns = null;
        StringBuilder targetColumns = null;

        while (rs.next()) {
            String pkTableName = rs.getString ("PKTABLE_NAME");
            String pkColumnName = rs.getString("PKCOLUMN_NAME");
            String fkColumnName = rs.getString("FKCOLUMN_NAME");
            short keySeq = rs.getShort("KEY_SEQ");

            short updateRule = rs.getShort("UPDATE_RULE");
            short deleteRule = rs.getShort("DELETE_RULE");

            // Ignore DEFERRABILITY, because HSQLDB doesn't support deferred constraints

            String constraintName = rs.getString("FK_NAME");

            if (keySeq == 1) {
                if (targetTable != null) {
                    String constraintText = String.format("foreign key (%s) references %s (%s)",
                        sourceColumns, targetTable, targetColumns);

                    String updateAction = decodeCascadeRule(updateRule);
                    if (updateAction != null) {
                        constraintText += " on update " + updateAction;
                    }

                    String deleteAction = decodeCascadeRule(deleteRule);
                    if (deleteAction != null) {
                        constraintText += " on delete " + deleteAction;
                    }

                    logger.info(String.format("Adding foreign key %s -> %s", tableName, targetTable));
                    if (currentConstraint == null) {
                        addConstraint(schema, tableName, constraintText);
                    } else {
                        addConstraint(schema, tableName,
                            String.format("constraint \"%s\" %s", currentConstraint, constraintText));
                    }
                }
                currentConstraint = constraintName;
                targetTable = pkTableName;
                sourceColumns = new StringBuilder();
                targetColumns = new StringBuilder();
            }
            else {
                sourceColumns.append(", ");
                targetColumns.append(", ");
            }
            sourceColumns.append(fkColumnName);
            targetColumns.append(pkColumnName);
        }
        rs.close();
    }

    private void createIndices(String schema, String tableName) throws SQLException {
        String indexName = null;
        StringBuilder columns = null;

        ResultSet rs = sourceMeta.getIndexInfo(null, schema, tableName, false, false);
        while (rs.next()) {
            short indexType = rs.getShort("TYPE");
            if (indexType == DatabaseMetaData.tableIndexStatistic) {
                continue;
            }

            short pos = rs.getShort("ORDINAL_POSITION");
            String columnName = rs.getString("COLUMN_NAME");
            boolean isUnique = !rs.getBoolean("NON_UNIQUE");

            if (pos == 1) {
                if (columns != null) {
                    createIndex(tableName, indexName, columns.toString(), isUnique);
                }
                indexName = rs.getString("INDEX_NAME");
                columns = new StringBuilder();
            } else {
                columns.append(", ");
            }
            columns.append(columnName);
        }
        rs.close();
    }

    private void createIndex(String tableName, String indexName, String columns, boolean unique) throws SQLException {
        if (columns.contains("(")) {
            logger.debug(String.format("Ignoring index %s (%s) on table %s", indexName, columns, tableName));
            return;
        }
        String createIndex;
        if (unique) {
            createIndex = String.format("create unique index \"%s\" on %s (%s)",
                indexName, tableName, columns);
        } else {
            createIndex = String.format("create index \"%s\" on %s (%s)",
                indexName, tableName, columns);
        }

        logger.trace(createIndex);
        Statement st = target.createStatement();
        st.executeUpdate(createIndex);
        st.close();
    }

    private static final String CHECK_CONSTRAINTS_SQL
          = "select pg_namespace.nspname  as schema"
            +"      , pg_class.relname      as table_name"
            +"      , pg_constraint.conname as constraint_name"
            +"      , pg_constraint.consrc  as condition"
            +" from pg_constraint"
            +" join pg_namespace on pg_constraint.connamespace = pg_namespace.oid"
            +" join pg_class on pg_constraint.conrelid = pg_class.oid"
            +" where pg_constraint.contype = 'c'"
            +" and pg_namespace.nspname = ?"
            +" and pg_class.relname = ?";

    private void createCheckConstraints(String schema, String tableName) throws SQLException {
        PreparedStatement st = source.prepareStatement(CHECK_CONSTRAINTS_SQL);
        st.setString(1, schema);
        st.setString(2, tableName);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            String constraintName = rs.getString("constraint_name");
            String condition = rs.getString("condition");

            //re-format the condition
            condition = condition.replace("::text", "");

            addConstraint(schema, tableName,
                String.format("constraint \"%s\" check (%s)", constraintName, condition));
        }
        st.close();
    }

    private String decodeCascadeRule(short action) {
        switch(action) {
        case DatabaseMetaData.importedKeyCascade:
            return "cascade";
        case DatabaseMetaData.importedKeySetDefault:
            return "set default";
        case DatabaseMetaData.importedKeySetNull:
            return "set null";
        default:
            return null;
        }
    }

    private void addConstraint(String schema, String tableName, String constraint) throws SQLException {
        logger.trace(constraint);
        Statement st = target.createStatement();
        st.executeUpdate(String.format("alter table %s.%s add %s", schema, tableName, constraint));
        st.close();
    }

    private String createStatementForTable(String schema, String tableName, boolean createCached) throws SQLException {
        StringBuilder sb = new StringBuilder();

        String create = createCached ? "create cached table " : "create table ";
        sb.append(create + schema + "." + tableName + " (\n");

        ResultSet columns = sourceMeta.getColumns(null, schema, tableName, null);
        boolean first = true;
        while (columns.next()) {
            String columnName = columns.getString("COLUMN_NAME");
            String columnType = columns.getString("TYPE_NAME");
            int columnSize = columns.getInt("COLUMN_SIZE");
            boolean notNull = columns.getString("IS_NULLABLE").equals("NO");
            String defaultValue = columns.getString("COLUMN_DEF");

            if (first) {
                first = false;
                sb.append("  ");
            } else {
                sb.append(", ");
            }
            sb.append(columnDefinition(tableName, columnName, columnType, columnSize, notNull, defaultValue));
            sb.append('\n');
        }

        if (!tablesWithIdentityColumn.contains(tableName)) {
            String primaryKey = primaryKeyForTable(schema, tableName);
            if (primaryKey != null) {
                sb.append(", ");
                sb.append(primaryKey);
            }
        }

        sb.append(")\n");

        return sb.toString();
    }

    private String primaryKeyForTable(String schema, String tableName) throws SQLException {
        StringBuilder columns = new StringBuilder();

        ResultSet rs = sourceMeta.getPrimaryKeys(null, schema, tableName);
        String keyName = null;
        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");

            if (keyName == null) {
                keyName = rs.getString("PK_NAME");
            }

            if (!rs.isFirst()) {
                columns.append(", ");
            }
            columns.append(columnName);
        }
        rs.close();

        if (columns.length() == 0) {
            return null;
        }

        if (keyName == null) {
            return String.format("primary key (%s)", columns);
        } else {
            return String.format("constraint \"%s\" primary key (%s)", keyName, columns);
        }
    }

    /**
     * Maps PostgreSQL types to the corresponding HSQLDB type.
     */
    private static Map<String,String> typeNameMap = new HashMap<String,String>() {{
        put("serial", "identity");
        put("int2", "smallint");
        put("int4", "integer");
        put("int8", "bigint");
        put("text", "longvarchar");
        put("float4", "float");
        put("float8", "double");
        put("bpchar", "char");
        put("bool", "boolean");
    }};

    /**
     * Maps PostgreSQL default values to the equivalent HSQLDB function.
     */
    private static Map<String,String> defaultValueMap = new HashMap<String,String>() {{
        put("\"current_user\"()", "current_user");
        put("now()", "now");
        put("''::text", "''");
        put("''::character varying", "''");
        put("'DELETE'::character varying", "'DELETE'");
        put("'INSERT'::character varying", "'INSERT'");
        put("'UPDATE'::character varying", "'UPDATE'");
    }};

    private Set<String> tablesWithIdentityColumn = new HashSet<String>();

    private String columnDefinition(String tableName, String columnName, String columnType,
            int columnSize, boolean notNull, String defaultValue) {
        if (typeNameMap.containsKey(columnType)) {
            columnType = typeNameMap.get(columnType);
        }

        if ("identity".equals(columnType)) {
            defaultValue = null;
            if (tablesWithIdentityColumn.contains(tableName)) {
                logger.warn(String.format("Table '%s' has more than one identity column; HSQLDB only allows one per table.\nColumn '%s' will not be auto-incremented.",
                    tableName, columnName));
                columnType = "integer";
            } else {
                tablesWithIdentityColumn.add(tableName);
            }
        }

        if (defaultValue != null) {
            if (defaultValueMap.containsKey(defaultValue)) {
                defaultValue = defaultValueMap.get(defaultValue);
            }
            columnType += " default " + defaultValue;
        }

        if (notNull) {
            columnType += " not null";
        }

        if (columnType.equals("varchar") || columnType.equals("char")) {
            return String.format("%s %s(%d)", columnName, columnType, columnSize);
        }

        return String.format("%s %s", columnName, columnType);
    }
}
