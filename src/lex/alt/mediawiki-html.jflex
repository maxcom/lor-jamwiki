package org.vqwiki.lex.alt;

/*
 * This class adds paragraph tags as appropriate.
 *   
 *
 * @author W. Ryan Holliday
 */

import java.util.Stack;
import org.apache.log4j.Logger;
import org.vqwiki.WikiBase;

%%

%public
%class MediaWikiHTML
%implements org.vqwiki.lex.Lexer
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
    if (yystate() == P) {
        endState();
        output.append("</p>");
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger log = Logger.getLogger(MediaWikiHTML.class.getName());
    /** Member variable used to keep track of the state history for the lexer. */
    protected Stack states = new Stack();
    protected String virtualWiki;

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
    public void setVirtualWiki(String vWiki) {
        this.virtualWiki = vWiki;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
inputcharacter     = [^\r\n\<]
whitespace         = {newline} | [ \t\f]

/* paragraph */
nonparagraphstart  = "<table" | "<div" | "<h1" | "<h2" | "<h3" | "<h4" | "<h5" | "<pre" | "<ul" | "<dl" | "<ol" | "</td>" | "<span"
nonparagraphend    = "</table>" | "</div>" | "</h1>" | "</h2>" | "</h3>" | "</h4>" | "</h5>" | "</pre>" | "</ul>" | "</dl>" | "</ol>" | "<td" [^\>]* ~">" | "</span>"
paragraphend       = ({newline} {newline})
paragraphstart     = {nonparagraphend} {inputcharacter}
paragraphstart2    = {inputcharacter} | "<i>" | "<b>" | "<a href"

%state NORMAL, P, NONPARAGRAPH

%%

<NORMAL, P, NONPARAGRAPH>{nonparagraphstart} {
    log.debug("nonparagraphstart: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        output.append("</p>");
        endState();
    }
    beginState(NONPARAGRAPH);
    return output.toString() + yytext();
}

<NONPARAGRAPH>{nonparagraphend} {
    log.debug("nonparagraphend: " + yytext() + " (" + yystate() + ")");
    endState();
    if (yystate() != NONPARAGRAPH) {
        // if not non-paragraph, roll back to allow potential paragraph start
        yypushback(yytext().length());
    }
    return yytext();
}

<NORMAL>{paragraphstart} {
    log.debug("paragraphstart: " + yytext() + " (" + yystate() + ")");
    beginState(P);
    // start paragraph, then rollback to allow normal processing
    yypushback(1);
    return yytext() + "<p>";
}

<NORMAL>^{paragraphstart2} {
    log.debug("paragraphstart2: " + yytext() + " (" + yystate() + ")");
    beginState(P);
    // start paragraph, then rollback to allow normal processing
    yypushback(yytext().length());
    return "<p>";
}

<P>{paragraphend} {
    log.debug("end of paragraph: " + yytext() + " (" + yystate() + ")");
    endState();
    return "</p>" + yytext();
}

<NORMAL, NONPARAGRAPH, P>{whitespace} {
    log.debug("{whitespace}: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, NONPARAGRAPH, P>. {
    log.debug("default: " + yytext() + " (" + yystate() + ")");
    return yytext();
}
