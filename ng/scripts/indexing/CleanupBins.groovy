import groovy.sql.Sql

class Feature {
	Integer feature_id
	String uniquename
	String type
	Integer fmin
	Integer fmax
	Boolean is_obsolete
	String phase
	int strand
	String residues
}

class Cvterm {
	Integer cvterm_id
	String name
}

def getFeature(String uniquename, Sql sql) {
	Feature feature = sql.firstRow("""
		SELECT f.uniquename, f.feature_id, f.residues, type.name as type
		FROM feature f 
		JOIN cvterm type ON f.type_id = type.cvterm_id
		WHERE f.uniquename = ${uniquename} 
	""")
	return feature
}

def getBinContigs(Feature bin, sql) {
	def contigs = []
	sql.eachRow("""
		SELECT
			f.feature_id,
			f.uniqueName, 
			type.name as type, 
			fl.fmin, 
			fl.fmax, 
			f.is_obsolete
			
		FROM feature f
		
			JOIN cvterm type ON f.type_id = type.cvterm_id AND type.name = 'contig'
			JOIN featureloc fl ON (f.feature_id = fl.feature_id AND fl.srcfeature_id = (select feature_id from feature where uniqueName = ${bin.uniquename} ) )
		
		ORDER BY fl.fmin, fl.fmax;
	
	""") { row ->
				Feature contig = new Feature( row.toRowResult() )
				contigs << contig
			}
	return contigs
}

def getFeaturesSpanningTheInsidesOfAContig(Feature bin, Feature contig, sql) {
	def features = []
	
	
	sql.eachRow("""
		SELECT f.feature_id, f.uniqueName, type.name as type, fl.phase, fl.strand , fl.fmin, fl.fmax
		FROM feature f 
			JOIN featureloc fl ON f.feature_id = fl.feature_id AND fl.srcfeature_id = ${bin.feature_id}
			JOIN cvterm type ON f.type_id = type.cvterm_id
		WHERE fl.fmin >= ${contig.fmin} AND fl.fmax <= ${contig.fmax}
		AND f.uniqueName != ${contig.uniquename}

	""") { row ->
		Feature feature = new Feature( row.toRowResult() )
		features << feature
	}
	return features
}

def Cvterm getTopLevelFeatureCvtermId(Sql sql) {
	Cvterm cvterm = sql.firstRow("select cvterm_id from cvterm join cv using (cv_id) where cv.name = 'genedb_misc' and cvterm.name = 'top_level_seq'")
	return cvterm
}


def promoteContigToTopLevel (Feature bin, Feature feature, Cvterm topLevelType, Sql sql ) {
	
	def residues = bin.residues.substring(feature.fmin, feature.fmax)
	
	sql.execute("""
		UPDATE feature set residues = ${residues}
			WHERE feature_id = ${feature.feature_id}
	""")
	
	
	def rows = sql.rows("""
		SELECT f.feature_id 
		FROM feature f 
		JOIN featureprop fp on fp.feature_id = f.feature_id 
			AND fp.type_id = ${topLevelType.cvterm_id} 
			AND f.feature_id = ${feature.feature_id}
	""")
	
	println "Rows : ${rows}"
	
	if (rows.size() > 0) {
		println "${feature.uniquename} already is a top level feature"		 
	} else {
		e = """
			INSERT INTO featureprop (feature_id, type_id, value)
				VALUES ( ${feature.feature_id}, ${topLevelType.cvterm_id}, 'true')
		""" 
		sql.execute("""
			INSERT INTO featureprop (feature_id, type_id, value)
				VALUES ( ${feature.feature_id}, ${topLevelType.cvterm_id}, 'true')
		""")
	}
	
}

def relocateFeatureToContig(Feature bin, Feature contig, Feature feature, Sql sql) {
	
	Integer featureloc_id = sql.firstRow("""
		SELECT fl.featureloc_id, f.feature_id, f.uniqueName
		FROM feature f 
			JOIN featureloc fl ON f.feature_id = fl.feature_id AND fl.srcfeature_id = ${bin.feature_id}
		WHERE f.uniquename = ${feature.uniquename} 
	""").featureloc_id
	
	if (featureloc_id == null) {
		throw new RuntimeException("The featureloc is null for ${feature.uniquename} on ${bin.uniquename} ")
	}
	
	
	sql.execute("""
		DELETE FROM featureloc where featureloc_id = ${featureloc_id}
	""")
	
	def newFmin = feature.fmin - contig.fmin
	def newFmax = feature.fmax - contig.fmin
	println "${contig.feature_id}, ${feature.feature_id}, ${newFmin}, ${newFmax}, ${feature.phase}, ${feature.strand}"
	sql.execute("""
		INSERT INTO featureloc (srcfeature_id, feature_id, fmin, fmax, phase, strand) VALUES
			(${contig.feature_id}, ${feature.feature_id}, ${newFmin}, ${newFmax}, ${feature.phase}, ${feature.strand})
	""")
		
	
}

String config = this.args[0]
String bin = this.args[1]

Boolean debug = false

Properties props = new java.util.Properties()
props.load(new FileInputStream("property-file.${config}"))

def dbname = props.getProperty('dbname')
def dbport = props.getProperty('dbport')
def dbhost = props.getProperty('dbhost')
def dbuser = props.getProperty('dbuser')
def dbpassword = props.getProperty('dbpassword')

Sql sql = Sql.newInstance(
	"jdbc:postgresql://${dbhost}:${dbport}/${dbname}",
	"${dbuser}",
	"${dbpassword}",
	"org.postgresql.Driver")

try {
	
	sql.withTransaction {
	
		Feature binFeature = getFeature(bin, sql)
		
		if (binFeature == null) {
			println "${bin} does not exist"
			System.exit(1)
		} 
		
		if (binFeature.type != "chromosome") {
			println "${bin} is not a chromosome, it's a ${binFeature.type}"
			System.exit(1)
		}
		
		
		Cvterm topLevelType = getTopLevelFeatureCvtermId(sql)
		println topLevelType.cvterm_id
		
		def contigs = getBinContigs(binFeature, sql)
		
		for (Feature contig in contigs) {
			
			promoteContigToTopLevel(binFeature, contig, topLevelType, sql)
			
			println ">> ${contig.uniquename} ${contig.feature_id} "
			
			def features = getFeaturesSpanningTheInsidesOfAContig(binFeature, contig, sql)
			for (Feature feature : features ) {
				println " >>>> ${feature.uniquename} ${feature.type}"
				relocateFeatureToContig(binFeature, contig, feature, sql)
			}
		}
		
		if (debug) {
			println "Rolling back ..."
			sql.rollback()
		}
	}
} finally {
	if (sql != null) {
		sql.close()
	}
}



