import groovy.sql.Sql

//String HOST = "pathdbsrv1a"
String PATH = "/lustre/scratch101/blastdb/Pathogen/website/genedb/"
//String PATH="/nfs/pathdb/tmp/blast-ng/"

List<String> orgs = new ArrayList<String>()

if (args.length >= 1) {
    orgs = args[0].split(":")

} else {

    def sql = Sql.newInstance("jdbc:postgresql://pgsrv2/nightly", "genedb",
                                      "genedb", "org.postgresql.Driver")

    sql.eachRow("select distinct(o.common_name) from organism o, feature f where f.organism_id = o.organism_id and o.common_name != 'dummy'") { row ->
        def org = row.common_name
        orgs.add(org)
    }
    sql.close()
}

boolean worked = true
for (org in orgs) {

	// PROTEIN Files
    String scriptName = PATH + "GeneDB_" + org + "_Proteins"
    File script = new File(scriptName)
    script.delete()

    File serr = new File("/tmp/stderr."+org + ".txt")
    serr.delete()

    print "${scriptName} : "
    Process p = ["chado_dump_proteins -s -o ${org}"].execute()
    def sout = new FileOutputStream(script)
    def serros = new FileOutputStream(serr)
    p.consumeProcessOutput(sout, serros)
    p.waitFor()
    sout.close()
    serros.close()

    if (serr.length() > 70) {
        // Hack as script outputs a progress msg
        println("Looks like we got a problem")
		println(serr.text)
        worked = false
    } else {
        println("OK")
	    //serr.delete()
    }

	// Transcript Files
    scriptName = PATH + "GeneDB_" + org + "_Genes"
    script = new File(scriptName)
    script.delete()

    serr = new File("/tmp/stderr.spliced."+org + ".txt")
    serr.delete()

    print "${scriptName} : "
    p = ["chado_dump_transcripts ${org}"].execute()
    sout = new FileOutputStream(script)
    serros = new FileOutputStream(serr)
    p.consumeProcessOutput(sout, serros)
    p.waitFor()
    sout.close()
    serros.close()
    if (serr.length() > 0) {
        println("Looks like we got a problem")
		println(serr.text)
        worked = false
    } else {
        println("OK")
	    //serr.delete()
    }


	// Whole genome Files
	scriptName = PATH + "GeneDB_" + org + "_Contigs"
	script = new File(scriptName)
	script.delete()

	serr = new File("/tmp/stderr.contig."+org + ".txt")
	serr.delete()

	print "${scriptName} : "
 	p = ["chado_dump_genome -o ${org}"].execute()
	sout = new FileOutputStream(script)
	serros = new FileOutputStream(serr)
	p.consumeProcessOutput(sout, serros)
	p.waitFor()
	sout.close()
	serros.close()
	if (serr.length() > 0) {
		println("Looks like we got a problem")
		println(serr.text)
		worked = false
	} else {
		println("OK")
	    //serr.delete()
	}
}


if (worked) {
    System.exit(0)
}

System.exit(101)
