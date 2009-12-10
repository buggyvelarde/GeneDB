import groovy.sql.Sql

//String baseDir = "/nfs/pathdb/genedb/nightly/bulk/${args[0]}"
//new File("${baseDir}/scripts").mkdirs()
//new File("${baseDir}/output").mkdir()

List<String> orgs = new ArrayList<String>()

if (args.length >= 1) {
    orgs = args[0].split(":")

} else {

    def sql = Sql.newInstance("jdbc:postgresql://pathdbsrv1b/nightly", "pathdb",
                                      "genedb", "org.postgresql.Driver")

    sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->
        def org = row.common_name
        orgs.add(org)
    }
    sql.close()
}

for (org in orgs) {

    //new File("${baseDir}/output/${org}").mkdir()

    String scriptName = "/tmp/" + org + ".prot.txt"
    File script = new File(scriptName)

	File serr = new File("/tmp/stderr")
	serr.delete()

    print "${org} "
    Process p = ["ssh", "pcs4j", "chado_dump_proteins --nostop ${org}"].execute()
    def sout = new FileOutputStream(script)
    def serros = new FileOutputStream(errorOutput)
    p.consumeProcessOutput(sout, serros)
    p.waitFor()
    //println "Output: ${sout}"
    //println "Error: ${serr}"
	if (serr.size > 0) {
		println("Looks like we got a problem")
	}

//    Process p = ["ssh", "pcs4j", "chado_dump_transcripts ${org}"].execute()
//    def sout = new StringBuffer()
//    def serr = new StringBuffer()
//    p.consumeProcessOutput(sout, serr)
//    p.waitFor()
//    println "Output: ${sout}"
//    println "Error: ${serr}"
}

println "All jobs submitted - waiting for them to finish"

boolean worked = true


if (worked) {
    System.exit(0)
}

System.exit(101)
