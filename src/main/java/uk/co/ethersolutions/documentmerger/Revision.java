/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;

/**
 * Functions for handling revision(s).
 * @author kris
 */
public class Revision {
    // String constants used in serialising a revision to a String
    public static final String ID_PREFIX = "ID [";
    public static final String ID_POSTFIX = "] : ";
    public static final String EQUALS_STRING = " = ";
    public static final String ROW_TERMINATOR = ";";

    /**
     * Takes a set of revisions, a Map of dIDs to a Map of field names to
     * values, and renders it as a String
     * 
     * @param rows The revisions to render as a String
     * @return A String representation of the input revisions
     */
    static String revisionsToString(final Map<String, Map<String, String>> rows) {
        //
        StringBuilder buff = new StringBuilder();
        //
        for (Map.Entry<String, Map<String, String>> row : rows.entrySet()) {
            //
            Map<String, String> datas = row.getValue();
            buff.append(ID_PREFIX);
            buff.append(row.getKey());
            buff.append(ID_POSTFIX);
            //
            for (Map.Entry<String, String> data : datas.entrySet()) {
                //
                buff.append(data.getKey());
                buff.append(EQUALS_STRING);
                buff.append(data.getValue());
                buff.append(ROW_TERMINATOR);
            }
            buff.append(System.lineSeparator());
        }
        //
        return buff.toString();
    }

    /**
     * Takes a DataObject and converts it into a Map of field names to values.
     * @param row
     * @param fields
     * @return 
     */
    static Map<String, String> revision(final DataObject row,
            final List<DataResultSet.Field> fields) {
        //
        final Map<String, String> rev = new LinkedHashMap<>();
        //
        for (DataResultSet.Field field : fields) {
            //
            final String name = field.getName();
            final String value = row.get(name);
            rev.put(name, value);
        }
        //
        return rev;
    }

    /**
     * Takes a DataResultSet and converts this into a Map of Maps (dID to a
     * Map of field names to values for a revisions).
     * @param resultSet
     * @return 
     */
    static Map<String, Map<String, String>> revisions(
            final DataResultSet resultSet) {
        //
        final Map<String, Map<String, String>> revisions = new LinkedHashMap<>();
        final List<DataObject> rows = resultSet.getRows();
        List<DataResultSet.Field> fields = resultSet.getFields();
        //
        for (DataObject row : rows) {
            //
            final String dID = row.get(MetaData.DID);
            revisions.put(dID, Revision.revision(row, fields));
        }
        //
        return revisions;
    }
}
