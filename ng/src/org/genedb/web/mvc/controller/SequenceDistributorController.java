/*
 * Copyright (c) 2006 Genome Research Limited.
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

package org.genedb.web.mvc.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 * @author gv1
 */
@Controller
@RequestMapping("/SequenceDistributor")
public class SequenceDistributorController {
    private static final Logger logger = Logger.getLogger(SequenceDistributorController.class);

    private static final String LOCAL_BLAST = "http://www.genedb.org/blast/submitblast";

    private SequenceDao sequenceDao;

    @RequestMapping(method=RequestMethod.GET, value="/{name}/{type}/{destination}")
    public void process(
    		HttpServletResponse response,
            @PathVariable(value="name") String uniqueName,
            @PathVariable(value="destination") String destination,
            @PathVariable(value="type") String sequenceType
    ) throws IOException {
    	
    	Writer writer = response.getWriter();
    	response.setContentType("text/html");
    	
        Feature feature = sequenceDao.getFeatureByUniqueName(uniqueName, Feature.class);
        if (feature == null) {
            writer.append(String.format("Failed to find feature '%s'", uniqueName));
            //be.reject("no.results");
            //return showForm(request, response, be);
            return; // FIXME
        }
        Transcript transcript = modelBuilder.findTranscriptForFeature(feature);

        String sequence = null;
        String sequence2 = null;
        boolean nucleotide = true;
        String program = "wublastn";

        SequenceType st = SequenceType.valueOf(sequenceType);
        switch (st) {
        case GENE_SEQUENCE:
            sequence = transcript.getGene().getResidues();
            sequence2 = getSequence(transcript, GeneSection.TRANSCRIPTIONAL_START, 0, GeneSection.POLY_A, 0, true, true);
            compareSequences(sequence, sequence2);
            break;
        case TRANSCRIPT:
            sequence = transcript.getResidues();
            sequence2 = getSequence(transcript, GeneSection.TRANSCRIPTIONAL_START, 0, GeneSection.POLY_A, 0, true, false);
            compareSequences(sequence, sequence2);
            break;
        case CDS:
            sequence = transcript.getResidues();
            sequence2 = getSequence(transcript, GeneSection.START_CODON, 0, GeneSection.STOP_CODON, 0, true, false);
            compareSequences(sequence, sequence2);
            break;
        case PROTEIN:
            if (transcript instanceof ProductiveTranscript) {
                Polypeptide pp = ((ProductiveTranscript) transcript).getProtein();
                if (pp != null) {
                    sequence = pp.getResidues();
                    if (sequence.endsWith("*")) {
                        sequence = sequence.substring(0, sequence.length()-1);
                    }
                    nucleotide = false;
                }
            }
            program = "wublastx";
        }

        sequence = splitSequenceIntoLines(sequence);

        SequenceDestination sd = SequenceDestination.valueOf(destination);
        
        
        
        switch (sd) {
        case BLAST:
        	
        	String uri = String.format("%s/%s", LOCAL_BLAST, "GeneDB_" + transcript.getOrganism().getCommonName() );
        	Map<String,String> parameters = new Hashtable<String,String>();
        	parameters.put("sequence", sequence);
        	parameters.put("blast_type", program);
        	
        	writer.append( post(uri, parameters) );
        	break;
        	
//        	String returnable = String.format("redirect:%s/%s?sequence=%s&blast_type=%s",
//                    LOCAL_BLAST,
//                    "GeneDB_" + transcript.getOrganism().getCommonName(),
//                    sequence,
//                    program
//                );
//        	
//        	
        	
//        	logger.error(returnable);
//            return returnable;
        	
        case OMNIBLAST:
        	
        	String uri2 = String.format("%s/%s", LOCAL_BLAST, nucleotide ? "GeneDB_transcripts/omni" : "GeneDB_proteins/omni" );
        	Map<String,String> parameters2 = new Hashtable<String,String>();
        	parameters2.put("sequence", sequence);
        	parameters2.put("blast_type", program);
        	
        	writer.append( post(uri2, parameters2)) ;
        	break;
        	
//            return String.format("redirect:%s/%s?sequence=%s&blast_type=%s",
//                LOCAL_BLAST,
//                nucleotide ? "GeneDB_transcripts/omni" : "GeneDB_proteins/omni",
//                sequence,
//                program
//            );
        	
        case NCBI_BLAST:
        	
        	String uri3 = "http://blast.ncbi.nlm.nih.gov/Blast.cgi";
        	
        	Map<String,String> parameters3 = new Hashtable<String,String>();
        	parameters3.put("PAGE_TYPE", "BlastSearch");
        	parameters3.put("SHOW_DEFAULTS", "on");
        	parameters3.put("LINK_LOC", "blasthome");
        	
        	if (nucleotide) {
        		parameters3.put("PROGRAM", "blastn");
        		parameters3.put("BLAST_PROGRAMS", "megaBlast");
        		parameters3.put("DBTYPE", "gc");
        		parameters3.put("DATABASE", "nr");
        	} else {
        		parameters3.put("PROGRAM", "blastp");
        		parameters3.put("BLAST_PROGRAMS", "blastp");
        	}
        	
        	parameters3.put("QUERY", sequence);
        	
        	writer.append( postForm(uri3, parameters3));
        	break;
        	
//            return String.format("redirect:%s&%s&QUERY=%s",
//                "http://blast.ncbi.nlm.nih.gov/Blast.cgi?PAGE_TYPE=BlastSearch&SHOW_DEFAULTS=on&LINK_LOC=blasthome",
//                nucleotide ?
//                    "PROGRAM=blastn&BLAST_PROGRAMS=megaBlast&DBTYPE=gc&DATABASE=nr"
//                    : "PROGRAM=blastp&BLAST_PROGRAMS=blastp",
//                sequence
//            );
        	
        default:
            throw new RuntimeException("Unknown sequence destination");
        }
    }
    
