package org.genedb.db.loading;

import java.util.BitSet;
import java.util.List;

import org.gmod.schema.utils.propinterface.Rankable;

public class RankableUtils {

	public static <T extends Rankable> int getNextRank(List<T> list) {
		BitSet bs = new BitSet(list.size()+1);
		for (Rankable r : list) {
			bs.set(r.getRank());
		}
		return bs.nextClearBit(0);
	}

}
