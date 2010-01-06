package org.genedb.install;

class PostgreSql8 implements DbServer {

    String postgresql = "postgresql-8.3RC2.tar.bz2"
    String version = "postgresql-8.3RC2"
    String archive = ".tar.bz2"

    public void install(AntBuilder ant, String repository, String target, String port) {
        String staging = "${target}/external/build/"
        // Untar file from repository to new home
        ant.sequential {

            untar(src:"${repository}${version}${archive}", dest:"${staging}", compression:'bzip2')

            chmod(file:"${staging}${version}/configure", perm:'744')

            exec(dir:            "${staging}${version}",
                 executable:     "${staging}${version}/configure",
                 resultproperty: 'configResult',
                 append: true,
                 errorproperty:  'configError',
                 output:         "${staging}/postgres-config-log.txt")
                 {
                	arg(line: " --prefix=${target}/external/${version}  --with-pgport=$port")
            	}

            //echo('${configError} - Got here')

            echo("Making ${version} - this may take a few minutes")
            exec(dir:            "${staging}${version}",
                 executable:     "make",
                 resultproperty: 'configResult',
                 append:          true,
                 errorproperty:  'configError',
                 output:         "${staging}/postgres-config-log.txt")

            echo("Installing ${version}")
            exec(dir:            "${staging}${version}",
                    executable:     "make",
                    resultproperty: 'configResult',
                    append:         true,
                    errorproperty:  'configError',
                    output:         "${staging}/postgres-config-log.txt")
                    {
                   	arg(line: "install")
               	}

       		delete(includeemptydirs:true, quiet:true, dir:"${staging}")
        }


        // Run configure
        // Run make
        // Run make install
    }

    void loadSchema() {
        List modules = ("general");
    }

    void loadBootstrapData() {
    }

}
