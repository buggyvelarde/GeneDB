import groovy.sql.Sql
import java.util.regex.Matcher


//To list notes
//select fp.featureprop_id, f.uniquename, fp.rank, fp.value  from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 order by f.uniquename, fp.rank;
def db = Sql.newInstance(
        'jdbc:postgresql://localhost/nightly',
        'art',
        '',
        'org.postgresql.Driver')

// Delete bogus /notes
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id  from feature f, featureprop fp, cvterm c where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=c.cvterm_id and fp.type_id=168340 order by c.name)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id  from feature f, featureprop fp, cvterm c where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=c.cvterm_id and fp.type_id=168338 order by c.name)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id  from feature f, featureprop fp, cvterm c where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=c.cvterm_id and fp.type_id=168342 order by c.name)")

db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like 'InterPro:IPR%' order by f.uniquename, fp.rank)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like 'GPI-Anchor Signal predicted for %' order by f.uniquename, fp.rank)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like '__\\% similar to %' order by f.uniquename, fp.rank)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like '__._\\% similar to %' order by f.uniquename, fp.rank)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like 'identical to __:%' order by f.uniquename, fp.rank)")
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like 'identical to ___:%' order by f.uniquename, fp.rank)");

// Multipart signal peptide
List rows = db.rows(
    "select fp.featureprop_id, fp.rank, f.feature_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value = 'Signal peptide' order by f.uniquename, fp.rank")

rows.each({
    def fpid = it['featureprop_id']
    int rank = it['rank']
    def fid = it['feature_id']

    def nextRow = db.firstRow("select featureprop_id, value from featureprop where feature_id='"+fid+"' and type_id=1672 and rank="+(rank+1))
    if (!(nextRow['value']).startsWith(';query')) {
        throw new RuntimeException("Problem with finding second row for ${fid} ${nextRow}")
    }
    def fpid2 = nextRow['featureprop_id']

    nextRow = db.firstRow("select featureprop_id, value from featureprop where feature_id='"+fid+"' and type_id=1672 and rank="+(rank+2))
    if (!(nextRow['value']).matches('[0-9]\\.[0-9]*')) {
        throw new RuntimeException("Problem with finding third row for ${fid} ${nextRow}")
    }
    def fpid3 = nextRow['featureprop_id']

    nextRow = db.firstRow("select featureprop_id, value from featureprop where feature_id='"+fid+"' and type_id=1672 and rank="+(rank+3))
    if (!(nextRow['value']).matches('[0-9]\\.[0-9]*')) {
        throw new RuntimeException("Problem with finding fourth row for ${fid} ${nextRow}")
    }
    def fpid4 = nextRow['featureprop_id']

    String cmd = "delete from featureprop where featureprop_id in ("+fpid+', '+fpid2+', '+fpid3+', '+fpid4+")"
    db.execute(cmd)
})




// Find multi note signal anchor

rows = db.rows(
"select fp.featureprop_id, fp.rank, f.feature_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value = 'Signal anchor' order by f.uniquename, fp.rank")

rows.each({
    def fpid = it['featureprop_id']
    int rank = it['rank']
    def fid = it['feature_id']

    def nextRow = db.firstRow("select featureprop_id, value from featureprop where feature_id='"+fid+"' and type_id=1672 and rank="+(rank+1))
    if (!(nextRow['value']).matches('[0-9]\\.[0-9]*')) {
        throw new RuntimeException("Problem with finding third row for ${fid} ${nextRow}")
    }
    def fpid3 = nextRow['featureprop_id']

    nextRow = db.firstRow("select featureprop_id, value from featureprop where feature_id='"+fid+"' and type_id=1672 and rank="+(rank+2))
    if (!(nextRow['value']).matches('[0-9]\\.[0-9]*')) {
        throw new RuntimeException("Problem with finding fourth row for ${fid} ${nextRow}")
    }
    def fpid4 = nextRow['featureprop_id']

    String cmd = "delete from featureprop where featureprop_id in ("+fpid+', '+fpid3+', '+fpid4+")"
    db.execute(cmd)
})

// then remaining /query
db.execute("delete from featureprop where featureprop_id in (select fp.featureprop_id from feature f, featureprop fp where f.organism_id=19 and f.type_id=191 and fp.feature_id=f.feature_id and fp.type_id=1672 and fp.value like ';query %-%;description=%;score=%')")

//Stuff in EMBL_qualifier - not for now


//  product with evidence code

rows = db.rows(
        "select f.uniquename, c.cvterm_id, c.name, fc.feature_cvterm_id from feature f, feature_cvterm fc, cvterm c where f.organism_id=19 and f.type_id=191 and fc.feature_id=f.feature_id and fc.cvterm_id=c.cvterm_id and c.cv_id=25 and c.name like 'term=%' order by c.cvterm_id")

