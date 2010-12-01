package org.genedb.misc.remapper

import groovy.sql.Sql


public class RemappedContigLoader {

    def db
    def organismId
    def typeId = 427

    RemappedContigLoader(def organismId) {
        db = Sql.newInstance(
            'jdbc:postgresql://pgsrv1:5432/pathogens',
            'pathdb',
            'LongJ!@n',
            'org.postgresql.Driver')
        this.organismId = organismId
    }

    public String loadSequence(String sequence) {
        // Read new chromsome sequence
        File seq = new File(sequence)
        StringBuilder newSeq = new StringBuilder()
        seq.eachLine({
//            println it
            if (!it.startsWith(">")) {
	       newSeq.append(it)
//	       println it
	    }
        })
        String munged = newSeq.toString()
        //return newSeq.toString()
//	println munged


        String cmd = "insert into feature(uniquename, type_id, organism_id, residues, seqlen) values('${sequence}__new', $typeId, $organismId, '${munged}', ${munged.length()})";
//        println cmd
        db.executeUpdate(cmd)

	cmd = "select c.cvterm_id from cvterm c, cv cv where c.name='top_level_seq' and c.cv_id=cv.cv_id and cv.name='genedb_misc'"
        def row = db.firstRow(cmd)
	def cvtermId = row[0]

	cmd = "select feature_id from feature where uniquename='${sequence}__new'"
        row = db.firstRow(cmd)
        def featureId = row[0]


	cmd = "insert into featureprop(feature_id, type_id) values(${featureId}, ${cvtermId})"
        db.executeUpdate(cmd)
	

    }

    private static void usage() {
      println "RemappedContigLoader organism_id contigName1 contigName2"
    }

    public static void main(String[] args) {
      if (args.length < 2) {
        usage()
        System.exit(3)
      }

      RemappedContigLoader app = new RemappedContigLoader(args[0])

      for (String arg in args[1 .. args.length-1]) {
          app.loadSequence(arg)
      }

    }

}
