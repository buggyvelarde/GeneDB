package org.genedb.install;

class Derby10 implements Server {
    
    String version = "db-derby-10.2.2.0-lib"
    String archive = ".tar.gz"
    
    void install(AntBuilder ant, String repository, String target, String port) {
        String external = "${target}/external/"
        // Copy file from repository to new home
        ant.untar(src:"${repository}${version}${archive}", dest:"${external}", compression:'gzip')     
    }

}
