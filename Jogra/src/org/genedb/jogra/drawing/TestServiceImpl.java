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

package org.genedb.jogra.drawing;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureProp;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
@Transactional
public class TestServiceImpl implements TestService {
    
    private SessionFactory sessionFactory;
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Feature> doSomething1() {
        Query q = sessionFactory.getCurrentSession().createQuery("");
        List<Feature> features = (List<Feature>) q.list();
        return features;
    }

    public void doSomething2() {
        List<Feature> features = doSomething1();
        Feature f = features.get(0);
        Collection<FeatureProp> props = f.getFeatureProps();
        for (FeatureProp featureProp : props) {
            System.err.println(featureProp.getCvTerm().getName()+"  :  "+featureProp.getValue());
        }
    }

}
