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

import org.genedb.db.dao.SequenceDao;
import org.genedb.querying.core.LuceneIndex;
import org.genedb.querying.core.LuceneIndexFactory;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.web.mvc.controller.HistoryManagerFactory;
import org.genedb.web.utils.DownloadUtils;

import org.gmod.schema.feature.AbstractGene;
import org.gmod.schema.feature.Transcript;
import org.gmod.schema.mapped.Feature;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;



/**
 * List Download
 *
 * @author Chinmay Patel (cp2)
 * @author Adrian Tivey (art)
 */
@Controller
@RequestMapping("/Download")
public class DownloadController {

    private Logger logger = Logger.getLogger(this.getClass());

    private SequenceDao sequenceDao;
    private HistoryManagerFactory historyManagerFactory;
    private String downloadView;
    private DataFetcher dataFetcher;



    public void setDataFetcher(DataFetcher dataFetcher) {
        this.dataFetcher = dataFetcher;
    }




    @RequestMapping(method=RequestMethod.GET, value="/{historyItem}")
    public ModelAndView displayForm(
            @PathVariable("historyItem") int historyItem) {
        logger.error("displayForm called");
        ModelAndView mav = new ModelAndView("history/historyForm", "historyItem", historyItem);
        return mav;
    }


    @RequestMapping(method=RequestMethod.POST, value="/{historyItem}")
    public ModelAndView onSubmit(
            @PathVariable("historyItem") int historyItem,
            @RequestParam("cust_format") OutputFormat outputFormat,
            @RequestParam("cust_field") String[] custFields,
            @RequestParam("output_dest") OutputDestination outputDestination,
            @RequestParam(value="sequenceType", required=false) SequenceType sequenceType,
            @RequestParam("cust_header") boolean includeHeader,
            @RequestParam("field_sep") String fieldSeperator,
            @RequestParam("field_blank") String blankField,
            @RequestParam("field_intsep") String fieldInternalSeperator,
            HttpServletRequest request,
            HttpServletResponse response,
            Writer out
            ) throws Exception {

        HistoryManager historyManager = historyManagerFactory.getHistoryManager(request.getSession());
        List<HistoryItem> historyItems = historyManager.getHistoryItems();
        if (historyItem > historyItems.size()) {
            response.sendError(511);
            return null;
        }

        List<OutputOption> outputOptions = Lists.newArrayList();
        for (String custField : custFields) {
            outputOptions.add(OutputOption.valueOf(custField));
        }

        HistoryItem hItem = historyItems.get(historyItem-1);
        List<String> uniqueNames = hItem.getIds();
        List<Integer> featureIds = convertUniquenamesToFeatureIds(uniqueNames);
        TroubleTrackingIterator<DataRow> iterator = dataFetcher.iterator(featureIds, fieldSeperator);

        String file = request.getSession().getId();
//        String columns[] = null;
//        if (request.getParameter("columns") != null) {
//            columns = request.getParameter("columns").split(",");
//        }

        File output = null;

        switch (outputFormat) {
            case EXCEL:
                    //OutputStream outStream = response.getOutputStream();
                    //response.setContentType("application/vnd.ms-excel");
                    //response.setHeader("Content-Disposition", "attachment; filename=results.xls");
                    ExportExcel excel = new ExportExcel();
                    //createExcel(topDocs,file,columns, outStream);
                    return null;

            case CSV:
            case TAB:
                response.setContentType("application/x-download");
                response.setHeader("Content-Disposition", "attachment; filename=results.txt");
                CsvOutputFormatter csv = new CsvOutputFormatter(out);
                csv.setOutputOptions(outputOptions);
                csv.setHeader(true);
                csv.writeHeader();
                csv.writeBody(iterator);
                csv.writeFooter();
                logProblems(iterator);
                return null;

            case HTML:
                HtmlOutputFormatter html = new HtmlOutputFormatter(out);
                html.setOutputOptions(outputOptions);
                html.setHeader(true);
                html.writeHeader();
                html.writeBody(iterator);
                html.writeFooter();
                logProblems(iterator);
                return null;

            case FASTA:
                    output = createFasta(dataFetcher,file,outputOptions,sequenceType);
                    response.setContentType("application/x-download");
                    response.setHeader("Content-Disposition", "attachment");
                    response.setHeader("filename", output.getName());
                    logProblems(iterator);
            }
        return null;
    }



    private void logProblems(TroubleTrackingIterator<DataRow> iterator) {
        List<Integer> problems = iterator.getProblems();
        for (Integer problem : problems) {
            logger.error("Unable to retrieve details for '"+problem+"'");
        }
    }




    private List<Integer> convertUniquenamesToFeatureIds(List<String> uniqueNames) {
        List<Integer> ret = Lists.newArrayList();
        for (String name : uniqueNames) {
            logger.error("Trying to lookup '"+name+"' as a Transcript");
            Feature f = sequenceDao.getFeatureByUniqueName(name, Feature.class);
            logger.error("The value is '"+f+"'");
            ret.add(f.getFeatureId());
        }

        return ret;
    }




    private File createFasta(DataFetcher df, String file,List<OutputOption> outputOptions, SequenceType sequenceType) {

        StringBuilder whole = new StringBuilder();
        StringBuilder row;

        //add data
//        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//            row = new StringBuilder();
//            Document doc = null;
//            try {
//                doc = fetchDocument(scoreDoc.doc);
//            } catch (CorruptIndexException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (OutputOption outputOption: outputOptions) {
//                String column = outputOption.name();
//                if(!column.equals("sequence")) {
//                    row.append(fetcher.getValue(column) + ";");
//                }
//            }
//            row.deleteCharAt(row.length()-1);
//
//            AbstractGene gene =  (AbstractGene)sequenceDao.getFeatureByUniqueName(doc.get("uniqueName"), Feature.class);
//            logger.info(String.format(" Gene %s Type %s",doc.get("uniqueName"),gene.getType().getName() ));
//            String sequence = DownloadUtils.getSequence(gene, sequenceType);
//            String entry;
//            if(sequence != null) {
//                entry = DownloadUtils.writeFasta(row.toString(), sequence);
//            } else {
//                entry = String.format("%s \n Alternately spliced or sequence not attached ", row.toString());
//            }
//
//            whole.append(entry);
//            whole.append("\n\n");
//        }



        BufferedWriter out = null;
        File outFile = new File("/");//getServletContext().getRealPath("/" + file));
        try {
            out = new BufferedWriter(new FileWriter(outFile));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

        try {
            out.write(whole.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outFile;
    }


//        BufferedWriter out = null;
//        File outFile = new File(getServletContext().getRealPath("/" + file));
//        try {
//            out = new BufferedWriter(new FileWriter(outFile));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            out.write(whole.toString());
//            out.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public void setHistoryManagerFactory(HistoryManagerFactory historyManagerFactory) {
        this.historyManagerFactory = historyManagerFactory;
    }

    public String getDownloadView() {
        return downloadView;
    }

    public void setDownloadView(String downloadView) {
        this.downloadView = downloadView;
    }

}

