package org.genedb.web.mvc.controller.download;

import java.io.PrintWriter;
import java.util.Map;

public class DownloadUtils {

	private static int FEATURE_PREFIX_WIDTH = 22;
	private static int MAX_FEATURE_WIDTH = 18;
	private static final String FEATURE_TABLE_PREFIX = String.format("%-"+FEATURE_PREFIX_WIDTH+"s", "FT");
	private static final int FASTA_WIDTH = 60;

	public static void writeFasta(PrintWriter out, String header, String sequence) {
		out.print("> ");
		out.println(header);
		
		int startPos = 0;
		int sequenceLen = sequence.length();
		while (startPos < sequenceLen) {
			int endPos = startPos + FASTA_WIDTH;
			if (endPos > sequenceLen) {
				endPos = sequenceLen;
			}
			out.println(sequence.substring(startPos, endPos));
			startPos += FASTA_WIDTH;
		}
		
	}
	
	
	public static void writeEmblEntry(PrintWriter out, String featureType, 
			boolean forwardStrand, int min, int max,
			Map<String, String> qualifiers) {

		if (featureType.length() > MAX_FEATURE_WIDTH) {
			featureType = featureType.substring(0, MAX_FEATURE_WIDTH);
		}
		
		out.format("FT %-"+(FEATURE_PREFIX_WIDTH-3)+"s", featureType);
		if (!forwardStrand) {
			out.print("complement(");
		}
		
		out.print(min - 1 +".."+max); // Interbase conversion
		
		if (!forwardStrand) {
			out.print(")");
		}
		out.println();
		
		for (Map.Entry<String, String> qualifier: qualifiers.entrySet()) {
			out.println(FEATURE_TABLE_PREFIX+"/"+qualifier.getKey()+"=\""+qualifier.getValue()+"\"");
		}
		
	}
	
}
