package org.genedb.db.helpers;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public class TextByteType implements UserType{

	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return null;
	}

	public Object deepCopy(Object value) throws HibernateException {
		if (value == null) return null;
        return (byte[])value;
		
	}

	public Serializable disassemble(Object arg0) throws HibernateException {
		return null;
	}

	public boolean equals(Object x, Object y) throws HibernateException {
		return (x == y)
        || (x != null
            && y != null
            && (x.equals(y)));
	}

	public int hashCode(Object arg0) throws HibernateException {
		return 0;
	}

	public boolean isMutable() {
		return false;
	}

	public Object nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
		String string = rs.getString(7);
		if (string == null) {
			return null;
		}
		return string.getBytes();
	}

	public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
		if(value == null) {
			st.setString(index, "");
		} else {
			byte[] b = (byte[]) value;
			String s = new String(b);
			st.setString(index, s);
		}
	}

	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return null;
	}

	public Class<Byte> returnedClass() {
		return byte.class;
	}

	public int[] sqlTypes() {
		return new int[] {Types.VARCHAR};
	}

}
