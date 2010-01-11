/*
 * Copyright (c) 2006-2007 Genome Research Limited.
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

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeArrayPropertyEditor;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.support.WebRequestDataBinder;

import javax.servlet.http.HttpServletRequest;

/**
 * A SimpleFormController which already binds a TaxonNode[] property editor and
 * allows submission by GET as well as POST
 *
 * @author Adrian Tivey (art)
 */
public class TaxonNodeBindingFormController extends BaseController {

    private TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor;
    private HistoryManagerFactory historyManagerFactory;

    @InitBinder
    protected void initBinder(HttpServletRequest request, WebRequestDataBinder wrdb)
            throws Exception {
        if (taxonNodeArrayPropertyEditor == null) {
            throw new RuntimeException("Required property 'taxonNodeArrayPropertyEditor' is missing for "+this.getClass());
        }
        wrdb.registerCustomEditor(TaxonNode[].class, taxonNodeArrayPropertyEditor);
    }

    //@Required
    public void setTaxonNodeArrayPropertyEditor(
            TaxonNodeArrayPropertyEditor taxonNodeArrayPropertyEditor) {
        this.taxonNodeArrayPropertyEditor = taxonNodeArrayPropertyEditor;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    protected HistoryManagerFactory getHistoryManagerFactory() {
        return historyManagerFactory;
    }

}
