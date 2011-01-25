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

props = new java.util.Properties()
props.load(new FileInputStream("property-file.${config}"))
def dbname = props.getProperty('dbname')


List<String> orgs = new ArrayList<String>()

if (args.length >= 5) {
    orgs = args[4].split(":")

} else {
	
	def dbport = props.getProperty('dbport')
	def dbhost = props.getProperty('dbhost')
	def dbuser = props.getProperty('dbuser')
	def dbpassword = props.getProperty('dbpassword')

    def sql = Sql.newInstance("jdbc:postgresql://${dbhost}:${dbport}/${dbname}", "${dbuser}",
                                      "${dbpassword}", "org.postgresql.Driver")

    sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->
        def org = row.common_name
        orgs.add(org)
    }
    sql.close()
}

def classPathFileLocation = "/tmp/genedb-apps.txt"

Process cpProcess = ["ant", "-f", "build-apps.xml", "-Dclasspath.outputfile=${classPathFileLocation}", "write-classpath"].execute()
def cpProcessOut = new StringBuffer()
def cpProcessErr = new StringBuffer()
cpProcess.consumeProcessOutput(cpProcessOut, cpProcessErr)
cpProcess.waitFor()

if (cpProcess.exitValue()) {
	println cpProcess.err.text
	System.exit(101)
} else {
	//print cpProcess.text
}
	
def cacheClassPath = new File(classPathFileLocation).getText();


List jobList = new ArrayList();

for (org in orgs) {

    def execLine

    switch (action) {

    case "Lucene":
        new File("${baseDir}/output/${org}").mkdir()
        // execLine="ant -f build-apps.xml -Dconfig=${config} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _LuceneIndex "
		execLine="java -server -Djava.awt.headless=true  -Xmx2000m  -classpath ${cacheClassPath}  org.genedb.web.mvc.model.PopulateLuceneIndices -o ${org} -i ${baseDir}/output/${org} ${dbname} "
        break;

    case "DTO":
        // execLine="ant -f build-apps.xml -Dconfig=${config} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _PopulateCaches "
		// -XX:+HeapDumpOnOutOfMemoryError
		execLine="java -server -Djava.awt.headless=true  -Xmx2000m -classpath ${cacheClassPath} org.genedb.web.mvc.model.PopulateCaches -o ${org} --globalFileRoot ${baseDir}/output/${org} "
        new File("${baseDir}/output/${org}").mkdir()
        break;

    default:
        throw new RuntimeException("Don't know how to run '${action}'")
    }
	
	jobList.add(org)

    String scriptName = "${baseDir}/scripts/${org}.script"
    File script = new File(scriptName)
    //println "Created? " + script.createNewFile() 
    script.write(boilerPlate)
    script.append(execLine + '\n')
    "chmod 755 ${scriptName}".execute()
	
	def outFileName= "${scriptName}.out"
	File outFile = new File(outFileName)
	if (outFile.exists()) {
		try {
			outFile.delete
	    } catch (groovy.lang.MissingPropertyException mpe) {
	        println "WARNING could not delete file: ${outFileName} " + mpe.getProperty() + " - " + mpe.getType() + " - " + 	getMessage() 
	    }
	}
	
	def errFileName= "${scriptName}.err"
	File errFile = new File(errFileName)
	if (errFile.exists()) {
		try {
			errFile.delete
		} catch (groovy.lang.MissingPropertyException mpe) {
			println "WARNING could not delete file: ${errFileName} " + mpe.getProperty() + " - " + mpe.getType() + " - " + 	getMessage()
		}
	}
	
	
    Process p = ["ssh", "pcs4a", "bsub -q ${queueName} -M 2000000 -o ${outFileName} -e ${errFileName} ${scriptName}"].execute()
    def sout = new StringBuffer()
    def serr = new StringBuffer()
    p.consumeProcessOutput(sout, serr)
    p.waitFor()
	
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
	def now = new Date()
    System.err.println(" \n${now} Jobs ${jobList.size()} remaining of ${originalJobListSize}");
}

if (worked) {
    System.exit(0)
}

System.exit(101)