    private String postForm(String uri, Map<String,String> parameters) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(String.format("<form action='%s' method='POST'>", uri));
    	
    	for(Entry<String,String> entry : parameters.entrySet()) {
    		sb.append(String.format("<form name='%s' value='%s' type='HIDDEN'>", entry.getKey(), entry.getValue()));
    	}
    	
    	sb.append("<input type='SUBMIT'></form>");
    	
		return sb.toString();
    }
    
    private String post(String uri, Map<String,String> parameters) {
    	final PostMethod postMethod = new PostMethod(uri);
    	
    	logger.error(uri);
    	
    	for(Entry<String,String> entry : parameters.entrySet()) {
    		logger.error(entry.getKey());
    		logger.error(entry.getValue());
    		postMethod.addParameter( entry.getKey(), entry.getValue());
    	}
    	
    	HttpClient client = new HttpClient();
    	
		int statusCode;
		try {
			statusCode = client.executeMethod( postMethod );
			
			if( statusCode == HttpStatus.SC_OK )
			{
			    final InputStream responseBodyStream = postMethod.getResponseBodyAsStream();
			    StringWriter writer = new StringWriter();
			    IOUtils.copy(responseBodyStream, writer);
			    
			    responseBodyStream.close();
			    
			    return writer.toString();
			    
			}
			
		} catch (HttpException e) {
			logger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
			
		
		return "Sorry but could not prepare the BLAST form. Please contact webmaster@genedb.org with information on the gene that caused this problem.";
    }
    
    private String getSequence(Transcript transcript, GeneSection start, int length1, GeneSection end,
            int length2, boolean exons, boolean introns) {
        Feature topLevelFeature = transcript.getPrimarySourceFeature();
        int min = transcript.getFmin() - length1;
        min = (min < 0) ? 0 : min;
        int max = transcript.getFmax() + length2;
        max = (max < topLevelFeature.getSeqLen()) ? topLevelFeature.getSeqLen() : max;
        String seq = topLevelFeature.getResidues(min, max);
        return seq;
    }

    private void compareSequences(String sequence, String sequence2) {
        if (sequence.equals(sequence2)) {
            logger.error("Sequences match");
        } else {
            logger.error("Sequences differ");
        }

    }

    private int LINE_LENGTH = 40;

    private String splitSequenceIntoLines(String sequence) {

        StringBuilder ret = new StringBuilder();
        String remaining = sequence;

        while (remaining.length() > LINE_LENGTH) {
            ret.append(remaining.substring(0, LINE_LENGTH));
            ret.append("%0A");
            remaining = remaining.substring(LINE_LENGTH);
        }
        if (remaining.length() != 0) {
            ret.append(remaining);
        }

        return ret.toString();
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    private ModelBuilder modelBuilder;

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }
}
