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
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * List Download
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/DownloadHistoryItem")
public class ExportExcel {

    private Logger logger = Logger.getLogger(this.getClass());

    private LuceneIndexFactory luceneIndexFactory;
    private LuceneIndex luceneIndex;

    //@PostConstruct
    private void openLuceneIndex() {
        luceneIndex = luceneIndexFactory.getIndex("org.gmod.schema.mapped.Feature");
    }

    private TopDocs lookupInLucene(List<String> ids) throws IOException {
        logger.debug(String.format("#~# luceneIndexFactory is '%s'", luceneIndexFactory));
        BooleanQuery bQuery = new BooleanQuery();
        for (String id : ids) {
            bQuery.add(new TermQuery(new Term("uniqueName",id)), Occur.SHOULD);
        }
        logger.debug(String.format("#~# luceneIndex is '%s'", luceneIndex));
        return luceneIndex.search(bQuery);
    }

    public void createExcel(TopDocs topDocs,String file,String[] columns, OutputStream out) throws IOException {
        int rcount = 2;
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet();
        HSSFRow heading = sheet.createRow(rcount++);
        short count = 1;

        //add headers
        for (String column: columns) {
            HSSFCell cell = heading.createCell(count);
            HSSFRichTextString value = null;
            if(column.equals("organism.commonName")) {
                value = new HSSFRichTextString("Organism");
            } else if (column.equals("uniqueName")) {
                value = new HSSFRichTextString("Systematic Id");
            } else if (column.equals("synonym")) {
                value = new HSSFRichTextString("Synonyms");
            } else if (column.equals("locs")) {
                value = new HSSFRichTextString("Location");
            } else if (column.equals("chr")) {
                value = new HSSFRichTextString("Chromosome");
            } else if (column.equals("product")) {
                value = new HSSFRichTextString("Product");
            }

            cell.setCellValue(value);
            count++;
        }
        rcount++;

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = null;
            try {
                doc = fetchDocument(scoreDoc.doc);
            } catch (CorruptIndexException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            HSSFRow row = sheet.createRow(rcount++);
            count = 1;
            for (String column: columns) {
                HSSFCell cell = row.createCell(count);
                HSSFRichTextString value = new HSSFRichTextString(getValue(column,doc));
                cell.setCellValue(value);
                count++;
            }
        }

//        File dir = new File(getServletContext().getRealPath("/"));
//        File f = File.createTempFile(file, ".xls",dir);
//
//        FileOutputStream stream=null;
//        try {
//            stream = new FileOutputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        try {
            workbook.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return f;
    }

    private String getValue(String column, Document doc) {
        if(column.equals("locs")) {
            int start = Integer.parseInt(doc.get("start"));
            int stop = Integer.parseInt(doc.get("stop"));
            String strand = doc.get("strand");

            String locs = String.format("%d-%d", start,stop);
            if(strand.equals("-1")) {
                locs = String.format("(%d-%d)", start,stop);
            }
            return locs;
        }
        return doc.get(column);
    }


    private Document fetchDocument(int docId) throws CorruptIndexException, IOException {
        return luceneIndex.getDocument(docId);
    }

//    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
//        this.historyManagerFactory = historyManagerFactory;
//    }


    //@Required
    public void setLuceneIndexFactory(LuceneIndexFactory luceneIndexFactory) {
        this.luceneIndexFactory = luceneIndexFactory;
    }

}

