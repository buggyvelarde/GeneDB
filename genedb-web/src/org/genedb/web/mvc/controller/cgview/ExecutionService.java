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

import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutionService {

    //private ProcessBuilder processBuilder;
    
    private List<String> command;
    
    private ExecutionEnvironment executionEnvironment;
    
    public Process start(Map<String, String> arguments) throws IOException {
        // TODO Substitute into cmd line from arguments
        ProcessBuilder pb = createProcessBuilder(arguments);
        return pb.start();
    }

    
    public Object startManaged(Map<String, String> arguments) throws IOException {
        ProcessBuilder pb = createProcessBuilder(arguments);
        Process p = pb.start();
        InputStream out = p.getInputStream();
        StringBuilder buffer = new StringBuilder();
        while (out.available() > 0) {
            buffer.append(out.read());
        }
        // TODO Pass to filter, get back object
        return null;
    }
    
    private ProcessBuilder createProcessBuilder(Map<String, String> arguments) {
        List<String> substituted = new ArrayList<String>(command.size());
        for (String word : command) {
            if (!word.startsWith(":")) {
                substituted.add(word);
            } else {
                String key = word.substring(1);
                if (arguments.containsKey(key)) {
                    substituted.add(arguments.get(key));
                } else {
                    substituted.add(word);
                }
            }
        }
 
        ProcessBuilder pb = new ProcessBuilder(substituted);
        pb.directory(executionEnvironment.getWorkingDirectory());
        pb.environment().putAll(executionEnvironment.getEnviron());
        return pb;
    }
 

    @Required
    public void setExecutionEnvironment(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

}
