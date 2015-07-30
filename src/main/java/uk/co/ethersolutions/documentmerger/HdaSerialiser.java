/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.impl.DataFactoryImpl;

/**
 * Contains functions for serialising my maps (representing revisions) into
 * DataBinders.
 * @author kris
 */
public class HdaSerialiser {

    static DataBinder serialise(final DataObject localData,
            final String resultSetName,
            final List<DataResultSet.Field> fields,
            final Map<String, Map<String, String>> rows) {
        //
        final DataFactoryImpl dataFactoryImpl = new DataFactoryImpl();
        final DataBinder binder = dataFactoryImpl.createBinder();
        final DataResultSet exportResults = dataFactoryImpl.createResultSet();
        //
        int local_cnt = 0;
        for (Map.Entry<String, String> prop : localData.entrySet()) {
            //
            final String name = prop.getKey();
            final String value = prop.getValue();
            binder.putLocal(prop.getKey(), prop.getValue());
            local_cnt++;
        }
        //
        for (DataResultSet.Field field : fields) {
            //
            if (!exportResults.hasField(field.getName())) {
                //
                exportResults.addField(field, field.getName());
            }
        }
        //
        for (Map.Entry<String, Map<String, String>> row : rows.entrySet()) {
            //
            final String name = row.getKey();
            final Map<String, String> value = row.getValue();
            final DataObject doo = dataFactoryImpl.createDataObject();
            final List<String> vals = new ArrayList<>();
            //
            int field_count = 0;
            for (Map.Entry<String, String> element : value.entrySet()) {
                //
                vals.add(element.getValue());
                field_count++;
            }
            exportResults.addRow(vals);
        }
        binder.addResultSet(resultSetName, exportResults);
        //
        return binder;
    }
    
}
