/*
 * The pre-processor performs processes metadata and prepares the
 * document for the full parsing by the processor.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiPreProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiPreProcessor.class.getName());
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
htmlpre            = ({htmlprestart}) ~({htmlpreend})
wikipre            = (" ") ([^\n])+ ~({newline})
wikipreend         = [^ ] | {newline}

/* processing commands */
noeditsection      = ({newline})? "__NOEDITSECTION__"

/* wiki links */
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n]+) "]"
htmllinkraw        = ({protocol}) ([^ <'\"\n\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
wikilinkcontent    = [^\n\]] | "]" [^\n\]] | {htmllink}
wikilink           = "[[" ({wikilinkcontent})+ "]]" [a-z]*
nestedwikilink     = "[[" ({wikilinkcontent})+ "|" ({wikilinkcontent} | {wikilink})+ "]]"

/* redirect */
redirect           = "#REDIRECT" [ ]* {wikilink}

/* image gallery */
gallery            = (<[ ]*gallery[^>]*>) ~(<[ ]*\/[ ]*gallery[ ]*>)

%state WIKIPRE

%%

/* ----- nowiki ----- */

<YYINITIAL, WIKIPRE> {
    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }
}

/* ----- wikipre ----- */

<YYINITIAL, WIKIPRE> {
    ^{wikipre} {
        if (logger.isTraceEnabled()) logger.trace("wikipre: " + yytext() + " (" + yystate() + ")");
        // rollback all but the first (space) character for further processing
        yypushback(yytext().length() - 1);
        if (yystate() != WIKIPRE) {
            beginState(WIKIPRE);
        }
        return yytext();
    }
}
<WIKIPRE> {
    ^{wikipreend} {
        if (logger.isTraceEnabled()) logger.trace("wikipreend: " + yytext() + " (" + yystate() + ")");
        endState();
        // rollback everything to allow processing as non-pre text
        yypushback(yytext().length());
        return "";
    }
    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}

<YYINITIAL> {

    /* ----- redirect ----- */

    ^{redirect} {
        if (logger.isTraceEnabled()) logger.trace("redirect: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_REDIRECT, yytext());
    }

    /* ----- pre ----- */

    {htmlpre} {
        if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- processing commands ----- */

    {noeditsection} {
        if (logger.isTraceEnabled()) logger.trace("noeditsection: " + yytext() + " (" + yystate() + ")");
        this.parserInput.setAllowSectionEdit(false);
        return (this.mode < JFlexParser.MODE_PREPROCESS) ? yytext() : "";
    }

    /* ----- wiki links ----- */

    {wikilink} {
        if (logger.isTraceEnabled()) logger.trace("wikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext());
    }
    {nestedwikilink} {
        if (logger.isTraceEnabled()) logger.trace("nestedwikilink: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_LINK, yytext(), "nested");
    }

    /* ----- image gallery ----- */

    {gallery} {
        if (logger.isTraceEnabled()) logger.trace("gallery: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_GALLERY, yytext());
    }

    /* ----- other ----- */

    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}
