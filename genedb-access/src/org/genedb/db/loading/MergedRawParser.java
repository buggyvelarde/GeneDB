/*
 * Copyright (c) 2007 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

/**
 * This reads a GO association file and stores GO annotation with
 * the corresponding "gene"
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 */
package org.genedb.db.loading;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gmod.schema.sequence.Feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MergedRawParser {


	protected static final Log logger = LogFactory.getLog(MergedRawParser.class);
	private Map cache;
	private int count;
	private boolean mungeCase;
	private Map uniprotSysId;
	private boolean discardDupNodes;

	//    0     1 2       3          4          5      6
	// GeneACC  ? ? NATIVE_PROG NATIVE_ACC NATIVE_DESC ?

	// 7 8 9 10   11      12       13
	// ? ? ? ?  IP_ACC IP_DESC GO_DETAILS

	private static final int COL_ID=0;
	private static final int COL_NATIVE_PROG=3;
	private static final int COL_NATIVE_ACC=4;
	private static final int COL_ACC=11;
	private static final int COL_GO=13;

	private static Map<String, String> months = new HashMap<String, String>(12);

	private static Map<String, String> dbs = new HashMap<String, String>();

	static {
		dbs.put("HMMPfam", "Pfam");
		dbs.put("ScanProsite", "PROSITE");
		dbs.put("FPrintScan", "PRINTS");
		dbs.put("ProfileScan", "PROSITE");
		dbs.put("ScanRegExp", "PROSITE");
		dbs.put("HMMSmart", "SMART");
		dbs.put("BlastProDom", "ProDom");
		dbs.put("Superfamily", "Superfamily");
		dbs.put("superfamily", "Superfamily");

		months.put("Jan", "01");
		months.put("Feb", "02");
		months.put("Mar", "03");
		months.put("Apr", "04");
		months.put("May", "05");
		months.put("Jun", "06");
		months.put("Jul", "07");
		months.put("Aug", "08");
		months.put("Sep", "09");
		months.put("Oct", "10");
		months.put("Nov", "11");
		months.put("Dec", "12");

	}



	public int getCount() {
		return count;
	}


	//------------------------------------------------------------------------------------




	public MergedRawParser(Map cache, String filename, double score) {

		this.cache = cache;

		//System.err.println("Filename is  "+filename);
		File fl = new File(filename);
		if (!fl.exists()) {
			System.err.println("WARN: INTERPRO file/directory doesn't exist: "+filename);
			return;
		}

		if ( fl.isDirectory() ) {
			String[] fls = fl.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if ( name.endsWith("~")) {
						return false;
					}
					return true;
				}
			});
			for (int i=0; i < fls.length; i++) {
				new MergedRawParser(cache, filename+"/"+fls[i], score);
			}
			return;
		}


		System.err.println("Reading interpro from "+filename);

		String[][] ret = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader( fl ) );
			CharSVParser parser = new CharSVParser(br, "\\t", true, 0, "#" );
			ret = parser.getValues();
		} catch (FileNotFoundException exp) {
			exp.printStackTrace();
		} catch (IOException exp) {
			exp.printStackTrace();
		}


		if ( ret != null ) {

			// Go through the results and pull the rows into the
			// hashmap genes, keyed on gene names, where the values
			// are ArrayLists of String[]
			Map genes = new HashMap();
			List col = new ArrayList();
			for ( int i = 0; i < ret.length; i++ ) {
				String id = ret[i][COL_ID];
				if ( genes.containsKey(id) ) {
					col = (ArrayList) genes.get(id);
				} else {
					col = new ArrayList();
					genes.put(id, col);
				}
				col.add(ret[i]);
			}
			ret = null;

			Set strangeProgram = new HashSet();
			sub1(genes, col, strangeProgram);

			Iterator it = strangeProgram.iterator();
			if ( it.hasNext()) {
				logger.warn("WARN: Strange program name found in Interpro file: ");
				while (it.hasNext()) {
					System.err.print(it.next());
					System.err.print(' ');
				}
				System.err.print('\n');
			}


		} // if ( ret != null )

	}


	private void sub1(Map genes, List col, Set strangeProgram) {
		// Go through each key and sort the ArrayLists
		Iterator geneIterator = genes.keySet().iterator();

		Feature feature; // TODO lookup

//		while ( geneIterator.hasNext() ) {
		String id;
//		String id = (String) geneIterator.next();
//		if ( cache.containsKey( id ) ) {
//		brna = (BaseRNA) cache.get (id);
//		} else {
//		System.err.println("WARN: Cache doesn't contain id of :" + id);
//		continue;
//		}
		Set goIdsLinked = new HashSet();
		col = (ArrayList) genes.get(id);
		boolean swap = true;
		while (swap) {
			swap = false;
			for (int i = 0; i < col.size() - 1; i++) {
				String[] a = (String[]) col.get(i);
				String[] b = (String[]) col.get(i + 1);
				String aAccNum = "NULL";
				String bAccNum = "NULL";
				if ( a.length > COL_ACC) {
					aAccNum = a[COL_ACC];
				}
				if ( b.length > COL_ACC) {
					bAccNum = b[COL_ACC];
				}
				int cmp = aAccNum.compareTo(bAccNum);
				if ( cmp > 0 ) {
					col.set(i + 1, a);
					col.set(i, b);
					swap = true;
				} else {
					if ( cmp == 0 ) {
						if ( a[COL_NATIVE_PROG].compareTo(b[COL_NATIVE_PROG]) > 0 ) {
							col.set(i + 1, a);
							col.set(i, b);
							swap = true;
						}
					}
				}
			}
		}

		// col is now sorted by interpro, then program
		HashSet ip = new HashSet();
		for ( int i = 0; i < col.size(); i++) {
			String[] a = (String[]) col.get(i);
			String aAccNum = "NULL";
			if ( a.length > COL_ACC) {
				aAccNum = a[COL_ACC];
			}
			ip.add(aAccNum);
		}
		int max;
		int min;
		Iterator it = ip.iterator();
		while ( it.hasNext()) {
			max = -1;
			min = col.size();
			String ipNum = (String) it.next();
			for ( int i = 0; i < col.size(); i++ ) {
				String[] a = (String[]) col.get(i);
				String aAccNum = "NULL";
				if ( a.length > COL_ACC) {
					aAccNum = a[COL_ACC];
				}
				if ( aAccNum.equals(ipNum)) {
					if ( i < min) {
						min = i;
					}
					if ( i > max) {
						max = i;
					}
				}
			}
			// Now know upper and lower bound of this interpro num
			HashSet progs = new HashSet();
			for ( int i = min; i <= max ; i++) {
				String prog = (( String[]) col.get(i))[COL_NATIVE_PROG];
				String db = (String) dbs.get(prog);
				if ( db == null) {
					strangeProgram.add(prog);
				} else {
					progs.add(prog);
				}
				String[] thisRow = (String[]) col.get(i);
				if (thisRow.length >= COL_GO+1) {
					//System.err.println("Adding GO terms for "+gene.getId());
					//addGoTerms(feature, thisRow, goIdsLinked);
				}
			}

			if ( !ipNum.equals("NULL")) {
				StringBuffer note = new StringBuffer();
				note.append("Derived from hit");
				if ( progs.size() > 1 ) {
					note.append("s");
				}
				note.append(": ");
				Iterator it2 = progs.iterator();
				while ( it2.hasNext() ) {
					note.append(dbs.get(it2.next()) + " ");
				}
				//System.err.println("Adding Interpro hit for "+gene.getId());
				GeneUtils.addLink(feature, "InterPro", ipNum, note.toString());
			}
			// Now go thru' individual hits even if InterPro is null
			Iterator it2 = progs.iterator();
			while ( it2.hasNext() ) {
				String prog = (String) it2.next();
				ArrayList coords = new ArrayList();
				List coordinates = new ArrayList();
				String dbacc = null;
				int count = 0;
				for ( int i = min; i <= max ; i++) {
					StringBuffer tmp = new StringBuffer();
					String[] a = (String[]) col.get(i);
					if ( a[3].equals(prog)) {
						tmp.append( a[6] + "-" + a[7] );
						if ( count == 0) {
							tmp.append( " (Score: ");
						} else {
							tmp.append(" (");
						}
						tmp.append(a[8] + ")");
						coords.add(tmp.toString());
						coordinates.add(new String[] {a[6], a[7]});
						dbacc = a[COL_NATIVE_ACC];
						count++;
					}
				}
				StringBuffer note = new StringBuffer("Residue");
				if ( coords.size() > 1 ) {
					note.append("s");
				}
				note.append(": ");
				for ( int i = 0; i < coords.size(); i++) {
					if ( i > 0 && (i != coords.size() - 1)) {
						note.append(", ");
					}
					if ( (i == coords.size() - 1) && coords.size() != 1) {
						note.append(" and ");
					}
					note.append( (String) coords.get(i) );
				}
				String db = (String) dbs.get(prog);
				if ( db == null ) {
					strangeProgram.add(prog);
				} else {
					// Hack for superfamily as InterPro reports acc as SF12345 rather than 12345
					if ("Superfamily".equalsIgnoreCase(db) && dbacc.startsWith("SF")) {
						dbacc = dbacc.substring(2);
					}
					DBLink link = DBLinkManager.getInstance().getDBLink(db, dbacc, note.toString());
					if ( coordinates.size() > 0) {
						link.setCoords( coordinates );
					}
					//System.err.println("Adding "+db+" hit for "+gene.getId());
					feature.addDBLink(link, DBLinkType.DB_XREF);
				}
			}
		}

	} // Got all the interpro numbers



