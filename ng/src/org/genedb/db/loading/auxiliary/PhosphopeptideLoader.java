package org.genedb.db.loading.auxiliary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.ModifiedAminoAcidFeature;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.FeatureLoc;
import org.hibernate.Session;

public class PhosphopeptideLoader extends Loader {

    private static final Logger logger    = Logger.getLogger(PhosphopeptideLoader.class);
    private int                 n         = 0;
    private String              delimiter = ",";
    private boolean delete = false;

    private class Instruction {
        private String  geneName;
        private int     position;

        Instruction(String line) {
            String[] split = line.split(delimiter);

            geneName = split[0];
            position = Integer.parseInt(split[1]);

        }
    }

    @Override
    protected void doLoad(InputStream inputStream, Session session) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        LinkedHashMap<String, List<Instruction>> instructions = new LinkedHashMap<String, List<Instruction>>();

        // to catch any format errors prior to loading, preparse the file
        while ((line = reader.readLine()) != null) {
            if (line.length() > 0 && line.contains(delimiter)) {

                Instruction instruction = new Instruction(line);

                if (!instructions.containsKey(instruction.geneName)) {
                    instructions.put(instruction.geneName, new ArrayList<Instruction>());
                }
                instructions.get(instruction.geneName).add(instruction);

            }
        }

        for (Entry<String, List<Instruction>> geneInstructions : instructions.entrySet()) {

            for (Instruction instruction : geneInstructions.getValue()) {
                Feature feature = sequenceDao.getFeatureByUniqueName(instruction.geneName, Feature.class);

                if (feature == null) {
                    logger.warn(String.format("%s is not a feature, skipping", instruction.geneName));
                    continue;
                }

                if (feature instanceof AbstractGene) {
                    createOrDestroy(session, instruction, (AbstractGene) feature);
                } else if (feature instanceof Transcript) {
                    createOrDestroy(session, instruction, (Transcript) feature);
                } else if (feature instanceof Polypeptide) {
                    createOrDestroy(session, instruction, (Polypeptide) feature);
                } else {
                    logger.warn(String.format("%s is not a gene model feature, skipping", instruction.geneName));
                    continue;
                }

            }
        }
    }

    private void createOrDestroy(Session session, Instruction instruction, AbstractGene gene) {
        for (Transcript transcript : gene.getTranscripts()) {
            createOrDestroy(session, instruction, transcript);
        }
    }

    private void createOrDestroy(Session session, Instruction instruction, Transcript transcript) {
        Polypeptide polypeptide = transcript.getPolypeptide();
        if (polypeptide != null) {
            createOrDestroy(session, instruction, polypeptide);
        } else {
            logger.warn(String.format("Could not find a polypeptide on %s", transcript.getUniqueName()));
        }
    }

    private void createOrDestroy(Session session, Instruction instruction, Polypeptide polypeptide) {
        boolean found = false;

        for (FeatureLoc loc : polypeptide.getAminoAcidFeatureLocs(ModifiedAminoAcidFeature.class)) {
            if (loc.getFmin().equals(instruction.position)) {

                found = true;

                // easiest to delete it when we have hold of the featureloc
                if (delete) {
                    ModifiedAminoAcidFeature maaf = (ModifiedAminoAcidFeature) loc.getFeature();

                    logger.info(String.format("Deleting %s %s", instruction.geneName, instruction.position));

                    session.delete(loc);
                    session.delete(maaf);
                    return;
                }
            }
        }

        // can't find it, instructed not to delete it, therefore must create it
        if (!found && !delete) {

            logger.info(String.format("Creating %s %s", instruction.geneName, instruction.position));

            ModifiedAminoAcidFeature maaf = new ModifiedAminoAcidFeature();
            FeatureLoc floc = new FeatureLoc(polypeptide, maaf, instruction.position, false, instruction.position, false, (short) 0, 0, 0, 0);

            session.persist(maaf);
            session.persist(floc);
        }

        if (n % 50 == 1) {
            logger.info("Clearing session");
            session.flush();
            session.clear();
        }
        n++;

    }
    
    @Override
    protected Set<String> getOptionNames() {
        Set<String> options = new HashSet<String>();
        Collections.addAll(options, "delimiter", "delete");
        return options;
    }
    
    @Override
    protected boolean processOption(String optionName, String optionValue) {
        
        logger.info(String.format("Setting option: '%s' :: '%s'", optionName, optionValue));
        
        if (optionName.equals("delimiter")) {
            delimiter = optionValue;
            return true;
        }
        if (optionName.equals("delete")) {
            delete = Boolean.parseBoolean(optionValue);
            return true;
        }
        
        return false;
    }

}
