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

package org.genedb.db.loading;

import org.genedb.db.dao.OrganismDao;
import org.genedb.db.dao.SequenceDao;

import org.gmod.schema.feature.Chromosome;
import org.gmod.schema.feature.Contig;
import org.gmod.schema.mapped.Organism;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.utils.ChangeVetoException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;

import uk.co.flamingpenguin.jewel.cli.ArgumentValidationException;
import uk.co.flamingpenguin.jewel.cli.Cli;
import uk.co.flamingpenguin.jewel.cli.CliFactory;
import uk.co.flamingpenguin.jewel.cli.Option;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;



/**
 * This class is the main entry point for the new GeneDB data miners.
 * It's designed to be called from the command-line. It looks for a
 * config. file which specifies which files to process.
 *
 * Usage: NewRunner common_nane [config_file]
 *
 *
 * @author Adrian Tivey (art)
 */
@Transactional
@Configurable
public class FastaLoader implements FastaLoaderI {

    protected static final Log logger = LogFactory.getLog(FastaLoader.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private SequenceDao sequenceDao;

    @Autowired
    private OrganismDao organismDao;

    // These are set (directly or indirectly) from the command line
    private String dataDirectory;

    private Organism organism;


    /**
     * Create a list of Biojava sequences from a FASTA file. It fails fatally if no sequences are found.
     *
     * @param file the FASTA file to read in
     * @return the list of sequences
     */
    public List<Sequence> extractSequencesFromFile(File file) {
        logger.debug("Parsing file '"+file.getAbsolutePath()+"'");
        List<Sequence> ret = new ArrayList<Sequence>();

        Reader in = null;
        try {
            in = new FileReader(file);
            SequenceIterator seqIt = SeqIOTools.readFastaDNA(new BufferedReader(in));

            while ( seqIt.hasNext() ) {
                ret.add(seqIt.nextSequence());
            }

        } catch (FileNotFoundException exp) {
            System.err.println("Couldn't open input file: " + file);
            exp.printStackTrace();
            System.exit(-1);
        } catch (BioException exp) {
            System.err.println("Couldn't open input file: " + file);
            exp.printStackTrace();
            System.exit(-1);
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Shouldn't happen!
                    e.printStackTrace();
                }
            }
        }
        if (ret.size() == 0) {
            logger.fatal("No sequences found in '"+file.getAbsolutePath()+"'");
            System.exit(-1);
        }
        return ret;
    }

    /**
     * The core processing loop. Read the config file to find out which EMBL files to read,
     * and which 'synthetic' features to create
     */
    @Transactional
    public void process() {
        long start = new Date().getTime();

        File[] files = getFiles();
        for (File file : files) {
            List<Sequence> seqs = this.extractSequencesFromFile(file);
            processSequence(file, seqs);
        }

        long duration = (new Date().getTime()-start)/1000;
        logger.info("Processing completed: "+duration / 60 +" min "+duration  % 60+ " sec.");
    }


    private File[] getFiles() {
        File f = new File(dataDirectory);
        return f.listFiles();
    }

    /**
     * This method is called once for each sequence. First it examines the source features,
     * then CDSs, then other features
     *
     * @param seq The sequence to parse
     * @param parent The parent object, if reparenting is taking place, or null
     * @param offset The base offset, when reparenting is taking place
     */
    private void processSequence(File file, List<Sequence> sequenceList) {

        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        try {
            logger.debug("Processing '"+file.getAbsolutePath()+"'");

            StringBuilder allResidues = new StringBuilder();

            for (Sequence sequence : sequenceList) {
                allResidues.append(sequence.seqString());
            }
            logger.info(String.format("The length of '%s' is '%d'", file.getAbsolutePath(), allResidues.length()));

            // Create chromosome from this sequence
            Timestamp now = new Timestamp(new Date().getTime());
            Chromosome chromosome = new Chromosome(organism, file.getName(), false, false, now);
            chromosome.setResidues(allResidues.toString());
            chromosome.markAsTopLevelFeature();

            session.persist(chromosome);

            int start = 0;
            for (Sequence sequence : sequenceList) {
                int end = start + sequence.length();
                logger.info(String.format("  Creating a contig from '%d' to '%d'", start, end));
                Contig contig = new Contig(organism, sequence.getName(), false, false, now);
                chromosome.addLocatedChild(contig, start, end);
                session.persist(contig);
                start = end;
            }
        } catch (ChangeVetoException exp) {
            exp.printStackTrace();
        }

        session.flush();
    }


    /**
     * Main entry point. It uses a BeanPostProcessor to apply a set of overrides
     * based on a Properties file, based on the organism. This is passed in on
     * the command-line.
     *
     * @param args organism_common_name, [conf file path]
     */
    public static void main (String[] args) {

        Cli<Args> cli = CliFactory.createCli(Args.class);
        Args arguments = null;
        try {
          arguments = cli.parseArguments(args);
        }
        catch(ArgumentValidationException exp) {
            System.err.println("Unable to run:");
            System.err.println(cli.getHelpMessage());
            return;
        }

        // Override properties in Spring config file (using a
        // BeanFactoryPostProcessor) based on command-line args
        Properties overrideProps = new Properties();
        overrideProps.setProperty("datasource.dbname", "malaria_workshop");
        overrideProps.setProperty("dataSource.username", "pathdb");
        overrideProps.setProperty("datasource.dbport", "10101");
        overrideProps.setProperty("dataSource.dbhost", "pathdbsrv1a");

        PropertyOverrideHolder.setProperties("dataSourceMunging", overrideProps);

        ApplicationContext ctx = new ClassPathXmlApplicationContext(
                new String[] {"FastaLoader.xml"});

        FastaLoaderI runner = (FastaLoaderI) ctx.getBean("runner", FastaLoaderI.class);
        runner.setOrganismName(arguments.getOrganismCommonName());
        runner.setDataDirectory(arguments.getDataDirectory());
        runner.process();
    }

    public void setOrganismName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Can't recognise '%s' as an organism name", organismCommonName));
        }
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    interface Args {
        @Option(shortName="o", description="Organism common name")
        String getOrganismCommonName();

        @Option(shortName="d", description="Path to data directory")
        String getDataDirectory();

    }

}
