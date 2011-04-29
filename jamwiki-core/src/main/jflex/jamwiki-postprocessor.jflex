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

/* ----- nowiki ----- */

<YYINITIAL, PRE>{nowiki} {
    if (logger.isFinerEnabled()) logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    return JFlexParserUtil.tagContent(yytext());
}

/* ----- pre ----- */

<YYINITIAL>{htmlprestart} {
    if (logger.isFinerEnabled()) logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    if (logger.isFinerEnabled()) logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<YYINITIAL>{toc} {
    if (logger.isFinerEnabled()) logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInput.getTableOfContents().attemptTOCInsertion();
}

/* ----- references ----- */

<YYINITIAL>{references} {
    if (logger.isFinerEnabled()) logger.finer("references: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_REFERENCES, yytext());
}

/* ----- javascript ----- */

<YYINITIAL>{javascript} {
    if (logger.isFinerEnabled()) logger.finer("javascript: " + yytext() + " (" + yystate() + ")");
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
