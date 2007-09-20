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

package org.genedb.jogra.domain;

import java.util.HashMap;
import java.util.Map;

public class ElementDao {
    
    private Map<String, Element> mapping = new HashMap<String, Element>();
    
    public ElementDao() {
        String[] names = {"abc1234", "abc5678", "def1234"};
        for (String name : names) {
            mapping.put(name, new Element(name));
        }
    }

    public Element retrieveElementByName(String name) {
        return mapping.get(name);
    }
    
    

}
