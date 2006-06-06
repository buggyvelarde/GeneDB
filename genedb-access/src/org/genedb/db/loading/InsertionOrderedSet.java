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

package org.genedb.db.loading;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * <code></code>
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
 *
 */
public class InsertionOrderedSet<T> extends ArrayList<T> implements Set<T> {

    public boolean add(T t) {
        if (this.contains(t)) {
            return false;
        }
        return super.add(t);
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean ret = false;
        Iterator<? extends T> it = c.iterator();
        while (it.hasNext()) {
            T t = it.next();
            boolean r = this.add(t);
            if ( !ret) {
                ret = r;
            }
        }
        return ret;
    }


} // end class
