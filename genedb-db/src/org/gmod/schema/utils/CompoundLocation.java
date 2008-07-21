package org.gmod.schema.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class CompoundLocation implements StrandedLocation {
	
	List<SingleLocation> locs;
	

	public CompoundLocation(org.biojava.bio.symbol.Location loc, org.biojava.bio.seq.StrandedFeature.Strand strand) {
		locs = new ArrayList<SingleLocation>();
		Iterator<org.biojava.bio.symbol.Location> it = loc.blockIterator();
		while (it.hasNext()) {
			SingleLocation location = new SingleLocation(it.next(), strand);
			locs.add(location);
		}
	}
	
	private CompoundLocation(CompoundLocation other, boolean convertToInterbase) {
		this.locs = new ArrayList<SingleLocation>(other.getLocations().size());
		for (SingleLocation loc : other.getLocations()) {
			SingleLocation convert = new SingleLocation(loc, convertToInterbase);
			this.locs.add(convert);
		}
	}

	@Override
	public int getMax() {
		return locs.get(locs.size()-1).getMax();
	}

	@Override
	public int getMin() {
		return locs.get(0).getMin();
	}

	public Strand getStrand() {
		return locs.get(0).getStrand();
	}

	public boolean isInterbase() {
		return locs.get(0).isInterbase();
	}

	public boolean isMaxPartial() {
		return locs.get(locs.size()-1).isMaxPartial();
	}

	public boolean isMinPartial() {
		return locs.get(0).isMinPartial();
	}

	@Override
	public List<SingleLocation> getLocations() {
		return Collections.unmodifiableList(locs);
	}

	@Override
	public StrandedLocation getInterbaseVersion() {
		if (this.isInterbase()) {
			return this;
		}
		return new CompoundLocation(this, true);
	}
	
}

