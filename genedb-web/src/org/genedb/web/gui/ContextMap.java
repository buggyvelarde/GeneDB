package org.genedb.web.gui;

import net.jmge.gif.Gif89Encoder;

import org.genedb.web.gui.filters.ComboFeatureFilter;
import org.genedb.web.gui.filters.NamedStrandedFeatureFilter;
import org.genedb.web.gui.filters.RNAFilter;

import org.biojava.bio.gui.sequence.FeatureBlockSequenceRenderer;
import org.biojava.bio.gui.sequence.FeatureRenderer;
import org.biojava.bio.gui.sequence.FilteringRenderer;
import org.biojava.bio.gui.sequence.ImageMap;
import org.biojava.bio.gui.sequence.MultiLineRenderer;
import org.biojava.bio.gui.sequence.OverlayRendererWrapper;
import org.biojava.bio.gui.sequence.RulerRenderer;
import org.biojava.bio.gui.sequence.SequenceRenderer;
import org.biojava.bio.gui.sequence.TranslatedSequencePanel;
import org.biojava.bio.gui.sequence.ZiggyFeatureRenderer;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.Sequence;
import org.biojava.utils.ChangeVetoException;
import org.biojava.utils.net.URLFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;


/**
 *
 *
 * @author <a href="mailto:art@sanger.ac.uk">Adrian Tivey</a>
*/
public class ContextMap {

    public static final int DEFAULT_WINDOW_WIDTH = 600;
    public static final int DEFAULT_WINDOW_HEIGHT = 75;
    public static final int DEFAULT_RANGE = 27000;
    public static final float DEFAULT_SCALE = 1.0f * DEFAULT_WINDOW_WIDTH/DEFAULT_RANGE;

    private URLFactory urlFactory;
    private ImageMap imageMap;
    private List zirs;
    private ImageSize sizer;
    private Border border;
    private boolean imageSet = false;
    private boolean imageMapSet = false;
    private boolean showBorder = true;

    private MultiLineRenderer tracks;
    private TranslatedSequencePanel panel;

//    private ComboFeatureFilter plusCDStarget;
//    private ComboFeatureFilter plusCDSneighbour;
//
//    private ComboFeatureFilter revCDStarget;
//    private ComboFeatureFilter revCDSneighbour;

    private NamedStrandedFeatureFilter CDSplusTarget;
    private NamedStrandedFeatureFilter CDSrevTarget;

    private float scale;
    private BufferedImage image = null;
    private Graphics2D g2d = null;
    private boolean cdsHighlight = true;
    private boolean customRuler = false;

    private boolean haveBiojavaBorder=false;

    public static final int LEAD_BORDER          = 14;
    public static final int TRAIL_BORDER         = 14;



    public ContextMap() {
        this(DEFAULT_SCALE, true);
    }

