import groovy.sql.Sql

def boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
unset DISPLAY
cd "/nfs/pathdb/.hudson/jobs/GeneDB Full Nightly Build/workspace/genedb-ng/genedb-web"
'''

def queueName = "basement";

String baseDir = "/nfs/pathdb/genedb/nightly/bulk/${args[0]}"
new File("${baseDir}/scripts").mkdirs()
new File("${baseDir}/output").mkdir()

List<String> orgs = new ArrayList<String>()

if (args.length >= 3) {
    orgs = args[2].split(":")

} else {

    def sql = Sql.newInstance("jdbc:postgresql://pgsrv2/nightly", "genedb",
                                      "genedb", "org.postgresql.Driver")

    sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->
        def org = row.common_name
        orgs.add(org)
    }
    sql.close()
}

List jobList = new ArrayList();

for (org in orgs) {

    def antLine

    switch (this.args[0]) {

    case "Lucene":
        new File("${baseDir}/output/${org}").mkdir()
        antLine="ant -Dconfig=${args[1]} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _LuceneIndex"
        break;

    case "DTO":
        antLine="ant -Dconfig=${args[1]} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _PopulateCaches"
        new File("${baseDir}/output/${org}").mkdir()
        break

    default:
        throw new RuntimeException("Don't know how to run '${args[0]}'")
    }

    jobList.add(org)

    String scriptName = "${baseDir}/scripts/${org}.script"
    File script = new File(scriptName)
    script << boilerPlate
    script << antLine + '\n'
    "chmod 755 ${scriptName}".execute()

    print "${org} "
    //Process p = ["ssh", "pcs4a", "bsub -q ${queueName} -M 786432 -o ${scriptName}.out -e ${scriptName}.err ${scriptName}"].execute()
    //def sout = new StringBuffer()
    //def serr = new StringBuffer()
    //p.consumeProcessOutput(sout, serr)
    //p.waitFor()
    //println "Output: ${sout}"
    //println "Error: ${serr}"
}

println "All jobs submitted - waiting for them to finish"

boolean worked = true
def scriptDir = new File(baseDir, "scripts")
while (jobList.size() > 0) {
    sleep 5*60;
    
    List finishedJobs = new ArrayList()
    for (job in jobList) {
        File f = new File("${baseDir}/scripts/${job}.script.err")
        if (f.exists()) {
            if (f.size() == 0) {
                finishedJobs.add(job)
                println("WORKED The script ${baseDir}/scripts/${job}.script has run")
            } else {
                worked = false
                println("FAILED The script ${baseDir}/scripts/${job}.script.err has failed")
            }
        } else {
            System.err.println("Still waiting on ${job}")
        }
    } 
    for (job in finishedJobs) {
        jobList.remove(job)
    }
}

if (worked) {
    System.exit(0)
}

System.exit(101)
