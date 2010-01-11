package org.genedb.querying.parsing;

import org.genedb.querying.core.BooleanQuery;
import org.genedb.querying.core.BooleanQueryMode;
import org.genedb.querying.core.Query;
import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class QueryLineParser implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Query parse(String line, HistoryManager historyManager) throws RecognitionException {
        // Create an input character stream from standard in
        line = "3 union geneByNonsense{wibble=\"fred\"; wobble=\"weeble\"; big=\"small\"} not ( geneByUri{ url=\"http://www.genedb.org/Things?a=1&b=2\"} intersect result)";
        // Create an ExprLexer that feeds from that stream
        ANTLRStringStream input = new ANTLRStringStream(line);
        // ANTLRInputStream input = new ANTLRInputStream(System.in);
        // Create an ExprLexer that feeds from that stream
        ExprLexer lexer = new ExprLexer(input);
        // Create a stream of tokens fed by the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        // Create a parser that feeds off the token stream
        ExprParser parser = new ExprParser(tokens);
        // Begin parsing at rule prog
        ExprParser.prog_return r = parser.prog();

        // WALK RESULTING TREE
        CommonTree t = (CommonTree) r.getTree(); // get tree from parser

        // showNode(t, 0);
        return processTree(t, historyManager);
    }

    public static void main(String[] args) throws Exception {
        // Create an input character stream from standard in
        String line = "3 union geneByNonsense{wibble=\"fred\"; wobble=\"weeble\"; big=\"small\"} not ( geneByUri{ url=\"http://www.genedb.org/Things?a=1&b=2\"} intersect result)";
        QueryLineParser qlp = new QueryLineParser();
        Query q = qlp.parse(line, null);
        System.out.println(q.getResults());
    }

    // private void showNode(CommonTree t, int depth) {
    // for (int i = 0; i < depth; i++) {
    // System.err.print("\t");
    // }
    // System.err.println(t);
    // for (int i = 0; i < t.getChildCount(); i++) {
    // showNode((CommonTree) t.getChild(i), depth+1);
    // }
    // }

    private Query processTree(CommonTree t, HistoryManager historyManager) {
        Query results = null;
        switch (t.getType()) {
        case ExprParser.SUBTRACT:
            results = operator(t, BooleanQueryMode.SUBTRACT, historyManager);
            break;
        case ExprParser.UNION:
            results = operator(t, BooleanQueryMode.UNION, historyManager);
            break;
        case ExprParser.INTERSECT:
            results = operator(t, BooleanQueryMode.INTERSECT, historyManager);
            break;
        case ExprParser.QUERY:
            results = findQuery(t);
            break;
        case ExprParser.HISTORY_BY_NUM:
            String number = ((CommonTree) t.getChild(0)).getText();
            int num = Integer.parseInt(number);
            System.err.println("H_NUM: '" + t.getChild(0) + "'");
            results = historyManager.getHistoryItems().get(num).getQuery();
            break;
        case ExprParser.HISTORY_BY_NAME:
            String name = ((CommonTree) t.getChild(0)).getText();
            System.err.println("H_NAME: '" + t.getChild(0) + "'");
            for (HistoryItem item : historyManager.getHistoryItems()) {
                if (name.equals(item.getName())) {
                    results = item.getQuery();
                }
            }
            break;
        default:
            throw new RuntimeException("Unhandled tree node '" + t + "', token index '"
                    + t.token.getTokenIndex() + "'");
        }
        return results;
    }

    private Query findQuery(CommonTree t) {
        String queryName = ((CommonTree) t.getChild(0)).getText();

        Query query = applicationContext.getBean(queryName, Query.class);

        BeanWrapperImpl bw = new BeanWrapperImpl(query);
        if (t.getChildCount() > 1) {
            CommonTree params = (CommonTree) t.getChild(1);
            for (int i = 0; i < params.getChildCount(); i++) {
                CommonTree param = (CommonTree) params.getChild(i);
                String key = ((CommonTree) param.getChild(0)).getText();
                String value = ((CommonTree) param.getChild(1)).getText();
                bw.setPropertyValue(key, value);
            }
        }
        // System.err.println("QUERY: '"+queryName+"' with '"+map+"'");
        return query;
    }

    private Query operator(CommonTree t, BooleanQueryMode mode, HistoryManager historyManager) {
        Query results;
        Query left = processTree((CommonTree) t.getChild(0), historyManager);
        Query right = processTree((CommonTree) t.getChild(1), historyManager);
        results = new BooleanQuery(mode, left, right);
        return results;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
