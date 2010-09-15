package org.genedb.web.utils;


import org.apache.log4j.Logger;
import org.genedb.web.mvc.controller.download.SequenceType;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


public class DownloadUtils {

	private static Logger logger = Logger.getLogger(DownloadUtils.class);
	
    private static int FEATURE_PREFIX_WIDTH = 22;
    private static int MAX_FEATURE_WIDTH = 18;
    private static final String FEATURE_TABLE_PREFIX = String.format("%-"+FEATURE_PREFIX_WIDTH+"s", "FT");
    private static final int FASTA_WIDTH = 60;
    private static final int BASES_WIDTH = 10;

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

   public static String writeFasta(String header, String sequence) {
       StringBuilder fasta = new StringBuilder();
       fasta.append(">");
       fasta.append(header);
       fasta.append("\n");

        int startPos = 0;
        int sequenceLen = sequence.length();
        while (startPos < sequenceLen) {
            int endPos = startPos + BASES_WIDTH;
            if (endPos > sequenceLen) {
                endPos = sequenceLen;
            }

            fasta.append(sequence.substring(startPos, endPos));
            fasta.append(" ");
            startPos += BASES_WIDTH;
            if(startPos % 60 == 0) {
                fasta.append("\n");
            }
        }

        return fasta.toString();
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

    public static String writeEmblEntry(String featureType,
            boolean forwardStrand, int min, int max,
            Map<String, String> qualifiers) {

        StringBuilder embl = new StringBuilder();

        if (featureType.length() > MAX_FEATURE_WIDTH) {
            featureType = featureType.substring(0, MAX_FEATURE_WIDTH);
        }

        embl.append(String.format("FT %-"+(FEATURE_PREFIX_WIDTH-3)+"s", featureType));
        if (!forwardStrand) {
            embl.append("complement(");
        }

        embl.append(min - 1 +".."+max); // Interbase conversion

        if (!forwardStrand) {
            embl.append(")");
        }
        embl.append("\n");

        for (Map.Entry<String, String> qualifier: qualifiers.entrySet()) {
            embl.append(FEATURE_TABLE_PREFIX+"/"+qualifier.getKey()+"=\""+qualifier.getValue()+"\"");
            embl.append("\n");
        }

        return embl.toString();
    }

    public static String getSequence(AbstractGene gene,SequenceType sequenceType) {
        String sequence = null;
        boolean alternateSpliced = false;
        Collection<Transcript> transcripts = gene.getTranscripts();
        ProductiveTranscript transcript = null;

        if(transcripts.size() > 1 ) {
            alternateSpliced = true;
        } else {
           Transcript t = transcripts.iterator().next();
           if(t instanceof ProductiveTranscript) {
               transcript = (ProductiveTranscript) t;
           }
        }

        if(!alternateSpliced && transcript!=null) {
            switch (sequenceType) {
                case SPLICED_DNA:
                    if(transcript.getResidues() != null) {
                        sequence = new String(transcript.getResidues());
                    }
                    break;
                case UNSPLICED_DNA:
                    if(transcript.getResidues() != null) {
                        sequence = new String(gene.getResidues());
                    }
                    break;
                case PROTEIN:
                    if(transcript.getProtein().getResidues() != null) {
                        sequence = new String(transcript.getProtein().getResidues());
                    }
                    break;
                case INTRON_AND_EXON:
                	sequence = getIntronsAndExons(transcript);
                    break;
            }
        }

        return sequence;
    }
    
    
    private static class Position {
    	public int start;
    	public int stop;
    	public boolean upper = false;
    }
    
    
    /**
     * Retrieves the entire sequence of the transcript, with the exons capitalized. 
     * 
     * @param transcript
     * @return
     */
    private static String getIntronsAndExons(ProductiveTranscript transcript) {
    	String sequence = new String();
    	
    	List<Position> positions = new ArrayList<Position>(); 
    	
    	int lastPoint = -1;
    	
    	for (AbstractExon exon : transcript.getExons()) {
    		
    		int exonStart = exon.getStart();
    		
    		if (lastPoint == -1) {
    			lastPoint = exonStart;
    		}
    		
    		int intronStart = exonStart - lastPoint;
    		
    		if (intronStart > 0) {
    			Position intronPosition = new Position();
    			intronPosition.start = lastPoint;
    			intronPosition.stop = exonStart;
    			positions.add(intronPosition);
    			logger.debug(intronPosition.start + " ... " + intronPosition.stop);    			
    		}
    		
    		logger.debug(exon.getStart() + " <...> " + exon.getStop());
    		
    		Position exonPosition = new Position();
    		exonPosition.start = exon.getStart();
    		exonPosition.stop = exon.getStop();
    		exonPosition.upper = true;
			positions.add(exonPosition);
    		
    		lastPoint = exon.getStop();
    		
    	}
    	
    	for (Position p : positions) {
    		
    		String str;
    		if (p.upper) {
    			str = new String(transcript.getGene().getRankZeroFeatureLoc().getSourceFeature().getResidues(p.start, p.stop).toUpperCase() );
    		} else {
    			str = new String(transcript.getGene().getRankZeroFeatureLoc().getSourceFeature().getResidues(p.start, p.stop) );
    		}
    		
    		sequence = sequence.concat(str);
    	}
		
		
		//    	for (AbstractExon exon : transcript.getExons()) {
		//			int start = exon.getStart();
		//			int stop = exon.getStop();
		//			String str = new String(transcript.getGene().getRankZeroFeatureLoc().getSourceFeature().getResidues(start, stop));
		//			sequence = sequence.concat(str);
		//		}
		return sequence;
    }

    public static String getSequence(Transcript t, SequenceType sequenceType, int prime3, int prime5) {
    	String sequence = null;
    	ProductiveTranscript transcript = null;

    	if (t instanceof ProductiveTranscript) {
    		transcript = (ProductiveTranscript) t;
    	}

    	if (transcript!=null) {
    		switch (sequenceType) {
    		case SPLICED_DNA:
    			if (transcript.getResidues() != null) {
    				return new String(transcript.getResidues());
    			}
    			//break;
    		case UNSPLICED_DNA:
    			return new String(transcript.getGene().getResidues());
    			//break;
    		case PROTEIN:
    			return new String(transcript.getProtein().getResidues());
    			//break;
    		case INTRON_AND_EXON:
    			return getIntronsAndExons(transcript);
    			
    		case INTERGENIC_3:
    			return fetchParentSequence(t, false, prime3, 0);

    		case INTERGENIC_5:
    			return fetchParentSequence(t, false, 0, prime5);

    		case INTERGENIC_3and5:
    			return fetchParentSequence(t, true, prime3, prime5);

    		}
    	}
    	return sequence;
    }

    // TODO Check off by one
	private static String fetchParentSequence(Transcript t, boolean includeTranscript, int prime3, int prime5) {
	    if (prime3>0 && prime5 > 0 && !includeTranscript) {
	        throw new IllegalArgumentException("Can't fetch sequence from both sides of a transcript but not include the transcript");
	    }
	    if (prime3<0 || prime5 < 0) {
	        throw new IllegalArgumentException("Can't use -ve sequence offsets");
	    }
		FeatureLoc fl = t.getRankZeroFeatureLoc();
		int start;
		int end;
		if (includeTranscript) {
		      start = fl.getFmin() - prime3;
		      end = fl.getFmax() + prime5;
		} else {
		    if (prime3 > 0) {
		        start = fl.getFmin() - prime3;
		        end = fl.getFmin();
		    } else {
		        // Prime 5 end
		        start = fl.getFmax();
		        end = fl.getFmax() + prime5;
		    }
		}

		Feature parent = fl.getSourceFeature();
		if (start > end) {
		    int tmp = start;
		    start = end;
		    end = tmp;
		}
		return new String(parent.getResidues(start, end, fl.getStrand()==1));
	}


}

//public class FastaUtils {
//
//    public static void exportFeatureFasta(Writer w, boolean spaces, Feature feat) throws IOException {
//        exportFasta(w, feat.getType().getName()+":"+feat.getUniqueName(), feat.getResidues(), spaces);
//    }
//
//    public static void exportFastaRegion(Writer w, String header, boolean spaces,
//            Feature feat, Strand strand, int min, int max) throws IOException {
//        String seq = feat.getResidues(min, max, (strand == Strand.REVERSE));
//        exportFasta(w, header, seq, spaces);
//    }
//
//
//    public static void exportFasta(Writer w, String header, String seq, boolean spaces) throws IOException {
//        w.write('>' + header + '\n');
//        int count = 0;
//        for (char c : seq.toCharArray()) {
//            if (count % 60 == 0) {
//                w.write('\n');
//            } else {
//                if (spaces && count % 10 == 0) {
//                    w.write(' ');
//                }
//                count++;
//            }
//            w.write(c);
//            count++;
//        }
//    }
//
//}

//public class EmblUtils {
//
//    public static void exportEmbl(Writer w, Feature feat, int min, int max, boolean internal, boolean strict, boolean truncateEnds) throws IOException {
//        exportHeader();
//        exportTab();
//        exportSequence(w, feat, min, max);
//    }
//
//    private static void exportSequence(Writer w, Feature feat, int min, int max) throws IOException {
//        // TODO - ignores strand
//        String seq = feat.getResidues(min, max);
//        exportSequence(w, seq);
//    }
//
//    private static void exportSequence(Writer w, String seq) throws IOException {
//        // XX
//        // SQ   Sequence 29663 BP; 9792 A; 5106 C; 5232 G; 9533 T; 0 other;
//        //     gatcacgtac atcaccttgt aagaatttat ctgcaatagt ccttcggtat tgtacattgt        60
//        //     ...
//        //     tggttctgat attgaacaaa tagaactaca aaatatgcct actcctgtga aaaaataatt     29640
//        //     ttctttatcg ttttcatgat ccc                                             29663
//        // //
//        pln(w, "XX");
//
//
//        w.write("SQ   Sequence ");
//        w.write(seq.length());
//        w.write(" BP;");
//        // TODO stats
//        w.write('\n');
//
//        for (int i = 0; i < seq.length(); i++) {
//            char c = seq.charAt(i);
//            if (i % 60 == 0) {
//                String count = Integer.toString(i);
//                w.write(StringUtils.leftPad(" ", 10, count));
//                w.write('\n');
//            } else {
//                if (i % 10 == 0) {
//                    w.write(' ');
//                }
//            }
//            w.write(c);
//        }
//        if (seq.length() % 60 != 0) {
//            // TODO cope with remainder on last line
//            int used = seq.length() % 60;
//            int toPad = 75; // 10 *6 + 1 *5 + 10
//            String count = Integer.toString(seq.length());
//            w.write(StringUtils.leftPad(" ", toPad, count));
//            w.write('\n');
//        }
//        pln(w, "//");
//    }
//
//    private static void pln(Writer w, String line) throws IOException {
//        w.write(line);
//        w.write('\n');
//    }
//
//    private static void exportTab() {
//        // TODO Auto-generated method stub
//    }
//
//    private static void exportHeader() {
//        // TODO Auto-generated method stub
//    }
//}
