// Farha Ferdous
// CSCI 316 Project 1: Lexer Analyzer

package Spring2026;

/**
 * This class is a lexical analyzer for the tokens defined in the Token Specification:
 * 
 * IDENT      : starts with letter, composed entirely of letters and numbers
 * INT        : integer literal
 * FLOAT      : decimal literal (may begin but not end with decimal point, no exponentiation)
 * ASSIGN     : <-
 * LPAREN     : (
 * RPAREN     : )
 * ADD        : +
 * SUB        : -
 * MUL        : *
 * DIV        : /
 * COMMA      : ,
 * EQ         : =
 * NEQ        : /=
 * LT         : <
 * GT         : >
 * LE         : <=
 * GE         : >=
 * LBRACE     : {
 * RBRACE     : }
 * 
 * Keywords: DISPLAY, INPUT, MOD, RANDOM, NOT, AND, OR, IF, ELSE, REPEAT, TIMES, UNTIL, 
 *           PROCEDURE, RETURN
 */

public class Lexer extends IO
{
    // changed: complete redesign of State enum for new token set
    // changed: moved MINUS, ANGLE, DIV_PART, GT_PART to final states section
    // removed: DOUBLE_E, DOUBLE_E_MINUS, TIMES, END, PLUS, MINUS (as final states)
    // removed: EXP_DOUBLE, DOUBLE
    // added: FLOAT, ASSIGN, LPAREN, RPAREN, ADD, SUB, MUL, DIV, COMMA, EQ, NEQ, LT, GT, LE, GE, LBRACE, RBRACE
    // added: MINUS, ANGLE, DIV_PART, GT_PART as final states for single-character operators

    public enum State 
    {
        // non-final states (kept START and DOT from original)
        START,
        DOT,            // for FLOAT starting with decimal point
        
        // final states
        IDENT,  // kept from original
        INT,    // kept from original
        FLOAT,  // changed: from DOUBLE to FLOAT (no exponentiation)
        ASSIGN, // changed: from "=" to "<-"
        LPAREN, // added: (
        RPAREN, // added: )
        ADD,    // changed: from PLUS to ADD
        SUB,    // changed: from MINUS to SUB (handled separately)
        MUL,    // changed: from TIMES to MUL
        DIV,    // changed: from DIV to DIV (but different context)
        COMMA,  // added: ,
        EQ,     // added: =
        NEQ,    // added: /=
        LT,     // added: <
        GT,     // added: >
        LE,     // added: <=
        GE,     // added: >=
        LBRACE, // added: {
        RBRACE, // added: }
        MINUS,          // added: for SUB when alone
        ANGLE,          // added: for LT when alone
        DIV_PART,       // changed: for DIV when alone
        GT_PART,        // added: for GT when alone
        
        UNDEFINED;

        // changed: isFinal logic updated to work with new enum ordering
        private boolean isFinal()
        {
            return this.compareTo(State.IDENT) >= 0 && this != State.UNDEFINED;
        }
    }

    public static String t; // holds an extracted token
    public static State state; // the current state of the FA

    // keywords list (all uppercase as specified)
    public static String[] keywords = {
        "DISPLAY", "INPUT", "MOD", "RANDOM", "NOT", "AND", "OR", 
        "IF", "ELSE", "REPEAT", "TIMES", "UNTIL", "PROCEDURE", "RETURN"
    };

    private static State nextState[][] = new State[State.values().length][128];

    private static int driver()
    {
        State nextSt;
        t = "";
        state = State.START;

        if (Character.isWhitespace((char) a))
            a = getChar();
        if (a == -1)
            return -1;

        while (a != -1)
        {
            c = (char) a;
            System.out.println("Debug2: state=" + state + ", char='" + c + "'");    // added: Debug output (will be removed for final submission)
            nextSt = nextState[state.ordinal()][a];
            
            if (nextSt == State.UNDEFINED)
            {
                if (state.isFinal())
                {
                    // special check if FLOAT ends with decimal point (invalid)
                    if (state == State.FLOAT && t.endsWith("."))
                    {
                        t = t + c;  // Add the invalid character to the token
                        a = getNextChar();
                        return 0;   // Lexical error
                    }
                    return 1;       // Valid token
                }
                else
                {
                    t = t + c;
                    a = getNextChar();
                    return 0;       // Lexical error
                }
            }
            else
            {
                state = nextSt;
                t = t + c;
                a = getNextChar();
            }
        }

        //end of stream reached
        if (state.isFinal())
        {
            // check if FLOAT ends with decimal point (invalid)
            if (state == State.FLOAT && t.endsWith("."))
            {
                return 0; // Lexical error
            }
            return 1;
        }
        else
        {
            return 0; // not final state at end of input = error
        }
    }

