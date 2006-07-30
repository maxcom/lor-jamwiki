/*
 * This class adds paragraph tags as appropriate.
 */
package org.jamwiki.parser;

import java.util.Stack;
import org.apache.log4j.Logger;

%%

%public
%class JAMWikiPostProcessor
%implements org.jamwiki.parser.Lexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        endState();
        output.append("</p>");
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(JAMWikiPostProcessor.class.getName());
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
inputcharacter     = [^\r\n\<]
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowikistart        = (<[ ]*nowiki[ ]*>)
nowikiend          = (<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
toc                = "__TOC__"

/* paragraph */
nonparagraphstart  = "<table" | "<div" | "<h1" | "<h2" | "<h3" | "<h4" | "<h5" | "<pre" | "<ul" | "<dl" | "<ol" | "</td>" | "<span"
nonparagraphend    = "</table>" | "</div>" | "</h1>" | "</h2>" | "</h3>" | "</h4>" | "</h5>" | "</pre>" | "</ul>" | "</dl>" | "</ol>" | "<td" [^\>]* ~">" | "</span>"
paragraphend       = ({newline} {newline})
paragraphstart     = {nonparagraphend} {inputcharacter}
paragraphstart2    = {inputcharacter} | "<i>" | "<b>" | "<a href"

%state NOWIKI, PRE, NORMAL, P, NONPARAGRAPH

%%

/* ----- nowiki ----- */

<PRE, NORMAL, P, NONPARAGRAPH>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return "";
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return "";
}

/* ----- pre ----- */

<NORMAL, P, NONPARAGRAPH>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return "<pre>";
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return "</pre>";
}

/* ----- processing commands ----- */

<NORMAL, P, NONPARAGRAPH>{toc} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInfo.getTableOfContents().attemptTOCInsertion();
}

/* ----- javascript ----- */

<NORMAL, P, NONPARAGRAPH>{javascript} {
    logger.debug("javascript: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- layout ----- */

<NORMAL, P, NONPARAGRAPH>{nonparagraphstart} {
    logger.debug("nonparagraphstart: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        output.append("</p>");
        endState();
    }
    beginState(NONPARAGRAPH);
    return output.toString() + yytext();
}

<NONPARAGRAPH>{nonparagraphend} {
    logger.debug("nonparagraphend: " + yytext() + " (" + yystate() + ")");
    endState();
    if (yystate() != NONPARAGRAPH) {
        // if not non-paragraph, roll back to allow potential paragraph start
        yypushback(yytext().length());
    }
    return yytext();
}

<NORMAL>{paragraphstart} {
    logger.debug("paragraphstart: " + yytext() + " (" + yystate() + ")");
    beginState(P);
    // start paragraph, then rollback to allow normal processing
    yypushback(1);
    return yytext() + "<p>";
}

<NORMAL>^{paragraphstart2} {
    logger.debug("paragraphstart2: " + yytext() + " (" + yystate() + ")");
    beginState(P);
    // start paragraph, then rollback to allow normal processing
    yypushback(yytext().length());
    return "<p>";
}

<P>{paragraphend} {
    logger.debug("end of paragraph: " + yytext() + " (" + yystate() + ")");
    endState();
    return "</p>" + yytext();
}

/* ----- other ----- */

<PRE, NOWIKI, NORMAL, NONPARAGRAPH, P>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NOWIKI, NORMAL, NONPARAGRAPH, P>. {
    // no need to log this
    return yytext();
}
