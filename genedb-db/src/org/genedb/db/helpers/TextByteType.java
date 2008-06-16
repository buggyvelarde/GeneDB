package org.genedb.db.helpers;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;


/**
 * Converts text strings into byte arrays. Used for the feature.residues column,
 * which uses only a limited set of characters (i.e. letters representing nucleotides
 * or amino acids) to reduce the space requirements.
 */
public class TextByteType implements UserType {
    private static final Logger logger = Logger.getLogger(TextByteType.class);

    public byte[] assemble(Serializable cached, @SuppressWarnings("unused") Object owner) throws HibernateException {
        logger.trace("assemble");
        return deepCopy(cached);
    }

    public byte[] deepCopy(Object originalOb) throws HibernateException {
        logger.trace("deepCopy");
        byte[] original = (byte[]) originalOb;
        if (original == null)
            return null;
        return Arrays.copyOf(original, original.length);
    }

    public byte[] disassemble(Object value) throws HibernateException {
        logger.trace("disassemble");
        return deepCopy(value);
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        logger.trace("equals");
        return Arrays.equals((byte[]) x, (byte[]) y);
    }

    public int hashCode(@SuppressWarnings("unused") Object arg0) throws HibernateException {
        logger.trace("hashCode");
        return 0;
    }

    public boolean isMutable() {
        logger.trace("isMutable");
        return true;
    }

    public byte[] nullSafeGet(ResultSet rs, String[] names, @SuppressWarnings("unused") Object owner)
            throws HibernateException, SQLException {
        logger.trace("nullSafeGet");
        String string = rs.getString(names[0]);
        if (string == null) {
            return null;
        }
        return string.getBytes();
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        logger.trace("nullSafeSet");

        if (value == null) {
            st.setString(index, "");
        } else {
            byte[] b = (byte[]) value;
            String s = new String(b);
            st.setString(index, s);
        }
    }

    public byte[] replace(Object original, @SuppressWarnings("unused") Object target, @SuppressWarnings("unused") Object owner) throws HibernateException {
        logger.trace("replace");
        return deepCopy(original);
    }

    public Class<byte[]> returnedClass() {
        return byte[].class;
    }

    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

}
