package org.genedb.querying.parsing;

// $ANTLR 3.0 Expr.g 2007-06-19 14:34:38

//import org.antlr.runtime.CharStream;
//import org.antlr.runtime.EarlyExitException;
//import org.antlr.runtime.Lexer;
//import org.antlr.runtime.MismatchedSetException;
//import org.antlr.runtime.NoViableAltException;
//import org.antlr.runtime.RecognitionException;

public class ExprLexer {
//public class ExprLexer extends Lexer {
    public static final int T21=21;
    public static final int UNION=7;
    public static final int T29=29;
    public static final int LCA=10;
    public static final int T22=22;
    public static final int WS=12;
    public static final int HISTORY_BY_NUM=17;
    public static final int STRING=6;
    public static final int T28=28;
    public static final int T23=23;
    public static final int QUERY=15;
    public static final int HISTORY_BY_NAME=16;
    public static final int T20=20;
    public static final int T25=25;
    public static final int T18=18;
    public static final int T26=26;
    public static final int INT=4;
    public static final int EOF=-1;
    public static final int UCA=11;
    public static final int SUBTRACT=9;
    public static final int Tokens=30;
    public static final int PARAM=13;
    public static final int T27=27;
    public static final int PARAMS=14;
    public static final int NAME=5;
    public static final int T24=24;
    public static final int T19=19;
    public static final int INTERSECT=8;
    public ExprLexer() {;}
/*    public ExprLexer(CharStream input) {
        super(input);
    }
    @Override
    public String getGrammarFileName() { return "Expr.g"; }

    // $ANTLR start T18
    public final void mT18() throws RecognitionException {
        try {
            int _type = T18;
            // Expr.g:3:7: ( '(' )
            // Expr.g:3:7: '('
            {
            match('(');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T18

    // $ANTLR start T19
    public final void mT19() throws RecognitionException {
        try {
            int _type = T19;
            // Expr.g:4:7: ( ')' )
            // Expr.g:4:7: ')'
            {
            match(')');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T19

    // $ANTLR start T20
    public final void mT20() throws RecognitionException {
        try {
            int _type = T20;
            // Expr.g:5:7: ( '{' )
            // Expr.g:5:7: '{'
            {
            match('{');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T20

    // $ANTLR start T21
    public final void mT21() throws RecognitionException {
        try {
            int _type = T21;
            // Expr.g:6:7: ( '}' )
            // Expr.g:6:7: '}'
            {
            match('}');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T21

    // $ANTLR start T22
    public final void mT22() throws RecognitionException {
        try {
            int _type = T22;
            // Expr.g:7:7: ( ';' )
            // Expr.g:7:7: ';'
            {
            match(';');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T22

    // $ANTLR start T23
    public final void mT23() throws RecognitionException {
        try {
            int _type = T23;
            // Expr.g:8:7: ( '=' )
            // Expr.g:8:7: '='
            {
            match('=');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T23

    // $ANTLR start T24
    public final void mT24() throws RecognitionException {
        try {
            int _type = T24;
            // Expr.g:9:7: ( 'union' )
            // Expr.g:9:7: 'union'
            {
            match("union");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T24

    // $ANTLR start T25
    public final void mT25() throws RecognitionException {
        try {
            int _type = T25;
            // Expr.g:10:7: ( 'or' )
            // Expr.g:10:7: 'or'
            {
            match("or");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T25

    // $ANTLR start T26
    public final void mT26() throws RecognitionException {
        try {
            int _type = T26;
            // Expr.g:11:7: ( 'intersect' )
            // Expr.g:11:7: 'intersect'
            {
            match("intersect");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T26

    // $ANTLR start T27
    public final void mT27() throws RecognitionException {
        try {
            int _type = T27;
            // Expr.g:12:7: ( 'and' )
            // Expr.g:12:7: 'and'
            {
            match("and");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T27

    // $ANTLR start T28
    public final void mT28() throws RecognitionException {
        try {
            int _type = T28;
            // Expr.g:13:7: ( 'subtract' )
            // Expr.g:13:7: 'subtract'
            {
            match("subtract");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T28

    // $ANTLR start T29
    public final void mT29() throws RecognitionException {
        try {
            int _type = T29;
            // Expr.g:14:7: ( 'not' )
            // Expr.g:14:7: 'not'
            {
            match("not");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end T29

    // $ANTLR start UNION
    public final void mUNION() throws RecognitionException {
        try {
            int _type = UNION;
            // Expr.g:38:9: ( 'OP_UNION' )
            // Expr.g:38:9: 'OP_UNION'
            {
            match("OP_UNION");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end UNION

    // $ANTLR start INTERSECT
    public final void mINTERSECT() throws RecognitionException {
        try {
            int _type = INTERSECT;
            // Expr.g:40:4: ( 'OP_INTERSECT' )
            // Expr.g:40:4: 'OP_INTERSECT'
            {
            match("OP_INTERSECT");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INTERSECT

    // $ANTLR start SUBTRACT
    public final void mSUBTRACT() throws RecognitionException {
        try {
            int _type = SUBTRACT;
            // Expr.g:41:11: ( 'OP_SUBTRACT' )
            // Expr.g:41:11: 'OP_SUBTRACT'
            {
            match("OP_SUBTRACT");


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end SUBTRACT

    // $ANTLR start NAME
    public final void mNAME() throws RecognitionException {
        try {
            int _type = NAME;
            // Expr.g:43:8: ( ( LCA | UCA ) ( LCA | UCA | '_' | '0' .. '9' )* )
            // Expr.g:43:8: ( LCA | UCA ) ( LCA | UCA | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // Expr.g:43:18: ( LCA | UCA | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
                case 1 :
                    // Expr.g:
                    {
                    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;

                default :
                    break loop1;
                }
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end NAME

    // $ANTLR start STRING
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            // Expr.g:45:11: ( '\"' ( LCA | UCA | '0' .. '9' | WS | ':' | '/' | '.' | '?' | '=' | '&' )* '\"' )
            // Expr.g:45:11: '\"' ( LCA | UCA | '0' .. '9' | WS | ':' | '/' | '.' | '?' | '=' | '&' )* '\"'
            {
            match('\"');
            // Expr.g:45:15: ( LCA | UCA | '0' .. '9' | WS | ':' | '/' | '.' | '?' | '=' | '&' )*
            loop2:
            do {
                int alt2=11;
                switch ( input.LA(1) ) {
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt2=1;
                    }
                    break;
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                    {
                    alt2=2;
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt2=3;
                    }
                    break;
                case '\t':
                case '\n':
                case '\r':
                case ' ':
                    {
                    alt2=4;
                    }
                    break;
                case ':':
                    {
                    alt2=5;
                    }
                    break;
                case '/':
                    {
                    alt2=6;
                    }
                    break;
                case '.':
                    {
                    alt2=7;
                    }
                    break;
                case '?':
                    {
                    alt2=8;
                    }
                    break;
                case '=':
                    {
                    alt2=9;
                    }
                    break;
                case '&':
                    {
                    alt2=10;
                    }
                    break;

                }

                switch (alt2) {
                case 1 :
                    // Expr.g:45:16: LCA
                    {
                    mLCA();

                    }
                    break;
                case 2 :
                    // Expr.g:45:20: UCA
                    {
                    mUCA();

                    }
                    break;
                case 3 :
                    // Expr.g:45:24: '0' .. '9'
                    {
                    matchRange('0','9');

                    }
                    break;
                case 4 :
                    // Expr.g:45:33: WS
                    {
                    mWS();

                    }
                    break;
                case 5 :
                    // Expr.g:45:36: ':'
                    {
                    match(':');

                    }
                    break;
                case 6 :
                    // Expr.g:45:40: '/'
                    {
                    match('/');

                    }
                    break;
                case 7 :
                    // Expr.g:45:44: '.'
                    {
                    match('.');

                    }
                    break;
                case 8 :
                    // Expr.g:45:48: '?'
                    {
                    match('?');

                    }
                    break;
                case 9 :
                    // Expr.g:45:52: '='
                    {
                    match('=');

                    }
                    break;
                case 10 :
                    // Expr.g:45:56: '&'
                    {
                    match('&');

                    }
                    break;

                default :
                    break loop2;
                }
            } while (true);

            match('\"');

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end STRING

    // $ANTLR start INT
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            // Expr.g:47:7: ( ( '0' .. '9' )+ )
            // Expr.g:47:7: ( '0' .. '9' )+
            {
            // Expr.g:47:7: ( '0' .. '9' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
                case 1 :
                    // Expr.g:47:7: '0' .. '9'
                    {
                    matchRange('0','9');

                    }
                    break;

                default :
                    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end INT

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            // Expr.g:48:6: ( ( ' ' | '\\t' | '\\n' | '\\r' )+ )
            // Expr.g:48:6: ( ' ' | '\\t' | '\\n' | '\\r' )+
            {
            // Expr.g:48:6: ( ' ' | '\\t' | '\\n' | '\\r' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='\t' && LA4_0<='\n')||LA4_0=='\r'||LA4_0==' ') ) {
                    alt4=1;
                }


                switch (alt4) {
                case 1 :
                    // Expr.g:
                    {
                    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }


                    }
                    break;

                default :
                    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);

            skip();

            }

            this.type = _type;
        }
        finally {
        }
    }
    // $ANTLR end WS

    // $ANTLR start PARAM
    public final void mPARAM() throws RecognitionException {
        try {
            // Expr.g:51:9: ( 'param' )
            // Expr.g:51:9: 'param'
            {
            match("param");


            }

        }
        finally {
        }
    }
    // $ANTLR end PARAM

    // $ANTLR start PARAMS
    public final void mPARAMS() throws RecognitionException {
        try {
            // Expr.g:53:10: ( 'params' )
            // Expr.g:53:10: 'params'
            {
            match("params");


            }

        }
        finally {
        }
    }
    // $ANTLR end PARAMS

    // $ANTLR start QUERY
    public final void mQUERY() throws RecognitionException {
        try {
            // Expr.g:55:9: ( 'query' )
            // Expr.g:55:9: 'query'
            {
            match("query");


            }

        }
        finally {
        }
    }
    // $ANTLR end QUERY

    // $ANTLR start HISTORY_BY_NAME
    public final void mHISTORY_BY_NAME() throws RecognitionException {
        try {
            // Expr.g:58:4: ( 'by_name' )
            // Expr.g:58:4: 'by_name'
            {
            match("by_name");


            }

        }
        finally {
        }
    }
    // $ANTLR end HISTORY_BY_NAME

    // $ANTLR start HISTORY_BY_NUM
    public final void mHISTORY_BY_NUM() throws RecognitionException {
        try {
            // Expr.g:61:4: ( 'by_num' )
            // Expr.g:61:4: 'by_num'
            {
            match("by_num");


            }

        }
        finally {
        }
    }
    // $ANTLR end HISTORY_BY_NUM

    // $ANTLR start LCA
    public final void mLCA() throws RecognitionException {
        try {
            // Expr.g:64:4: ( 'a' .. 'z' )
            // Expr.g:64:4: 'a' .. 'z'
            {
            matchRange('a','z');

            }

        }
        finally {
        }
    }
    // $ANTLR end LCA

    // $ANTLR start UCA
    public final void mUCA() throws RecognitionException {
        try {
            // Expr.g:67:4: ( 'A' .. 'Z' )
            // Expr.g:67:4: 'A' .. 'Z'
            {
            matchRange('A','Z');

            }

        }
        finally {
        }
    }
    // $ANTLR end UCA

    @Override
    public void mTokens() throws RecognitionException {
        // Expr.g:1:10: ( T18 | T19 | T20 | T21 | T22 | T23 | T24 | T25 | T26 | T27 | T28 | T29 | UNION | INTERSECT | SUBTRACT | NAME | STRING | INT | WS )
        int alt5=19;
        switch ( input.LA(1) ) {
        case '(':
            {
            alt5=1;
            }
            break;
        case ')':
            {
            alt5=2;
            }
            break;
        case '{':
            {
            alt5=3;
            }
            break;
        case '}':
            {
            alt5=4;
            }
            break;
        case ';':
            {
            alt5=5;
            }
            break;
        case '=':
            {
            alt5=6;
            }
            break;
        case 'u':
            {
            int LA5_7 = input.LA(2);

            if ( (LA5_7=='n') ) {
                int LA5_18 = input.LA(3);

                if ( (LA5_18=='i') ) {
                    int LA5_25 = input.LA(4);

                    if ( (LA5_25=='o') ) {
                        int LA5_32 = input.LA(5);

                        if ( (LA5_32=='n') ) {
                            int LA5_40 = input.LA(6);

                            if ( ((LA5_40>='0' && LA5_40<='9')||(LA5_40>='A' && LA5_40<='Z')||LA5_40=='_'||(LA5_40>='a' && LA5_40<='z')) ) {
                                alt5=16;
                            }
                            else {
                                alt5=7;}
                        }
                        else {
                            alt5=16;}
                    }
                    else {
                        alt5=16;}
                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'o':
            {
            int LA5_8 = input.LA(2);

            if ( (LA5_8=='r') ) {
                int LA5_19 = input.LA(3);

                if ( ((LA5_19>='0' && LA5_19<='9')||(LA5_19>='A' && LA5_19<='Z')||LA5_19=='_'||(LA5_19>='a' && LA5_19<='z')) ) {
                    alt5=16;
                }
                else {
                    alt5=8;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'i':
            {
            int LA5_9 = input.LA(2);

            if ( (LA5_9=='n') ) {
                int LA5_20 = input.LA(3);

                if ( (LA5_20=='t') ) {
                    int LA5_27 = input.LA(4);

                    if ( (LA5_27=='e') ) {
                        int LA5_33 = input.LA(5);

                        if ( (LA5_33=='r') ) {
                            int LA5_41 = input.LA(6);

                            if ( (LA5_41=='s') ) {
                                int LA5_47 = input.LA(7);

                                if ( (LA5_47=='e') ) {
                                    int LA5_52 = input.LA(8);

                                    if ( (LA5_52=='c') ) {
                                        int LA5_57 = input.LA(9);

                                        if ( (LA5_57=='t') ) {
                                            int LA5_62 = input.LA(10);

                                            if ( ((LA5_62>='0' && LA5_62<='9')||(LA5_62>='A' && LA5_62<='Z')||LA5_62=='_'||(LA5_62>='a' && LA5_62<='z')) ) {
                                                alt5=16;
                                            }
                                            else {
                                                alt5=9;}
                                        }
                                        else {
                                            alt5=16;}
                                    }
                                    else {
                                        alt5=16;}
                                }
                                else {
                                    alt5=16;}
                            }
                            else {
                                alt5=16;}
                        }
                        else {
                            alt5=16;}
                    }
                    else {
                        alt5=16;}
                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'a':
            {
            int LA5_10 = input.LA(2);

            if ( (LA5_10=='n') ) {
                int LA5_21 = input.LA(3);

                if ( (LA5_21=='d') ) {
                    int LA5_28 = input.LA(4);

                    if ( ((LA5_28>='0' && LA5_28<='9')||(LA5_28>='A' && LA5_28<='Z')||LA5_28=='_'||(LA5_28>='a' && LA5_28<='z')) ) {
                        alt5=16;
                    }
                    else {
                        alt5=10;}
                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 's':
            {
            int LA5_11 = input.LA(2);

            if ( (LA5_11=='u') ) {
                int LA5_22 = input.LA(3);

                if ( (LA5_22=='b') ) {
                    int LA5_29 = input.LA(4);

                    if ( (LA5_29=='t') ) {
                        int LA5_35 = input.LA(5);

                        if ( (LA5_35=='r') ) {
                            int LA5_42 = input.LA(6);

                            if ( (LA5_42=='a') ) {
                                int LA5_48 = input.LA(7);

                                if ( (LA5_48=='c') ) {
                                    int LA5_53 = input.LA(8);

                                    if ( (LA5_53=='t') ) {
                                        int LA5_58 = input.LA(9);

                                        if ( ((LA5_58>='0' && LA5_58<='9')||(LA5_58>='A' && LA5_58<='Z')||LA5_58=='_'||(LA5_58>='a' && LA5_58<='z')) ) {
                                            alt5=16;
                                        }
                                        else {
                                            alt5=11;}
                                    }
                                    else {
                                        alt5=16;}
                                }
                                else {
                                    alt5=16;}
                            }
                            else {
                                alt5=16;}
                        }
                        else {
                            alt5=16;}
                    }
                    else {
                        alt5=16;}
                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'n':
            {
            int LA5_12 = input.LA(2);

            if ( (LA5_12=='o') ) {
                int LA5_23 = input.LA(3);

                if ( (LA5_23=='t') ) {
                    int LA5_30 = input.LA(4);

                    if ( ((LA5_30>='0' && LA5_30<='9')||(LA5_30>='A' && LA5_30<='Z')||LA5_30=='_'||(LA5_30>='a' && LA5_30<='z')) ) {
                        alt5=16;
                    }
                    else {
                        alt5=12;}
                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'O':
            {
            int LA5_13 = input.LA(2);

            if ( (LA5_13=='P') ) {
                int LA5_24 = input.LA(3);

                if ( (LA5_24=='_') ) {
                    switch ( input.LA(4) ) {
                    case 'I':
                        {
                        int LA5_37 = input.LA(5);

                        if ( (LA5_37=='N') ) {
                            int LA5_43 = input.LA(6);

                            if ( (LA5_43=='T') ) {
                                int LA5_49 = input.LA(7);

                                if ( (LA5_49=='E') ) {
                                    int LA5_54 = input.LA(8);

                                    if ( (LA5_54=='R') ) {
                                        int LA5_59 = input.LA(9);

                                        if ( (LA5_59=='S') ) {
                                            int LA5_64 = input.LA(10);

                                            if ( (LA5_64=='E') ) {
                                                int LA5_68 = input.LA(11);

                                                if ( (LA5_68=='C') ) {
                                                    int LA5_70 = input.LA(12);

                                                    if ( (LA5_70=='T') ) {
                                                        int LA5_72 = input.LA(13);

                                                        if ( ((LA5_72>='0' && LA5_72<='9')||(LA5_72>='A' && LA5_72<='Z')||LA5_72=='_'||(LA5_72>='a' && LA5_72<='z')) ) {
                                                            alt5=16;
                                                        }
                                                        else {
                                                            alt5=14;}
                                                    }
                                                    else {
                                                        alt5=16;}
                                                }
                                                else {
                                                    alt5=16;}
                                            }
                                            else {
                                                alt5=16;}
                                        }
                                        else {
                                            alt5=16;}
                                    }
                                    else {
                                        alt5=16;}
                                }
                                else {
                                    alt5=16;}
                            }
                            else {
                                alt5=16;}
                        }
                        else {
                            alt5=16;}
                        }
                        break;
                    case 'S':
                        {
                        int LA5_38 = input.LA(5);

                        if ( (LA5_38=='U') ) {
                            int LA5_44 = input.LA(6);

                            if ( (LA5_44=='B') ) {
                                int LA5_50 = input.LA(7);

                                if ( (LA5_50=='T') ) {
                                    int LA5_55 = input.LA(8);

                                    if ( (LA5_55=='R') ) {
                                        int LA5_60 = input.LA(9);

                                        if ( (LA5_60=='A') ) {
                                            int LA5_65 = input.LA(10);

                                            if ( (LA5_65=='C') ) {
                                                int LA5_69 = input.LA(11);

                                                if ( (LA5_69=='T') ) {
                                                    int LA5_71 = input.LA(12);

                                                    if ( ((LA5_71>='0' && LA5_71<='9')||(LA5_71>='A' && LA5_71<='Z')||LA5_71=='_'||(LA5_71>='a' && LA5_71<='z')) ) {
                                                        alt5=16;
                                                    }
                                                    else {
                                                        alt5=15;}
                                                }
                                                else {
                                                    alt5=16;}
                                            }
                                            else {
                                                alt5=16;}
                                        }
                                        else {
                                            alt5=16;}
                                    }
                                    else {
                                        alt5=16;}
                                }
                                else {
                                    alt5=16;}
                            }
                            else {
                                alt5=16;}
                        }
                        else {
                            alt5=16;}
                        }
                        break;
                    case 'U':
                        {
                        int LA5_39 = input.LA(5);

                        if ( (LA5_39=='N') ) {
                            int LA5_45 = input.LA(6);

                            if ( (LA5_45=='I') ) {
                                int LA5_51 = input.LA(7);

                                if ( (LA5_51=='O') ) {
                                    int LA5_56 = input.LA(8);

                                    if ( (LA5_56=='N') ) {
                                        int LA5_61 = input.LA(9);

                                        if ( ((LA5_61>='0' && LA5_61<='9')||(LA5_61>='A' && LA5_61<='Z')||LA5_61=='_'||(LA5_61>='a' && LA5_61<='z')) ) {
                                            alt5=16;
                                        }
                                        else {
                                            alt5=13;}
                                    }
                                    else {
                                        alt5=16;}
                                }
                                else {
                                    alt5=16;}
                            }
                            else {
                                alt5=16;}
                        }
                        else {
                            alt5=16;}
                        }
                        break;
                    default:
                        alt5=16;}

                }
                else {
                    alt5=16;}
            }
            else {
                alt5=16;}
            }
            break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'p':
        case 'q':
        case 'r':
        case 't':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt5=16;
            }
            break;
        case '\"':
            {
            alt5=17;
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt5=18;
            }
            break;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt5=19;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens : ( T18 | T19 | T20 | T21 | T22 | T23 | T24 | T25 | T26 | T27 | T28 | T29 | UNION | INTERSECT | SUBTRACT | NAME | STRING | INT | WS );", 5, 0, input);

            throw nvae;
        }

        switch (alt5) {
            case 1 :
                // Expr.g:1:10: T18
                {
                mT18();

                }
                break;
            case 2 :
                // Expr.g:1:14: T19
                {
                mT19();

                }
                break;
            case 3 :
                // Expr.g:1:18: T20
                {
                mT20();

                }
                break;
            case 4 :
                // Expr.g:1:22: T21
                {
                mT21();

                }
                break;
            case 5 :
                // Expr.g:1:26: T22
                {
                mT22();

                }
                break;
            case 6 :
                // Expr.g:1:30: T23
                {
                mT23();

                }
                break;
            case 7 :
                // Expr.g:1:34: T24
                {
                mT24();

                }
                break;
            case 8 :
                // Expr.g:1:38: T25
                {
                mT25();

                }
                break;
            case 9 :
                // Expr.g:1:42: T26
                {
                mT26();

                }
                break;
            case 10 :
                // Expr.g:1:46: T27
                {
                mT27();

                }
                break;
            case 11 :
                // Expr.g:1:50: T28
                {
                mT28();

                }
                break;
            case 12 :
                // Expr.g:1:54: T29
                {
                mT29();

                }
                break;
            case 13 :
                // Expr.g:1:58: UNION
                {
                mUNION();

                }
                break;
            case 14 :
                // Expr.g:1:64: INTERSECT
                {
                mINTERSECT();

                }
                break;
            case 15 :
                // Expr.g:1:74: SUBTRACT
                {
                mSUBTRACT();

                }
                break;
            case 16 :
                // Expr.g:1:83: NAME
                {
                mNAME();

                }
                break;
            case 17 :
                // Expr.g:1:88: STRING
                {
                mSTRING();

                }
                break;
            case 18 :
                // Expr.g:1:95: INT
                {
                mINT();

                }
                break;
            case 19 :
                // Expr.g:1:99: WS
                {
                mWS();

                }
                break;

        }

    }


 */

}