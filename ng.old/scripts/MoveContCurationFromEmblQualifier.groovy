import groovy.sql.Sql
import java.util.regex.Matcher


//To list notes
//select fp.featureprop_id, f.uniquename, fp.rank, fp.value  from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 order by f.uniquename, fp.rank;
def db = Sql.newInstance(
        'jdbc:postgresql://localhost/nightly',
        'art',
        '',
        'org.postgresql.Driver')


def ts = [
'RNAi: normal cell differentiation during bloodstream stage',
'RNAi: normal growth in procyclic form',
'RNAi: normal cell morphogenesis during bloodstream stage',
'RNAi: normal cell cycle during bloodstream stage',
'RNAi: normal mitochondrion organization and biogenesis during bloodstream stage',
'RNAi: normal golgi organization and biogenesis during bloodstream stage',
'RNAi: normal endocytosis during bloodstream stage',
'RNAi: normal regulation of cell motility during bloodstream stage',
'RNAi: peptidase activity absent in procyclic form',
'RNAi: lethal  during bloodstream stage',
'RNAi: normal growth during bloodstream stage',
'RNAi phenotype: lethal in procyclic',
'RNAi phenotype: essential in procyclic'
].each({
    createCvTerm(it, db)
})

//  EMBL_qualifier with /controlled_curation
rows = db.rows(
        "select f.feature_id, fp.value, fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=47726 and fp.value like '/controlled_curation=%'")

for (def row in rows) {
    //println row
    def featureId = row['feature_id']
    def fprop = row['featureprop_id']
    def term, evidence, dbxref, date, cv
    def line = row['value'].replace("/controlled_curation=", "")
    println line
    if (line.startsWith('"')) {
        line = line.substring(1)
    }
    if (line.endsWith('"')) {
        line = line.substring(0, line.length()-1)
    }
    println line
    List parts = line.split(";")
    for (def part in parts) {
        switch (part) {
        case ~'term=.*':
            term = part.replace("term=","")
            break
        case ~'evidence=.*':
            evidence = part.replace("evidence=","")
            break
        case ~'db_xref=.*':
            dbxref = part.replace("db_xref=","")
            break
        case ~'date=.*':
            date = part.replace("date=","")
            break
        case ~'cv=.*':
            cv = part.replace("cv=","")
            break
        }
    }

    def cmd = "select c.cvterm_id from cvterm c, cv cv where c.name='"+term+"' and c.cv_id=cv.cv_id and cv.name='"+cv+"'"
    println cmd
    def cvterms = db.firstRow(cmd)
    def cvtermId = cvterms['cvterm_id']

    println cvtermId

    def dbxrefs = db.firstRow("select * from pub where uniquename='"+dbxref+"'")
    int pubId = dbxrefs['pub_id']
    //println pubId

    def products = db.firstRow("select cvterm_id from cvterm where name='"+term+"'")
    int num = products['cvterm_id']

    // Create a feature_cvterm
    try {
        cmd = "insert into feature_cvterm (feature_id, cvterm_id, pub_id) values(${featureId}, ${cvtermId}, ${pubId})"
        println cmd
        db.execute(cmd)
    }
    catch (Exception exp) {
        exp.printStackTrace()
    }

    // Find id of feature_cvterm just added
    int fcid = db.firstRow("select feature_cvterm_id from feature_cvterm where feature_id="+featureId+" and cvterm_id="+cvtermId+" and pub_id="+pubId)['feature_cvterm_id']

    // Add evidence
    if (evidence) {
        cmd = "insert into feature_cvtermprop (feature_cvterm_id, type_id, value) values("+fcid+", 26761,'"+evidence+"')"
        db.executeUpdate(cmd)
        }
    if (date) {
        cmd = "insert into feature_cvtermprop (feature_cvterm_id, type_id, value) values("+fcid+", 1697,'"+date+"')"
        db.executeUpdate(cmd)
    }

    cmd = "delete from featureprop where featureprop_id="+fprop
    db.executeUpdate(cmd)

}

println "Finished"


void createCvTerm(String t, def db) {
    if (!db.firstRow("select c.* from cvterm c, cv cv where c.name='"+t+"' and c.cv_id = cv.cv_id and cv.name='CC_genedb_controlledcuration'")) {
        db.execute("insert into dbxref (db_id, accession) values(1, '"+t+"')")
        int dbxrefId = db.firstRow("select dbxref_id from dbxref where accession ='"+t+"'")['dbxref_id']
        db.execute("insert into cvterm (cv_id, name, dbxref_id) values(20, '"+t+"', "+dbxrefId+")")
    }
}
