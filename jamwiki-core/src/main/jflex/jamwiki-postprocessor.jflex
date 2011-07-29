/*
 * This class handles references, TOC insertion, and other elements that require
 * pre-processing before they can be fully parsed.
 */
package org.jamwiki.parser.jflex;

%%

%public
%class JAMWikiPostLexer
%extends JFlexLexer
%type String
%unicode
%ignorecase

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

<YYINITIAL, PRE> {
    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return JFlexParserUtil.tagContent(yytext());
    }
}

/* ----- pre ----- */

<YYINITIAL> {
    {htmlprestart} {
        if (logger.isTraceEnabled()) logger.trace("htmlprestart: " + yytext() + " (" + yystate() + ")");
        beginState(PRE);
        return yytext();
    }
}
<PRE> {
    {htmlpreend} {
        if (logger.isTraceEnabled()) logger.trace("htmlpreend: " + yytext() + " (" + yystate() + ")");
        endState();
        return yytext();
    }
    {whitespace} | . {
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

    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}
