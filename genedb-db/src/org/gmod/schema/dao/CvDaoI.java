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

import org.genedb.db.hibernate3gen.Cv;
import org.genedb.db.hibernate3gen.CvTerm;

import java.util.List;

public interface CvDaoI {

    
    /**
     * Get a CV by id
     * 
     * @param id the cv id (primary key)
     * @return the corresponding Cv, or null
     */
    public abstract Cv getCvById(int id);


    /**
     * 
     * 
     * @param name
     * @return
     */
    public abstract List<Cv> getCvByName(String name);

    /**
     * Retrieve a CvTerm by id
     * 
     * @param id then cvterm id (primary key)
     * @return the corresponding CvTerm, or null
     */
    public abstract CvTerm getCvTermById(int id);


    // TODO Should this return a list or just one?
    /**
     * Retrieve a named CvTerm from a given Cv
     * 
     * @param cvTermName the name of the cvterm
     * @param cv the controlled vocabulary this cvterm is part of
     * @return a (possibly empty) list of matching cvterms
     */
    public abstract List<CvTerm> getCvTermByNameInCv(String cvTermName, Cv cv);


    /**
     * 
     * 
     * @param value
     * @return
     */
    public abstract CvTerm getGoCvTermByAcc(String value);


    /**
     * 
     * @param id
     * @return
     */
    public abstract CvTerm getGoCvTermByAccViaDb(final String id);

}
