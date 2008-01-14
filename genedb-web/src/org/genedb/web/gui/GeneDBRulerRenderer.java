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

import org.biojava.bio.gui.sequence.SequenceRenderContext;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.SequenceViewerEvent;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.List;


/**
 * <p><code>RulerRenderer</code> renders numerical scales in sequence
 * coordinates. The tick direction may be set to point upwards (or
 * left when the scale is vertical) or downwards (right when the scale
 * is vertical).</p>
 *
 * <p>Note: The Compaq Java VMs 1.3.1 - 1.4.0 on Tru64 appear to have
 * a bug in font transformation which prevents a vertically oriented
 * ruler displaying correctly rotated text.</p>
 *
 * @author Matthew Pocock
 * @author Thomas Down
 * @author David Huen
 * @author Keith James
 */
public class GeneDBRulerRenderer implements SequenceRenderer
{
    /**
     * <code>TICKS_UP</code> indicates that the ticks will point
     * upwards from a baseline.
     */
    public static final int TICKS_UP = 0;
    /**
     * <code>TICKS_DOWN</code> indicates that the ticks will point
     * downwards from a baseline.
     */
    public static final int TICKS_DOWN = 1;

    private Line2D line;
    private double depth;
    private float tickHeight;
    private float horizLabelOffset;


    /**
     * Creates a new <code>RulerRenderer</code> with the specified
     * tick direction.
     *
     * @param tickDirection an <code>int</code>.
     * @exception IllegalArgumentException if an error occurs.
     */
    public GeneDBRulerRenderer() throws IllegalArgumentException
    {
        line   = new Line2D.Double();

        depth      = 11.0;
        tickHeight = 4.0f;

        horizLabelOffset = 10.0f;
    }

    public double getMinimumLeader(SequenceRenderContext context)
    {
        return 0.0;
    }

    public double getMinimumTrailer(SequenceRenderContext context)
    {
        return 0.0;
    }

    public double getDepth(SequenceRenderContext src)
    {
        return depth + 1.0;
    }

    public void paint(Graphics2D g2, SequenceRenderContext context)
    {

        g2.setPaint(Color.black);

        int min = context.getRange().getMin();
        int max = context.getRange().getMax();
        double minX = context.sequenceToGraphics(min);
        double maxX = context.sequenceToGraphics(max);
        double scale = context.getScale();

        double halfScale = scale * 0.5;

        line.setLine(minX - halfScale, 0.0,
                             maxX + halfScale, 0.0);

        g2.draw(line);

        FontMetrics fMetrics = g2.getFontMetrics();

        // The widest (== maxiumum) coordinate to draw
        int coordWidth = fMetrics.stringWidth(Integer.toString(max));

        // Minimum gap getween ticks
        double minGap = Math.max(coordWidth, 40);

        // How many symbols does a gap represent?
        int realSymsPerGap = (int) Math.ceil(((minGap + 5.0) / context.getScale()));

        // We need to snap to a value beginning 1, 2 or 5.
        double exponent = Math.floor(Math.log(realSymsPerGap) / Math.log(10));
        double characteristic = realSymsPerGap / Math.pow(10.0, exponent);

        int snapSymsPerGap;
        if (characteristic > 5.0)
        {
            // Use unit ticks
            snapSymsPerGap = (int) Math.pow(10.0, exponent + 1.0);
        }
        else if (characteristic > 2.0)
        {
            // Use ticks of 5
            snapSymsPerGap = (int) (5.0 * Math.pow(10.0, exponent));
        }
        else
        {
            snapSymsPerGap = (int) (2.0 * Math.pow(10.0, exponent));
        }

        int minP = min + (snapSymsPerGap - min) % snapSymsPerGap;

        int count =0;
        for (int index = minP; index <= max; index += snapSymsPerGap)
        {
            double offset = context.sequenceToGraphics(index);
            String labelString = String.valueOf(index);
            float labelWidth = fMetrics.stringWidth(labelString);

            line.setLine(offset + halfScale, 0.0, offset + halfScale, tickHeight);
            g2.draw(line);
            if ( index == minP) {
                g2.drawString(String.valueOf(index), (float) (offset + halfScale +1.0f),
                              horizLabelOffset);
            }
            if ((index+snapSymsPerGap) >= max) {
                g2.drawString(String.valueOf(index), (float) (offset + halfScale - 2.0 - labelWidth),
                      horizLabelOffset);
            }

            count++;
        }
    }

    public SequenceViewerEvent processMouseEvent(SequenceRenderContext context,
                                                 MouseEvent            me,
                                                 List                  path)
    {
        path.add(this);
        int sPos = context.graphicsToSequence(me.getPoint());
        return new SequenceViewerEvent(this, null, sPos, me, path);
    }
}
