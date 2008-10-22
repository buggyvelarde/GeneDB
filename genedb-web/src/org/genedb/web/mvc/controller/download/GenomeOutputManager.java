package org.genedb.web.mvc.controller.download;

import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.mapped.Organism;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class GenomeOutputManager {

    private Organism organism;
    private SequenceDao sequenceDao;

    public GenomeOutputManager(Organism organism) {
        this.organism = organism;
    }


    void writeStream(PrintWriter pw, OutputFormat of) {
        if (!goodOutputFormat(of)) {
            return;
        }

        OutputManager outputManager = new OutputManager(of, OutputContent.ALL);

        List<TopLevelFeature> topLevelFeatures = null;//organism.getTopLevelFeatures();
        for (TopLevelFeature topLevelFeature : topLevelFeatures) {
            outputManager.write(topLevelFeature, pw);
        }
    }

    void writeFiles(File outputDir, OutputFormat of) throws IOException {
        if (!goodOutputFormat(of)) {
            return;
        }
        OutputManager outputManager = new OutputManager(of, OutputContent.ALL);

        List<TopLevelFeature> topLevelFeatures = null;//organism.getTopLevelFeatures();
        for (TopLevelFeature topLevelFeature : topLevelFeatures) {
            File out = new File(outputDir, topLevelFeature.getUniqueName());
            PrintWriter pw = new PrintWriter(new FileWriter(out));

            outputManager.write(topLevelFeature, pw);
            pw.close();
        }
    }


    private boolean goodOutputFormat(OutputFormat of) {

        return false;
    }

}
