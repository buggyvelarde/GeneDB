/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.genedb.web.gui;

import org.biojava.bio.gui.sequence.AbstractBeadRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.seq.Feature;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
//import org.biojava.bio.seq.FeatureHolder;
//import org.biojava.bio.seq.StrandedFeature;
//import org.biojava.bio.symbol.Location;
//import org.biojava.utils.ChangeEvent;
//import org.biojava.utils.ChangeSupport;
//import org.biojava.utils.ChangeType;
//import org.biojava.utils.ChangeVetoException;

/**
 * <code>TriangularBeadRenderer</code> renders features as
 * simple rectangles. Their outline and fill <code>Paint</code>,
 * <code>Stroke</code>, feature depth, Y-axis displacement are
 * configurable.
 *
 * @author <a href="mailto:kdj@sanger.ac.uk">Keith James</a>
 * @since 1.2
 */
public class GapBeadRenderer extends AbstractBeadRenderer
{


    /**
     * Creates a new <code>RectangularBeadRenderer</code>.
     *
     * @param beadDepth a <code>double</code>.
     * @param beadDisplacement a <code>double</code>.
     * @param beadOutline a <code>Paint</code>.
     * @param beadFill a <code>Paint</code>.
     * @param beadStroke a <code>Stroke</code>.
     */
    public GapBeadRenderer(double beadDepth,
                                   double beadDisplacement)
    {
        super(beadDepth, beadDisplacement, Color.black, Color.black, new BasicStroke());
    }

    /**
     * <code>renderBead</code> renders features as simple rectangle.
     *
     * @param g2 a <code>Graphics2D</code>.
     * @param f a <code>Feature</code> to render.
     * @param context a <code>SequenceRenderContext</code> context.
     */
    public void renderBead(final Graphics2D            g2,
                           final Feature               f,
                           final SequenceRenderContext context)
    {
        // Deliberately empty
    }

    /**
     * <code>getDepth</code> calculates the depth required by this
     * renderer to display its beads.
     *
     * @param context a <code>SequenceRenderContext</code>.
     *
     * @return a <code>double</code>.
     */
    public double getDepth(final SequenceRenderContext context)
    {
        // Get max depth of delegates using base class method
        double maxDepth = super.getDepth(context);
        return Math.max(maxDepth, (beadDepth + beadDisplacement));
    }

}
