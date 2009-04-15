import groovy.sql.Sql

def boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
unset DISPLAY
cd /nfs/pathdb/genedb/staging/cvs/genedb-ng/genedb-web
'''

def queueName = "normal";

String baseDir = "/nfs/pathdb/genedb/staging/bulk/${args[0]}"
new File("${baseDir}/scripts").mkdirs()
new File("${baseDir}/output").mkdir()


def sql = Sql.newInstance("jdbc:postgresql://pathdbsrv1-dmz:5432/staging", "genedb_ro",
                                  "genedb_ro", "org.postgresql.Driver")

sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->

    def org = row.common_name
    def antLine

    switch (this.args[0]) {

    case "Lucene":
        new File("${baseDir}/output/${org}").mkdir()
        antLine="ant -Ddeploy=${args[1]} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _LuceneIndex"
        break;

    case "DTO":
        antLine="ant -Ddeploy=${args[1]} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _PopulateCaches"
        new File("${baseDir}/output/${org}").mkdir()
        break

    default:
        throw new RuntimeException("Don't know how to run '${args[0]}'")
    }


    String scriptName = "${baseDir}/scripts/script.${args[0]}.${org}"
    File script = new File(scriptName)
    script << boilerPlate
    script << antLine + '\n'
    "chmod 755 ${scriptName}".execute()

    print "${org} "
    Process p = ["ssh", "pcs4", "bsub -q ${queueName} -o ${scriptName}.out -e ${scriptName}.err ${scriptName}"].execute()
    def sout = new StringBuffer()
    def serr = new StringBuffer()
    p.consumeProcessOutput(sout, serr)
    p.waitFor()
    //println "Output: ${sout}"
    //println "Error: ${serr}"
}

println ""
sql.close()
