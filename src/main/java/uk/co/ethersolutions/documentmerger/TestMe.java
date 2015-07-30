/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

//
//import intradoc.data.DataBinderUtils;
//import intradoc.tools.utils.SimpleDataBinderUtils;
import uk.co.ethersolutions.documentmerger.MergeException;
import java.io.File;
import java.io.FileInputStream;
//import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
//
import intradoc.serialize.DataBinderSerializer;
//
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//import java.util.Set;
//
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
//
import oracle.stellent.ridc.model.serialize.HdaBinderSerializer;
//import oracle.stellent.ridc.model.serialize.HdaSerializerUtils;
//
import oracle.stellent.ridc.model.impl.DataFactoryImpl;

/**
 *
 * @author kris
 */
public class TestMe {
    
    public static final void main(String[] args) throws Exception {
        //
        final File archive = new File("/home/kris/NetBeansProjects/JavaLibrary1/archive.hda");
        final FileInputStream fis = new FileInputStream(archive);
        List<DataResultSet.Field> fields;
        //
        final HdaBinderSerializer serialiser =
                new HdaBinderSerializer(StandardCharsets.UTF_8.name(),
                                        new DataFactoryImpl());
        final DataBinder binder = serialiser.parseBinder(fis);
        Collection<String> fieldTypeNames = binder.getFieldTypeNames();
        //
        final DataObject localData = binder.getLocalData();
        final DataResultSet resultSet = binder.getResultSet(MetaData.EXPORT_RESULTS);
        //
        final Map<String, Map<String, String>> revisions = Revision.revisions(resultSet);

        // Set the dDocName - this is the doc into which all the revisions will
        // be merged.
        final String mergedDocName = "LOCALHOSTLOCAL000002";
        // Create a list of the dIDs that will ocnstitute the new doc
        //
        final List<String> resultantOrder = new ArrayList<>();
        resultantOrder.add("5");
        resultantOrder.add("3");
        resultantOrder.add("4");
        resultantOrder.add("1");
        resultantOrder.add("2");
        // Create an array of the revisions by this ordering. Whilst doing so
        // we need to update various meta-data:
        // * dID         : This remains the same
        // * dDocName    : All docs to have the same dDocName. That of the doc
        //                 into which they are being merged
        // * dRevisionID : Will be numbered from 1 for all revisions
        // * dRevClassID : Will be the value of dRevClassID for the doc into
        //                 which the revisions will be merged
        // * dRevLabel   : Will be equal to dRevisionID
        //
        Map<String, Map<String, String>> merged = merge(revisions, resultantOrder, mergedDocName);
        
        // Serialise the data binder for display and checking
        DataBinder outBinder = HdaSerialiser.serialise(localData, MetaData.EXPORT_RESULTS, resultSet.getFields(), merged);
        //
        //
        final String strOld = Revision.revisionsToString(revisions);
        final String strNew = Revision.revisionsToString(merged);
        //
        System.out.println("==============================================");
        System.out.println(" Read in Binder::");
        System.out.println(strOld);
        System.out.println("==============================================");
        System.out.println(" Merged Binder::");
        System.out.println(strNew);
        System.out.println("==============================================");
        System.out.println(" Written out Binder :");
        //DataBinderSerializer ser = new DataBinderSerializer();
        serialiser.serializeBinder(System.out, outBinder);
        //
        return;
    }
    
    private static Map<String, Map<String, String>> merge(
            final Map<String, Map<String, String>> revs,
            final List<String> resultantOrder,
            final String mergeDDocName) throws Exception {
        //
        final Map<String, Map<String, String>> revsOut = new LinkedHashMap<>();
        // Extract revisionClassID for the merge dDocName form the revisions
        String revisionClassID = null;
        try {
            //
            revisionClassID = MetaData.extractRevisionClassID(revs,
                    mergeDDocName);
        } catch (MergeException me) {
            //
            throw new MergeException("Failed to locate the dRevClassID. "
                    + "Aborting megre", me);
        }
        
        // Loop over the output order (the passed in dIDs) and extract the
        // matching dID from the passed in revisions. Fix up that revision
        // and place it in the ouptut revisions
        int index = 1;
        //
        for (String dID : resultantOrder) {
            //
            if (!revs.containsKey(dID)) {
                //    
                throw new IllegalArgumentException(
                        "The passed in list of dIDs "
                        + "describing the output ordering for the merged "
                        + "revisions contained a dID that was not present "
                        + "within the input revisions.");
            }
            
            Map<String, String> rev = revs.get(dID);
            Map<String, String> tRev = transformRevision(
                    rev,
                    Integer.toString(index),
                    mergeDDocName,
                    revisionClassID);
            revsOut.put(dID, tRev);
            // Don't like this - but I am lazy :)
            index++;
        }
        //
        return revsOut;
    }
    
    private static Map<String, String> transformRevision(
            final Map<String, String> rev,
            final String revisionNumber,
            final String dDocName,
            final String docRevisionsClassID) {
        // Make a (shallow) clone of the mpa. We will transform the copy
        Map<String, String> revOut = new LinkedHashMap<>(rev);
        // * Set the dDocName to be the merged dDocName
        revOut.put(MetaData.D_DOC_NAME, dDocName);
        // * Set dRevClassID to the passed in one
        revOut.put(MetaData.D_REV_CLASS_ID, docRevisionsClassID);
        // * Set dRevLabel & dRevisionID to the passed in counter
        revOut.put(MetaData.D_REV_LABEL, revisionNumber);
        revOut.put(MetaData.D_REVISION_ID, revisionNumber);
        //
        return revOut;
    }
}
