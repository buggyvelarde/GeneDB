package org.genedb.query;

import org.antlr.runtime.*; 
import org.antlr.runtime.tree.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class QueryLineParser { 
    public static void main(String[] args) throws Exception { 
        // Create an input character stream from standard in 
        String line = "3 union geneByNonsense{wibble=\"fred\"; wobble=\"weeble\"; big=\"small\"} not ( geneByUri{ url=\"http://www.genedb.org/Things?a=1&b=2\"} intersect result)";
        // Create an ExprLexer that feeds from that stream
        ANTLRStringStream input = new ANTLRStringStream(line);
        //ANTLRInputStream input = new ANTLRInputStream(System.in); 
        // Create an ExprLexer that feeds from that stream 
        ExprLexer lexer = new ExprLexer(input); 
        // Create a stream of tokens fed by the lexer 
        CommonTokenStream tokens = new CommonTokenStream(lexer); 
        // Create a parser that feeds off the token stream 
        ExprParser parser = new ExprParser(tokens); 
        // Begin parsing at rule prog 
        ExprParser.prog_return r = parser.prog(); 

        // WALK RESULTING TREE
        CommonTree t = (CommonTree)r.getTree(); // get tree from parser

        showNode(t, 0);
        List<String> answer = processTree(t);
    } 
    
    private static void showNode(CommonTree t, int depth) {
        for (int i = 0; i < depth; i++) {
            System.err.print("\t");
        }
        System.err.println(t);
        for (int i = 0; i < t.getChildCount(); i++) {
            showNode((CommonTree) t.getChild(i), depth+1);
        }
    }
    
    private static List<String> processTree(CommonTree t) {
        List<String> results = null;
        List<String> right = null;
        System.err.println(t);
        switch (t.getType()) {
            case ExprParser.SUBTRACT:
                results = processTree((CommonTree)t.getChild(0));
                right = processTree((CommonTree)t.getChild(1));
                results.removeAll(right); 
                break;
            case ExprParser.UNION:
                results = processTree((CommonTree)t.getChild(0));
                right = processTree((CommonTree)t.getChild(1));
                results.addAll(right); 
                break;
            case ExprParser.INTERSECT:
                results = processTree((CommonTree)t.getChild(0));
                right = processTree((CommonTree)t.getChild(1));
                results.retainAll(right); 
                break;
            case ExprParser.QUERY:
                Map<String, String> map = new HashMap<String, String>();
                String queryName =  ((CommonTree)t.getChild(0)).getText();
                if (t.getChildCount()>1) {
                    CommonTree params = (CommonTree)t.getChild(1);
                    for (int i = 0; i < params.getChildCount(); i++) {
                        CommonTree param = (CommonTree)params.getChild(i);
                        String key = ((CommonTree)param.getChild(0)).getText();
                        String value = ((CommonTree)param.getChild(1)).getText();
                        map.put(key, value);
                    }
                }
                // Run query
                System.err.println("QUERY: '"+queryName+"' with '"+map+"'");
                results = new ArrayList<String>();
                break;
            case ExprParser.HISTORY_BY_NUM:
                String number =  ((CommonTree)t.getChild(0)).getText();
                int num = Integer.parseInt(number);
                System.err.println("H_NUM: '"+(CommonTree)t.getChild(0)+"'");
                results = new ArrayList<String>();
                break;
            case ExprParser.HISTORY_BY_NAME:
                String name =  ((CommonTree)t.getChild(0)).getText();
                System.err.println("H_NAME: '"+(CommonTree)t.getChild(0)+"'");
                results = new ArrayList<String>();
                break;
            default:
                throw new RuntimeException("Unhandled tree node '"+t+"', token index '"+t.token.getTokenIndex()+"'"); 
        }
        return results;
    }
} 
