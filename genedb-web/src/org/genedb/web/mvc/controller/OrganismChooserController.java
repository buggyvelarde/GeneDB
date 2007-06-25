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
import org.genedb.db.loading.TaxonNode;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * Choose an organism, or set of orgs, usually as a 'filter' before going to another controller
 * 
 * @author Adrian Tivey (art)
 */
public class OrganismChooserController extends TaxonNodeBindingFormController {

    private String listResultsView;
    private SequenceDao sequenceDao;

    @Override
    protected ModelAndView onSubmit(Object command, 
    		BindException be) throws Exception {
    	
        OrganismChooserBean ocb = (OrganismChooserBean) command;
        
        TaxonNode[] taxonNode = ocb.getOrganism();
        
        Map<String, Object> model = new HashMap<String, Object>(4);
        String viewName = null;
        

        return new ModelAndView(viewName, model);
    }

	public void setListResultsView(String listResultsView) {
        this.listResultsView = listResultsView;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

	
}

class OrganismChooserBean {
    
    private TaxonNode[] organism;
    
    public TaxonNode[] getOrganism() {
        return this.organism;
    }
    public void setOrganism(TaxonNode[] organism) {
        this.organism = organism;
    }
    
}
