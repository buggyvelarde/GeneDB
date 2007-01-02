package org.genedb.install;



public class Jetty6 implements Server {
    
    String version = "jetty-6.0.2"
    String archive = ".zip"
    
    void install(AntBuilder ant, String repository, String target, String port) {
        String external = "${target}/external/"
        String conf = "${external}${version}/conf/server.xml"
        // Copy file from repository to new home
        ant.sequential {
            unzip(src:"${repository}${version}${archive}", dest:"${external}")
        }
        
    }

}