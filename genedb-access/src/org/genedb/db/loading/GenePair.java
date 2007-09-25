package org.genedb.db.loading;

public class GenePair implements Comparable {
	private String id1;
	private String id2;
	
	public GenePair(String id1, String id2) {
		if (id1.compareTo(id2) > 0) {
			this.id1 = id2;
			this.id2 = id1;
		} else {
			this.id1 = id1;
			this.id2 = id2;
		}
	}

	public int compareTo(Object o) {
		if (o instanceof GenePair) {
			GenePair pair2 = (GenePair) o;
			int first = id1.compareTo(pair2.id1);
			if (first <0) {
				return -1;
			}
			if (first > 0) {
				return 1;
			}
			return id2.compareTo(pair2.id2);
		}
		throw new RuntimeException("The type in compareTo is wrong");
	}

	public String getSecond() {
		return id2;
	}

	public String getFirst() {
		return id1;
	}

	@Override
	public boolean equals(Object obj) {
		return obj.hashCode()==hashCode();
	}

	@Override
	public int hashCode() {
		return (id1 + ":" + id2).hashCode();
	}
	
	
	
}
