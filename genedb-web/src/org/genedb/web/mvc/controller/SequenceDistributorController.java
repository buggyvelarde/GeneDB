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

import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.ProductiveTranscript;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Looks up a feature by unique name
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/SequenceDistributor")
public class SequenceDistributorController {
    private static final Logger logger = Logger.getLogger(SequenceDistributorController.class);

    private SequenceDao sequenceDao;
    private String geneSequenceView;

    @RequestMapping(method=RequestMethod.GET)
    public String process(
            @RequestParam(value="name") String uniqueName,
            @RequestParam(value="destination") SequenceDestination destination,
            @RequestParam(value="type") SequenceType sequenceType
    ) {

        Feature feature = sequenceDao.getFeatureByUniqueName(uniqueName, Feature.class);
        if (feature == null) {
            logger.warn(String.format("Failed to find feature '%s'", uniqueName));
            //be.reject("no.results");
            //return showForm(request, response, be);
            return null; // FIXME
        }
        Transcript transcript = modelBuilder.findTranscriptForFeature(feature);

        String sequence;
        boolean nucleotide = true;

        switch (sequenceType) {
        case UNSPLICED:
            sequence = transcript.getGene().getResidues();
            break;
        case SPLICED:
            sequence = transcript.getResidues();
            break;
        case PROTEIN:
            if (transcript instanceof ProductiveTranscript) {
                Polypeptide pp = ((ProductiveTranscript) transcript).getProtein();
                if (pp != null) {
                    sequence = pp.getResidues();
                    nucleotide = false;
                }
            }
        }

        String url = null;

        switch (destination) {
        case BLAST:
            url = "redirect:/wibble";
            break;
        case OMNIBLAST:
            url = "redirect:/wibble";
            break;
        case NCBI_BLAST:
            url = "redirect:/wibble";
            break;
        }

       return url;

    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setGeneSequenceView(String geneSequenceView) {
        this.geneSequenceView = geneSequenceView;
    }

    private ModelBuilder modelBuilder;

    public void setModelBuilder(ModelBuilder modelBuilder) {
        this.modelBuilder = modelBuilder;
    }
}