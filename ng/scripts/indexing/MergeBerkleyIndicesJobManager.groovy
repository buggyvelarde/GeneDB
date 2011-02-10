
def usage () {
	println "groovy MergeBerkleyIndicesJobManager.groovy SOURCE_HOME SCRIPT_DIR TO FROM"
}

String SOURCE_HOME = this.args[0];
String SCRIPT_DIR = this.args[1];
String TO = this.args[2];
String FROM = this.args[3];


if (args.length < 4) {
    usage()
    System.exit(101)
}

def queueName = "yesterday";


def boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
unset DISPLAY

cd "${SOURCE_HOME}"

'''

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
    print cpProcess.text
}
    
def cacheClassPath = new File(classPathFileLocation).getText();

execLine="java -server -Djava.awt.headless=true -Xmx3000m  -classpath ${cacheClassPath}  org.genedb.web.mvc.model.MergeBerkeleyIndices -r ${TO} ${FROM} "

String scriptName = "${SCRIPT_DIR}/MERGE.script"
File script = new File(scriptName)
println "Created? ${scriptName} " + script.createNewFile() 
script.write(boilerPlate)
script.append(execLine + '\n')
"chmod 755 ${scriptName}".execute()


def outFileName= "${scriptName}.out"
File outFile = new File(outFileName)
if (outFile.exists()) {
    try {
        outFile.delete
    } catch (groovy.lang.MissingPropertyException mpe) {
        println "WARNING could not delete file: ${outFileName} " + mpe.getProperty() + " - " + mpe.getType() + " - " +  mpe.getMessage() 
    }
}

def errFileName= "${scriptName}.err"
File errFile = new File(errFileName)
if (errFile.exists()) {
    try {
        errFile.delete
    } catch (groovy.lang.MissingPropertyException mpe) {
        println "WARNING could not delete file: ${errFileName} " + mpe.getProperty() + " - " + mpe.getType() + " - " +  mpe.getMessage()
    }
}



def processExecLine = "bsub -q ${queueName} -R 'select[mem>3000] rusage=[mem=3000]' -M 3000000 -o ${outFileName} -e ${errFileName} ${scriptName}"
println processExecLine 
Process p = ["ssh", "pcs4a", processExecLine].execute()
def sout = new StringBuffer()
def serr = new StringBuffer()
p.consumeProcessOutput(sout, serr)
p.waitFor()


println "Merge Job submitted - waiting for it to finish"

boolean finished = false;
boolean worked = false

while (! finished) {
    sleep 300000; // 5*60*1000
	
	if (errFile.exists()) {
		
        if (errFile.size() == 0) {
			worked = true
			println "WORKED The script errFileName has worked"
        } else {
            worked = false
            println "FAILED The script errFileName has failed"
			print errFile.getText();
        }
		
		finished = true;
		
    } else {
        def now = new Date()
		println "\n${now} Waiting for merge job to be finished"
    }
	
}


if (worked) {
    System.exit(0)
}

System.exit(101)
