/*
 * The template processor performs initial parsing steps to replace
 * syntax that should not be saved to the database, processes templates
 * and prepares the document for further processing.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringUtils;
import org.jamwiki.utils.Utilities;

%%

%public
%class JAMWikiTemplateLexer
%extends AbstractJAMTemplateLexer
%type String
%unicode
%ignorecase

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
templatestart      = "{{" (.{2})
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\n]+ "}}}"
includeonly        = (<[ ]*includeonly[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*includeonly[ ]*>)
noinclude          = (<[ ]*noinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*noinclude[ ]*>)
onlyinclude        = (<[ ]*onlyinclude[ ]*[\/]?[ ]*>) ~(<[ ]*\/[ ]*onlyinclude[ ]*>)

/* signatures */
wikisignature      = ([~]{3,5})

%state TEMPLATE

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
}

/* ----- templates ----- */

<YYINITIAL, TEMPLATE> {
    {templatestart} {
        if (logger.isTraceEnabled()) logger.trace("templatestart: " + yytext() + " (" + yystate() + ")");
        // four characters will be matched.  there are multiple possibilities:
        //   * "{{xy" = template start
        //   * "{{{x" = possibly a param start
        //   * "{{{{" = template start + either another template or a param
        String raw = yytext();
        boolean isParam = (!raw.equals("{{{{") && raw.startsWith("{{{"));
        if (isParam) {
            yypushback(3);
            if (yystate() == YYINITIAL) {
                return raw.substring(0, 1);
            } else {
                this.templateString += raw.substring(0, 1);
                return "";
            }
        }
        // push back the two extra characters
        yypushback(2);
        if (!allowTemplates()) {
            return yytext();
        }
        this.templateString += raw.substring(0, 2);
        if (yystate() != TEMPLATE) {
            beginState(TEMPLATE);
        }
        return "";
    }
    {includeonly} {
        if (logger.isTraceEnabled()) logger.trace("includeonly: " + yytext() + " (" + yystate() + ")");
        String parsed = this.parse(TAG_TYPE_INCLUDE_ONLY, yytext());
        if (yystate() == TEMPLATE) {
            this.templateString += parsed;
        }
        return (yystate() == YYINITIAL) ? parsed : "";
    }
    {noinclude} {
        if (logger.isTraceEnabled()) logger.trace("noinclude: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_NO_INCLUDE, yytext());
    }
    {onlyinclude} {
        if (logger.isTraceEnabled()) logger.trace("onlyinclude: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_ONLY_INCLUDE, yytext());
    }
}
<YYINITIAL> {
    {templateparam} {
        if (logger.isTraceEnabled()) logger.trace("templateparam: " + yytext() + " (" + yystate() + ")");
        return yytext();
    }
}
<TEMPLATE> {
    {templateendchar} {
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
    {whitespace} | . {
        // no need to log this
        this.templateString += yytext();
        return "";
    }
}

<YYINITIAL> {

    /* ----- signatures ----- */

    {wikisignature} {
        if (logger.isTraceEnabled()) logger.trace("wikisignature: " + yytext() + " (" + yystate() + ")");
        return this.parse(TAG_TYPE_WIKI_SIGNATURE, yytext());
    }

    /* ----- comments ----- */

    ^{htmlcomment} {newline} {
        if (logger.isTraceEnabled()) logger.trace("htmlcomment + newline: " + yytext() + " (" + yystate() + ")");
        // strip out the comment and newline
        return (this.mode < JFlexParser.MODE_TEMPLATE) ? yytext() : "";
    }

    {htmlcomment} {
        if (logger.isTraceEnabled()) logger.trace("htmlcomment: " + yytext() + " (" + yystate() + ")");
        // strip out the comment
        return (this.mode < JFlexParser.MODE_TEMPLATE) ? yytext() : "";
    }

    /* ----- other ----- */

    {whitespace} | . {
        // no need to log this
        return yytext();
    }
}

<<EOF>> {
    if (logger.isTraceEnabled()) logger.trace("EOF (" + yystate() + ")");
    StringBuilder output = new StringBuilder();
    if (!StringUtils.isBlank(this.templateString)) {
        // FIXME - this leaves unparsed text
        output.append(this.templateString);
        this.templateString = "";
    }
    return (output.length() == 0) ? null : output.toString();
}
