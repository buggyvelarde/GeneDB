package org.genedb.install


class MiniReportGene {
	int featureId;
	String uniqueName
	String chromosome
	List<String> products = new ArrayList<String>()
	List<String> names = new ArrayList<String>()
	List<IntRange> exonLocs = new ArrayList<IntRange>()
	String location
	int numExons
	List<String> goTerms = new ArrayList<String>()
	List<String> privates = new ArrayList<String>()
	List<String> ecNums = new ArrayList<String>()
	boolean locsSorted = false

	public addExonLoc(IntRange exon) {
	    boolean added = false;
		for (int i=0; i < exonLocs.size(); i++) {
			Range r = exonLocs.get(i)
			if (exon.getFromInt() < r.getFromInt()) {
				exonLocs.add(i,exon)
				added = true
				break
			}
			if (exon.getFromInt() == r.getFromInt()) {
				if (exon.getToInt() < r.getToInt()) {
					exonLocs.add(i, exon)
					added = true
					break
				}
			}
		}
        if (!added) {
            exonLocs.add(exon);
        }
	}

	public addProduct(String product) {
		if (!product.contains("hypothetical") && !product.contains("unknown") && !product.contains("conserved")) {
			products.add(product)
		}
	}
}
