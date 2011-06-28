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
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/Basket")
public class BasketController {

    private static final Logger logger = Logger.getLogger(BasketController.class);

    private SequenceDao sequenceDao;
    private HistoryManagerFactory hmFactory;
    //private ModelBuilder modelBuilder;

    @RequestMapping(method=RequestMethod.GET, value="/{name}")
    protected void addFeatureToBasket(
            @PathVariable("name") String name,
            HttpSession session,
            HttpServletResponse response
    ) throws Exception {

        logger.debug("Trying to find NamedFeature of '"+name+"'");

        Feature feature = sequenceDao.getFeatureByUniqueName(name, Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", name));
            return;
        }

//        Transcript transcript = modelBuilder.findTranscriptForFeature(feature);
//        if (transcript == null) {
//            // If feature isn't transcript redirect - include model
//            // is it part of a gene
//            logger.warn(String.format("Failed to find transcript for an id of '%s'", name));
//            //be.reject("no.results");
//            return;
//        }
            logger.trace("dto cache hit for '"+feature.getUniqueName());
            HistoryManager hm = hmFactory.getHistoryManager(session);
            hm.addHistoryItem(HistoryType.BASKET, feature.getUniqueName());
                // Add messag
            response.setStatus(HttpServletResponse.SC_OK);
        return;
    }

//    public void setModelBuilder(ModelBuilder modelBuilder) {
//        this.modelBuilder = modelBuilder;
//    }

    public void setHistoryManagerFactory(HistoryManagerFactory hmFactory) {
        this.hmFactory = hmFactory;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

}