    // rewritten for new token set
    private static void setNextState()
    {
        // Initialize all transitions to UNDEFINED
        for (int s = 0; s < nextState.length; s++)
            for (int c = 0; c < nextState[0].length; c++)
                nextState[s][c] = State.UNDEFINED;

        // Digits -- modified:  removed EXP_DOUBLE, DOUBLE_E, DOUBLE_E_MINUS transitions
        for (char c = '0'; c <= '9'; c++)
        {
            nextState[State.START.ordinal()][c] = State.INT;
            nextState[State.INT.ordinal()][c] = State.INT;
            nextState[State.DOT.ordinal()][c] = State.FLOAT;
            nextState[State.FLOAT.ordinal()][c] = State.FLOAT;
            nextState[State.IDENT.ordinal()][c] = State.IDENT;
        }

        // Letters (for IDENT and keywords)
        for (char c = 'A'; c <= 'Z'; c++)
        {
            nextState[State.START.ordinal()][c] = State.IDENT;
            nextState[State.IDENT.ordinal()][c] = State.IDENT;
        }
        for (char c = 'a'; c <= 'z'; c++)
        {
            nextState[State.START.ordinal()][c] = State.IDENT;
            nextState[State.IDENT.ordinal()][c] = State.IDENT;
        }

        // Single character tokens -- replaced with new token types
        // REMOVED: ';' (END), '+' (PLUS), '-' (MINUS), '*' (TIMES), '/' (DIV), '=' (ASSIGN)
        // ADDED: new single-character tokens
        nextState[State.START.ordinal()]['('] = State.LPAREN;
        nextState[State.START.ordinal()][')'] = State.RPAREN;
        nextState[State.START.ordinal()]['+'] = State.ADD;
        nextState[State.START.ordinal()]['*'] = State.MUL;
        nextState[State.START.ordinal()][','] = State.COMMA;
        nextState[State.START.ordinal()]['='] = State.EQ;
        nextState[State.START.ordinal()]['{'] = State.LBRACE;
        nextState[State.START.ordinal()]['}'] = State.RBRACE;
        
        // Special handling for multi-character operators -- added
        nextState[State.START.ordinal()]['-'] = State.MINUS;        // could be SUB or part of ASSIGN
        nextState[State.START.ordinal()]['/'] = State.DIV_PART;    // could be DIV or part of NEQ
        nextState[State.START.ordinal()]['<'] = State.ANGLE;       // could be ASSIGN, LT, or LE
        nextState[State.START.ordinal()]['>'] = State.GT_PART;     // could be GT or part of GE
        
        // Decimal point - kept but targets FLOAT instead of DOUBLE
        nextState[State.START.ordinal()]['.'] = State.DOT;
        nextState[State.INT.ordinal()]['.'] = State.FLOAT;

        // Transitions for ASSIGN (<-) -- added
        nextState[State.MINUS.ordinal()]['>'] = State.ASSIGN;
        
        // Transitions from ANGLE state -- added
        nextState[State.ANGLE.ordinal()]['-'] = State.ASSIGN;  // <-
        nextState[State.ANGLE.ordinal()]['='] = State.LE;      // <=
        // note: If ANGLE state is final with no more chars, it becomes LT
        
        // Transitions for NEQ (/=)
        nextState[State.DIV_PART.ordinal()]['='] = State.NEQ;
        
        // Transitions for GE (>=)
        nextState[State.GT_PART.ordinal()]['='] = State.GE;
    }

    // setLex() - KEPT IDENTICAL to original (just calls setNextState)
    public static void setLex()
    {
        setNextState();
    }

    // main() - MOSTLY KEPT SAME STRUCTURE, with ADDED handling for new states
    public static void main(String argv[])
    {
        // added: usage check (not in original but good practice)
        if (argv.length < 2)
        {
            System.out.println("Usage: java Spring2026.Lexer <input file> <output file>");
            return;
        }

        setIO(argv[0], argv[1]);
        setLex();

        int result;

        while (a != -1)
        {
            result = driver();
            
            if (result == 1)
            {
                // Check if it's a keyword (IDENT that matches a keyword)
                if (state == State.IDENT)
                {
                    boolean isKeyword = false;
                    for (String keyword : keywords)
                    {
                        if (t.equals(keyword))
                        {
                            displayln(t + "\t: Keyword_" + t);
                            isKeyword = true;
                            break;
                        }
                    }
                    if (!isKeyword)
                        displayln(t + "\t: " + state.toString());
                }
                // Handle MINUS state specially - if we get here with MINUS, it's actually SUB
                else if (state == State.MINUS)
                {
                    displayln(t + "\t: SUB");
                }
                // Handle ANGLE state specially - if we get here with ANGLE, it's actually LT
                else if (state == State.ANGLE)
                {
                    displayln(t + "\t: LT");
                }
                // Handle DIV_PART state specially - if we get here with DIV_PART, it's actually DIV
                else if (state == State.DIV_PART)
                {
                    displayln(t + "\t: DIV");
                }
                // Handle GT_PART state specially - if we get here with GT_PART, it's actually GT
                else if (state == State.GT_PART)
                {
                    displayln(t + "\t: GT");
                }
                else
                {
                    displayln(t + "\t: " + state.toString());
                }
            }
            else if (result == 0)
            {
                displayln(t + "\t: Lexical Error, invalid token");
            }
        }

        closeIO();
    }
}