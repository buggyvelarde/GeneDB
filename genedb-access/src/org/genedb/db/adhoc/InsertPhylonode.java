package org.genedb.db.adhoc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Insert a leaf phylonode (corresponding to an organism that already exists in the organism table)
 * into phylotree 1.
 *
 * @author rh11
 *
 */
public class InsertPhylonode {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        if (args.length != 2 && args.length != 4) {
            System.err.println("Usage: java InsertPhylonode <jdbc URL> <database username> [<parent phylonode label> <organism common name>]");
            System.exit(1);
        }
        String jdbcURL = args[0];
        String databaseUsername = args[1];

        String databasePassword = System.getProperty("password");
        if (databasePassword == null) {
            databasePassword = new String(
                System.console().readPassword("Password for %s @ %s: ", databaseUsername, jdbcURL)
            );
        }

        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(jdbcURL, databaseUsername, databasePassword);
        InsertPhylonode insertPhylonode = new InsertPhylonode(conn);

        if (args.length > 2) {
            String parentPhylonodeLabel = args[2];
            String organismCommonName = args[3];

            insertPhylonode.insertBelow(organismCommonName, parentPhylonodeLabel);
        }

        System.out.println(insertPhylonode.root);
        conn.close();
    }

    private Connection conn;
    private Phylonode root;
    private InsertPhylonode(Connection conn) throws SQLException {
        this.conn = conn;
        this.root = getRootPhylonode();
    }

    private Phylonode getRootPhylonode() throws SQLException {
        Phylonode root = null;

        PreparedStatement st = conn.prepareStatement("select * from phylonode order by left_idx");
        try {
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                int phylonodeId = rs.getInt("phylonode_id");
                int leftIndex    = rs.getInt("left_idx");
                int rightIndex = rs.getInt("right_idx");
                double distance = rs.getDouble("distance");
                String label     = rs.getString("label");

                Phylonode phylonode = new Phylonode(phylonodeId, leftIndex, rightIndex, distance, label);

                int parentPhylonodeId = rs.getInt("parent_phylonode_id");
                if (rs.wasNull()) {
                    root = phylonode;
                } else {
                    Phylonode parentPhylonode = Phylonode.phylonodesById.get(parentPhylonodeId);
                    if (parentPhylonode == null) {
                        throw new RuntimeException(
                            String.format("Phylonode '%s' (ID=%d) has parent ID=%d, which we cannot find",
                            label, phylonodeId, parentPhylonodeId));
                    }
                    phylonode.setParent(parentPhylonode);
                }
            }
        } finally {
            try { st.close(); } catch (SQLException e) { }
        }

        if (root == null) {
            throw new RuntimeException("No root found!");
        }
        return root;
    }

    private void insertBelow(String organismCommonName, String parentPhylonodeLabel) throws SQLException {
        Phylonode parent = Phylonode.phylonodesByLabel.get(parentPhylonodeLabel);
        if (parent == null) {
            throw new RuntimeException(String.format("Could not find phylonode '%s'", parentPhylonodeLabel));
        }

        Phylonode child = new Phylonode(0, 0, 0, 1 + parent.distance, organismCommonName);
        child.setParent(parent);

        conn.setAutoCommit(false);
        root.insertNewNodes(conn);
        root.recomputeIndexes(conn, 1);
        makeIndexesPositive();
        conn.commit();
    }

    private void makeIndexesPositive() throws SQLException {
        PreparedStatement st = conn.prepareStatement(
            "update phylonode set left_idx = -left_idx, right_idx = -right_idx "+
            "where phylotree_id = 1 and (left_idx < 0 or right_idx < 0)"
        );
        try {
            st.executeUpdate();
        } finally {
            try { st.close(); } catch (SQLException e) { }
        }
    }
}


class Phylonode {
    static Map<String,Phylonode> phylonodesByLabel = new HashMap<String,Phylonode>();
    static Map<Integer,Phylonode> phylonodesById = new HashMap<Integer,Phylonode>();

