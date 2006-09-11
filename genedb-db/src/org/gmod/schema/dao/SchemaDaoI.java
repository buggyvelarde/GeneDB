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


import java.sql.SQLException;
import java.util.List;

public interface SchemaDaoI {

    /**
     * Given a list of distict cvterm_id/type_id's of feature types
     * that have residues (from getResidueType()) in the given schema
     * and the schema name return a list of features in the schema
     * with residues.
     * @param cvterm_ids list of cvterm_id/type_id's
     * @param schema schema/organism name or null
     * @return the <code>List</code> of <code>Feature</code> objects
     * @throws SQLException
     */
    public List getResidueFeatures(List cvterm_ids, final String schema) throws SQLException;


    /**
     * For a schema return the type_id's with residues.
     * @param schema schema/organism name or null
     * @return the <code>List</code> of type_id's as <code>String</code> objects
     * @throws SQLException
     */
    public List getResidueType(String schema) throws SQLException;

    /**
     * Get available schemas (as a <code>List</code> of <code>String</code>
     * objects).
     * @return the available schemas
     * @throws SQLException
     */
    public List getSchema() throws SQLException;


}
