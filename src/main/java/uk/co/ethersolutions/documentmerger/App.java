package uk.co.ethersolutions.documentmerger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import oracle.stellent.ridc.model.DataBinder;
import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import oracle.stellent.ridc.model.impl.DataFactoryImpl;
import oracle.stellent.ridc.model.serialize.HdaBinderSerializer;
import org.apache.commons.cli.BasicParser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.io.FileUtils;

/**
 * Tool that merges documents that been exported from UCM via the archiver.
 * Please consult that README.txt file bundled with this for instructions
 * on use, caveats and building.
 * 
 * @author Kris Foster
 *
 */
public class App 
{
    
    
    public static void main( String[] args )
    {
        // Some test params..
        //
        final String[] args1 = {
            //"-help",
            "-batchfile",
            "/home/kris/Oracle/Middleware/user_projects/domains/wcc_domain/ucm/cs/archives/merge/15-jul-30_15.47.20_160_03/15211154720~1.hda",
            "-orderfile",
            "./order.txt",
            "-mergedoc",
            "LOCALHOSTLOCAL000001"};
        try {
            //
            CommandLine cmds = parseArgs(args);
            //
            if (cmds.hasOption("help")) {
                //
                printUsage();
                return;
            }
            //
            final String batchFile = cmds.getOptionValue("batchfile");
            final String orderFile = cmds.getOptionValue("orderfile");
            final String mergeDoc = cmds.getOptionValue("mergedoc");
            // Load the inputs
            final DataBinder binder = loadBinder(batchFile);
            final List<String> order = loadResultantOrder(orderFile);
            final Collection<String> fieldTypeNames = binder.getFieldTypeNames();
            // TODO: THis has an isuse. It ignores reading in most of the fields contained
            // within the local data properties section!
            // Am working on a fix (https://github.com/krisfoster/hda) but till
            // I finish that (if I ever will!) I am putting a work-around in
            // place...
            final Map<String, String> localProps = ripLocalProps(batchFile);
            final DataObject localData = binder.getLocalData();
            // Merge in the read local props to the local props
            for (Map.Entry<String, String> prop : localProps.entrySet()) {
                //
                final String key = prop.getKey();
                final String val = prop.getValue();
                if (null != key && null != val) {
                    //
                    localData.put(key, val);
                }
            }
            final DataResultSet resultSet = binder.getResultSet(MetaData.EXPORT_RESULTS);
            //
            final Map<String, Map<String, String>> revisions =
                    Revision.revisions(resultSet);
            // Transform the archive
            Map<String, Map<String, String>> merged = merge(revisions, order,
                    mergeDoc);
            // Convert the merged data back into a DataBinder
            DataBinder outBinder = HdaSerialiser.serialise(
                    localData,
                    MetaData.EXPORT_RESULTS,
                    resultSet.getFields(),
                    merged);
            
            // Back-up the original archive batch file
            final File backupFile = backupFile(batchFile);
            // Over-write the new version of the archive batch file
            File outFile = new File(batchFile);
            final OutputStream out = new FileOutputStream(outFile);
            writeHDAFile(out, outBinder);
            //
        } catch (ParseException pex) {
            //
            System.err.println("Failed to parse command line options.");
            printUsage();
        } catch (MergeException mex) {
            //
            System.err.println("Failed to merge the revisions:: "
                    + mex.getMessage());
        } catch (IOException ioex) {
            //
            System.err.println("Failed to read one of the input files or write "
                    + "to one of the ouput files: "
                    + ioex.getMessage());
        }
    }
    
    private static File backupFile(final String path) throws IOException {
        //
        Path file = Paths.get(path);
        File source = file.toFile();
        String destPath = source.getAbsolutePath()
                + Long.toString(System.currentTimeMillis()) + ".back";
        File dest = Paths.get(destPath).toFile();
        //
        FileUtils.copyFile(source, dest);
        //
        return dest;
    }
    
