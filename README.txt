# DocumentMerge Tool

## Overview

This tool allows the merging of revisions within a UCM archives
into a single document.

Th basic usage scenario is as follows:

* Create an archive that will archive out the revisions you
  with to merge. This will obviously include the destination
  documents revisions

* Export the archive - deleting the revisions as a part of pthe
  archive. Please ensure that only one batch file is in the archive.
  I think it will work with more than one, but assume that it doesn't
  till you are able to test explicitly that it doesn't

* Run the tool against the archive batch hda file. This is the file
  that defines the contents of the archived batch. If you wish to merge
  many, more than 1000 say, revisions you may need to use more than one
  archive batch. In that scenario I don't think this tool will as it
  currently exists

* The tool will over-write the archive batch file

* Import the new archive into the content server

* The revisions will be merged into a single document


### Caveats

* I am assuming that *all* the revisions of the destination
  document are archived out - not just some of them. It may
  well work if you only archive some of that out and it may
  equally fail. What ends up happening will be dependent on what
  revisions label etc are present - at least I believe so

* Revision labels: It doesn't preserve revision labels. It
  is unclear, to me at least, how the existing revision labels
  could be preserved. So I have taken the approach of adding new
  ones, numeric and starting from 1 that are assigned to all of
  the merged revisions in the order that they are combined within
  the destination document. An alternative approach could be used
  but we would need to look at the rules carefuly

* Large numbers of revisions: This tool relies on there being
  a single archive batch file, in its current revision. So if you
  have a *lot* of revisions, that span multiple batches, then this won't
  work. But then again combining such a large number of revisions into a
  single document is not a common thing to do

* Java target and source version: These are set within the pom.xml
  file too 1.7. If this is an issue then you can change them. But
  the code may need a re-write (possibly the generics stuff).

## Building

The Java code relies on Maven to build.

### Installing Maven

Maven can be downloaded from the following link:

    https://maven.apache.org/download.cgi

### Build

First install the UCM specific dependencies into your local maven. the included
linux shell script will do this for you. If you are using windows the contents os
the script can be very easily ported to a windows script.

    $ ./install-ucm-jar.sh

The build the code:

    $ mvn assembly:assembly -DdescriptorId=jar-with-dependencies # Will build a jar containing deps - see output for location and name

Or if you are using netbeans, or similar, the pom file can be loaded into
the editor to set the project up for you to build, edit etc.

## Running

Create a file that will hold the order of the revisions. The format of the file
is one revision per line. The revisions at the top of the file will be added to
the document first (will have lowest revisionLabel) and those at the end will
be added last.

Build it, see previous section, and the run as follows:

      $ java -jar target/DocumentMerger-1.0-SNAPSHOT-jar-with-dependencies.jar -batchfile /home/kris/Oracle/Middleware/user_projects/domains/wcc_domain/ucm/cs/archives/merge/15-jul-30_15.02.50_239_01/15211150250~1.hda -orderfile ./order.txt -mergedoc LOCALHOSTLOCAL000001

The above is illustrative only. You will need to update it for the location
of your batch file, revisions order file and destination document name etc.

### Command Line Options

cols: 54
Row 1 : starts at 66
66 + 54 = 120
54 * 5 = 250 + 20 = 270 + 

66 + 108 = 174

