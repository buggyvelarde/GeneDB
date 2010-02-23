import groovy.sql.Sql

String boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
unset DISPLAY
cd "/nfs/pathdb/.hudson/jobs/GeneDB Full Nightly Build/workspace/genedb-web"
'''

def queueName = "yesterday";

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
    script.delete()
    script << boilerPlate
    script << antLine + '\n'
    "chmod 755 ${scriptName}".execute()

    new File(scriptName + ".out").delete()
    new File(scriptName + ".err").delete()

    print "${org} "
    Process p = ["ssh", "pcs4a", "bsub -q ${queueName} -M 1179648 -o ${scriptName}.out -e ${scriptName}.err ${scriptName}"].execute()
    def sout = new StringBuffer()
    def serr = new StringBuffer()
    p.consumeProcessOutput(sout, serr)
    p.waitFor()
    //println "Output: ${sout}"
    //println "Error: ${serr}"
}

println "All jobs submitted - waiting for them to finish"

boolean worked = true
def scriptDir = new File(baseDir, "scripts")
def originalJobListSize = jobList.size()
while (jobList.size() > 0) {
    sleep 300000; // 5 *60*1000

    List finishedJobs = new ArrayList()
    List failedJobs = new ArrayList()
    List justFinishedJobs = new ArrayList()
    for (job in jobList) {
        File f = new File("${baseDir}/scripts/${job}.script.err")
        if (f.exists()) {
            if (f.size() == 0) {
                finishedJobs.add(job)
                justFinishedJobs.add("WORKED The script ${baseDir}/scripts/${job}.script has run")
            } else {
                worked = false
                finishedJobs.add(job)
		failedJobs.add(job)
                justFinishedJobs.add("FAILED The script ${baseDir}/scripts/${job}.script.err has failed")
            }
        } else {
            System.err.print(" ${job}")
        }
    }
    System.err.println();
    for (job in justFinishedJobs) {
	System.err.println(job)
    }
    for (job in finishedJobs) {
        jobList.remove(job)
    }
    System.err.println(" \nJobs ${jobList.size()} remaining of ${originalJobListSize}");
}

if (worked) {
    System.exit(0)
}

System.exit(101)
