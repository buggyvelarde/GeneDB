package org.genedb.install;

class Installer {

    //String root = '/software/pathogen/projects/genedb/tmp/'
    String root = "/Users/art/Scratch/genedb-installer/"
    String repository = "${root}/downloaded/"
    String target = "${root}/test"
    
    def ant = new AntBuilder()
    
  static void main(args) {
        Installer installer = new Installer()
    	installer.install()
  }

  private void install() {
      
      ant.delete(includeemptydirs:true, quiet:true) {
          fileset(dir:"${target}/external")
      }
      
      Server servletEngine = new Tomcat5()
      //Server servletEngine = new Jetty6()
      servletEngine.install(ant, repository, target, '9005')
      
      //DbServer db = new PostgreSql8()
      DbServer db = new Derby10()
      db.install(ant, repository, target, '9007')

      prepareJars(ant)
      
  	 prepareGmod()
  	 
  	 db.loadSchema()
  }
  
  void prepareJars(AntBuilder ant) {
//      ant.sequential {
//          cvs(command:  	   "checkout",
//                  cvsRoot: 	   "the CVSROOT variable",
//                  cvsRsh: 	   "the CVS_RSH variable",
//                  dest: 	   "the directory where the checked out files should be placed",
//                  'package':   "schema",
//                  quiet:       false,
//                  failonerror: true
//      )}   
  }
  
}
