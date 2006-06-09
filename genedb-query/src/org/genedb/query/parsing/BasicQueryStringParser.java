package org.genedb.query.parsing;

import org.genedb.query.QueryI;
import org.genedb.query.QueryStringParser;

import java.util.Map;
import java.util.Stack;

/**
 * 
 * It parsers queries of the form:
 * queryname[querycontext]{{param1=val1}{param2=val2}} SET_OP (... e.g.
 * queryname[species=pombe]{{min=3}{max=7}}
 * 
 * @author art
 * 
 */
public class BasicQueryStringParser implements QueryStringParser {

    private enum ParseState {
        QUERY_NAME, CONTEXT, POST_CONTEXT, PARAMSET, PARAM, OUTSIDE, OPERATOR;
    }
    
    private enum Token {
        LEFT_PAREN, RIGHT_PAREN;
    }

    private class StackItem {
        private Token token;
        private Object data;
    }
    
    public ParsedElement parseOneQueryInString(String queryString, Stack stack) {

        queryString = queryString.trim();
        int pos = 0;

        ParsedElement ret = new ParsedElement();
        String context;
        String paramPairs;

        StringBuffer acc = new StringBuffer();
        ParseState state = ParseState.QUERY_NAME;
        while ((pos < queryString.length()) && (state != ParseState.OUTSIDE)) {
            char c = queryString.charAt(pos);
            switch (c) {
            
            case '(':
                if (state == ParseState.OUTSIDE) {
                    stack.push(Token.LEFT_PAREN);
                    break;
                }  else {
                    acc.append(c);
                }
                break;
            
            case ')':
                if (state == ParseState.OUTSIDE) {
                    stack.push(Token.RIGHT_PAREN);
                    break;
                }  else {
                    acc.append(c);
                }
                break;
                
            case '[':
                if (state == ParseState.QUERY_NAME) {
                    ret.setQueryName(acc.toString());
                    acc.setLength(0);
                    state = ParseState.CONTEXT;
                } else {
                    acc.append(c);
                }
                break;

            case ']':
                if (state == ParseState.CONTEXT) {
                    context = acc.toString();
                    acc.setLength(0);
                    state = ParseState.POST_CONTEXT;
                } else {
                    acc.append(c);
                }
                break;

            case '{':
                switch (state) {
                case POST_CONTEXT:
                    state = ParseState.PARAMSET;
                    break;
                default:
                    acc.append(c);
                    break;
                }

            case '}':
                switch (state) {
                case PARAMSET:
                    state = ParseState.OUTSIDE;
                    break;
                case PARAM:
                    state = ParseState.PARAMSET;
                default:
                    acc.append(c);
                    break;
                }

            case ' ':
                switch (state) {
                case PARAMSET:
                    break; // Ignore white space between param pairs
                default:
                    acc.append(c);
                    break;
                }

            default:
                acc.append(c);
            }
        }

        if (pos < queryString.length() - 1) {
            ret.remainder = queryString.substring(pos);
        }
        return ret;
    }

    /**
     * @see org.genedb.zoe.query.QueryStringParser#parseQueryString(java.lang.String)
     */
    public QueryI parseQueryString(String queryString) {
        // TODO Auto-generated method stub
        return null;
    }

    private class ParsedElement {
        private String queryName;

        private Map context;

        private Map params;

        private String remainder;

        public Map getContext() {
            return this.context;
        }

        public void setContext(Map context) {
            this.context = context;
        }

        public Map getParams() {
            return this.params;
        }

        public void setParams(Map params) {
            this.params = params;
        }

        public String getQueryName() {
            return this.queryName;
        }

        public void setQueryName(String queryName) {
            this.queryName = queryName;
        }

        public String getRemainder() {
            return this.remainder;
        }

        public void setRemainder(String remainder) {
            this.remainder = remainder;
        }
    }

}
