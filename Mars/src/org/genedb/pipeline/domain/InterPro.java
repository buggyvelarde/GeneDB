package org.genedb.pipeline.domain;

import java.util.Date;
import java.util.Set;

public class InterPro {

	private String id; //	NF00181542	is the id of the input sequence.
	private String checksum; //	27A9BBAC0587AB84	is the crc64 (checksum) of the protein sequence (supposed to be unique).
	private int length; //	272	is the length of the sequence (in AA).
	private String analysis;//	HMMPIR	is the anaysis method launched.
	private String nativeDbAccession; //	PIRSF001424	is the database members entry for this match.
	private String nativeDbDescription; //	Prephenate dehydratase	is the database member description for the entry.
	private int start;  //	1	is the start of the domain match.
	private int end; //	270	is the end of the domain match.
	private double score;//	6.5e-141	is the evalue of the match (reported by member database method).
	private boolean statusConfirmed; //	T	is the status of the match (T: true, ?: unknown).
	private Date dateRun;//	06-Aug-2005	is the date of the run.
	private String iprAccession; //	IPR008237	is the corresponding InterPro entry (if iprlookup requested by the user).
	private String iprDescription; //	Prephenate dehydratase with ACT region	is the description of the InterPro entry.
	private Set<SimpleGOEntry> goSet; //Molecular Function:prephenate dehydratase activity (GO:0004664)	is the GO (gene ontology) description for the InterPro entry.

	class SimpleGOEntry {
		String description;
		String accession;
		GoCategory category;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	public String getNativeDbAccession() {
		return nativeDbAccession;
	}

	public void setNativeDbAccession(String nativeDbAccession) {
		this.nativeDbAccession = nativeDbAccession;
	}

	public String getNativeDbDescription() {
		return nativeDbDescription;
	}

	public void setNativeDbDescription(String nativeDbDescription) {
		this.nativeDbDescription = nativeDbDescription;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public boolean isStatusConfirmed() {
		return statusConfirmed;
	}

	public void setStatusConfirmed(boolean statusConfirmed) {
		this.statusConfirmed = statusConfirmed;
	}

	public Date getDateRun() {
		return dateRun;
	}

	public void setDateRun(Date dateRun) {
		this.dateRun = dateRun;
	}

	public String getIprAccession() {
		return iprAccession;
	}

	public void setIprAccession(String iprAccession) {
		this.iprAccession = iprAccession;
	}

	public String getIprDescription() {
		return iprDescription;
	}

	public void setIprDescription(String iprDescription) {
		this.iprDescription = iprDescription;
	}

	public Set<SimpleGOEntry> getGoSet() {
		return goSet;
	}

	public void setGoSet(Set<SimpleGOEntry> goSet) {
		this.goSet = goSet;
	}

}
