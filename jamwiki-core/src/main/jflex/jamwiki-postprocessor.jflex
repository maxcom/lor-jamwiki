/*
 * This class adds paragraph tags as appropriate.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiPostProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiPostProcessor.class.getName());
%}

/* character expressions */
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ ]+) {htmlpreattributes} ([ ]*=[^>\n]+[ ]*)*
htmlprestart       = (<[ ]*pre ({htmlpreattribute})* [ ]* (\/)? [ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
toc                = "__TOC__"

/* references */
references         = (<[ ]*) "references" ([ ]*[\/]?[ ]*>)

%state PRE

%%

<YYINITIAL, PRE> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return JFlexParserUtil.tagContent(yytext());
    }
}

<YYINITIAL> {

    /* ----- pre ----- */

    {htmlprestart} {
        if (logger.isTraceEnabled()) logger.trace("htmlprestart: " + yytext() + " (" + yystate() + ")");
        beginState(PRE);
        return yytext();
    }
}

<PRE> {

    /* ----- pre ----- */

    {htmlpreend} {
        if (logger.isTraceEnabled()) logger.trace("htmlpreend: " + yytext() + " (" + yystate() + ")");
        endState();
        return yytext();
    }
    {whitespace} {
        // no need to log this
        return yytext();
    }
    . {
        // no need to log this
        return yytext();
    }
}

<YYINITIAL> {

    /* ----- processing commands ----- */

    {toc} {
        if (logger.isTraceEnabled()) logger.trace("toc: " + yytext() + " (" + yystate() + ")");
        return this.parserInput.getTableOfContents().attemptTOCInsertion();
    }

    /* ----- references ----- */

    {references} {
        if (logger.isTraceEnabled()) logger.trace("references: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_REFERENCES, yytext());
    }

    /* ----- javascript ----- */

    {javascript} {
        if (logger.isTraceEnabled()) logger.trace("javascript: " + yytext() + " (" + yystate() + ")");
        // javascript tags are parsed in the processor step, but parse again here as a security
        // check against potential XSS attacks.
        return this.parse(TAG_TYPE_JAVASCRIPT, yytext());
    }

    /* ----- other ----- */

    {whitespace} {
        // no need to log this
        return yytext();
    }
    . {
        // no need to log this
        return yytext();
    }
}
