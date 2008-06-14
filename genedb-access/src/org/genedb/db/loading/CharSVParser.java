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

/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
package org.genedb.db.loading;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


public class CharSVParser {

    private String[][] ret;
    private boolean inMemory;
    private String comment;
    private boolean parsed = false;
    private BufferedReader in;
    private String columnSeperator;
    private List<CharSVParsingListener> listeners;
    private CharSVParsingEvent event;
    private int skip;

    public CharSVParser(Reader r) {
        this(r, "\t", true, 0, null);
    }

    public CharSVParser(Reader r, String columnSeperatorRegexp, boolean inMemory, int skip, String comment) {
        if ( r instanceof BufferedReader ) {
            in = (BufferedReader) r;
        } else {
            in = new BufferedReader(r);
        }
        this.columnSeperator = columnSeperatorRegexp;
        this.skip = skip;
        this.inMemory = inMemory;
        this.comment = comment;
    }


    public void go() throws IOException {
        String line;
        List<String[]> temp = new ArrayList<String[]>();
        if (skip > 0) {
            while ( in.readLine() != null && skip > 0 ) {
                skip--;
            }
        }
        while ((line = in.readLine()) != null) {
            // Process line
            //System.err.println("Line is " + line);
        		if (isComment(line)) {
        			continue;
        		}
            String[] sa = line.split(columnSeperator);
            //System.err.println(line);
            fireEvent(sa);
            if ( inMemory ) {
                temp.add(sa);
            }
        }
        // Convert collection to an array of String arrays
        if ( inMemory ) {
            if ( temp.size() > 0 ) {
                ret = temp.toArray(new String[1][]);
            } else {
                ret = null;
            }
        }
        parsed = true;
    }



    private void fireEvent(String[] val) {
        if (listeners != null) {
            event.setTerms(val);
            for (CharSVParsingListener listener : listeners)
                listener.rowParsed(event);
        }
    }

    public void addCharSVParsingListener( CharSVParsingListener l) {
        if ( listeners == null ) {
            listeners = new ArrayList<CharSVParsingListener>();
        }
        listeners.add(l);
        if ( event == null) {
            event = new CharSVParsingEvent();
        }
    }

    public void removeCharSVParsingListeners( CharSVParsingListener l) {
        listeners.remove(l);
        if ( listeners.size() == 0) {
            listeners = null;
        }
    }

    private boolean isComment(String line) {
        return ( comment != null  && line.startsWith( comment ));
    }

    public String[][] getValues() throws IOException {
        if ( !parsed ) {
            go();
        }
        return ret;
    }
}
