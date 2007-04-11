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


import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.impl.SubSequence;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.RichSequence.IOTools;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Looks up a feature by uniquename, and possibly synonyms
 * 
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class FlatFileReportController extends PostOrGetFormController {

    private String listResultsView;
    private String formInputView;
    private SequenceDao sequenceDao;
    private OrganismDao organismDao;


    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        FlatFileReportBean ffrb = (FlatFileReportBean) command;
        
        System.err.println("onSubmit has been called");
        
        String outputFormat = ffrb.getOutputFormat();
            
        if ("Artemis".equals(outputFormat)) {
            //response.setContentType("text/plain");
            response.setContentType("application/x-java-jnlp-file");
            PrintWriter out = response.getWriter();
                
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.println("<jnlp");
            out.println("spec=\"1.0+\"");
            out.println("codebase=\"http://www.sanger.ac.uk/Software/Artemis/v8/\">");
            out.println("<information>");
            out.println("<title>Artemis</title>");
            out.println("<vendor>Sanger Institute</vendor>"); 
            out.println("<homepage href=\"http://www.sanger.ac.uk/Software/Artemis/\"/>");
            out.println("<description>Artemis</description>");
            out.println("<description kind=\"short\">DNA sequence viewer and annotation tool.");
            out.println("</description>");
            out.println("<offline-allowed/>");
            out.println("</information>");
            out.println("<security>");
            out.println("<all-permissions/>");
            out.println("</security>");
            out.println("<resources>");
            out.println("<j2se version=\"1.4+ 1.4.2\" initial-heap-size=\"32m\" max-heap-size=\"200m\"/>");
            out.println("<jar href=\"http://www.sanger.ac.uk/Software/Artemis/v8/sartemis_v8.jar\"/>");
            out.println("</resources>");
            out.println("<application-desc main-class=\"uk.ac.sanger.artemis.components.ArtemisMain\">");
            out.print("<argument>http://pathdbsrv1a:9005");
            out.print(generateLinkBackURL(ffrb.getOrganism(), ffrb.getMin(), ffrb.getMax(), "EMBL"));
            out.println("</argument>");
            out.println("</application-desc>");
            out.println("</jnlp>");
            out.flush();
            
            return null;
        }
            
        
        if ("EMBL".equals(outputFormat)) {
            response.setContentType("text/plain");
            
            SubSequence sub = extractSubSequence(ffrb);
            
            OutputStream out = response.getOutputStream();
            SeqIOTools.writeEmbl(out, sub);

            return null;
        }
        
        if ("Table".equals(outputFormat)) {
            //Create subset, parse and forward to page
            SubSequence sub = extractSubSequence(ffrb);
            FeatureHolder fh = sub.filter(new FeatureFilter.ByType("CDS"));
            Map<String, Object> model = new HashMap<String, Object>(3);
            model.put("features", fh.features());
            String viewName = null;
            return new ModelAndView(viewName, model);
        }
        
        return null;
    }

    
    private SubSequence extractSubSequence(FlatFileReportBean ffrb) throws FileNotFoundException, BioException {
        String ROOT = "/nfs/team81/art/circ_genome_data/";
        String fileName = ROOT+"styphi/chr1/St.embl";
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        SequenceIterator iter = SeqIOTools.readEmbl(br);
        Sequence seq = iter.nextSequence();
        
        SubSequence sub = new SubSequence(seq, ffrb.getMin(), ffrb.getMax());
        return sub;
    }
    
    private String generateLinkBackURL(String organism, int bottom, int top, String of) {
        String ret = null;
        try {
            ret = "/FlatFileReport?organism="+URLEncoder.encode(organism,"UTF-8")
            +"&min="+bottom+"&max="+top+"&outputFormat="+of;
        } catch (UnsupportedEncodingException e) {
            // Deliberately empty - using a required encoding
        }
        return ret;
    }
    

    public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

	public void setFormInputView(String formInputView) {
		this.formInputView = formInputView;
	}

    public void setOrganismDao(OrganismDao organismDao) {
        this.organismDao = organismDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }
}

    class FlatFileReportBean {
        private String organism;
        private String outputFormat;
        private int min;
        private int max;
        
        public int getMax() {
            return this.max;
        }
        public void setMax(int max) {
            this.max = max;
        }
        public String getOrganism() {
            return this.organism;
        }
        public void setOrganism(String organism) {
            this.organism = organism;
        }
        public String getOutputFormat() {
            return this.outputFormat;
        }
        public void setOutputFormat(String outputFormat) {
            this.outputFormat = outputFormat;
        }
        public int getMin() {
            return this.min;
        }
        public void setMin(int min) {
            this.min = min;
        }
        
        
    }
    
 