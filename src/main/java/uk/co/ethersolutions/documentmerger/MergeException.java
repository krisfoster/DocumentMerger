/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.ethersolutions.documentmerger;

/**
 * Represents an exception thrown during the merge process.
 * 
 * @author kris
 */
public class MergeException extends Exception {
    
    public MergeException() {
        //
        super();
    }

    public MergeException(final String msg) {
        //
        super(msg);
    }

    public MergeException(final Throwable cause) {
        //
        super(cause);
    }
    
    public MergeException(final String msg, final Throwable cause) {
        //
        super(msg, cause);
    }
}
