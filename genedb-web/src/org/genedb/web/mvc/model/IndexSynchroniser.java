package org.genedb.web.mvc.model;

import java.util.Set;

import org.apache.log4j.Logger;
import org.genedb.db.audit.ChangeSet;
import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Gap;
import org.gmod.schema.feature.Gene;
import org.gmod.schema.feature.Polypeptide;
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
            
            if(feature instanceof Polypeptide){
                logger.debug("Poly added: " + ((Polypeptide)feature).getClass());
            }else{
                logger.debug(feature);
            }
            
            logger.debug("--featureID: " + featureId + " loaded...");
            session.index(feature);
            
            logger.debug("--featureID: " + featureId + " indexed...");
        } 
        
        logger.debug("Exiting indexFeatures");
    }
    
    private void deleteFeatures(FullTextSession session, ChangeSet changeSet){
        logger.debug("Starting deleteFeatures");
        
        // Let's process deletes first
        Set<Integer> deletedIds = Sets.newHashSet();
        deletedIds.addAll(changeSet.deletedFeatureIds(Gene.class));
        deletedIds.addAll(changeSet.deletedFeatureIds(Transcript.class));
        deletedIds.addAll(changeSet.deletedFeatureIds(Polypeptide.class));
        deletedIds.addAll(changeSet.deletedFeatureIds(Gap.class));
        
        for (Integer featureId : deletedIds) {    
            session.purge(Feature.class, featureId);
            logger.debug("featureID: " + featureId + " purged...");
        } 
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
