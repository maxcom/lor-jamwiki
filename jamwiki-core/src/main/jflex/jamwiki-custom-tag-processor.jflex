/*
 * This processor runs after the template processor and before the pre-processor
 * and converts XML-like custom tags to standard wiki syntax.  For example, if
 * a custom "bold" tag were to be created with syntax of the form
 * "<bold>text</bold>", then this processor would initialize an instance of the
 * "bold" tag processor, pass it the tags attributes and content, and most likely
 * return an output of the form "'''bold'''".
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiCustomTagProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiCustomTagProcessor.class.getName());
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

/* comments */
htmlcomment        = "<!--" ~"-->"

/* image gallery */
gallery            = (<[ ]*gallery[^>]*>) ~(<[ ]*\/[ ]*gallery[ ]*>)

%%

<YYINITIAL> {

    /* ----- nowiki ----- */

    {nowiki} {
        if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- pre ----- */

    {htmlpre} {
        if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }

    /* ----- comments ----- */

    {htmlcomment} {
        if (logger.isTraceEnabled()) logger.trace("htmlcomment: " + yytext() + " (" + yystate() + ")");
        if (this.mode < JFlexParser.MODE_TEMPLATE) {
            return yytext();
        }
        // strip out the comment
        return "";
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
