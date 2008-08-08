package org.genedb.web.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;


public class DiagramLayoutTest {

    @Before
    public void setUpLogging() {
        String log4jprops = "/log4j.test.properties";
        URL url = this.getClass().getResource(log4jprops);
        System.out.printf("Configuring Log4J from '%s'\n", url);
        PropertyConfigurator.configure(url);
    }

    @Test
    public void simpleFindFirstFit() {
        DiagramLayout layout = new DiagramLayout();
        assertEquals(1, layout.chooseTrack(0, 10, 1));
    }

    @Test
    public void tracksAreFilled() {
        DiagramLayout layout = new DiagramLayout();
        layout.addBlock(0,  10, 1);

        assertTrue(layout.filledTracksAtPosition(5).get(1));
        assertTrue(layout.filledTracksAtPosition(0).get(1));
        assertTrue(layout.filledTracksAtPosition(10).get(1));
    }

    @Test
    public void oneTrackLayout() {
        DiagramLayout layout = new DiagramLayout();
        assertEquals(1, layout.addBlock(0,  10, 1));
        assertEquals(2, layout.addBlock(10, 20, 1));
        assertEquals(1, layout.addBlock(20, 30, 1));
        assertEquals(2, layout.addBlock(30, 40, 1));

        assertEquals(2, layout.addBlock(2, 3, 1));
        assertEquals(3, layout.addBlock(25, 35, 1));
    }

    @Test
    public void anotherOneTrackLayout() {
        /*    ----  ----
         * ----  ----  ----
         */
        DiagramLayout layout = new DiagramLayout();
        assertEquals(1, layout.addBlock(1,  5, 1));
        assertEquals(1, layout.addBlock(10, 14, 1));
        assertEquals(2, layout.addBlock(4, 8, 1));
        assertEquals(3, layout.addBlock(7, 11, 1));
        assertEquals(2, layout.addBlock(13, 17, 1));

        assertEquals(1, layout.addBlock(100, 120, 1));
        assertEquals(2, layout.addBlock(100, 110, 1));
        assertEquals(3, layout.addBlock(100, 120, 1));
        assertEquals(2, layout.addBlock(115, 125, 1));
    }

    /**
     * This test is based on the IPR001680 domains of PFI0290c.
     */
    @Test
    public void complexOneTrackLayout() {
        DiagramLayout layout = new DiagramLayout();

        assertEquals(1, layout.addBlock(3, 43, 1));
        assertEquals(2, layout.addBlock(10, 271, 1));
        assertEquals(1, layout.addBlock(45, 85, 1));
        assertEquals(3, layout.addBlock(58, 85, 1));
        assertEquals(1, layout.addBlock(87, 127, 1));
        assertEquals(3, layout.addBlock(88, 127, 1));
        assertEquals(4, layout.addBlock(94, 130, 1));
        assertEquals(5, layout.addBlock(94, 128, 1));
        assertEquals(1, layout.addBlock(130, 171, 1));
        assertEquals(3, layout.addBlock(131, 171, 1));
        assertEquals(4, layout.addBlock(137, 180, 1));
        assertEquals(5, layout.addBlock(137, 172, 1));
        assertEquals(6, layout.addBlock(157, 172, 1));
        assertEquals(1, layout.addBlock(178, 220, 1));
        assertEquals(3, layout.addBlock(179, 220, 1));
        assertEquals(4, layout.addBlock(184, 221, 1));
        assertEquals(5, layout.addBlock(185, 229, 1));
        assertEquals(6, layout.addBlock(206, 221, 1));
        assertEquals(1, layout.addBlock(222, 262, 1));
        assertEquals(3, layout.addBlock(223, 262, 1));
        assertEquals(4, layout.addBlock(229, 262, 1));
        assertEquals(6, layout.addBlock(229, 261, 1));
        assertEquals(5, layout.addBlock(248, 263, 1));
        assertEquals(7, layout.addBlock(248, 263, 1));
    }

    @Test
    public void edgeCases() {
        DiagramLayout layout = new DiagramLayout();
        assertEquals(1, layout.addBlock(10, 20, 1));
        assertEquals(2, layout.addBlock(20, 30, 1));
        assertEquals(2, layout.addBlock(0, 10, 1));
    }

    @Test
    public void multiTrackLayout() {
        DiagramLayout layout = new DiagramLayout();
        assertEquals(1, layout.addBlock(1, 14, 2));
        assertEquals(3, layout.addBlock(10, 20, 2));
        assertEquals(5, layout.addBlock(14, 16, 20));
    }
}
