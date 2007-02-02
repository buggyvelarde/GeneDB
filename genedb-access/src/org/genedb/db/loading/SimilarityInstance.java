package org.genedb.db.loading;

public class SimilarityInstance {
	
	private String algorithm;
	
	private String priDatabase;
	
	private String secDatabase;
	
	private String organism;
	
	private String product;
	
	private String gene;
	
	private String length;
	
	private String id;
	
	private String ungappedId;
	
	private String evalue;
	
	private String score;
	
	private String overlap;
	
	private String query;
	
	private String subject;

	public String getOverlap() {
		return overlap;
	}

	public void setOverlap(String overlap) {
		this.overlap = overlap;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getUngappedId() {
		return ungappedId;
	}

	public void setUngappedId(String ungappedId) {
		this.ungappedId = ungappedId;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getEvalue() {
		return evalue;
	}

	public void setEvalue(String evalue) {
		this.evalue = evalue;
	}

	public String getGene() {
		return gene;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getOrganism() {
		return organism;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public String getPriDatabase() {
		return priDatabase;
	}

	public void setPriDatabase(String priDatabase) {
		this.priDatabase = priDatabase;
	}

	public String getSecDatabase() {
		return secDatabase;
	}

	public void setSecDatabase(String secDatabase) {
		this.secDatabase = secDatabase;
	}
	
	
}
