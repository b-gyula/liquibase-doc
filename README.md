# doc generator

This directory contains source code for java program that will generate all the documentation for the
various change types that liquibase supports. These are found in this github project at 
`liquibase.github.com/documentation/changes/`

To re-build the doc generator and run it, just run `mvn clean package` in this directory. You may want 
to update the POM so that it uses the version of liquibase you want to document. 

It will create `liquibase-docgenerator-1.0.0.local-SNAPSHOT.jar` in the root of the project. Switch to
that directory and then run `java -jar liquibase-docgenerator-1.0.0.local-SNAPSHOT.jar` to execute.

Running the `generateChangeDocs.sh` in the root of the project accomplishes both of the steps above in
one swell foop.

Check the diffs before comitting the changes. 
