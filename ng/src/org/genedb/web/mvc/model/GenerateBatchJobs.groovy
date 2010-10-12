import groovy.sql.Sql

def usage () {
	println "groovy -cp /path/to/psql-driver.jar GenerateBatchJobs.groovy [Lucene|DTO] [config] [sourceHome] [baseDirPrefix] org1:org2:org3"
	println "Arguments (in order):"
	println "	Lucene | DTO"
	println "		whether to build lucene indices or DTOs"
	println "	config"
	println "		the name of the ant config to be used"
	println "	sourceName"
	println "		the root folder of the genedb checkout to be used to run the ant tasks"
	println "	baseOutputDir"
	println "		the output folder (in which sub-organism folders will be made)"
	println "	org1:org2:org3"
	println "		a colon separated list of organisms"
}

if (args.length < 4) {
	usage()
	System.exit(101)
}

String action = this.args[0];
String config = this.args[1];

String sourceHome = this.args[2];
String baseOutputDir = this.args[3];

def boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
unset DISPLAY

'''

boilerPlate += 'cd "' +  sourceHome + '"\n'  

def queueName = "yesterday";

String baseDir = "${baseOutputDir}/${action}"
new File("${baseDir}/scripts").mkdirs()
new File("${baseDir}/output").mkdir()

List<String> orgs = new ArrayList<String>()

if (args.length >= 5) {
    orgs = args[4].split(":")

} else {

    def sql = Sql.newInstance("jdbc:postgresql://pgsrv2/nightly", "genedb",
                                      "genedb", "org.postgresql.Driver")

    sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->
        def org = row.common_name
        orgs.add(org)
    }
    sql.close()
}

def classPathFileLocation = "/tmp/genedb-apps.txt"

println "Running ant"

Process cpProcess = ["ant", "-f", "build-apps.xml", "-Dclasspath.outputfile=${classPathFileLocation}", "write-classpath"].execute()
def cpProcessOut = new StringBuffer()
def cpProcessErr = new StringBuffer()
cpProcess.consumeProcessOutput(cpProcessOut, cpProcessErr)
cpProcess.waitFor()

if (cpProcess.exitValue()) {
	println cpProcess.err.text
	System.exit(101)
} else {
	println cpProcess.text
}
	
def cacheClassPath = new File(classPathFileLocation).getText();

println "Using class path:"
println cacheClassPath

def config_slurped = new ConfigSlurper().parse(new File('property-file.${config}' ).toURL())
def dbname = config_slurped.dbname

List jobList = new ArrayList();

for (org in orgs) {

    def antLine

    switch (action) {

    case "Lucene":
        new File("${baseDir}/output/${org}").mkdir()
        // antLine="ant -f build-apps.xml -Dconfig=${config} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _LuceneIndex "
		antLine="java -cp ${cacheClassPath} -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -o ${organism} -i ${baseDir}/output/${org} ${dbname} -server -Djava.awt.headless=true org.genedb.web.mvc.model.PopulateLuceneIndices"
        break;

    case "DTO":
        // antLine="ant -f build-apps.xml -Dconfig=${config} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _PopulateCaches "
		antLine="java -cp ${cacheClassPath} -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -o ${organism} --globalFileRoot ${dir} -server -Djava.awt.headless=true org.genedb.web.mvc.model.PopulateCaches"
        new File("${baseDir}/output/${org}").mkdir()
        break;

    default:
        throw new RuntimeException("Don't know how to run '${action}'")
    }
	
	jobList.add(org)

    String scriptName = "${baseDir}/scripts/${org}.script"
    File script = new File(scriptName)
    print "Created? " + script.createNewFile() + " \n"
    script.write(boilerPlate)
    script.append(antLine + '\n')
    "chmod 755 ${scriptName}".execute()

    try {
       new File(scriptName + ".out").delete
    } catch (groovy.lang.MissingPropertyException mpe) {
        println "\nWARNING could not delete file: " + scriptName + ".out\n"
    }

    try {
       new File(scriptName + ".err").delete
    } catch (groovy.lang.MissingPropertyException mpe) {
       println "\nWARNING could not delete file: " + scriptName + ".err\n"
    }


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
    sleep 30000; // 5 *60*1000

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
