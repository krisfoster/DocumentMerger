/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

import uk.co.ethersolutions.documentmerger.MergeException;
import java.util.Map;

/**
 * Various useful functions for dealing with meta-data.
 * @author kris
 */
public class MetaData {
    // UCM meta-data constants
    public static final String EXPORT_RESULTS = "ExportResults";
    public static final String D_REV_CLASS_ID = "dRevClassID";
    public static final String D_REV_LABEL = "dRevLabel";
    public static final String D_DOC_NAME = "dDocName";
    public static final String DID = "dID";
    public static final String D_REVISION_ID = "dRevisionID";

    /**
     * Returns the value of a field or the empty String
     * 
     * @param rev A revision of a document (no more than a map of fields to
     * values)
     * @param fieldName The field name we are looking for
     * @return  The value for the field
     */
    static String getField(final Map<String, String> rev, final String fieldName) {
        if (rev.containsKey(fieldName)) {
            return rev.get(fieldName);
        } else {
            return "";
        }
    }

    /**
     * Takes a set of revisions and looks for and extracts the dRevClassID for
     * one particular document contained within the set of revisions.
     * 
     * @param revs The set of revisions
     * @param dDocName The document, whose dRevClassID we wish to extract
     * @return The dRevClassID for the given document
     */
    static String extractRevisionClassID(
            final Map<String, Map<String, String>> revs,
            final String dDocName) throws MergeException {
        //
        if (dDocName == null || dDocName.length() <= 0) {
            //
            throw new MergeException("Passed in Merge dDocName was empty or "
                    + "null");
        }
        //
        String ret = null;
        //
        for (Map.Entry<String, Map<String, String>> rev : revs.entrySet()) {
            //
            final String dID = rev.getKey();
            final Map<String, String> fields = rev.getValue();
            if (fields.containsKey(D_DOC_NAME) &&
                    dDocName.equals(fields.get(D_DOC_NAME))) {
                //
                ret = fields.get(D_REV_CLASS_ID);
                break;
            }
        }
        //
        if (null == ret) {
            //
            throw new MergeException("Failed to locate the dRevClassID for the "
                    + "dpocument with dDocName, " + dDocName);
        }
        //
        return ret;
    }
    
}
