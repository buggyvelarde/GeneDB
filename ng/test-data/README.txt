This directory contains HSQL test databases. The easiest way to generate these
is to invoke the ant target rebuild-test-databases, e.g.

 ant rebuild-test-databases -Dsource.url=jdbc:postgresql://localhost:10101/malaria_workshop -Dsource.username=genedb
