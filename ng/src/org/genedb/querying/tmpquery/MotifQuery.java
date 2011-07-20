package org.genedb.querying.tmpquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.regex.RegexQuery;

import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryParam;
import org.genedb.querying.core.LuceneQuery.Pager;


public class MotifQuery extends OrganismLuceneQuery {
	
	private static Logger logger = Logger.getLogger(MotifQuery.class);
	
	
	@QueryParam(
            order=1,
            title="The search string"
    )
    private String search = "";
	
	private String actualSearch;
	private Pattern pattern; 
	
	
	public void setSearch(String search) {
		this.search = search;
	}
	
	public String getSearch() {
		return search;
	}
	
	@Override
	public String getQueryName() {
		return "Motif";
	}

	
	@Override
	protected void getQueryTermsWithoutOrganisms(List<Query> queries) {
		logger.info(search);
		
		actualSearch = search;
		pattern = Pattern.compile(actualSearch);
		
		// let's ignore any motifs smaller than 2 characters
		if (actualSearch.length() < 3) {
			throw new RuntimeException("Sorry, cannot handle motifs under 3 characters long.");
		}
			
		String starter = "[A-Z]+";
		if (actualSearch.startsWith(".+") ) {
			actualSearch.replaceFirst("\\.\\+", starter);
		}
		else if (actualSearch.startsWith(".*")) {
			actualSearch.replaceFirst("\\.\\*", starter);
		}
		else if (! search.startsWith("^") && !search.startsWith(starter)) {
			actualSearch = starter + search;
		}
		
		logger.info(String.format("%s  ----- > %s", search, actualSearch));
		
		queries.add(new TermQuery(new Term("type.name", "polypeptide")));
		
		RegexQuery r = new RegexQuery(new Term("sequenceResidues",actualSearch));
		
		JavaUtilRegexCapabilities capabilites = new JavaUtilRegexCapabilities();
		r.setRegexImplementation(capabilites);
		// logger.info("max results " + maxResults);
		logger.info(r.getRegexImplementation().getClass().toString());
		
		queries.add(r);
		
		
	}

	@Override
    protected String getluceneIndexName() {
        return "org.gmod.schema.mapped.Feature";
    }

    @Override
    public String getQueryDescription() {
    	return "Searches for polypeptide residue patterns with a regular expression.";
    }

	@Override
	protected String[] getParamNames() {
		return new String[] {"search"};
	}
	
	
	
	public class MotifDetail {
		
		String residues;
		String formattedResidues;
		
	}
	
	public class MotifResult {
		String match;
		String residues;
		int start;
		int end;
		String displayId;
		String pre;
		String post;
		
		public String getPre() {
			return pre;
		}
		public void setPre(String pre) {
			this.pre = pre;
		}
		public String getPost() {
			return post;
		}
		public void setPost(String post) {
			this.post = post;
		}

		
		
		public String getDisplayId() {
			return displayId;
		}
		public void setDisplayId(String displayId) {
			this.displayId = displayId;
		}
		public String getMatch() {
			return match;
		}
		public void setMatch(String match) {
			this.match = match;
		}
		public String getResidues() {
			return residues;
		}
		public void setResidues(String residues) {
			this.residues = residues;
		}
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		public int getEnd() {
			return end;
		}
		public void setEnd(int end) {
			this.end = end;
		}
		
		public MotifResult(String displayId, String match, String residues, int start, int end, String pre, String post) {
			this.match = match;
			this.residues = residues;
			this.start = start;
			this.end = end;
			this.displayId = displayId;
			this.pre = pre;
			this.post = post;
		}
		
	}
	
	protected Pager<MotifResult> motifResultPager = new Pager<MotifResult>() {
		
		@Override public MotifResult convert(Document document) {
			
			String displayId = getGeneUniqueNameOrUniqueName(document);
			
			String residues = document.get("sequenceResidues");
			logger.info(residues);
			
			Matcher m = pattern.matcher(residues);
			m.find();
			
			String match = m.group();
			int start = m.start();
			int end = m.end();
			
			logger.info(String.format("%s %d-%d", match, start, end));
			
			String newResidues = residues.substring(0, start) + "*" + residues.substring(start, end) + "*" + residues.substring(end);
			String newResidues2 = residues.substring(0, start) + "*" + match + "*" + residues.substring(end);
			String newResidues3 = residues.substring(0, start) + "*" + actualSearch + "*" + residues.substring(end);
			
			logger.info(newResidues);
			logger.info(newResidues2);
			logger.info(newResidues3);
			
			return new MotifResult(displayId, match, residues, start, end, residues.substring(0, start), residues.substring(end));
			
		}
	};
	
	public Map<String,MotifResult> getMotifResults(int page, int length) throws QueryException {
		List<MotifResult> motifResults = motifResultPager.getResults(page, length);
		Map<String, MotifResult> map = new HashMap<String,MotifResult>();
		for (MotifResult mr : motifResults) {
			map.put(mr.getDisplayId(), mr);
		}
		return map;
	}

}