    int phylonodeId;
    int leftIndex, rightIndex;
    double distance;
    String label;
    List<Phylonode> children = new ArrayList<Phylonode>();
    Phylonode parent;

    Phylonode(int phylonodeId, int leftIndex, int rightIndex, double distance, String label) {
        this.phylonodeId = phylonodeId;
        this.leftIndex = leftIndex;
        this.rightIndex = rightIndex;
        this.distance = distance;
        this.label = label;

        phylonodesByLabel.put(label, this);
        phylonodesById.put(phylonodeId, this);
    }

    void setParent(Phylonode parent) {
        if (parent == null) {
            throw new NullPointerException("parent is null");
        }
        this.parent = parent;
        parent.children.add(this);
    }

    int recomputeIndexes(Connection conn, int previousIndex) throws SQLException {
        int oldLeftIndex    = this.leftIndex;
        int oldRightIndex = this.rightIndex;

        int index = previousIndex + 1;
        this.leftIndex = index;
        for (Phylonode child: children) {
            index = child.recomputeIndexes(conn, index);
        }
        this.rightIndex = ++index;

        if (oldLeftIndex != leftIndex || oldRightIndex != rightIndex) {
            updateIndexesForNode(conn);
        }

        return this.rightIndex;
    }

    void updateIndexesForNode(Connection conn) throws SQLException {
        System.out.printf("Updating phylonode '%s' [ID=%d] with left_idx=%d and right_idx=%d\n",
            label, phylonodeId, leftIndex, rightIndex);

        PreparedStatement st = conn.prepareStatement(
            "update phylonode set left_idx = -?, right_idx = -? where phylonode_id = ?"
        );
        try {
            st.setInt(1, leftIndex);
            st.setInt(2, rightIndex);
            st.setInt(3, phylonodeId);

            int numRows = st.executeUpdate();
            if (numRows != 1) {
                throw new RuntimeException("Expected one row to be affected, not " + numRows);
            }
        } finally {
            try { st.close(); } catch (SQLException e) { }
        }
    }

    void insertNewNodes(Connection conn) throws SQLException {
        if (phylonodeId == 0) {
            phylonodeId = insertNode(conn);
        }
        for (Phylonode child: children) {
            child.insertNewNodes(conn);
        }
    }

    private int insertNode(Connection conn) throws SQLException {
        System.out.printf("Inserting phylonode '%s'\n", label);

        PreparedStatement st = conn.prepareStatement(
            "    insert into phylonode ("+
            "        phylotree_id, parent_phylonode_id, left_idx, right_idx, distance, label"+
            "    ) values ("+
            "        1, ?, ?, ?, ?, ?"+
            "    )"
        );
        try {
            st.setInt(1, parent.phylonodeId);
            st.setInt(2, leftIndex);
            st.setInt(3, rightIndex);
            st.setDouble(4, distance);
            st.setString(5, label);
            st.executeUpdate();
        } finally {
            try { st.close(); } catch (SQLException e) { }
        }

        st = conn.prepareStatement(
            "    insert into phylonode_organism (phylonode_id, organism_id) ("+
            "        select currval('phylonode_phylonode_id_seq'::regclass), organism_id"+
            "        from organism where common_name = ?"+
            "    )"
        );
        try {
            st.setString(1, label); // Assume the label is the same as the common_name of the organism
            st.executeUpdate();
        } finally {
            try { st.close(); } catch (SQLException e) { }
        }

        st = conn.prepareStatement("select currval('phylonode_phylonode_id_seq'::regclass)");
        try {
            ResultSet rs = st.executeQuery();
            rs.next();
            return rs.getInt(1);
        } finally {
            try {st.close();} catch (SQLException e) { }
        }
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int indent) {
        String spaces = indent == 0 ? "" : String.format("%" + indent + "s", "");
        StringBuilder childString = new StringBuilder();
        for (Phylonode child: children) {
            childString.append(child.toString(indent + 2));
        }
        return String.format("%s[%d] %s (left=%d, right=%d)\n%s",
            spaces, phylonodeId, label, leftIndex, rightIndex, childString);
    }
}