////	Molecular Function: protein kinase (GO:0004672),
////	Molecular Function: ATP binding (GO:0005524),
////	Biological Process: protein amino acid phosphorylation (GO:0006468)
//	private void addGoTerms(Feature gene, String[] row, Set goIdsLinked) {
//	String goTerms = row[COL_GO];
//	if ( goTerms == null || "".equals(goTerms)) {
//	return;
//	}
//	List terms = new ArrayList();
//	int start=0;
//	int end=0;
//	while (end != -1) {
//	int temp = goTerms.indexOf("(GO:", start);
//	if (temp == -1) {
//	end = -1;
//	} else {
//	end = goTerms.indexOf(")", temp);
//	if ( end != -1) {
//	terms.add(goTerms.substring(start, end+1));
//	}
//	start = end+1;
//	}
//	}
//	for (int i=0; i < terms.size(); i++) {
//	String term = (String) terms.get(i);
//	if ( term.startsWith(",")) {
//	term = term.substring(1);
//	}
//	term = term.trim();
////	System.err.println(term);
//	int lb = term.indexOf("(GO:");
//	int rb = term.indexOf(")", lb);
//	String acc = term.substring(lb+4, rb);

//	if ( !goIdsLinked.contains(acc)) {
//	goIdsLinked.add(acc);
//	GoInstance c = new GoInstance();
//	c.setId( acc );
//	c.setEvidence( GoEvidenceCode.IEA );
//	c.setWithFrom( "Interpro:" + row[COL_ACC] );
//	c.setRef( "GOC:interpro2go" );
//	String[] rawDate = row[10].split("-");
//	String month = (String) months.get(rawDate[1]);
//	String date = rawDate[2]+month+rawDate[0];
//	c.setDate(date);

//	int colon = term.indexOf(":");
//	String label = term.substring(0, colon);
//	String category = null;
//	if ("Molecular Function".equals(label)
//	|| "function".equals(label)) {
//	category = "function";
//	}
//	if ("Biological Process".equals(label)
//	|| "process".equals(label)) {
//	category = "process";
//	}
//	if ("Cellular Component".equals(label)
//	|| "component".equals(label)) {
//	category = "component";
//	}
//	c.setSubtype( category );
////	c.setAspect( c.getSubtype().substring(0, 1).toUpperCase() );
//	String name = GoDictionary.getName( acc );
//	c.setName( name );

//	gene.addGOInst(c);
//	}
//	}
//	}



}
