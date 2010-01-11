package org.genedb.install;

public class Tomcat5 implements Server {
    
    String version = "apache-tomcat-6.0.14"
    String archive = ".tar.gz"
    
    void install(AntBuilder ant, String repository, String target, String port) {
        String external = "${target}/external/"
        String conf = "${external}${version}/conf/server.xml"
        // Copy file from repository to new home
        ant.sequential {
            
            untar(src:"${repository}${version}${archive}", dest:"${external}", compression:'gzip') 
            
       		// Remove unused webapps
       		delete(includeemptydirs:true, quiet:true) {
                fileset(dir:"${external}/${version}/webapps", includes:"**/*")
            }
 
	        // Change port no.s
 	       copy(file:"${conf}", toFile:"${conf}.orig")
        }
        
    	def server = new XmlParser().parse("${conf}")

		server.attributes()['port'] = "" + (port.toInteger() + 1)
		for (connector in server.Service.Connector) {
		    if (connector['@port'] == '8080') {
		        connector.attributes()['port'] = port
		    } else {
		      	connector.parent.children().remove(connector)   
		    }
		}
        
        PrintWriter w = new PrintWriter(new FileWriter("${conf}"))
    	new XmlNodePrinter(w).print(server)
    	w.close()
    }

}
