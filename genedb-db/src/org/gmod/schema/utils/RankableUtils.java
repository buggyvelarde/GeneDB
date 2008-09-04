package org.gmod.schema.utils;

import java.util.BitSet;
import java.util.List;


public class RankableUtils {

    public static <T extends Rankable> int getNextRank(List<T> list) {
        BitSet bs = new BitSet(list.size() + 1);
        for (Rankable r : list) {
            bs.set(r.getRank());
        }
        return bs.nextClearBit(0);
    }

    public static <T extends Rankable> T getRankZero(List<T> list) {
        for (T t : list) {
            if (t.getRank() == 0) {
                return t;
            }
        }
        return null;
    }

}
