/*
 * Copyright (c) 2002 Genome Research Limited.
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

package org.genedb.web.gui;


import org.biojava.bio.seq.Feature;
import org.biojava.utils.net.URLFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
class FeaturePageURLFactory implements URLFactory {

    private static final String bogusSite = "http://www.deliberatelybogusaddress.com";
    private static final String SEARCH_PREFIX = "/new/NamedFeature?name=";
    private URL baseURL;

    FeaturePageURLFactory() throws MalformedURLException {
        this.baseURL = new URL(bogusSite);
    }


    public java.net.URL createURL(java.lang.Object o) {
    	System.err.println("Asked to fetch URL for '"+o+"'");
        if ( !(o instanceof Feature)) {
            return null;
        }
        Feature  feat = (Feature) o;

        //if ( feat instanceof RNASummary) {
            //RNASummary rna = (RNASummary) feat;
            StringBuffer url = new StringBuffer( SEARCH_PREFIX );
            url.append(feat.getAnnotation().getProperty("systematic_id"));
            //url.append("&organism=");
            //url.append(rna.getOrganism());
            try {
                return new URL(baseURL, url.toString() );
            } catch (MalformedURLException exp) {
            	exp.printStackTrace();
                return null;
            }
        //}
        //return null;
    }

}
