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

import org.gmod.schema.cv.Cv;
import org.gmod.schema.cv.CvTerm;
import org.gmod.schema.general.DbXRef;
import org.gmod.schema.utils.CountedName;

import java.util.List;

public interface CvDaoI extends BaseDaoI {

    /**
     * Get a CV by id
     *
     * @param id the cv id (primary key)
     * @return the corresponding Cv, or null
     */
    public Cv getCvById(int id);

    /**
     * Retrieve a list of Cvs whose names match the given pattern
     * @param name
     * @return
     */
    public List<Cv> getCvsByNamePattern(String namePattern);

    /**
     * Retrieve a controlled vocabulary by its name
     *
     * @param name the name to lookup
     * @return the named Cv, or null if there is no Cv of that name
     */
    public Cv getCvByName(String name);

    /**
     * Retrieve a CvTerm by id
     *
     * @param id then cvterm id (primary key)
     * @return the corresponding CvTerm, or null
     */
    public CvTerm getCvTermById(int id);


    // TODO Should this return a list or just one?
    /**
     * Retrieve a named CvTerm from a given Cv
     *
     * @param cvTermName the name of the cvterm
     * @param cv the controlled vocabulary this cvterm is part of
     * @return a (possibly empty) list of matching cvterms
     */
    public List<CvTerm> getCvTermByNameInCv(String cvTermName, Cv cv);


    /**
     * Retrieve a CvTerm from the Gene Ontology
     *
     * @param value the
     * @return the corresponding CvTerm, or null
     */
    public CvTerm getGoCvTermByAcc(String value);

    /**
     * Retrieve a CvTerm from the Gene Ontology via it's database entry
     *
     * @param id the database name eg GO:123456
     * @return the corresponding CvTerm, or null
     */
    public CvTerm getGoCvTermByAccViaDb(final String id);

    /**
     * Retrieve all CvTerms
     * @return a list of all cvterms
     */
    public List<CvTerm> getCvTerms();

    /**
     * Retrieve a named CvTerm from a given Cv
     *
     * @param cvTermName the name of the cvterm
     * @param cvName the controlled vocabulary name this cvterm could be part of
     * @return a (possibly empty) cvterm
     */
    public CvTerm getCvTermByNameAndCvName(String cvTermName, String cvName);

    /**
     * Take a cv and cvterm and look it up, or create it if it doesn't exist
     *
     * @param cv name of the cv, which must already exist
     * @param cvTerm the cvTerm to find/create
     * @return the created or looked-up CvTerm
     */
    public CvTerm findOrCreateCvTermByNameAndCvName(String cvName, String cvTermName);

    /**
     * Get a CvTerm by DbXRef
     *
     * @param dbXRef the DbXRef
     * @return the corresponding CvTerm, or null
     */
    public CvTerm getCvTermByDbXRef(DbXRef dbXRef);

    public boolean existsNameInOntology(String name, Cv ontology);

    public List<String> getPossibleMatches(String search, Cv cv, int limit);

    public List<CountedName> getAllTermsInCvWithCount(Cv cv);
}
