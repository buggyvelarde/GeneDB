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

package org.genedb.ptoolsFilter;

import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FilteringUrlFilenameViewController extends
        UrlFilenameViewController {

    private boolean allowSubdirs=false;
    private boolean directOutput=true;
    private boolean directoryListing=false;
    private List allowedExtensions;
    
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = super.handleRequestInternal(request, response);
        
        String viewName = mav.getViewName();
        File view = new File(viewName);
        if (!view.exists()) {
            throw new RuntimeException("Not a valid file '"+viewName+"'");
        }

        try {
            if (!view.getCanonicalPath().startsWith(getPrefix())) {
                throw new RuntimeException("Not a legal file '"+view.getCanonicalPath()+"'");
            }
        } catch (IOException exp) {
            throw new RuntimeException("Not a valid file '"+viewName+"'");
        }
        
        if (directOutput) {
            try {
                OutputStream out = response.getOutputStream();
                InputStream in = new FileInputStream(view);
                FileCopyUtils.copy(in, out);
            } catch (IOException exp) {
                throw new RuntimeException("Can't copy '"+viewName+"'");
            }
        }
            
            return null;
        
        //return viewName;
    }

    
}
