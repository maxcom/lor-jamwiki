/*
 * The pre-processor performs initial parsing steps used to initialize
 * metadata, replace syntax that should not be saved to the database,
 * and prepare the document for the full parsing by the processor.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiPreProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code called after parsing is completed */
%eofval{
    StringBuilder output = new StringBuilder();
    if (!StringUtils.isBlank(this.templateString)) {
        // FIXME - this leaves unparsed text
        output.append(this.templateString);
        this.templateString = "";
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiPreProcessor.class.getName());
    protected int templateCharCount = 0;
    protected String templateString = "";
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
wikiprestart       = (" ")+ ([^ \t\n])
wikipreend         = ([^ ]) | ({newline})

/* comments */
htmlcomment        = "<!--" ~"-->"

/* wiki links */
wikilink           = "[[" [^\]\n]+ "]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n]+) "]"
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\]\[]* ({wikilink} | {htmllinkwiki}) [^\n\]\[]*)+ "]]"

/* templates */
templatestart      = "{{"
templatestartchar  = "{"
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\n]+ "}}}"
includeonly        = (<[ ]*includeonly[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*includeonly[ ]*>)
noinclude          = (<[ ]*noinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*noinclude[ ]*>)

/* signatures */
wikisignature      = ([~]{3,5})

%state PRE, WIKIPRE, TEMPLATE

%%

/* ----- nowiki ----- */

<YYINITIAL, WIKIPRE, PRE>{nowiki} {
    if (logger.isFinerEnabled()) logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- pre ----- */

<YYINITIAL>{htmlprestart} {
    if (logger.isFinerEnabled()) logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHTML()) {
        beginState(PRE);
    }
    return yytext();
}

<PRE>{htmlpreend} {
    if (logger.isFinerEnabled()) logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML() is true, so no need to check here
    endState();
    return yytext();
}

<YYINITIAL, WIKIPRE>^{wikiprestart} {
    if (logger.isFinerEnabled()) logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(yytext().length() - 1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
    }
    return yytext();
}

<WIKIPRE>^{wikipreend} {
    if (logger.isFinerEnabled()) logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return yytext();
}

/* ----- templates ----- */

<YYINITIAL, TEMPLATE>{templatestart} {
    if (logger.isFinerEnabled()) logger.finer("templatestart: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    if (!allowTemplates()) {
        return yytext();
    }
    this.templateString += raw;
    this.templateCharCount += 2;
    if (yystate() != TEMPLATE) {
        beginState(TEMPLATE);
    }
    return "";
}

<TEMPLATE>{templateendchar} {
    if (logger.isFinerEnabled()) logger.finer("templateendchar: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    this.templateString += raw;
    this.templateCharCount -= raw.length();
    if (this.templateCharCount == 0) {
        endState();
        String value = new String(this.templateString);
        this.templateString = "";
        return this.parse(TAG_TYPE_TEMPLATE, value);
    }
    return "";
}

<TEMPLATE>{templatestartchar} {
    if (logger.isFinerEnabled()) logger.finer("templatestartchar: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    this.templateString += raw;
    this.templateCharCount += raw.length();
    if (this.templateString.equals("{{{")) {
        // param, not a template
        this.templateCharCount = 0;
        endState();
        String value = new String(this.templateString);
        this.templateString = "";
        return value;
    }
    return "";
}

<YYINITIAL>{templateparam} {
    if (logger.isFinerEnabled()) logger.finer("templateparam: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<TEMPLATE>{whitespace} {
    // no need to log this
    this.templateString += yytext();
    return "";
}

<TEMPLATE>. {
    // no need to log this
    this.templateString += yytext();
    return "";
}

<TEMPLATE>{includeonly} {
    if (logger.isFinerEnabled()) logger.finer("includeonly: " + yytext() + " (" + yystate() + ")");
    this.templateString += this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
    return "";
}

<YYINITIAL>{includeonly} {
    if (logger.isFinerEnabled()) logger.finer("includeonly: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
}

<YYINITIAL, TEMPLATE>{noinclude} {
    if (logger.isFinerEnabled()) logger.finer("noinclude: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_NO_INCLUDE, yytext());
}

/* ----- wiki links ----- */

<YYINITIAL>{imagelinkcaption} {
    if (logger.isFinerEnabled()) logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

<YYINITIAL>{wikilink} {
    if (logger.isFinerEnabled()) logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

/* ----- signatures ----- */

<YYINITIAL>{wikisignature} {
    if (logger.isFinerEnabled()) logger.finer("wikisignature: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_SIGNATURE, yytext());
}

/* ----- comments ----- */

<YYINITIAL>{htmlcomment} {
    if (logger.isFinerEnabled()) logger.finer("htmlcomment: " + yytext() + " (" + yystate() + ")");
    if (this.mode < JFlexParser.MODE_PREPROCESS) {
        return yytext();
    }
    // strip out the comment
    return "";
}

/* ----- other ----- */

<YYINITIAL, WIKIPRE, PRE>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL, WIKIPRE, PRE>. {
    // no need to log this
    return yytext();
}
