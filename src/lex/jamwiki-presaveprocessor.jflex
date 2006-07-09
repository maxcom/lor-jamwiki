/*
 * This class adds paragraph tags as appropriate.
 */
package org.jamwiki.parser;

import java.util.Stack;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;

%%

%public
%class JAMWikiPreSaveProcessor
%implements org.jamwiki.parser.Lexer
%type String
%unicode

/* code included in the constructor */
%init{
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(JAMWikiPreSaveProcessor.class.getName());
    /** Member variable used to keep track of the state history for the lexer. */
    protected Stack states = new Stack();
    protected ParserInfo parserInfo = null;

    /**
     * Begin a new state and store the old state onto the stack.
     */
    protected void beginState(int state) {
        // store current state
        Integer current = new Integer(yystate());
        states.push(current);
        // switch to new state
        yybegin(state);
    }

    /**
     * End processing of a state and switch to the previous state.
     */
    protected void endState() {
        // revert to previous state
        int next = ((Integer)states.pop()).intValue();
        yybegin(next);
    }
    
    /**
     *
     */
    public void setParserInfo(ParserInfo parserInfo) {
        this.parserInfo = parserInfo;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowikistart        = "<nowiki>"
nowikiend          = "</nowiki>"

/* pre */
htmlprestart       = (<[ ]*[Pp][Rr][Ee][ ]*>)
htmlpreend         = (<[ ]*\/[ ]*[Pp][Rr][Ee][ ]*>)

/* processing commands */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

%state NOWIKI, PRE, NORMAL

%%

/* ----- parsing tags ----- */

<PRE, NORMAL>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return yytext();
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

<NORMAL>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<NORMAL>{wikisig3} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInfo, true, false);
}

<NORMAL>{wikisig4} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInfo, true, true);
}

<NORMAL>{wikisig5} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInfo, false, true);
}

/* ----- other ----- */

<PRE, NOWIKI, NORMAL>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NOWIKI, NORMAL>. {
    // no need to log this
    return yytext();
}
