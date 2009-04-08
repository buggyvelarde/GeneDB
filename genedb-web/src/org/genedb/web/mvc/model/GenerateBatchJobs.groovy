import groovy.sql.Sql

def boilerPlate = '''
export JAVA_HOME=/software/pathogen/external/applications/java/java6
export ANT_HOME=/software/pathogen/external/applications/ant/apache-ant
export PATH=${ANT_HOME}/bin:${JAVA_HOME}/bin:$PATH
cd /nfs/pathdb/genedb/staging/cvs/genedb-ng/genedb-web
'''

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
	    antLine=""
	    break

	default:
	    throw new RuntimeException("Don't know how to run '${args[0]}'")
	}


	String scriptName = "${baseDir}/scripts/script.${args[0]}.${org}"
	File script = new File(scriptName)
	script << boilerPlate
	script << antLine + '\n'
	"chmod 755 ${scriptName}".execute()

	println " ${org}"
	Process p = ["ssh", "pcs4", "bsub -q yesterday -o ${scriptName}.out -e ${scriptName}.err ${scriptName}"].execute()
	def sout = new StringBuffer()
        def serr = new StringBuffer()
	p.consumeProcessOutput(sout, serr)
	p.waitFor()
	println "Output: ${sout}"
	println "Error: ${serr}"
}

println ""
sql.close()


/*
def STAGING="/nfs/pathdb/genedb/staging"
def PROD="/nfs/pathdb/genedb/snapshot"
int num_features_limit = 200000

def ant = new AntBuilder()


//Check staging db exists and looks sensible


def row = stagingSql.firstRow("select count(*) as c from feature")
int count = row.c
stagingSql.close()
if (count < num_features_limit) {
    println "Not many features in db (${count}) - Aborting"
    System.exit(1)
}

ensureNewDirectory(PROD+"/new")

// Copy indices into near position
copyDir(STAGING+"/cache", PROD+"/new")
copyDir(STAGING+"/lucene", PROD+"/new")


// Deploy webapp to tmp position
//"cd ${STAGING}/cvs/genedb-ng/genedb-web ; ant -Ddeploy=snapshot deploy".execute()

// Copy current files to old
ensureNewDirectory(PROD+"/old")
copyDir(PROD+"/cache",        PROD+"/old")
copyDir(PROD+"/lucene",       PROD+"/old")
copyDir(PROD+"/webapps/ROOT", PROD+"/old")

//System.exit(-1)

// Turn off tomcat
//"/nfs/pathdb/bin/genedb-web-control snapshot stop".execute()

// Switch snapshot and staging dbs
def sql = Sql.newInstance("jdbc:postgresql://pathdbsrv1-dmz:5432/template1", "genedb_rw",
                      args[0], "org.postgresql.Driver")

//sql.execute("alter user genedb_ro nologin")
sql.eachRow("select 'kill ' || procpid from pg_stat_activity where datname = 'snapshot' and usename = 'genedb_ro'") { row2 ->
    println "Gromit likes ${row2}"
}
//sql.execute('drop database "snapshot-old";')
//sql.execute('alter database "snapshot" rename to "snapshot-old";')
//sql.execute('alter database "staging" rename to "snapshot";')
//sql.execute('alter user genedb_ro login')

// Copy files into place
//"mv ${PROD}/cache ${PROD}/cache.delete".execute()
//"mv ${PROD}/new/cache ${PROD}".execute()

//"mv ${PROD}/lucene ${PROD}/lucene.delete".execute()
//"mv ${PROD}/new/lucene ${PROD}".execute()

//"mv ${PROD}/webapps/ROOT ${PROD}/ROOT.delete".execute()
//"mv ${PROD}/new/ROOT ${PROD}/webapps".execute()

// Start tomcat
//"/nfs/pathdb/bin/genedb-web-control snapshot start".execute()

// Delete old directories (but not safety copies in old)
//rmDir(PROD+"/cache.delete")
//rmDir(PROD+"/lucene.delete")
//rmDir(PROD+"/ROOT.delete")


def copyDir(String source, String dest) {
 "cp -r $source $dest".execute()
}

def rmDir(String source) {
 "rm -fr $source".execute()
}

def ensureNewDirectory(String source) {
 File f = new File(source)
 f.delete()
 f.mkdirs()
}
*/