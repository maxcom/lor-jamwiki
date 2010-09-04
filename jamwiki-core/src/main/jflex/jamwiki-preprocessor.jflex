/*
 * The pre-processor performs initial parsing steps used to initialize
 * metadata, replace syntax that should not be saved to the database,
 * and prepare the document for the full parsing by the processor.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.Utilities;
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
htmlpre            = ({htmlprestart}) ~({htmlpreend})
wikiprestart       = (" ")+ ([^ \t\n])
wikipreend         = ([^ ]) | ({newline})

/* comments */
htmlcomment        = "<!--" ~"-->"

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

/* templates */
templatestart      = "{{" [^\{]
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\n]+ "}}}"
includeonly        = (<[ ]*includeonly[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*includeonly[ ]*>)
noinclude          = (<[ ]*noinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*noinclude[ ]*>)

/* signatures */
wikisignature      = ([~]{3,5})

%state WIKIPRE, TEMPLATE

%%

/* ----- nowiki ----- */

<YYINITIAL, WIKIPRE>{nowiki} {
    if (logger.isFinerEnabled()) logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- pre ----- */

<YYINITIAL>{htmlpre} {
    if (logger.isFinerEnabled()) logger.finer("htmlpre: " + yytext() + " (" + yystate() + ")");
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
    // push back the one non-template character that matched
    yypushback(1);
    String raw = yytext();
    if (!allowTemplates()) {
        return yytext();
    }
    this.templateString += raw;
    if (yystate() != TEMPLATE) {
        beginState(TEMPLATE);
    }
    return "";
}

<TEMPLATE>{templateendchar} {
    if (logger.isFinerEnabled()) logger.finer("templateendchar: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    this.templateString += raw;
    if (Utilities.findMatchingEndTag(this.templateString, 0, "{", "}") != -1) {
        endState();
        String value = this.templateString;
        this.templateString = "";
        return this.parse(TAG_TYPE_TEMPLATE, value);
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

/* ----- processing commands ----- */

<YYINITIAL>{noeditsection} {
    if (logger.isFinerEnabled()) logger.finer("noeditsection: " + yytext() + " (" + yystate() + ")");
    this.parserInput.setAllowSectionEdit(false);
    return (this.mode < JFlexParser.MODE_PREPROCESS) ? yytext() : "";
}

/* ----- wiki links ----- */

<YYINITIAL>{wikilink} {
    if (logger.isFinerEnabled()) logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

<YYINITIAL>{nestedwikilink} {
    if (logger.isFinerEnabled()) logger.finer("nestedwikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext(), "nested");
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

<YYINITIAL, WIKIPRE>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL, WIKIPRE>. {
    // no need to log this
    return yytext();
}
