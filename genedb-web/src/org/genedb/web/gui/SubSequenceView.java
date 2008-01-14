package org.genedb.web.gui;

import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.RangeLocation;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeListener;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeType;
import org.biojava.utils.ChangeVetoException;

import java.util.Iterator;
import java.util.List;

/**
 * View a sub-section of a given sequence object, including all the
 * features intersecting that region.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.2
 */

public class SubSequenceView implements Sequence {
    private Sequence parent;
    private SymbolList symbols;
    private String name;
    private String uri;
    private Annotation annotation;
    private int start;
    private int end;
    private transient FeatureHolder cachedFeatures;

    private transient ChangeSupport changeSupport;
    private transient ChangeListener seqListener;


    public FeatureFilter getSchema() {
        return FeatureFilter.top_level;
    }


    private void installSeqListener() {
        seqListener = new ChangeListener() {
                public void preChange(ChangeEvent cev)
                    throws ChangeVetoException
                {
                    if (changeSupport != null) {
                        changeSupport.firePreChangeEvent(makeChainedEvent(cev));
                    }
                }

                public void postChange(ChangeEvent cev) {
                    cachedFeatures = null;
                    if (changeSupport != null) {
                        changeSupport.firePostChangeEvent(makeChainedEvent(cev));
                    }
                }

                private ChangeEvent makeChainedEvent(ChangeEvent cev) {
                    return new ChangeEvent(this,
                                           FeatureHolder.FEATURES,
                                           null, null,
                                           cev);
                }
            } ;
        parent.addChangeListener(seqListener, FeatureHolder.FEATURES);
    }

    public void addChangeListener(ChangeListener l) {
        parent.addChangeListener(l);

    }
    public void removeChangeListener(ChangeListener l) {
        parent.removeChangeListener(l);

    }
    public void addChangeListener(ChangeListener l, ChangeType t) {
        parent.addChangeListener(l, t);

    }
    public void removeChangeListener(ChangeListener l, ChangeType t) {
        parent.removeChangeListener(l, t);

    }
    public boolean isUnchanging(ChangeType t) {
        return parent.isUnchanging(t);
    }


    /**
     * Construct a new SubSequence of the specified sequence.
     *
     * @param seq A sequence to view
     * @param start The start of the range to view
     * @param end The end of the range to view
     * @throws IndexOutOfBoundsException is the start or end position is illegal.
     */

    public SubSequenceView(Sequence seq, final int start, final int end) {
        this.parent = seq;
        this.start = start;
        this.end = end;
        this.symbols = seq.subList(1, seq.length());

        name = seq.getName() + " (" + start + " - " + end + ")";
        uri = seq.getURN() + "?start=" + start + ";end=" + end;
        annotation = seq.getAnnotation();
    }

    //
    // SymbolList stuff
    //

    public Symbol symbolAt(int pos) {
        return symbols.symbolAt(pos);
    }

    public Alphabet getAlphabet() {
        return symbols.getAlphabet();
    }

    public SymbolList subList(int start, int end) {
        return symbols.subList(start, end);
    }

    public String seqString() {
        return symbols.seqString();
    }

    public String subStr(int start, int end) {
        return symbols.subStr(start, end);
    }

    public List toList() {
        return symbols.toList();
    }

    public int length() {
        return symbols.length();
    }

    public Iterator iterator() {
        return symbols.iterator();
    }

    public void edit(Edit edit)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("Can't edit SubSequenceViews");
    }

    //
    // Implements featureholder
    //

    public int countFeatures() {
        return getFeatures().countFeatures();
    }

    public Iterator features() {
        return getFeatures().features();
    }

    public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
        return getFeatures().filter(ff, recurse);
    }

    public FeatureHolder filter(FeatureFilter ff) {
        return getFeatures().filter(ff, true); // FIXME - guess what default value is
    }

    public boolean containsFeature(Feature f) {
        return getFeatures().containsFeature(f);
    }

    public Feature createFeature(Feature.Template templ)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("Can't edit SubSequenceViews");
    }

    public void removeFeature(Feature f)
        throws ChangeVetoException
    {
        throw new ChangeVetoException("Can't edit SubSequenceViews");
    }

    protected FeatureHolder getFeatures() {
        if (seqListener == null) {
            installSeqListener();
        }
        if (cachedFeatures == null) {
            FeatureFilter filter = new FeatureFilter.OverlapsLocation(new RangeLocation(start, end));
            cachedFeatures = parent.filter(filter, true);
        }
        return cachedFeatures;
    }


    //
    // Identifiable
    //
    public String getName() {
        return name;
    }

    public String getURN() {
        return uri;
    }


    //
    // Annotatable
    //
    public Annotation getAnnotation() {
        return annotation;
    }

    public Sequence getParent() {
      return this.parent;
    }

}
