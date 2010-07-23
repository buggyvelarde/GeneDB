package org.genedb.web.mvc.model.types;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DtoStringArrayField implements Array {
    
    private List<String> field;

    public <T> DtoStringArrayField(List<String> arr){
        field = arr;
    }
    
    @Override
    public void free() throws SQLException {

    }

    @Override
    public Object getArray() throws SQLException {
        return field;
    }

    @Override
    public Object getArray(Map<String, Class<?>> arg0) throws SQLException {
        return null;
    }

    @Override
    public Object getArray(long arg0, int arg1) throws SQLException {
        return null;
    }

    @Override
    public Object getArray(long arg0, int arg1, Map<String, Class<?>> arg2)
            throws SQLException {
        return null;
    }

    @Override
    public int getBaseType() throws SQLException {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public String getBaseTypeName() throws SQLException {
        return "text";
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return null;
    }

    @Override
    public ResultSet getResultSet(Map<String, Class<?>> arg0)
            throws SQLException {
        return null;
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1) throws SQLException {
        return null;
    }

    @Override
    public ResultSet getResultSet(long arg0, int arg1,
            Map<String, Class<?>> arg2) throws SQLException {
        return null;
    }

    @Override
    public String toString(){
        if (field==null){
            return "{}";
        }
        
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for(Iterator<String> iter = field.iterator(); iter.hasNext();){
            String ele = iter.next();            
            sb.append("\"");
            sb.append(ele.toString());            
            sb.append("\"");                        
            if (iter.hasNext()){
                sb.append(",");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
