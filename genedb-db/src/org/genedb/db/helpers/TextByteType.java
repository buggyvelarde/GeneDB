package org.genedb.db.helpers;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;


/**
 * Converts text strings into byte arrays. Used for the feature.residues column,
 * which uses only a limited set of characters (i.e. letters representing nucleotides
 * or amino acids) to reduce the space requirements.
 */
public class TextByteType implements UserType {
    @SuppressWarnings("unused")
    public byte[] assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    public byte[] deepCopy(Object originalOb) throws HibernateException {
        byte[] original = (byte[]) originalOb;
        if (original == null)
            return null;
        return Arrays.copyOf(original, original.length);
    }

    @SuppressWarnings("unused")
    public byte[] disassemble(Object value) throws HibernateException {
        return deepCopy(value);
    }

    public boolean equals(Object x, Object y) throws HibernateException {
        return (x == y) || (x != null && y != null && (x.equals(y)));
    }

    @SuppressWarnings("unused")
    public int hashCode(Object arg0) throws HibernateException {
        return 0;
    }

    public boolean isMutable() {
        return false;
    }

    @SuppressWarnings("unused")
    public byte[] nullSafeGet(ResultSet rs, String[] names, Object owner)
            throws HibernateException, SQLException {
        String string = rs.getString(names[0]);
        if (string == null) {
            return null;
        }
        return string.getBytes();
    }

    public void nullSafeSet(PreparedStatement st, Object value, int index)
            throws HibernateException, SQLException {
        if (value == null) {
            st.setString(index, "");
        } else {
            byte[] b = (byte[]) value;
            String s = new String(b);
            st.setString(index, s);
        }
    }

    @SuppressWarnings("unused")
    public byte[] replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }

    public Class<byte[]> returnedClass() {
        return byte[].class;
    }

    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

}
