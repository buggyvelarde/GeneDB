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

package org.genedb.web.mvc.controller.cgview;

import org.springframework.beans.factory.InitializingBean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmbossDigestParser implements InitializingBean {

    String embossDir; 
    
    private List<String> digests = (List<String>) Collections.EMPTY_LIST;
        
    public List<String> getDigests() {
        return digests;
    }

    public void afterPropertiesSet() throws Exception {
        parseDigests();
    }
    
    private void parseDigests() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(embossDir+"/data/REBASE/embossre.enz")));
        String line;
        digests = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            int space = line.indexOf(" ");
            if (space>1) {
                digests.add(line.substring(0, space));
            } else {
                System.err.println("Couldn't get enzyme name from '"+line+"'");
            }
        }
    }

    public void setEmbossDir(String embossDir) {
        this.embossDir = embossDir;
    }

}
