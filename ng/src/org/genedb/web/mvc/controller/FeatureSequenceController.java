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

import org.genedb.db.dao.SequenceDao;
import org.genedb.util.Pair;
import org.genedb.util.SequenceUtils;

import org.gmod.schema.feature.AbstractExon;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 * @author gv1
 */
@Controller
@RequestMapping("/featureSeq")
public class FeatureSequenceController {
    private static final Logger logger = Logger.getLogger(FeatureSequenceController.class);

    private SequenceDao sequenceDao;
    private String geneSequenceView;

    @RequestMapping(method=RequestMethod.GET, value="/{name}")
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("name") String name) throws Exception {

        Feature feature = sequenceDao.getFeatureByUniqueName(name, Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", name));
            //be.reject("no.results");
            //return showForm(request, response, be);
            return new ModelAndView("redirect:/feature/notFound.jsp");
        }

        String viewName = geneSequenceView;
        Map<String, Object> model = Maps.newHashMap();
        
        Transcript transcript = null;
        boolean pseudogenic = false;
        
        if (feature instanceof Transcript) {
        	
            transcript = (Transcript) feature;
        	model.put("uniqueName", transcript.getUniqueName());
        	
        	if (feature instanceof PseudogenicTranscript) {
        	    pseudogenic = true;
        	}
        	
        } else {
        	
        	AbstractGene gene = sequenceDao.getGene(feature);
            if (gene == null) {
//            	model.put("uniqueName", feature.getUniqueName());
//            	model.put("gene_sequence", feature.getResidues());
//            	return new ModelAndView(viewName, model);
            	return new ModelAndView("redirect:/feature/notFound.jsp");
            }
        	
            if (gene instanceof Pseudogene) {
                pseudogenic = true;
            }
            
        	model.put("uniqueName", gene.getUniqueName());
        	transcript = gene.getFirstTranscript();
        	if (transcript == null) {
//            	model.put("gene_sequence", feature.getResidues());
//            	return new ModelAndView(viewName, model);
            	return new ModelAndView("redirect:/feature/notFound.jsp");
            }
        }
        
        model.put("coords", transcript.getExons());
        model.put("pseudogenic", pseudogenic);

        // ---------------------------------------------
        model.put("unspliced", transcript.getGene().getResidues());
        // geneSequence - from UTR to UTR inclusive, with introns
        String geneSequence = transcript.getGene().getResidues();

        boolean reverseCompliment = false;
        if (transcript.getRankZeroFeatureLoc().getStrand() < 0) {
            reverseCompliment = true;
        }
        if (reverseCompliment) {
           geneSequence = SequenceUtils.reverseComplement(geneSequence);
        }
        model.put("gene_sequence", geneSequence); // formerly unspliced


        if (transcript instanceof ProductiveTranscript) {
        	// Don't want CDS or protein for non-coding features

        	List<Pair<Integer, Integer>> coords;
        	for (AbstractExon exon : transcript.getExons()) {
        		exon.getFeatureLocs();
        	}


        	//Transcript - from UTR to UTR inclusive, without introns
        	model.put("transcript", transcript.getResidues()); // formerly spliced

        	//CDS - exons
        	SortedSet<AbstractExon> exons = transcript.getExons();
        	String buildup;
        	if (reverseCompliment) {
        		buildup = new String();
        		for (AbstractExon exon : exons) {
        			String seq = exon.getPrimarySourceFeature().getResidues(exon.getFmin(), exon.getFmax(), reverseCompliment);
        			buildup = seq + buildup;
        		}
        	} else {
        		StringBuilder sb = new StringBuilder();
        		for (AbstractExon exon : exons) {
        			String seq = exon.getPrimarySourceFeature().getResidues(exon.getFmin(), exon.getFmax(), reverseCompliment);
        			sb.append(seq);
        		}
        		buildup = sb.toString();
        	}
        	model.put("cds", buildup);

        	//model.put("cds", getSequence(transcript, GeneSection.START_CODON, 0, GeneSection.STOP_CODON, 0, true, false));


        	// -----------------------------------------------

            Polypeptide pp = ((ProductiveTranscript) transcript).getProtein();
            if (pp != null) {
                model.put("protein", pp.getResidues());
            }
        }

        return new ModelAndView(viewName, model);
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setGeneSequenceView(String geneSequenceView) {
        this.geneSequenceView = geneSequenceView;
    }



//    private ModelBuilder modelBuilder;
//
//    public void setModelBuilder(ModelBuilder modelBuilder) {
//        this.modelBuilder = modelBuilder;
//    }

    public static class FeatureSequenceBean {
        private String name;
        //private boolean sequenceView = false;

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}