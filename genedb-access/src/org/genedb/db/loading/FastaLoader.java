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

import org.gmod.schema.feature.Contig;
import org.gmod.schema.feature.Supercontig;
import org.gmod.schema.feature.TopLevelFeature;
import org.gmod.schema.mapped.Feature;
import org.gmod.schema.mapped.Organism;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.annotation.Transactional;


/**
 * Load a FASTA file into the database as a concatenated sequence of contigs.
 *
 */
@Transactional(rollbackFor=DataError.class) // Will also rollback for runtime exceptions, by default
@Configurable
public class FastaLoader {

    private static final Logger logger = Logger.getLogger(FastaLoader.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private OrganismDao organismDao;

    // Configurable parameters
    private Organism organism;
    private Class<? extends TopLevelFeature> topLevelFeatureClass = Supercontig.class;
    private Class<? extends TopLevelFeature> entryClass = Contig.class;

    public enum OverwriteExisting {YES, NO}
    private OverwriteExisting overwriteExisting = OverwriteExisting.NO;

    /**
     * Set the organism into which to load data.
     *
     * @param organismCommonName the common name of the organism
     */
    public void setOrganismCommonName(String organismCommonName) {
        this.organism = organismDao.getOrganismByCommonName(organismCommonName);
        if (organism == null) {
            throw new IllegalArgumentException(String.format("Organism '%s' not found", organismCommonName));
        }
    }

    /**
     * Set the class of top-level feature that this FASTA file represents.
     * The default, if this method is not called, is <code>Supercontig</code>.
     *
     * @param topLevelFeatureClass
     */
    public void setTopLevelFeatureClass(Class<? extends TopLevelFeature> topLevelFeatureClass) {
        this.topLevelFeatureClass = topLevelFeatureClass;
    }

    /**
     * Set the class of feature that each entry in this FASTA file represents.
     * The default, if this method is not called, is <code>Contig</code>.
     *
     * @param entryClass
     */
    public void setEntryClass(Class<? extends TopLevelFeature> entryClass) {
        this.entryClass = entryClass;
    }

    /**
     * Whether we should overwrite an existing top-level feature if it has
     * the same name as the one specified in this file. The default, if this
     * method is not called, is <code>NO</code>.
     *
     * If overwriteExisting is <code>NO</code>, the file will be skipped on the
     * grounds that it's already loaded. If it's <code>YES</code>, the previously
     * existing top-level feature, and features located on it, will
     * be deleted first.
     *
     * @param overwriteExisting <code>YES</code> if we should overwrite an
     * existing top-level feature, or <code>NO</code> if not.
     */
    public void setOverwriteExisting(OverwriteExisting overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    /**
     * This method is called once for each FASTA file.
     *
     * @param fileId the identifier of the file
     * @param records the records the file contains
     */
    public void load(String fileId, Iterable<FastaRecord> records) {
        logger.debug(String.format("beginFastaFile(%s)", fileId));

        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
        StringBuilder concatenatedSequences = new StringBuilder();

        Feature existingTopLevelFeature = (Feature) session.createCriteria(Feature.class)
            .add(Restrictions.eq("organism", organism))
            .add(Restrictions.eq("uniqueName", fileId))
            .uniqueResult();

        if (existingTopLevelFeature != null) {
            switch (overwriteExisting) {
            case YES:
                existingTopLevelFeature.delete();
                break;
            case NO:
                logger.error(String.format("The organism '%s' already has feature '%s'",
                    organism.getCommonName(), fileId));
                return;
            }
        }
        TopLevelFeature topLevelFeature = null;
        if (topLevelFeatureClass != null) {
            topLevelFeature = TopLevelFeature.make(topLevelFeatureClass, fileId, organism);
            topLevelFeature.markAsTopLevelFeature();
            session.persist(topLevelFeature);
        }

        int start = 0;
        for (FastaRecord record: records) {
            String id = record.getId();
            String sequence = record.getSequence();

            if (topLevelFeature != null) {
                concatenatedSequences.append(sequence);
            }

            int end = start + sequence.length();
            TopLevelFeature entry = TopLevelFeature.make(entryClass, id, organism);
            entry.setResidues(sequence);
            if (topLevelFeature == null) {
                entry.markAsTopLevelFeature();
            } else {
                topLevelFeature.addLocatedChild(entry, start, end);
            }
            session.persist(entry);
            start = end;
        }

        if (topLevelFeature != null) {
            topLevelFeature.setResidues(concatenatedSequences.toString());
        }
    }
}
