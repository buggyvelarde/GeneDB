import groovy.sql.Sql

String HOST = "pcs4j"
String PATH = "/lustre/pathogen/blast/website/genedb/"

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

boolean worked = true
for (org in orgs) {

	// PROTEIN Files
    String scriptName = PATH + "GeneDB_" + org + "_Proteins"
    File script = new File(scriptName)
    script.delete()

    File serr = new File("/tmp/stderr."+org + ".txt")
    serr.delete()

    print "${scriptName} : "
    Process p = ["ssh", HOST, "chado_dump_proteins --nostop -o ${org}"].execute()
    def sout = new FileOutputStream(script)
    def serros = new FileOutputStream(serr)
    p.consumeProcessOutput(sout, serros)
    p.waitFor()
    sout.close()
    serros.close()

    if (serr.length() > 70) {
        // Hack as script outputs a progress msg
        println("Looks like we got a problem")
        worked = false
    } else {
        println("OK")
	    serr.delete()
    }

	// Transcript Files
    scriptName = PATH + "GeneDB_" + org + "_Genes"
    script = new File(scriptName)
    script.delete()

    serr = new File("/tmp/stderr.spliced."+org + ".txt")
    serr.delete()

    print "${scriptName} : "
    p = ["ssh", HOST, "chado_dump_transcripts ${org}"].execute()
    sout = new FileOutputStream(script)
    serros = new FileOutputStream(serr)
    p.consumeProcessOutput(sout, serros)
    p.waitFor()
    sout.close()
    serros.close()
    if (serr.length() > 0) {
        println("Looks like we got a problem")
        worked = false
    } else {
        println("OK")
	    serr.delete()
    }


	// Whole genome Files
	scriptName = PATH + "GeneDB_" + org + "_Contigs"
	script = new File(scriptName)
	script.delete()

	serr = new File("/tmp/stderr.contig."+org + ".txt")
	serr.delete()

	print "${scriptName} : "
	p = ["ssh", HOST, "chado_dump_genome -o ${org}"].execute()
	sout = new FileOutputStream(script)
	serros = new FileOutputStream(serr)
	p.consumeProcessOutput(sout, serros)
	p.waitFor()
	sout.close()
	serros.close()
	if (serr.length() > 0) {
		println("Looks like we got a problem")
		worked = false
	} else {
		println("OK")
	    serr.delete()
	}
}


if (worked) {
    System.exit(0)
}

System.exit(101)
