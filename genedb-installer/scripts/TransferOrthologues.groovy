import groovy.sql.Sql
import java.util.regex.Matcher


def db = Sql.newInstance(
        'jdbc:postgresql://localhost/nightly',
        'art',
        '',
        'org.postgresql.Driver')


def newContig = "Tb927_10_v5"

// Go through new contig
List rows = db.rows("select f1.uniquename, f1.feature_id from feature f1, feature f2, featureloc fl, cvterm c where f2.uniquename='"+newContig+"' and fl.srcfeature_id=f2.feature_id and fl.feature_id=f1.feature_id and f1.type_id=c.cvterm_id and c.name in ('polypeptide')")

rows.each({ main ->
    def pepId = main['feature_id']
    def pepName = main['uniquename']
    //println pepId
    def transcriptId = db.firstRow("select f1.feature_id from feature f1, feature f2, feature_relationship fr, cvterm c where f2.feature_id=fr.subject_id and f1.feature_id=fr.object_id and fr.type_id=c.cvterm_id and c.name='derives_from' and f2.feature_id="+pepId)['feature_id']
    def gene = db.firstRow("select f1.feature_id, f1.uniquename from feature f1, feature f2, feature_relationship fr, cvterm c where f2.feature_id=fr.subject_id and f1.feature_id=fr.object_id and fr.type_id=c.cvterm_id and c.name='part_of' and f2.feature_id="+transcriptId)
    def geneName = gene['uniquename']

    // Look up by name everywhere else
    def match
    // Look up by previous sys_id
    def try2 = db.rows("select s.name from feature f, feature_synonym fs, synonym s, cvterm c where f.feature_id in ("+gene['feature_id']+", "+transcriptId+") and fs.feature_id=f.feature_id and fs.synonym_id=s.synonym_id and s.type_id = c.cvterm_id and c.name='previous_systematic_id'")
    //println try2
    try2.each({ tr ->
        def altName = tr['name']
        // Get feature id of match - and descend heirachy probably :-(
        def altGenes = db.firstRow("select feature_id from feature where uniquename='"+altName+"'")
        if (altGenes) {
            altGeneId = altGenes['feature_id']
            def altTranscriptId = db.firstRow("select f2.feature_id from feature f1, feature f2, feature_relationship fr, cvterm c where f2.feature_id=fr.subject_id and f1.feature_id=fr.object_id and fr.type_id=c.cvterm_id and c.name='part_of' and f1.feature_id="+altGeneId)['feature_id']
            def altPeptide = db.firstRow("select f2.feature_id from feature f1, feature f2, feature_relationship fr, cvterm c where f2.feature_id=fr.subject_id and f1.feature_id=fr.object_id and fr.type_id=c.cvterm_id and c.name='derives_from' and f1.feature_id="+altTranscriptId)
            if (altPeptide) {
                def altPeptideId = altPeptide['feature_id']
                println "Peptide: ${pepName} gene: ${geneName} -> gene: ${altName} ${altTranscriptId} ${altPeptideId}"
                // Look thru' feature_relationship where this is the subject_id orthologous_to a polypeptide
                db.rows("select fr.* from feature_relationship fr, cvterm c, feature f where fr.type_id=c.cvterm_id and c.name='orthologous_to' and "+altPeptideId+"=fr.subject_id and fr.object_id=f.feature_id and f.type_id=191")?.each({ row1 ->
                    if (row1['subject_id'] != altPeptideId) {
                        throw new RuntimeException();
                    }
                    println row1
                    // Once found, transfer any orthologues
                    db.execute("update feature_relationship set subject_id="+pepId+" where feature_relationship_id="+row['feature_relationship_id'])
                })
                // Repeat for reverse ie is the object
                db.rows("select fr.* from feature_relationship fr, cvterm c, feature f where fr.type_id=c.cvterm_id and c.name='orthologous_to' and "+altPeptideId+"=fr.object_id and fr.subject_id=f.feature_id and f.type_id=191")?.each({ row2 ->
                    if (row2['object_id'] != altPeptideId) {
                        throw new RuntimeException();
                    }
                    println row2
                    // Once found, transfer any orthologues
                    db.execute("update feature_relationship set object_id="+pepId+" where feature_relationship_id="+row['feature_relationship_id'])
                })
            } else {
                println "No peptide ${pepName} ${geneName} -> ${altName} ${altTranscriptId}"
            }
        } else {
            //println "No matching gene"
        }
    })

})
