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

package org.genedb.jogra.controller;

import org.springframework.util.StringUtils;

public class ImageFormBean {
    
    private final String[] EMPTY_STRING_ARRAY = new String[0];
    
    private String slideIds;
    private String[] anatomyNames;
    
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("ImageFormBean: slidesIds='");
        ret.append(slideIds);
        ret.append("' anatomyNames='");
        ret.append(StringUtils.arrayToDelimitedString(anatomyNames, ":"));
        ret.append("'");
        return ret.toString();
    }
    
    public String[] getAnatomyNames() {
        if (this.anatomyNames == null) {
            return EMPTY_STRING_ARRAY;
        }
        return this.anatomyNames;
    }
    
    public void setAnatomyNames(String[] anatomyNames) {
        this.anatomyNames = anatomyNames;
    }
    
    public String getSlideIds() {
        if (this.slideIds == null) {
            return "";
        }
        return this.slideIds;
    }
    
    public void setSlideIds(String slideIds) {
        this.slideIds = slideIds;
    }
    

}