    private static Map<String, Map<String, String>> merge(
            final Map<String, Map<String, String>> revs,
            final List<String> resultantOrder,
            final String mergeDDocName) throws MergeException {
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
        int index = resultantOrder.size();
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
            index--;
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

    private static void printUsage() {
       //
       HelpFormatter formatter = new HelpFormatter();
       formatter.printHelp("DocumentMerger", getCmdLineOptions());
    }
    
    private static Options getCmdLineOptions() {
        //
        Options opts = new Options();
        //
        final Option help = OptionBuilder.withArgName("help")
                .hasArg(false)
                .withDescription("prints out the command line params usage")
                .create("help");
        //
        final Option batchFile = OptionBuilder.withArgName("batchfile")
                .hasArg()
                .isRequired()
                .withDescription("path to the archive batch hda file "
                               + "containing the revisions to be merged")
                .create("batchfile");
        //
        final Option orderFile = OptionBuilder.withArgName("orderfile")
                .hasArg()
                .isRequired()
                .withDescription("path to the file containing the dIDs that "
                               + "should represent the output order of "
                               + "revisions for the merged documents")
                .create("orderfile");
        //
        final Option mergeDoc = OptionBuilder.withArgName("mergedoc")
                .hasArg()
                .isRequired()
                .withDescription("The dDocName of the document that revisions "
                               + "should be merged into")
                .create("mergedoc");
        //
        opts.addOption(help);
        opts.addOption(batchFile);
        opts.addOption(orderFile);
        opts.addOption(mergeDoc);
        //
        return opts;
    }
    
    private static CommandLine parseArgs(String[] args) throws ParseException {
        //
        final Options opts = getCmdLineOptions();
        //
        CommandLineParser parser = new BasicParser();
        CommandLine cmds = parser.parse(opts, args);
        //
        return cmds;
    }

    private static HdaBinderSerializer getDataBinderSerialiser() {
        //
        return new HdaBinderSerializer(StandardCharsets.UTF_8.name(),
                                        new DataFactoryImpl());
    }
    
    private static DataBinder loadBinder(final String path) throws IOException {
        //
        final File archive = new File(path);
        final FileInputStream fis = new FileInputStream(archive);
        //
        final HdaBinderSerializer serialiser = getDataBinderSerialiser();
        final DataBinder binder = serialiser.parseBinder(fis);
        //
        return binder;
    }
    
    private static void writeHDAFile(final OutputStream out,
            final DataBinder binder) throws IOException {
        //
        HdaBinderSerializer serialiser = getDataBinderSerialiser();
        serialiser.serializeBinder(out, binder);
    }
    
    private static List<String> loadResultantOrder(final String path)
        throws IOException {
        //
        List<String> ret = new ArrayList<>();
        final File order = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(order));
        try {
            for(String line; (line = br.readLine()) != null; ) {
                // process the line.
                ret.add(line.trim());
            }
        } finally {
            if (null != br) {
                //
                br.close();
            }
        }
        //
        return ret;
    }
    
    private static Map<String, String> ripLocalProps(final String batchFile)
            throws IOException {
        // Read the file in and extract the data fields from the local properties
        // and wrap 'em up as a Map. We will use these later when fixing up the
        // the output batch file
        final Map<String, String> props = new LinkedHashMap<>();
        boolean foundProps = false;
        boolean foundEnd = false;
        final File f = new File(batchFile);
        BufferedReader br = new BufferedReader(new FileReader(f));
        try {
            for(String line; (line = br.readLine()) != null; ) {
                // process the line.
                if (!foundProps) {
                    // look for start of props
                    foundProps = line.matches("^@Properties LocalData\\s*$");
                } else {
                    // we are in the props - so check that we are not at the end
                    // if not process
                    if (line.matches("^@end\\s*$")) {
                        //
                        break;
                    }
                    //
                    String[] parts = line.split("\\s*=\\s*");
                    //
                    if (parts.length == 2) {
                        //
                        final String key = parts[0].trim();
                        final String val = parts[1].trim();
                        //
                        props.put(key, val);
                    }
                }
            }
        } finally {
            if (null != br) {
                //
                br.close();
            }
        }
        //
        return props;
    }    
}
