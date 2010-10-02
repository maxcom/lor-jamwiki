/*
 * The template processor performs initial parsing steps to replace
 * syntax that should not be saved to the database, processes templates
 * and prepares the document for further processing.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiTemplateProcessor
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
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiTemplateProcessor.class.getName());
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

/* ----- templates ----- */

<YYINITIAL, TEMPLATE>{templatestart} {
    if (logger.isTraceEnabled()) logger.trace("templatestart: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isTraceEnabled()) logger.trace("templateendchar: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isTraceEnabled()) logger.trace("templateparam: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isTraceEnabled()) logger.trace("includeonly: " + yytext() + " (" + yystate() + ")");
    this.templateString += this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
    return "";
}

<YYINITIAL>{includeonly} {
    if (logger.isTraceEnabled()) logger.trace("includeonly: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
}

<YYINITIAL, TEMPLATE>{noinclude} {
    if (logger.isTraceEnabled()) logger.trace("noinclude: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_NO_INCLUDE, yytext());
}

/* ----- signatures ----- */

<YYINITIAL>{wikisignature} {
    if (logger.isTraceEnabled()) logger.trace("wikisignature: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_SIGNATURE, yytext());
}

/* ----- comments ----- */

<YYINITIAL>{htmlcomment} {
    if (logger.isTraceEnabled()) logger.trace("htmlcomment: " + yytext() + " (" + yystate() + ")");
    if (this.mode < JFlexParser.MODE_TEMPLATE) {
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