    public ContextMap(float scale, boolean cdsHighlight) {
        zirs = new ArrayList();
        sizer = new ImageSize();
        this.scale = scale;
        this.cdsHighlight = cdsHighlight;

        border = BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.black),
                                                    BorderFactory.createEmptyBorder(4,4,4,4));

    }


    public void haveBiojavaBorder(boolean haveBiojavaBorder) {
        this.haveBiojavaBorder = haveBiojavaBorder;
    }


    private void initImageLayout() {
        // Set up rendering structure
        ZiggyFeatureRenderer[] colouredRenderers = new ZiggyFeatureRenderer[ ArtemisColours.getNumCols()];

        try {

            try {
                urlFactory = new FeaturePageURLFactory();
            } catch (MalformedURLException exp) {
                exp.printStackTrace();
            }

            SequenceRenderer[] plus = new SequenceRenderer[ArtemisColours.getNumCols()];
            SequenceRenderer[] rev = new SequenceRenderer[ArtemisColours.getNumCols()];


            boolean overlap = true;
            for (int i = 0 ; i < ArtemisColours.getNumCols() ; i++) {
                //              if ( i== Status.STATUS_COL.length-1 ) {
                //                  overlap=false;
                //              }
                colouredRenderers[i] = new ZiggyFeatureRenderer();
                colouredRenderers[i].setFill(ArtemisColours.getColour(i));

                FeatureFilter plusFilter = new ComboFeatureFilter('+', i);
                FeatureFilter revFilter = new ComboFeatureFilter('-', i);

                plus[i] = getZiggySequenceRenderer(plusFilter, colouredRenderers[i], overlap);
                rev[i] =  getZiggySequenceRenderer(revFilter, colouredRenderers[i], overlap);

            }

            FeatureFilter plusRNAFilter = new RNAFilter('+');
            FeatureFilter revRNAFilter = new RNAFilter('-');
            SequenceRenderer plusRNAs =  getZiggySequenceRenderer(plusRNAFilter, colouredRenderers[11], false);
            SequenceRenderer revRNAs =  getZiggySequenceRenderer(revRNAFilter, colouredRenderers[11], false);


            tracks = new MultiLineRenderer();

            CDSplusTarget = new NamedStrandedFeatureFilter("CDS", '+');
            CDSrevTarget = new NamedStrandedFeatureFilter("CDS", '-');
            SequenceRenderer CDSplusHighlight = getHighlightSequenceRenderer(CDSplusTarget, Color.green, true);
            SequenceRenderer CDSrevHighlight = getHighlightSequenceRenderer(CDSrevTarget, Color.green, false);

            SequenceRenderer gap = new FeatureBlockSequenceRenderer(new GapBeadRenderer(4.0d, 0.0d));

            // CDS on plus strand
            tracks.addRenderer(gap);
            if ( cdsHighlight) {
                tracks.addRenderer(CDSplusHighlight);
                tracks.addRenderer(gap);
            }
            for (int i = 0 ; i < ArtemisColours.getNumCols() ; i++) {
                tracks.addRenderer(plus[i]);
            }
            tracks.addRenderer(plusRNAs);
            tracks.addRenderer(gap);

            // DNA ruler
            if (customRuler) {
                tracks.addRenderer(new GeneDBRulerRenderer());
            } else {
                tracks.addRenderer(new RulerRenderer());
            }

            //tracks.addRenderer(gap);
            // CDS on reverse strand
            for (int i = 0 ; i < ArtemisColours.getNumCols() ; i++) {
                tracks.addRenderer(rev[i]);
            }
            tracks.addRenderer(revRNAs);
            tracks.addRenderer(gap);

            if ( cdsHighlight) {
                tracks.addRenderer(CDSrevHighlight);
                tracks.addRenderer(gap);
            }
        } catch (ChangeVetoException exp) {
            exp.printStackTrace();
        }
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        imageSet = true;
    }

    public void setImageMap(ImageMap imageMap) {
        this.imageMap = imageMap;
        imageMapSet = true;
    }

    public void setShowBorder(boolean showBorder) {
        this.showBorder = showBorder;
    }

    public void setCustomRuler(boolean customRuler) {
        this.customRuler = customRuler;
    }

    public void setGraphics2D(Graphics2D g2d) {
        this.g2d = g2d;
    }

    public Image getImage() {
        return image;
    }


    public void drawMap(Sequence seq, ImageInfo info, RNASummary bottomRna, RNASummary targetRna,
                        RNASummary topRna, OutputStream out ) {
        int seqMin = bottomRna.getLocation().getMin();
        int seqMax = topRna.getLocation().getMax();
        //Sequence contextSeq = new SubSequence(seq, seqMin, seqMax);
        //seqMin = 1;
        //seqMax -= seqMin;

        //      System.err.println("  ");
        //System.err.println("IMAP: "+targetRna.getId());
        //System.err.println(" from: "+bottomRna.getId()+" : "+bottomRna.getLocation());
        //System.err.println("   to: "+topRna.getId()+" : "+topRna.getLocation());

        drawMap(seq, info, seqMin, seqMax, targetRna.getId(), true, out);
    }


    public ImageInfo drawMap(Sequence seq2, ImageInfo info, int seqMin, int seqMax, String targetId,
                        boolean makeSubSequence, OutputStream out) {

        Sequence seq = seq2;
        if ( makeSubSequence) {
            seq = new SubSequenceView(seq2, seqMin, seqMax);
        }
        //System.err.println("IMAP: min="+seqMin+"  max="+seqMax);

        if ( tracks == null) {
            initImageLayout();
        }

        if (!imageMapSet) {
        	System.err.println("### Setting imagemap");
            imageMap = new GeneDbClientSide("context");
        }
        try {
            for (int i = 0 ; i < zirs.size(); i++) {
                GeneDbZiggyImapRenderer zir = (GeneDbZiggyImapRenderer) zirs.get(i);
                zir.setImageMap(imageMap);
            }

            CDSplusTarget.setId(targetId);
            CDSrevTarget.setId(targetId);

            panel = new TranslatedSequencePanel();
            panel.setOpaque( true );
            panel.setScale( scale );
            //System.err.println("Scale is "+scale);
            panel.setBackground( Color.white );
            panel.setSequence( seq );
            panel.setRenderer( tracks );

            if (haveBiojavaBorder) {
                panel.getLeadingBorder().setSize(LEAD_BORDER);
                panel.getTrailingBorder().setSize(TRAIL_BORDER);
            }

            System.err.println("Trying to set symbol translation to '"+(seqMin-1)+"' but '"+seq.length()+"'" );
            if (seqMin > 1) {
                panel.setSymbolTranslation(seqMin-1);
            } else {
                panel.setSymbolTranslation(seqMin);
            }
            panel.resizeAndValidate();

            if (showBorder) {
                panel.setBorder(border);
            }


            int width = Math.round((seqMax - seqMin)*scale)+11;
            if (haveBiojavaBorder) {
                width = width + LEAD_BORDER + TRAIL_BORDER;
            }
            //System.err.println("IMAP: width="+width+"  scale="+scale);

            panel.setPreferredSize(new Dimension( width, DEFAULT_WINDOW_HEIGHT+10));
            panel.setSize(new Dimension( width, DEFAULT_WINDOW_HEIGHT+10));
            sizer.setWidth(width);
            sizer.setHeight(DEFAULT_WINDOW_HEIGHT+10);

            synchronized (getClass()) {
                //          if ( image == null ) {
                if (!imageSet) {
                    image = new BufferedImage(width, DEFAULT_WINDOW_HEIGHT+10, BufferedImage.TYPE_INT_RGB);
                    //              image = new PJABufferedImage(width, DEFAULT_WINDOW_HEIGHT+10, BufferedImage.TYPE_INT_RGB);
                    //      image = new com.eteks.awt.PJAImage( width, DEFAULT_WINDOW_HEIGHT+10);
                    //System.err.println("IMAP: image width="+image.getWidth());
                    g2d = image.createGraphics();
                }

                panel.paint(g2d);
            }

            if ( out != null ) {
                Gif89Encoder gifenc = new Gif89Encoder(image);
                gifenc.getFrameAt(0).setInterlaced( true );
                gifenc.encode(out);
            }

            if ( info != null) {
                info.contextMapData = imageMap.toString();
            	System.err.println("### Setting data to '"+imageMap.toString()+"'");
            } else {
            	System.err.println("### Not setting imagemap data as info is null");
            }


        } catch ( FileNotFoundException exp) {
            exp.printStackTrace();
        } catch ( IOException exp) {
            exp.printStackTrace();
        } catch ( ChangeVetoException exp) {
            exp.printStackTrace();
        }
        //     }
// }
        return info;
    }


    private SequenceRenderer getZiggySequenceRenderer(FeatureFilter ff,
                                                      ZiggyFeatureRenderer zfr,
                                                      boolean overlay) {
        GeneDbZiggyImapRenderer zir = new GeneDbZiggyImapRenderer(zfr, imageMap, urlFactory, sizer);
        zirs.add(zir);
        SequenceRenderer sr = new FilteringRenderer( new FeatureBlockSequenceRenderer( zir ),
                                                     ff, true);
        if ( overlay ) {
            return new OverlayRendererWrapper(sr);
        }
        return sr;
    }


    private SequenceRenderer getHighlightSequenceRenderer(FeatureFilter ff, Paint p, boolean reverse) {
        Stroke stroke = new BasicStroke();
        FeatureRenderer fr = new TriangularBeadRenderer(10.0, 0.0, p, p, stroke, reverse);
        //fr.setFill( p );
        return new FilteringRenderer( new FeatureBlockSequenceRenderer( fr ),
                                      ff, true);
    }

}
