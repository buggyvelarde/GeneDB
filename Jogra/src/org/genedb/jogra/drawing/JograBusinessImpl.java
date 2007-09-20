package org.genedb.jogra.drawing;

import org.gmod.schema.sequence.Feature;
import org.gmod.schema.sequence.FeatureLoc;
import org.gmod.schema.sequence.FeatureProp;
import org.gmod.schema.sequence.FeatureRelationship;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class JograBusinessImpl implements JograBusiness {

	private TestService testService;
	
	public void testTransactions() {
        //testService.doSomething2();
        List<Feature> features = testService.doSomething1();
        Feature f = features.get(0);
        f.getUniqueName();
        System.err.println(f);
        for (FeatureProp featureProp : f.getFeatureProps()) {
            System.err.println(featureProp.getCvTerm().getName()+"  :  "+featureProp.getValue());
        }
        for (FeatureRelationship rel : f.getFeatureRelationshipsForSubjectId()) {
			System.err.println(rel.getCvTerm().getName());
		}
        for (FeatureRelationship rel : f.getFeatureRelationshipsForObjectId()) {
			System.err.println(rel.getCvTerm().getName());
		}
        for (FeatureLoc loc : f.getFeatureLocsForFeatureId()) {
			System.err.println(loc.getFmin());
		}
	}

	public void setTestService(TestService testService) {
		this.testService = testService;
	}
	
	

}
