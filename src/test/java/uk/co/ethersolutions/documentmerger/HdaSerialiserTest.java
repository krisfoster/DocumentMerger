/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertTrue;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.impl.DataBinderImpl;
import oracle.stellent.ridc.model.impl.DataObjectImpl;

/**
 *
 * @author kris
 */
public class HdaSerialiserTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public HdaSerialiserTest(String testName)
    {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( HdaSerialiserTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testSerialisation()
    {
        final DataObject localProps = new DataObjectImpl();
        final Map<String, String> propsMap = new LinkedHashMap<>();
        final List<DataResultSet.Field> fields = new ArrayList<>();
        final String resultSetName = "MyTestRS";
        final Map<String , Map<String, String>> rows = new LinkedHashMap<>();
        
        // Set up the local properties
        propsMap.put("ContentFileNumber", "1");
        propsMap.put("IsLastFile", "1");
        propsMap.put("NumRows", "5");
        propsMap.put("blDateFormat",
                "'{ts' ''yyyy-MM-dd HH:mm:ss{.SSS}[Z]'''}'!tEurope/London");
        propsMap.put("blFieldTypes",
                "dCreateDate date,dDocCreatedDate date,dReleaseDate date,dInDate date,dDocLastModifiedDate date,dOutDate date");
        localProps.putAll(propsMap);
        
        // Set up the fields
        fields.add(new DataResultSet.Field("field1"));
        fields.add(new DataResultSet.Field("field2"));
        fields.add(new DataResultSet.Field("field3"));
        fields.add(new DataResultSet.Field("field4"));

        // Set up the result-set
        final String did1 = "1";
        final String did2 = "2";
        final Map<String, String> row1 = new LinkedHashMap<>();
        final Map<String, String> row2 = new LinkedHashMap<>();
        //propsMap.put(did1, row1);
        //propsMap.put(did2, row2);
        //assertTrue( true );
    }
}
