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

/* comments */
htmlcomment        = "<!--" ~"-->"

/* templates */
templatestart      = "{{" [^\{]
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\n]+ "}}}"
includeonly        = (<[ ]*includeonly[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*includeonly[ ]*>)
noinclude          = (<[ ]*noinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*noinclude[ ]*>)
onlyinclude        = (<[ ]*onlyinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*onlyinclude[ ]*>)

/* signatures */
wikisignature      = ([~]{3,5})

%state TEMPLATE

%%

/* ----- nowiki ----- */

<YYINITIAL>{nowiki} {
    if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- pre ----- */

<YYINITIAL>{htmlpre} {
    if (logger.isTraceEnabled()) logger.trace("htmlpre: " + yytext() + " (" + yystate() + ")");
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
        String result = this.parse(TAG_TYPE_TEMPLATE, this.templateString);
        this.templateString = "";
        return result;
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

<YYINITIAL, TEMPLATE>{includeonly} {
    if (logger.isTraceEnabled()) logger.trace("includeonly: " + yytext() + " (" + yystate() + ")");
    String parsed = this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
    if (yystate() == TEMPLATE) {
        this.templateString += parsed;
    }
    return (yystate() == YYINITIAL) ? parsed : "";
}

<YYINITIAL, TEMPLATE>{noinclude} {
    if (logger.isTraceEnabled()) logger.trace("noinclude: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_NO_INCLUDE, yytext());
}

<YYINITIAL, TEMPLATE>{onlyinclude} {
    if (logger.isTraceEnabled()) logger.trace("onlyinclude: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_ONLY_INCLUDE, yytext());
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

<YYINITIAL>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL>. {
    // no need to log this
    return yytext();
}