for (def row in rows) {
    def cvtid = row['cvterm_id']
    def fcid = row['feature_cvterm_id']
    def term, evidence, dbxref
    List parts = row['name'].split(";")
    for (def part in parts) {
        switch (part) {
        case ~'term=.*':
            term = part.replace("term=","")
            println "Term is '$term'"
            break
        case ~'evidence=.*':
            evidence = part.replace("evidence=","")
            break
        case ~'db_xref=.*':
            dbxref = part.replace("db_xref=","")
            break
        }
    }

    def dbxrefs = db.firstRow("select * from pub where uniquename='"+dbxref+"'")
    int pubId = dbxrefs['pub_id']

    def products = db.firstRow("select cvterm_id from cvterm where name='"+term+"'")
    int num = products['cvterm_id']
    def cmd = "insert into feature_cvtermprop (feature_cvterm_id, type_id, value) values("+fcid+", 26761,'"+evidence+"')"
    try {
        db.executeUpdate(cmd)
    }
    catch (Exception e) {}
    cmd = "update feature_cvterm set cvterm_id="+num+" where feature_cvterm_id="+fcid
    //println cmd
    db.executeUpdate(cmd)
    cmd = "update feature_cvterm set pub_id="+pubId+" where feature_cvterm_id="+fcid
    db.executeUpdate(cmd)
}


// Fix synonyms with current = false
rows = db.rows(
    "select s.name, fs.feature_synonym_id, fs.is_current, s.* from feature f, feature_synonym fs, synonym s where f.organism_id=19 and fs.feature_id=f.feature_id and fs.synonym_id=s.synonym_id and s.name like '%current=false' order by s.name")

for (def row in rows) {
    def fsid = row[1]
    def oldName = row[0]
    def name = oldName.replace(";current=false", "")
    def synonyms = db.firstRow("select synonym_id from synonym where name='"+name+"'")
    int num = synonyms['synonym_id']
    def cmd =  "update feature_synonym set synonym_id="+num+" where feature_synonym_id="+fsid
    db.executeUpdate(cmd)
}

// Copy across dates from old version to new
rows = db.rows("select f1.uniquename, f1.feature_id, f1.timelastmodified, f1.timeaccessioned from feature f1, feature f2, featureloc fl, cvterm c where f1.type_id=c.cvterm_id and c.name in ('mRNA', 'tRNA', 'snRNA', 'pseudogenic_transcript', 'rRNA', 'snoRNA', 'tRNA') and fl.feature_id=f1.feature_id and fl.srcfeature_id=f2.feature_id and f2.uniquename='Tb927_10_v5'")
for (def row in rows) {
    //println row
    def name = row[0]
    def featId =  row[1]
    def cmd = "select f1.uniquename from feature f1, feature f2, featureloc fl where f1.uniquename='"+name+"' and fl.feature_id=f1.feature_id and fl.srcfeature_id=f2.feature_id and f2.uniquename != 'Tb927_10_v5'"
    def match = db.firstRow(cmd)
    if (!match) {
        def matches = db.firstRow("select s.name from synonym s, feature_synonym fs, cvterm c where s.type_id = c.cvterm_id and c.name='previous_systematic_id' and fs.synonym_id=s.synonym_id and fs.feature_id="+featId+" and exists (select 8 from feature where uniquename=s.name)")
        match = matches?.getAt('name')
    }
    if (match) {
        //println "Match found for ${name} ie ${match}"
        cmd = "select f1.timelastmodified, f1.timeaccessioned from feature f1, feature f2, featureloc fl where f1.uniquename='"+match+"' and fl.feature_id=f1.feature_id and fl.srcfeature_id=f2.feature_id and f2.uniquename != 'Tb927_10_v5'"
        def times1 =  db.firstRow(cmd)
        if (times1) {
            Date modified = times1[0]
            Date create = times1[1]
            // We now have the old times, and featId for a transcript, find all parts of gene
            //println featId
            //cmd = "select c.name from cvterm c, feature f1, feature f2, feature_relationship fr where f1.uniquename='"+match+"' and fr.subject_id=f1.feature_id and fr.object_id=f2.feature_id and f2.type_id=c.cvterm_id"
            cmd = "select c.name, f2.uniquename, f2.timelastmodified, f2.timeaccessioned from cvterm c, feature f1, feature f2, feature_relationship fr where f1.uniquename='"+match+"' and fr.object_id=f1.feature_id and fr.subject_id=f2.feature_id and f2.type_id=c.cvterm_id"
            def transcript = db.firstRow(cmd)
            def transcriptName = transcript['uniquename']
            if (modified < transcript['timelastmodified']) {
                //println "Changing modified from ${modified} to ${transcript['timelastmodified']}"
                modified = transcript['timelastmodified']
            }
            if (create > transcript['timeaccessioned']) {
                create = transcript['timeaccessioned']
            }
            cmd = "select c.name, f2.uniquename, f2.timelastmodified, f2.timeaccessioned from cvterm c, feature f1, feature f2, feature_relationship fr where f1.uniquename='"+transcriptName+"' and fr.object_id=f1.feature_id and fr.subject_id=f2.feature_id and f2.type_id=c.cvterm_id"
            def polyp = db.firstRow(cmd)
            if (modified < polyp['timelastmodified']) {
                //println "Changing modified from ${modified} to ${polyp['timelastmodified']}"
                modified = polyp['timelastmodified']
            }
            if (create > polyp['timeaccessioned']) {
                create = polp['timeaccessioned']
            }
            println "${match} ${transcriptName} ${polyp['uniquename']}"
            db.execute("update feature set timelastmodified='"+modified+"' where feature_id='"+featId+"'")
            db.execute("update feature set timeaccessioned='"+create+"' where feature_id='"+featId+"'")
        }
    } else {
        //println "No match found for ${name}"
    }

}
println "Finished"
