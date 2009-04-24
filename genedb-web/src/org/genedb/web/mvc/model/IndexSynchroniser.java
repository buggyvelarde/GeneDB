package org.genedb.web.mvc.model;

import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.audit.ChangeSet;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.MRNA;
import org.gmod.schema.feature.NcRNA;
import org.gmod.schema.feature.Polypeptide;
import org.gmod.schema.feature.Pseudogene;
import org.gmod.schema.feature.PseudogenicTranscript;
import org.gmod.schema.feature.RRNA;
import org.gmod.schema.feature.Region;
import org.gmod.schema.feature.SnRNA;
import org.gmod.schema.feature.TRNA;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@Repository
@Transactional
public class IndexSynchroniser implements IndexUpdater{
    private static final Logger logger = Logger.getLogger(IndexSynchroniser.class);

    private SessionFactory sessionFactory;

    @Transactional
    public boolean updateAllCaches(ChangeSet changeSet) {
        logger.debug("Starting updateAllCaches");
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);    
        FullTextSession fullTextSession = Search.createFullTextSession(session);

        //Delete deleted features
        deleteFeatures(fullTextSession, changeSet);
        
        //Index altered features
        indexFeatures(fullTextSession, changeSet);
        
        return true;
    }
    
    private void indexFeatures(FullTextSession session, ChangeSet changeSet){
        logger.debug("Starting indexFeatures");
        
        Set<Integer> alteredIds = Sets.newHashSet();
        alteredIds.addAll(changeSet.newFeatureIds(Gene.class));
        alteredIds.addAll(changeSet.changedFeatureIds(Gene.class));
        alteredIds.addAll(changeSet.newFeatureIds(Transcript.class));
        alteredIds.addAll(changeSet.changedFeatureIds(Transcript.class));
        alteredIds.addAll(changeSet.newFeatureIds(Polypeptide.class));
        alteredIds.addAll(changeSet.changedFeatureIds(Polypeptide.class));
        alteredIds.addAll(changeSet.newFeatureIds(Gap.class));
        alteredIds.addAll(changeSet.changedFeatureIds(Gap.class));

        for (Integer featureId : alteredIds) {    
            logger.debug("featureID " + featureId + " being loaded");
            Feature feature = (Feature)session.load(Feature.class, featureId);            
            session.index(feature);            
            logger.debug("--featureID: " + featureId + " indexed...");
        } 
        
        logger.debug("Exiting indexFeatures");
    }
    
    private void deleteFeatures(FullTextSession session, ChangeSet changeSet){
        logger.debug("Starting deleteFeatures");
        for(Integer featureId: changeSet.deletedFeatureIds(Gene.class)){   
            session.purge(Gene.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        for(Integer featureId: changeSet.deletedFeatureIds(MRNA.class)){   
            session.purge(MRNA.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        for(Integer featureId: changeSet.deletedFeatureIds(TRNA.class)){   
            session.purge(TRNA.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        for(Integer featureId: changeSet.deletedFeatureIds(Transcript.class)){   
            session.purge(Transcript.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        for(Integer featureId: changeSet.deletedFeatureIds(Polypeptide.class)){   
            session.purge(Polypeptide.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        for(Integer featureId: changeSet.deletedFeatureIds(Gap.class)){   
            session.purge(Gap.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        }
        logger.debug("Ended deleteFeatures");
    }
    
    /*
     * This is useful for JUnit testing
     */
    @Transactional
    public void purgeAll(){
        logger.debug("Starting purgeAll");
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);    
        FullTextSession fullTextSession = Search.createFullTextSession(session);
        fullTextSession.purgeAll(Gap.class);
        fullTextSession.purgeAll(Gene.class);
        fullTextSession.purgeAll(Transcript.class);
        fullTextSession.purgeAll(MRNA.class);
        fullTextSession.purgeAll(NcRNA.class);
        fullTextSession.purgeAll(TRNA.class);
        fullTextSession.purgeAll(RRNA.class);
        fullTextSession.purgeAll(SnRNA.class);
        fullTextSession.purgeAll(Polypeptide.class);
        fullTextSession.purgeAll(Pseudogene.class);
        fullTextSession.purgeAll(PseudogenicTranscript.class);
        fullTextSession.getSearchFactory().optimize();
        logger.debug("Ended purgeAll");
    }    

    
    /*
     * This is useful for JUnit testing
     */
    @Transactional
    public void indexSingle(Integer featureId){
        logger.debug("Starting indexSingle");
        Session session = SessionFactoryUtils.getSession(sessionFactory, false);    
        FullTextSession fullTextSession = Search.createFullTextSession(session);
        Feature feature = (Feature)fullTextSession.load(Feature.class, featureId);
        fullTextSession.index(feature);
        logger.debug("Ended indexSingle");        
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
