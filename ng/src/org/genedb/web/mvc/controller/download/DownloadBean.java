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

package org.genedb.web.mvc.controller.download;


import java.util.List;


public class DownloadBean {

    private int historyItem = 0;
    private int version;
    private OutputDestination outputDestination = OutputDestination.TO_BROWSER;
    private OutputFormat outputFormat;
    private List<OutputOption> outputOption;
    private SequenceType sequenceType;

    public OutputDestination getOutputDestination() {
        return outputDestination;
    }

    public void setOutputDestination(OutputDestination outputDestination) {
        this.outputDestination = outputDestination;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(OutputFormat outputFormat) {
        this.outputFormat = outputFormat;
    }

    public List<OutputOption> getOutputOption() {
        return outputOption;
    }

    public void setOutputOption(List<OutputOption> outputOption) {
        this.outputOption = outputOption;
    }

    public int getHistoryItem() {
        return this.historyItem;
    }

    public void setHistoryItem(int historyItem) {
        this.historyItem = historyItem;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
    }


}

