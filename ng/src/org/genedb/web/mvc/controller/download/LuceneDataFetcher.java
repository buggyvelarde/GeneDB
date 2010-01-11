/*
 * Copyright (c) 2006 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;



/**
 * List Download
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
public class LuceneDataFetcher implements DataFetcher<String> {

    private LuceneIndex luceneIndex;

    //@Required
    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
    }

    public TroubleTrackingIterator<DataRow> iterator(List<String> ids, String delimeter) {
        return new LuceneDataRowIterator(luceneIndex, ids);
    }

}


class LuceneDataRowIterator implements TroubleTrackingIterator<DataRow> {

    private Logger logger = Logger.getLogger(LuceneDataRowIterator.class);

    LuceneIndex luceneIndex;
    Iterator<String> iterator;

    public LuceneDataRowIterator(LuceneIndex luceneIndex, List<String> ids) {
        this.luceneIndex = luceneIndex;
        this.iterator = ids.iterator();
    }


    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public DataRow next() {
        String id = iterator.next();
        //logger.debug(String.format("#~# luceneIndexFactory is '%s'", luceneIndexFactory));

        TermQuery q = new TermQuery(new Term("uniqueName",id));
        logger.debug(String.format("#~# luceneIndex is '%s'", luceneIndex));
        logger.error(String.format("Attempting to lookup '%s'", id));
        TopDocs td = luceneIndex.search(q);
        logger.error(String.format("Number of hits '%d'", td.totalHits));
        try {
            Document document = fetchDocument(td.scoreDocs[0].doc);
            logger.error(String.format("The document is '%s'", document));
            return new LuceneDataRow(document);
        }
        catch (IOException exp) {
            logger.error(String.format("Failed to fetch id '%s' due to '%s'", id, exp.getMessage()));
            return null;
        }
    }

    @Override
    public void remove() {
        throw new RuntimeException("Not implemented"); // FIXME
    }

    private Document fetchDocument(int docId) throws CorruptIndexException, IOException {
        return luceneIndex.getDocument(docId);
    }


	@Override
	public List<Integer> getProblems() {
		// TODO Auto-generated method stub
		return null;
	}

}


class LuceneDataRow implements DataRow {

    static private EnumMap<OutputOption, String> luceneFieldMapping = new EnumMap<OutputOption, String>(OutputOption.class);

    static {
        luceneFieldMapping.put(OutputOption.CHROMOSOME, "chr");
        luceneFieldMapping.put(OutputOption.EC_NUMBERS, "ec");
        luceneFieldMapping.put(OutputOption.GO_IDS, "go");
        luceneFieldMapping.put(OutputOption.GPI_ANCHOR, "gpi");
        luceneFieldMapping.put(OutputOption.INTERPRO_IDS, "interpro");
        luceneFieldMapping.put(OutputOption.ISOELECTRIC_POINT, "isoelectric");
        luceneFieldMapping.put(OutputOption.LOCATION, "location");
        luceneFieldMapping.put(OutputOption.MOL_WEIGHT, "mw");
        luceneFieldMapping.put(OutputOption.NUM_TM_DOMAINS, "tm");
        luceneFieldMapping.put(OutputOption.ORGANISM, "organism.commonName");
        luceneFieldMapping.put(OutputOption.PFAM_IDS, "pfam");
        luceneFieldMapping.put(OutputOption.PREV_SYS_ID, "psysid");
        luceneFieldMapping.put(OutputOption.PRIMARY_NAME, "primary");
        luceneFieldMapping.put(OutputOption.PRODUCT, "product");
        luceneFieldMapping.put(OutputOption.SIG_P, "sigp");
        luceneFieldMapping.put(OutputOption.SYNONYMS, "synonyms");
        luceneFieldMapping.put(OutputOption.SYS_ID, "uniqueName");
    }

    private Document document;

    public LuceneDataRow(Document document) {
        this.document = document;
    }

    @Override
    public String getValue(OutputOption oo) {
        String column = luceneFieldMapping.get(oo);
        if(column.equals("location")) {
            int start = Integer.parseInt(document.get("start"));
            int stop = Integer.parseInt(document.get("stop"));
            String strand = document.get("strand");

            String locs = String.format("%d-%d", start,stop);
            if(strand.equals("-1")) {
                locs = String.format("(%d-%d)", start,stop);
            }
            return locs;
        }
        return document.get(column);
    }

}
