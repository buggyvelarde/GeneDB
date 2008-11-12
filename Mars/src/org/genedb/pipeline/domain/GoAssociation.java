package org.genedb.pipeline.domain;

import java.util.Date;
import java.util.Set;

public class GoAssociation {

	String dbName; // 	required 	SGD
	String accession; // required 	S000000296
	String primaryName; // 	required 	PHO3
	Set<String> qualifiers; //	optional 	NOT
	String goAccession; //  	required 	GO:0003993
	Set<String> dbReference; // DB:Reference (|DB:Reference) 	required 	SGD_REF:S000047763|PMID:2676709
	String evidenceCode; // required 	IMP
	String withFrom; //	optional 	GO:0000346
	GoCategory goCategory;// 	required 	F
	String description; // 	optional 	acid phosphatase
	Set<String> synonyms; //  (|Synonym) 	optional 	YBR092C
	String objectType;// 	required 	gene
	Set<String> taxons; //(|taxon) 	required 	taxon:4932
	Date date;// 	required 	20010118
	String assignedBy; // 	required 	SGD 
	
	public String getDbName() {
		return dbName;
	}
	
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	public String getAccession() {
		return accession;
	}
	
	public void setAccession(String accession) {
		this.accession = accession;
	}
	
	public String getPrimaryName() {
		return primaryName;
	}
	
	public void setPrimaryName(String primaryName) {
		this.primaryName = primaryName;
	}
	
	public Set<String> getQualifiers() {
		return qualifiers;
	}
	
	public void setQualifiers(Set<String> qualifiers) {
		this.qualifiers = qualifiers;
	}
	
	public String getGoAccession() {
		return goAccession;
	}
	
	public void setGoAccession(String goAccession) {
		this.goAccession = goAccession;
	}
	
	public Set<String> getDbReference() {
		return dbReference;
	}
	
	public void setDbReference(Set<String> dbReference) {
		this.dbReference = dbReference;
	}
	
	public String getEvidenceCode() {
		return evidenceCode;
	}
	
	public void setEvidenceCode(String evidenceCode) {
		this.evidenceCode = evidenceCode;
	}
	
	public String getWithFrom() {
		return withFrom;
	}
	
	public void setWithFrom(String withFrom) {
		this.withFrom = withFrom;
	}
	
	public GoCategory getGoCategory() {
		return goCategory;
	}
	
	public void setGoCategory(GoCategory goCategory) {
		this.goCategory = goCategory;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Set<String> getSynonyms() {
		return synonyms;
	}
	
	public void setSynonyms(Set<String> synonyms) {
		this.synonyms = synonyms;
	}
	
	public String getObjectType() {
		return objectType;
	}
	
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	public Set<String> getTaxons() {
		return taxons;
	}
	
	public void setTaxons(Set<String> taxons) {
		this.taxons = taxons;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getAssignedBy() {
		return assignedBy;
	}
	
	public void setAssignedBy(String assignedBy) {
		this.assignedBy = assignedBy;
	}


}
