package org.genedb.web.mvc.model.types;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DtoObjectArrayField implements Array {

    private Iterator<?> iter = null;
    private String baseTypeName = null;
    
    public <T> DtoObjectArrayField(String objectType, List<T> arr){
        baseTypeName = objectType;
        iter = arr.iterator();
    }

    @Override
    public void free() throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getArray() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getArray(Map<String, Class<?>> arg0) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getArray(long arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getArray(long arg0, int arg1, Map<String, Class<?>> arg2)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getBaseType() throws SQLException {
        return java.sql.Types.JAVA_OBJECT;
    }

    @Override
    public String getBaseTypeName() throws SQLException {
        return baseTypeName;
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> arg0)
            throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1,
            Map<String, Class<?>> arg2) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }
    
//    @Override
//    public String getValue(){
//        return toString();
//    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        while(iter.hasNext()){  
            sb.append("\"");
            sb.append(iter.next());  
            sb.append("\"");                
            if (iter.hasNext()){
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }

}
