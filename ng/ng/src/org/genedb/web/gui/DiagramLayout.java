package org.genedb.web.gui;

import org.apache.log4j.Logger;

import java.util.BitSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Represents the layout of a tracked diagram (i.e. which tracks are filled where)
 * in a form that allows first-fit layout to be implemented reasonably efficiently.
 *
 * @author rh11
 */
public class DiagramLayout {
    protected static final Logger logger = Logger.getLogger(DiagramLayout.class);

    /**
     * Maps position to a set of tracks that are filled from that location rightwards.
     * There should be an entry here for each feature boundary.
     */
    private NavigableMap<Integer, BitSet> filledTracksAfter = new TreeMap<Integer, BitSet> ();

    public DiagramLayout() {
        // empty
    }

    private static final BitSet EMPTY = new UnmodifiableBitSet();

    protected BitSet filledTracksAtPosition(int position) {
        Map.Entry<Integer, BitSet> floorEntry = filledTracksAfter.floorEntry(position);
        if (floorEntry == null) {
            logger.trace(String.format("No filled tracks at position %d", position));
            return EMPTY;
        }
        logger.trace(String.format("Filled tracks at position %d are %s, from boundary at %d",
            position, floorEntry.getValue(), floorEntry.getKey()));
        return new UnmodifiableBitSet(floorEntry.getValue());
    }

    protected BitSet filledTracksInRange(int start, int end) {
        BitSet filledTracks = new BitSet();
        filledTracks.or(filledTracksAtPosition(start));
        for (BitSet bitSet: filledTracksAfter.subMap(start, true, end, true).values()) {
            filledTracks.or(bitSet);
        }
        logger.trace(String.format("Filled tracks in range %d-%d are %s",
            start, end, filledTracks.toString()));
        return filledTracks;
    }

    private int numberOfTracks = 0;

    protected void addPositionedBlock(int startPosition, int endPosition, int startTrack, int height) {
        assert height >= 1;

        if (startTrack + height - 1 > numberOfTracks) {
            numberOfTracks = startTrack + height - 1;
        }

        BitSet filledTracksAtStart = new BitSet();
        filledTracksAtStart.or(filledTracksAtPosition(startPosition));
        filledTracksAtStart.set(startTrack, startTrack + height);

        BitSet filledTracksAtEnd = filledTracksAtPosition(endPosition + 1);

        filledTracksAfter.put(startPosition, filledTracksAtStart);
        filledTracksAfter.put(endPosition + 1, filledTracksAtEnd);

        for (Map.Entry<Integer, BitSet> entry: filledTracksAfter.subMap(startPosition, false, endPosition, true).entrySet()) {
            BitSet set = new BitSet();
            set.or(entry.getValue());
            set.set(startTrack, startTrack + height);
            entry.setValue(set);
        }
    }

    protected int chooseTrack(int startPosition, int endPosition, int height) {
        BitSet filledTracks = filledTracksInRange(startPosition, endPosition);
        int i = 1;
        while (true) {
            i = filledTracks.nextClearBit(i);
            for (int j = i; j < i + height; j++) {
                if (filledTracks.get(j)) {
                    i = j;
                    continue;
                }
            }
            return i;
        }
    }

    /**
     * Add a block in the specified position.
     * @param startPosition
     * @param endPosition
     * @param height
     * @return the track number of the bottom of the block.
     */
    public int addBlock(int startPosition, int endPosition, int height) {
        int firstFit = chooseTrack(startPosition, endPosition, height);
        addPositionedBlock(startPosition, endPosition, firstFit, height);
        logger.trace(String.format("%s: Block %d-%d (height %d) allocated to track %d",
            this, startPosition, endPosition, height, firstFit));
        return firstFit;
    }

    public int numberOfTracks() {
        return numberOfTracks;
    }
}
