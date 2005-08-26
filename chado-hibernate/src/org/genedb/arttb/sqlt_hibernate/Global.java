package org.genedb.arttb.sqlt_hibernate;

public class Global {

	private static Munger munger = new Munger();
	
	public static Munger getMunger() {
		return munger;
	}
	
}
