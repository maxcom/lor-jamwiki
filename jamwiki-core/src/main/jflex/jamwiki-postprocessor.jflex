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
nowiki             = (<[ \t]*nowiki[ \t]*>) ~(<[ \t]*\/[ \t]*nowiki[ \t]*>)

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ \t]+) {htmlpreattributes} ([ \t]*=[^>\n]+[ \t]*)*
htmlprestart       = (<[ \t]*pre ({htmlpreattribute})* [ \t]* (\/)? [ \t]*>)
htmlpreend         = (<[ \t]*\/[ \t]*pre[ \t]*>)

/* javascript */
javascript         = (<[ \t]*script[^>]*>) ~(<[ \t]*\/[ \t]*script[ \t]*>)

/* processing commands */
toc                = "__TOC__"

/* references */
references         = (<[ \t]*) "references" ([ \t]*[\/]?[ \t]*>)

%state PRE

%%

/* ----- nowiki ----- */

<YYINITIAL, PRE>{nowiki} {
    if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
    return JFlexParserUtil.tagContent(yytext());
}

/* ----- pre ----- */

<YYINITIAL>{htmlprestart} {
    if (logger.isTraceEnabled()) logger.trace("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    if (logger.isTraceEnabled()) logger.trace("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<YYINITIAL>{toc} {
    if (logger.isTraceEnabled()) logger.trace("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInput.getTableOfContents().attemptTOCInsertion();
}

/* ----- references ----- */

<YYINITIAL>{references} {
    if (logger.isTraceEnabled()) logger.trace("references: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_REFERENCES, yytext());
}

/* ----- javascript ----- */

<YYINITIAL>{javascript} {
    if (logger.isTraceEnabled()) logger.trace("javascript: " + yytext() + " (" + yystate() + ")");
    // javascript tags are parsed in the processor step, but parse again here as a security
    // check against potential XSS attacks.
    return this.parse(TAG_TYPE_JAVASCRIPT, yytext());
}

/* ----- other ----- */

<YYINITIAL, PRE>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL, PRE>. {
    // no need to log this
    return yytext();
}
