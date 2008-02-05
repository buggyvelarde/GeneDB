/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.genedb.web.gui;

import org.biojava.bio.Annotation;
import org.biojava.bio.gui.sequence.ImageMap;
import org.biojava.bio.seq.Feature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <code>ClientSide</code> represents a client-side style image
 * map.
 */
public class GeneDbClientSide implements ImageMap, Serializable {
    private String name;
    private List hotSpots;

    /**
     * Creates a new <code>GeneDbClientSide</code> image map.
     *
     * @param name a <code>String</code> name by which the map
     * will be known.
     */
    public GeneDbClientSide(String name) {
        this.name = name;
        hotSpots = new ArrayList();
        System.err.println("I'm a new HotSpot collector");
    }

    public void addHotSpot(HotSpot hotSpot) {
        System.err.println("I've got a new hotspot added");
        hotSpots.add(hotSpot);
    }

    public Iterator hotSpots() {
        return hotSpots.iterator();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("<map name=");
        sb.append("\"");
        sb.append(name);
        sb.append("\">\n");

        for (Iterator hi = hotSpots.iterator(); hi.hasNext();) {
            HotSpot hs = (HotSpot) hi.next();
            Integer [] coords = hs.getCoordinates();
            if ( hs.getURL() != null ) {

                sb.append("<area shape=\"");
                sb.append(hs.getType());
                sb.append("\"");
                String path = hs.getURL().getPath();
                if (!"www.deliberatelybogusaddress.com".equals(hs.getURL().getHost())) {
                    path = "http://" + hs.getURL().getHost() + hs.getURL().getPath();
                }
                sb.append(" href=\"");
                sb.append(path);
                //System.err.println("path is :" +path.toString());
                if ( hs.getURL().getQuery() != null) {
                    sb.append('?');
                    sb.append(hs.getURL().getQuery());
                    sb.append("\"");
                }

                Feature f = (Feature) hs.getUserObject();
				//System.err.println("The type of the USEROBJECT is "+f);
                Annotation an = f.getAnnotation();
                String desc = "No description available";
                String dname = " ";
                if ( an.containsProperty("Tooltip")) {
                    desc = (String) an.getProperty("Tooltip");
                }
                if ( an.containsProperty("primary_name")) {
                    dname = (String) an.getProperty("primary_name");
                } else {
                    if ( an.containsProperty("systematic_id")) {
                        dname = (String) an.getProperty("systematic_id");
                    } else {
                        if ( an.containsProperty("tmp_systematic_id")) {
                            dname = (String) an.getProperty("tmp_systematic_id");
                        }
                    }
                }
                sb.append(" alt=\"\"");
                //sb.append(desc);
                //sb.append("\"");

                sb.append(" onmouseover=\"javascript:void(zmenu('");
                sb.append(dname);
                sb.append("','','");
                sb.append(desc);
                sb.append("'));\"");

//              sb.append(" onMouseOut=\"showtext(document.");
//              sb.append(name);
//              sb.append(", '')\"");
                sb.append(" coords=\"");

                int lastDelim = coords.length - 2;
                char delim = ',';

                for (int i = 0; i < coords.length; i++) {
                    sb.append(coords[i]);

                    if (i <= lastDelim) {
                        sb.append(delim);
                    }
                }
                sb.append("\">\n");
            }
        }

        sb.append("</map>");

        return sb.toString();
    }
}
