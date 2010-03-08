import groovy.sql.Sql

def myCwd = new File(".").getAbsolutePath()
if (!new File(myCwd, "GenerateBatchJobs.groovy").exists()) {
	println "Can't find script file in current dir. Please cd to the indexing directory first'"
	System.exit(-1)
}
println myCwd;
File srcDirRoot = new File(myCwd, "../..")


def cli = new CliBuilder(usage: 'GenerateBatchJobs.groovy -[dghoq] Lucene|DTO [prefix]')
cli.with {
	h longOpt: 'help', 'Show usage information'
	d longOpt: 'date', args:1, argName: 'date', 'The date after which an organism must have been modified eg 20100219'
	g longOpt: 'genedb', args: 1, argName: 'nickname', 'Nickname of GeneDB instance to connect to'
	o longOpt: 'output', args: 1, argName: 'directory', 'The subdir of /nfs/pathdb/genedb/ to use default: nightly'
	q longOpt: 'queue', args: 1, argName: 'queue', 'The LSF queue to use'
}

def options = cli.parse(args)
if (!options) {
	return
}

if (options.h) {
	cli.usage()
	return
}

// Handle all non-option arguments.
def prefix = ''  // Default is empty prefix.
def method
def extraArguments = options.arguments()
if (extraArguments) {
	method = extraArguments[0]
	// The rest of the arguments belong to the prefix.
	if (extraArguments.size() > 1) {
		prefix = extraArguments[1..-1].join(' ')
	}
}


String boilerPlate = """
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=\${ANT_HOME}/bin:\${JAVA_HOME}/bin:\$PATH
unset DISPLAY
cd "${srcDirRoot.toAbsolutePath()}"
"""

def queueName = options.q ? options.q : "yesterday";
def output = options.o ? options.o : "nightly";

String baseDir = "/nfs/pathdb/genedb/${output}/bulk/${method}"
new File("${baseDir}/scripts").mkdirs()
new File("${baseDir}/output").mkdir()

List<String> orgs = new ArrayList<String>()

if (args.length >= 3) {
    orgs = args[2].split(":")

} else {

    def sql = Sql.newInstance("jdbc:postgresql://pathdbsrv1b/nightly", "pathdb",
                                      "", "org.postgresql.Driver")

    def sqlCommand = "select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'"

	if (options.d) {
		sqlCommand = "select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy' and f.datelastmodified > ${opt.d}"
	}

    sql.eachRow(sqlCommand) { row ->
        orgs.add(row.common_name)
    }
    sql.close()
}

List jobList = new ArrayList();

for (org in orgs) {

    def antLine

    switch (method) {

    case "Lucene":
        new File("${baseDir}/output/${org}").mkdir()
        antLine="ant -Dconfig=${prefix} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _LuceneIndex"
        break;

    case "DTO":
        antLine="ant -Dconfig=${prefix} -Dorganism=${org} -Ddir=${baseDir}/output/${org} _PopulateCaches"
        new File("${baseDir}/output/${org}").mkdir()
        break

    default:
        throw new RuntimeException("Don't know how to run '${method}'")
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
	new File(baseDir, "output/${org}").delete()

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
