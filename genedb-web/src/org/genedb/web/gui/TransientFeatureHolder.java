package org.genedb.web.gui;

import org.biojava.bio.seq.AbstractFeatureHolder;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple implementation of FeatureHolder that doesn't 
 * serialise its features for memory reasons. 
 *
 */
public class TransientFeatureHolder extends AbstractFeatureHolder
    implements java.io.Serializable {
  /**
   * The child features.
   */
  private transient List features;
  private FeatureFilter schema;

  /**
   * Construct a new SimpleFeatureHolder with a non-informative schema.
   */

  public TransientFeatureHolder() {
      this.schema = FeatureFilter.all;
  }

  /**
   * Construct a new SimpleFeatureHolder with the specified schema.
   */

  public TransientFeatureHolder(FeatureFilter schema) {
      this.schema = schema;
  }

  /**
   * Initialize features.
   */
  {
    features = new ArrayList();
  }

  /**
  *Returns the list of features in this featureholder.
  */
  protected List getFeatures() {
    return features;
  }

  public int countFeatures() {
    return features.size();
  }

  public Iterator features() {
    return features.iterator();
  }

    /**
    *Add a feature to the featureholder
    */

  public void addFeature(Feature f)
  throws ChangeVetoException {
    if(!hasListeners()) {
      features.add(f);
    } else {
      ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
      synchronized(changeSupport) {
        ChangeEvent ce = new ChangeEvent(
          this, FeatureHolder.FEATURES,
          f, null
        );
        changeSupport.firePreChangeEvent(ce);
        features.add(f);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public void removeFeature(Feature f)
  throws ChangeVetoException {
    if(!hasListeners()) {
      features.remove(f);
    } else {
      ChangeSupport changeSupport = getChangeSupport(FeatureHolder.FEATURES);
      synchronized(changeSupport) {
        ChangeEvent ce = new ChangeEvent(
          this, FeatureHolder.FEATURES,
          null, f
        );
        changeSupport.firePreChangeEvent(ce);
        features.remove(f);
        changeSupport.firePostChangeEvent(ce);
      }
    }
  }

  public boolean containsFeature(Feature f) {
    return features.contains(f);
  }

  public FeatureFilter getSchema() {
      return schema;
  }
}
