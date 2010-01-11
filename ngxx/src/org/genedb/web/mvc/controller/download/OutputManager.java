package org.genedb.web.mvc.controller.download;

import org.genedb.web.utils.DownloadUtils;

import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.feature.Transcript;

import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.List;

public class OutputManager {

    private static Logger logger = Logger.getLogger(OutputManager.class);

    private OutputFormat outputFormat;
    private OutputContent outputContent;


    public OutputManager(OutputFormat outputFormat, OutputContent outputContent) {
        super();
        this.outputFormat = outputFormat;
        this.outputContent = outputContent;
    }


    public void write(TopLevelFeature feature, PrintWriter w) {
        if (!feature.isTopLevelFeature()) {
            logger.error(String.format("The feature '%s' isn't actually a top level feature", feature.getUniqueName()));
            return;
        }

        switch (outputFormat) {
        case FASTA:
            fastaOutput(feature, w);
            break;


        }
    }


    private void fastaOutput(TopLevelFeature feature, PrintWriter out) {

        switch(outputContent) {
        case ALL:
            DownloadUtils.writeFasta(out, feature.getUniqueName(), feature.getResidues());
            return;
        case TRANSCRIPT:
            List<Transcript> transcripts = null;//feature.getLocatedChildrenByClass(Transcript.class);
            for (Transcript transcript : transcripts) {
                DownloadUtils.writeFasta(out, feature.getUniqueName(), feature.getResidues());
            }
            return;
        }

    }

}
