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
nowiki             = (<[ \t]*nowiki[ \t]*>) ~(<[ \t]*\/[ \t]*nowiki[ \t]*>)

/* pre */
htmlpreattributes  = class|dir|id|lang|style|title
htmlpreattribute   = ([ \t]+) {htmlpreattributes} ([ \t]*=[^>\n]+[ \t]*)*
htmlprestart       = (<[ \t]*pre ({htmlpreattribute})* [ \t]* (\/)? [ \t]*>)
htmlpreend         = (<[ \t]*\/[ \t]*pre[ \t]*>)
htmlpre            = ({htmlprestart}) ~({htmlpreend})
wikiprestart       = (" ")+ ([^ \t\n])
wikipreend         = ([^ ]) | ({newline})

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

%state WIKIPRE

%%

/* ----- nowiki ----- */

<YYINITIAL, WIKIPRE>{nowiki} {
    if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- pre ----- */

<YYINITIAL>{htmlpre} {
    if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<YYINITIAL, WIKIPRE>^{wikiprestart} {
    if (logger.isTraceEnabled()) logger.trace("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(yytext().length() - 1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
    }
    return yytext();
}

<WIKIPRE>^{wikipreend} {
    if (logger.isTraceEnabled()) logger.trace("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return yytext();
}

/* ----- processing commands ----- */

<YYINITIAL>{noeditsection} {
    if (logger.isTraceEnabled()) logger.trace("noeditsection: " + yytext() + " (" + yystate() + ")");
    this.parserInput.setAllowSectionEdit(false);
    return (this.mode < JFlexParser.MODE_PREPROCESS) ? yytext() : "";
}

/* ----- wiki links ----- */

<YYINITIAL>{wikilink} {
    if (logger.isTraceEnabled()) logger.trace("wikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

<YYINITIAL>{nestedwikilink} {
    if (logger.isTraceEnabled()) logger.trace("nestedwikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext(), "nested");
}

/* ----- other ----- */

<YYINITIAL, WIKIPRE>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL, WIKIPRE>. {
    // no need to log this
    return yytext();
}
