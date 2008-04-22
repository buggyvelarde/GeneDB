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

package org.genedb.web.gui;


/**
 * Data structure for passing around where a map and its imagemap
 * is, in terms of real data and web URL
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
public class ImageInfo {

	// Fields for protein map
    public String protStructUrl;
    public String protStructMap;
    public String protStructMapData;
    public String protStructMsg;

    // Fields for context map
    public String contextUrl;
    public String contextMap;
    public String contextMapData;
    public String contextMsg;




}
