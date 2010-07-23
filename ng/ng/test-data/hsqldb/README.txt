This directory contains HSQL test databases. The easiest way to generate these
is to invoke the ant target rebuild-test-databases, e.g.

 ant rebuild-test-databases -Dsource.url=jdbc:postgresql://pgsrv2.internal.sanger.ac.uk:5433/pathclone -Dsource.username=genedb
