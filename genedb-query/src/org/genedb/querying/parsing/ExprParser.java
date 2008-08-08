package org.genedb.querying.parsing;

// $ANTLR 3.0 Expr.g 2007-06-19 14:34:36

import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteEarlyExitException;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;

@SuppressWarnings("all")
public class ExprParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "INT", "NAME", "STRING", "UNION", "INTERSECT", "SUBTRACT", "LCA", "UCA", "WS", "PARAM", "PARAMS", "QUERY", "HISTORY_BY_NAME", "HISTORY_BY_NUM", "'('", "')'", "'{'", "'}'", "';'", "'='", "'union'", "'or'", "'intersect'", "'and'", "'subtract'", "'not'"
    };
    public static final int UNION=7;
    public static final int LCA=10;
    public static final int INT=4;
    public static final int WS=12;
    public static final int UCA=11;
    public static final int EOF=-1;
    public static final int HISTORY_BY_NUM=17;
    public static final int SUBTRACT=9;
    public static final int STRING=6;
    public static final int PARAM=13;
    public static final int QUERY=15;
    public static final int PARAMS=14;
    public static final int NAME=5;
    public static final int HISTORY_BY_NAME=16;
    public static final int INTERSECT=8;

        public ExprParser(TokenStream input) {
            super(input);
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    @Override
    public String[] getTokenNames() { return tokenNames; }
    @Override
    public String getGrammarFileName() { return "Expr.g"; }


    public static class prog_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start prog
    // Expr.g:9:1: prog : ( expr )+ ;
    public final prog_return prog() throws RecognitionException {
        prog_return retval = new prog_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        expr_return expr1 = null;



        try {
            // Expr.g:9:8: ( ( expr )+ )
            // Expr.g:9:8: ( expr )+
            {
            root_0 = (CommonTree)adaptor.nil();

            // Expr.g:9:8: ( expr )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=INT && LA1_0<=NAME)||LA1_0==18) ) {
                    alt1=1;
                }


                switch (alt1) {
                case 1 :
                    // Expr.g:9:10: expr
                    {
                    pushFollow(FOLLOW_expr_in_prog36);
                    expr1=expr();
                    _fsp--;

                    adaptor.addChild(root_0, expr1.getTree());

                    }
                    break;

                default :
                    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end prog

    public static class expr_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start expr
    // Expr.g:11:1: expr : atom ( operator atom )* ;
    public final expr_return expr() throws RecognitionException {
        expr_return retval = new expr_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        atom_return atom2 = null;

        operator_return operator3 = null;

        atom_return atom4 = null;



        try {
            // Expr.g:11:8: ( atom ( operator atom )* )
            // Expr.g:11:8: atom ( operator atom )*
            {
            root_0 = (CommonTree)adaptor.nil();

            pushFollow(FOLLOW_atom_in_expr48);
            atom2=atom();
            _fsp--;

            adaptor.addChild(root_0, atom2.getTree());
            // Expr.g:11:13: ( operator atom )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=24 && LA2_0<=29)) ) {
                    alt2=1;
                }


                switch (alt2) {
                case 1 :
                    // Expr.g:11:14: operator atom
                    {
                    pushFollow(FOLLOW_operator_in_expr51);
                    operator3=operator();
                    _fsp--;

                    root_0 = (CommonTree)adaptor.becomeRoot(operator3.getTree(), root_0);
                    pushFollow(FOLLOW_atom_in_expr54);
                    atom4=atom();
                    _fsp--;

                    adaptor.addChild(root_0, atom4.getTree());

                    }
                    break;

                default :
                    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end expr

    public static class atom_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start atom
    // Expr.g:13:1: atom : ( INT -> ^( HISTORY_BY_NUM INT ) | NAME -> ^( HISTORY_BY_NAME NAME ) | bean | '(' expr ')' );
    public final atom_return atom() throws RecognitionException {
        atom_return retval = new atom_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token INT5=null;
        Token NAME6=null;
        Token char_literal8=null;
        Token char_literal10=null;
        bean_return bean7 = null;

        expr_return expr9 = null;


        CommonTree INT5_tree=null;
        CommonTree NAME6_tree=null;
        CommonTree char_literal8_tree=null;
        CommonTree char_literal10_tree=null;
        RewriteRuleTokenStream stream_NAME=new RewriteRuleTokenStream(adaptor,"token NAME");
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");

        try {
            // Expr.g:13:9: ( INT -> ^( HISTORY_BY_NUM INT ) | NAME -> ^( HISTORY_BY_NAME NAME ) | bean | '(' expr ')' )
            int alt3=4;
            switch ( input.LA(1) ) {
            case INT:
                {
                alt3=1;
                }
                break;
            case NAME:
                {
                int LA3_2 = input.LA(2);

                if ( (LA3_2==20) ) {
                    alt3=3;
                }
                else if ( (LA3_2==EOF||(LA3_2>=INT && LA3_2<=NAME)||(LA3_2>=18 && LA3_2<=19)||(LA3_2>=24 && LA3_2<=29)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("13:1: atom : ( INT -> ^( HISTORY_BY_NUM INT ) | NAME -> ^( HISTORY_BY_NAME NAME ) | bean | '(' expr ')' );", 3, 2, input);

                    throw nvae;
                }
                }
                break;
            case 18:
                {
                alt3=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("13:1: atom : ( INT -> ^( HISTORY_BY_NUM INT ) | NAME -> ^( HISTORY_BY_NAME NAME ) | bean | '(' expr ')' );", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // Expr.g:13:9: INT
                    {
                    INT5=(Token)input.LT(1);
                    match(input,INT,FOLLOW_INT_in_atom69); 
                    stream_INT.add(INT5);


                    // AST REWRITE
                    // elements: INT
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 13:13: -> ^( HISTORY_BY_NUM INT )
                    {
                        // Expr.g:13:16: ^( HISTORY_BY_NUM INT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(HISTORY_BY_NUM, "HISTORY_BY_NUM"), root_1);

                        adaptor.addChild(root_1, stream_INT.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;
                case 2 :
                    // Expr.g:14:5: NAME
                    {
                    NAME6=(Token)input.LT(1);
                    match(input,NAME,FOLLOW_NAME_in_atom83); 
                    stream_NAME.add(NAME6);


                    // AST REWRITE
                    // elements: NAME
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 14:10: -> ^( HISTORY_BY_NAME NAME )
                    {
                        // Expr.g:14:13: ^( HISTORY_BY_NAME NAME )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(HISTORY_BY_NAME, "HISTORY_BY_NAME"), root_1);

                        adaptor.addChild(root_1, stream_NAME.next());

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;
                case 3 :
                    // Expr.g:15:5: bean
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_bean_in_atom97);
                    bean7=bean();
                    _fsp--;

                    adaptor.addChild(root_0, bean7.getTree());

                    }
                    break;
                case 4 :
                    // Expr.g:16:5: '(' expr ')'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal8=(Token)input.LT(1);
                    match(input,18,FOLLOW_18_in_atom103); 
                    pushFollow(FOLLOW_expr_in_atom106);
                    expr9=expr();
                    _fsp--;

                    adaptor.addChild(root_0, expr9.getTree());
                    char_literal10=(Token)input.LT(1);
                    match(input,19,FOLLOW_19_in_atom108); 

                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end atom

    public static class bean_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start bean
    // Expr.g:19:1: bean : NAME '{' params '}' -> ^( QUERY NAME params ) ;
    public final bean_return bean() throws RecognitionException {
        bean_return retval = new bean_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NAME11=null;
        Token char_literal12=null;
        Token char_literal14=null;
        params_return params13 = null;


        CommonTree NAME11_tree=null;
        CommonTree char_literal12_tree=null;
        CommonTree char_literal14_tree=null;
        RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
        RewriteRuleTokenStream stream_20=new RewriteRuleTokenStream(adaptor,"token 20");
        RewriteRuleTokenStream stream_NAME=new RewriteRuleTokenStream(adaptor,"token NAME");
        RewriteRuleSubtreeStream stream_params=new RewriteRuleSubtreeStream(adaptor,"rule params");
        try {
            // Expr.g:19:8: ( NAME '{' params '}' -> ^( QUERY NAME params ) )
            // Expr.g:19:8: NAME '{' params '}'
            {
            NAME11=(Token)input.LT(1);
            match(input,NAME,FOLLOW_NAME_in_bean121); 
            stream_NAME.add(NAME11);

            char_literal12=(Token)input.LT(1);
            match(input,20,FOLLOW_20_in_bean123); 
            stream_20.add(char_literal12);

            pushFollow(FOLLOW_params_in_bean125);
            params13=params();
            _fsp--;

            stream_params.add(params13.getTree());
            char_literal14=(Token)input.LT(1);
            match(input,21,FOLLOW_21_in_bean127); 
            stream_21.add(char_literal14);


            // AST REWRITE
            // elements: NAME, params
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 19:28: -> ^( QUERY NAME params )
            {
                // Expr.g:19:31: ^( QUERY NAME params )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(QUERY, "QUERY"), root_1);

                adaptor.addChild(root_1, stream_NAME.next());
                adaptor.addChild(root_1, stream_params.next());

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end bean

    public static class params_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start params
    // Expr.g:21:1: params : param ( ';' param )* -> ^( PARAMS ( param )+ ) ;
    public final params_return params() throws RecognitionException {
        params_return retval = new params_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal16=null;
        param_return param15 = null;

        param_return param17 = null;


        CommonTree char_literal16_tree=null;
        RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
        RewriteRuleSubtreeStream stream_param=new RewriteRuleSubtreeStream(adaptor,"rule param");
        try {
            // Expr.g:21:10: ( param ( ';' param )* -> ^( PARAMS ( param )+ ) )
            // Expr.g:21:10: param ( ';' param )*
            {
            pushFollow(FOLLOW_param_in_params147);
            param15=param();
            _fsp--;

            stream_param.add(param15.getTree());
            // Expr.g:21:16: ( ';' param )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==22) ) {
                    alt4=1;
                }


                switch (alt4) {
                case 1 :
                    // Expr.g:21:18: ';' param
                    {
                    char_literal16=(Token)input.LT(1);
                    match(input,22,FOLLOW_22_in_params151); 
                    stream_22.add(char_literal16);

                    pushFollow(FOLLOW_param_in_params153);
                    param17=param();
                    _fsp--;

                    stream_param.add(param17.getTree());

                    }
                    break;

                default :
                    break loop4;
                }
            } while (true);


            // AST REWRITE
            // elements: param
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 21:31: -> ^( PARAMS ( param )+ )
            {
                // Expr.g:21:34: ^( PARAMS ( param )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(PARAMS, "PARAMS"), root_1);

                if ( !(stream_param.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_param.hasNext() ) {
                    adaptor.addChild(root_1, stream_param.next());

                }
                stream_param.reset();

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end params

    public static class param_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start param
    // Expr.g:23:1: param : NAME '=' STRING -> ^( PARAM NAME STRING ) ;
    public final param_return param() throws RecognitionException {
        param_return retval = new param_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token NAME18=null;
        Token char_literal19=null;
        Token STRING20=null;

        CommonTree NAME18_tree=null;
        CommonTree char_literal19_tree=null;
        CommonTree STRING20_tree=null;
        RewriteRuleTokenStream stream_NAME=new RewriteRuleTokenStream(adaptor,"token NAME");
        RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
        RewriteRuleTokenStream stream_STRING=new RewriteRuleTokenStream(adaptor,"token STRING");

        try {
            // Expr.g:23:9: ( NAME '=' STRING -> ^( PARAM NAME STRING ) )
            // Expr.g:23:9: NAME '=' STRING
            {
            NAME18=(Token)input.LT(1);
            match(input,NAME,FOLLOW_NAME_in_param174); 
            stream_NAME.add(NAME18);

            char_literal19=(Token)input.LT(1);
            match(input,23,FOLLOW_23_in_param176); 
            stream_23.add(char_literal19);

            STRING20=(Token)input.LT(1);
            match(input,STRING,FOLLOW_STRING_in_param178); 
            stream_STRING.add(STRING20);


            // AST REWRITE
            // elements: NAME, STRING
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 23:25: -> ^( PARAM NAME STRING )
            {
                // Expr.g:23:28: ^( PARAM NAME STRING )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(PARAM, "PARAM"), root_1);

                adaptor.addChild(root_1, stream_NAME.next());
                adaptor.addChild(root_1, stream_STRING.next());

                adaptor.addChild(root_0, root_1);
                }

            }



            }

            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end param

    public static class operator_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start operator
    // Expr.g:25:1: operator : ( union | intersect | subtract );
    public final operator_return operator() throws RecognitionException {
        operator_return retval = new operator_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        union_return union21 = null;

        intersect_return intersect22 = null;

        subtract_return subtract23 = null;



        try {
            // Expr.g:25:11: ( union | intersect | subtract )
            int alt5=3;
            switch ( input.LA(1) ) {
            case 24:
            case 25:
                {
                alt5=1;
                }
                break;
            case 26:
            case 27:
                {
                alt5=2;
                }
                break;
            case 28:
            case 29:
                {
                alt5=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("25:1: operator : ( union | intersect | subtract );", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // Expr.g:25:11: union
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_union_in_operator196);
                    union21=union();
                    _fsp--;

                    adaptor.addChild(root_0, union21.getTree());

                    }
                    break;
                case 2 :
                    // Expr.g:25:19: intersect
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_intersect_in_operator200);
                    intersect22=intersect();
                    _fsp--;

                    adaptor.addChild(root_0, intersect22.getTree());

                    }
                    break;
                case 3 :
                    // Expr.g:25:31: subtract
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_subtract_in_operator204);
                    subtract23=subtract();
                    _fsp--;

                    adaptor.addChild(root_0, subtract23.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end operator

    public static class union_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start union
    // Expr.g:27:1: union : ( 'union' -> ^( UNION ) | 'or' -> ^( UNION ) );
    public final union_return union() throws RecognitionException {
        union_return retval = new union_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal24=null;
        Token string_literal25=null;

        CommonTree string_literal24_tree=null;
        CommonTree string_literal25_tree=null;
        RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
        RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");

        try {
            // Expr.g:28:4: ( 'union' -> ^( UNION ) | 'or' -> ^( UNION ) )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==24) ) {
                alt6=1;
            }
            else if ( (LA6_0==25) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("27:1: union : ( 'union' -> ^( UNION ) | 'or' -> ^( UNION ) );", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // Expr.g:28:4: 'union'
                    {
                    string_literal24=(Token)input.LT(1);
                    match(input,24,FOLLOW_24_in_union215); 
                    stream_24.add(string_literal24);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 28:12: -> ^( UNION )
                    {
                        // Expr.g:28:15: ^( UNION )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(UNION, "UNION"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;
                case 2 :
                    // Expr.g:29:4: 'or'
                    {
                    string_literal25=(Token)input.LT(1);
                    match(input,25,FOLLOW_25_in_union226); 
                    stream_25.add(string_literal25);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 29:9: -> ^( UNION )
                    {
                        // Expr.g:29:12: ^( UNION )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(UNION, "UNION"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end union

    public static class intersect_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start intersect
    // Expr.g:30:1: intersect : ( 'intersect' -> ^( INTERSECT ) | 'and' -> ^( INTERSECT ) );
    public final intersect_return intersect() throws RecognitionException {
        intersect_return retval = new intersect_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal26=null;
        Token string_literal27=null;

        CommonTree string_literal26_tree=null;
        CommonTree string_literal27_tree=null;
        RewriteRuleTokenStream stream_26=new RewriteRuleTokenStream(adaptor,"token 26");
        RewriteRuleTokenStream stream_27=new RewriteRuleTokenStream(adaptor,"token 27");

        try {
            // Expr.g:31:4: ( 'intersect' -> ^( INTERSECT ) | 'and' -> ^( INTERSECT ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==26) ) {
                alt7=1;
            }
            else if ( (LA7_0==27) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("30:1: intersect : ( 'intersect' -> ^( INTERSECT ) | 'and' -> ^( INTERSECT ) );", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // Expr.g:31:4: 'intersect'
                    {
                    string_literal26=(Token)input.LT(1);
                    match(input,26,FOLLOW_26_in_intersect240); 
                    stream_26.add(string_literal26);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 31:16: -> ^( INTERSECT )
                    {
                        // Expr.g:31:19: ^( INTERSECT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(INTERSECT, "INTERSECT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;
                case 2 :
                    // Expr.g:32:4: 'and'
                    {
                    string_literal27=(Token)input.LT(1);
                    match(input,27,FOLLOW_27_in_intersect251); 
                    stream_27.add(string_literal27);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 32:10: -> ^( INTERSECT )
                    {
                        // Expr.g:32:13: ^( INTERSECT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(INTERSECT, "INTERSECT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end intersect

    public static class subtract_return extends ParserRuleReturnScope {
        CommonTree tree;
        @Override
        public Object getTree() { return tree; }
    };

    // $ANTLR start subtract
    // Expr.g:33:1: subtract : ( 'subtract' -> ^( SUBTRACT ) | 'not' -> ^( SUBTRACT ) );
    public final subtract_return subtract() throws RecognitionException {
        subtract_return retval = new subtract_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal28=null;
        Token string_literal29=null;

        CommonTree string_literal28_tree=null;
        CommonTree string_literal29_tree=null;
        RewriteRuleTokenStream stream_28=new RewriteRuleTokenStream(adaptor,"token 28");
        RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");

        try {
            // Expr.g:34:4: ( 'subtract' -> ^( SUBTRACT ) | 'not' -> ^( SUBTRACT ) )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==28) ) {
                alt8=1;
            }
            else if ( (LA8_0==29) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("33:1: subtract : ( 'subtract' -> ^( SUBTRACT ) | 'not' -> ^( SUBTRACT ) );", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // Expr.g:34:4: 'subtract'
                    {
                    string_literal28=(Token)input.LT(1);
                    match(input,28,FOLLOW_28_in_subtract265); 
                    stream_28.add(string_literal28);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 34:15: -> ^( SUBTRACT )
                    {
                        // Expr.g:34:18: ^( SUBTRACT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(SUBTRACT, "SUBTRACT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;
                case 2 :
                    // Expr.g:35:4: 'not'
                    {
                    string_literal29=(Token)input.LT(1);
                    match(input,29,FOLLOW_29_in_subtract276); 
                    stream_29.add(string_literal29);


                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 35:10: -> ^( SUBTRACT )
                    {
                        // Expr.g:35:13: ^( SUBTRACT )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(adaptor.create(SUBTRACT, "SUBTRACT"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }



                    }
                    break;

            }
            retval.stop = input.LT(-1);

                retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
                adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end subtract


 

    public static final BitSet FOLLOW_expr_in_prog36 = new BitSet(new long[]{0x0000000000040032L});
    public static final BitSet FOLLOW_atom_in_expr48 = new BitSet(new long[]{0x000000003F000002L});
    public static final BitSet FOLLOW_operator_in_expr51 = new BitSet(new long[]{0x0000000000040030L});
    public static final BitSet FOLLOW_atom_in_expr54 = new BitSet(new long[]{0x000000003F000002L});
    public static final BitSet FOLLOW_INT_in_atom69 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_atom83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_bean_in_atom97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_atom103 = new BitSet(new long[]{0x0000000000040030L});
    public static final BitSet FOLLOW_expr_in_atom106 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_atom108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NAME_in_bean121 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_20_in_bean123 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_params_in_bean125 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_21_in_bean127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_param_in_params147 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_22_in_params151 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_param_in_params153 = new BitSet(new long[]{0x0000000000400002L});
    public static final BitSet FOLLOW_NAME_in_param174 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_23_in_param176 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_STRING_in_param178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_union_in_operator196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_intersect_in_operator200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subtract_in_operator204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_union215 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_union226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_intersect240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_intersect251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_subtract265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_subtract276 = new BitSet(new long[]{0x0000000000000002L});

}