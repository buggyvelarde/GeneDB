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

package org.genedb.db.loading;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.NodeFactory;
import nu.xom.Serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class OrthologueStorage {
    private List<String> orthologues = new ArrayList<String>();
    private List<String> paralogues = new ArrayList<String>();
    private List<String> curatedOrthologues = new ArrayList<String>();
    private Map<String, List<String>> clusters = new HashMap<String, List<String>>();
    private boolean full = false;
    
    public void addOrthologue(String s) {
        this.orthologues.add(s);
        full = true;
    }
    
    
    public void addParalogue(String s) {
        this.paralogues.add(s);
        full = true;
    }
    
    public void addCuratedOrthologue(String s) {
        this.curatedOrthologues.add(s);
        full = true;
    }
    
    public void addCluster(String key, String s) {
        List<String> list;
        if (clusters.containsKey(key)) {
            list = clusters.get(key);
        } else {
            list = new ArrayList<String>();
            clusters.put(key, list);
        }
        list.add(s);
        full = true;
    }
    
    public void serialize(File f) throws IOException {
        Element root = new Element("orthologue_storage");
        Element orthologueElements = new Element("orthologues");
        root.appendChild(orthologueElements);
        
        Element curatedOrthologueElements = new Element("curated_orthologues");
        root.appendChild(curatedOrthologueElements);
        
        Element paralogueElements = new Element("paralogues");
        root.appendChild(paralogueElements);
        
        Element clusterElements = new Element("clusters");
        root.appendChild(clusterElements);
        
        Document document = new Document(root);
        FileOutputStream fos = new FileOutputStream(f);
        Serializer s = new Serializer(fos);
        s.write(document);
        fos.close();
    }
    
//    public static void main(String[] args) throws IOException {
//        OrthologueStorage os = new OrthologueStorage();
//        os.addOrthologue("wibble");
//        os.addCuratedOrthologue("wobble");
//        os.serialize(new File("/Users/art/Desktop/blah.txt"));
//    }

}
