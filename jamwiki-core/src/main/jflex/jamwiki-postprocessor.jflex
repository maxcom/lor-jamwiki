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

/* code included in the constructor */
%init{
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiPostProcessor.class.getName());
%}

/* character expressions */
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
toc                = "__TOC__"

/* references */
references         = (<[ ]*) "references" ([ ]*[\/]?[ ]*>)

%state PRE, NORMAL

%%

/* ----- nowiki ----- */

<PRE, NORMAL>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    return JFlexParserUtil.tagContent(yytext());
}

/* ----- pre ----- */

<NORMAL>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<NORMAL>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInput.getTableOfContents().attemptTOCInsertion();
}

/* ----- references ----- */

<NORMAL>{references} {
    logger.finer("references: " + yytext() + " (" + yystate() + ")");
    WikiReferencesTag parserTag = new WikiReferencesTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

/* ----- javascript ----- */

<NORMAL>{javascript} {
    logger.finer("javascript: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- other ----- */

<PRE, NORMAL>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NORMAL>. {
    // no need to log this
    return yytext();
}
