package org.genedb.querying.tmpquery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.regex.JakartaRegexpCapabilities;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.regex.RegexQuery;

import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryParam;
import org.genedb.querying.core.LuceneQuery.Pager;


public class MotifQuery extends OrganismLuceneQuery {
	
	private static Logger logger = Logger.getLogger(MotifQuery.class);
	
	private static Map<Character,String> PROTEIN_GROUP_MAP;
	
	static {
        
        PROTEIN_GROUP_MAP = new HashMap<Character, String>();
        PROTEIN_GROUP_MAP.put('B', "[AGS]");          //tiny
        PROTEIN_GROUP_MAP.put('Z', "[ACDEGHKNQRST]"); //turnlike
        PROTEIN_GROUP_MAP.put('0', "[DE]");           //acidic
        PROTEIN_GROUP_MAP.put('1', "[ST]");           //alcohol
        PROTEIN_GROUP_MAP.put('2', "[ILV]");          //aliphatic
        PROTEIN_GROUP_MAP.put('3', "[FHWY]");         //aromatic
        PROTEIN_GROUP_MAP.put('4', "[HKR]");          //basic
        PROTEIN_GROUP_MAP.put('5', "[DEHKR]");        //charged
        PROTEIN_GROUP_MAP.put('6', "[AFILMVWY]");     //hydrophobic
        PROTEIN_GROUP_MAP.put('7', "[DEHKNQR]");      //hydrophilic
        PROTEIN_GROUP_MAP.put('8', "[CDEHKNQRST]");   //polar
        PROTEIN_GROUP_MAP.put('9', "[ACDGNPSTV]");    //small
        
        
    }
	
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
		
		actualSearch = new String(search);
		for (Entry<Character, String> entry : PROTEIN_GROUP_MAP.entrySet()) {
            logger.info(Character.toString(entry.getKey()) + "  " + entry.getValue());
            actualSearch = actualSearch.replaceAll(Character.toString(entry.getKey()), entry.getValue());
            logger.info(actualSearch);
        }
		pattern = Pattern.compile(actualSearch);
		
		// let's ignore any motifs smaller than 2 characters
		if (actualSearch.length() < 3) {
			throw new RuntimeException("Sorry, cannot handle motifs under 3 characters long.");
		}
			
		String starter = ".*";
//		if (actualSearch.startsWith(".+") ) {
//			actualSearch.replaceFirst("\\.\\+", starter);
//		}
//		else if (actualSearch.startsWith(".*")) {
//			actualSearch.replaceFirst("\\.\\*", starter);
//		}
//		else
		
		if (! search.startsWith("^") && !search.startsWith(starter)) {
			actualSearch = starter + actualSearch;
		}
		
		logger.info(String.format("%s  ----- > %s", search, actualSearch));
		
		queries.add(new TermQuery(new Term("type.name", "polypeptide")));
		
		RegexQuery r = new RegexQuery(new Term("sequenceResidues",actualSearch));
		
		JavaUtilRegexCapabilities capabilites = new JavaUtilRegexCapabilities();
		//JakartaRegexpCapabilities c = new JakartaRegexpCapabilities();
		
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
	
	public Map<String,Object> prepareModelData() {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("search", search);
		return map;
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
			
			String pre = residues.substring(0, start);
			
			int len = 30; 
			
			if (pre.length() > len) {
				int i = pre.length() - len;
				pre = "..." + pre.substring(i);
			}
			
			String post = residues.substring(end);
			
			if (post.length() > len) {
				post = post.substring(0, len) + "...";
			}
			
			return new MotifResult(displayId, match, residues, start, end, pre, post);
			
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
