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

package org.gmod.schema.dao;

import org.genedb.db.helpers.NameLookup;
import org.genedb.db.hibernate3gen.CvTerm;
import org.genedb.db.hibernate3gen.FeatureCvTerm;
import org.genedb.db.hibernate3gen.Synonym;
import org.genedb.db.jpa.Feature;

import java.util.List;

public interface SequenceDaoI {

    /**
     * Return the feature corresponding to this feature_id 
     * 
     * @param id the systematic id
     * @return the Feature, or null
     */
    public Feature getFeatureById(int id);

    /**
     * Return a features with this systematic id
     *  
     * @param name the systematic id
     * @return the Feature, or null
     */
    public Feature getFeatureByUniqueName(String name);

    /**
     * Return a list of features with any current (ie non-obsolete) name or synonym
     *  
     * @param name the lookup name
     * @return a (possibly empty) List<Feature> of children with this current name
     */
    public List getFeaturesByAnyCurrentName(String name);
    
    /**
     * Return a list of features with this name or synonym (including obsolete names)
     *  
     * @param name the lookup name
     * @return a (possibly empty) List<Feature> of children with this name
     */
    public List getFeaturesByAnyName(NameLookup nl, String featureType);

    
    // TODO Document overlap behaviour
    /**
     * Return a list of features located on a source Feature, within a given range
     *  
     * @param min the minimum (interbase) coordinate
     * @param max the maximum (interbase) coordinate
     * @param strand 
     * @param parent the source feature
     * @param type 
     * @return a List<Feature> which ??? this range
     */
    public List getFeaturesByRange(int min, int max, int strand,
            Feature parent, String type);

    /**
     * Return a list of features located on a source Feature 
     *  
     * @param parent the parent feature
     * @return a (possibly empty) List<Feature> of children located on this parent
     */
    public List getFeaturesByLocatedOnFeature(Feature parent);

    /**
     * Return the FeatureCvTerm that links a given Feature and CvTerm, with a given value of 'not'
     * 
     * @param feature the Feature to test the link for
     * @param cvTerm the CvTerm to test the link for
     * @param not test for the not flag in the FeatureCvTerm 
     * @return the Feature, or null
     */
    public FeatureCvTerm getFeatureCvTermByFeatureAndCvTerm(Feature feature,
            CvTerm cvTerm, boolean not);

    /**
     * Return a synonym of the given name and type if it exists
     * 
     * @param name the name to lookup
     * @param type the type of the Synonym
     * @return a Synonym, or null  
     */
    public Synonym getSynonymByNameAndCvTerm(String name, CvTerm type);

    /**
     * Return a list of FeatureSynonyms which link a given Feature and Synonym
     * 
     * @param feature the test Feature
     * @param synonym the test Synonym
     * @return a (possibly empty) List<FeatureSynonym>
     */
    public List getFeatureSynonymsByFeatureAndSynonym(
            Feature feature, Synonym synonym);

}
