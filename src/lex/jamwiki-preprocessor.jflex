/*
 *
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.Environment;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

%%

%public
%class JAMWikiPreProcessor
%extends AbstractLexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    allowHTML = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
    allowJavascript = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    if (StringUtils.hasText(this.templateString)) {
        // FIXME - this leaves unparsed text
        output.append(this.templateString);
        this.templateString = "";
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiPreProcessor.class.getName());
    protected boolean allowHTML = false;
    protected boolean allowJavascript = false;
    protected int templateCharCount = 0;
    protected String templateString = "";
    
    /**
     *
     */
    protected boolean allowJavascript() {
        return (allowJavascript && yystate() != PRE && yystate() != WIKIPRE);
    }
    
    /**
     *
     */
    public void init(ParserInput parserInput, ParserDocument parserDocument, int mode) throws Exception {
        this.parserInput = parserInput;
        this.parserDocument = parserDocument;
        this.mode = mode;
        boolean validated = true;
        // validate parser settings
        if (this.mode > JFlexParser.MODE_PREPROCESS) validated = false;
        if (this.mode == JFlexParser.MODE_SAVE) {
            if (this.parserInput.getUserIpAddress() == null) validated = false;
        }
        if (this.mode >= JFlexParser.MODE_TEMPLATE) {
            if (this.parserInput.getVirtualWiki() == null) validated = false;
        }
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ") ([^ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* javascript */
jsopen             = (<[ ]*) script ([ ]+[^>\/]+)* ([ ]*[\/]?[ ]*>)
jsclose            = (<[ ]*\/[ ]*script[ ]*>)

/* comments */
htmlcomment        = "<!--" ~"-->"

/* wiki links */
wikilink           = "[[" [^\]\n\r]+ "]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n\r]+) "]"
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\r\]\[]* ({wikilink} | {htmllinkwiki}) [^\n\r\]\[]*)+ "]]"

/* templates */
templatestart      = "{{" ([^\{\}]+)
templatestartchar  = "{"
templateendchar    = "}"
templateparam      = "{{{" [^\{\}\r\n]+ "}}}"
includeonlyopen    = (<[ ]*includeonly[ ]*[\/]?[ ]*>)
includeonlyclose   = (<[ ]*\/[ ]*includeonly[ ]*>)
noincludeopen      = (<[ ]*noinclude[ ]*[\/]?[ ]*>)
noincludeclose     = (<[ ]*\/[ ]*noinclude[ ]*>)

/* signatures */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

%state NORMAL, PRE, JAVASCRIPT, WIKIPRE, TEMPLATE, NOINCLUDE, INCLUDEONLY

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiNowikiTag wikiNowikiTag = new WikiNowikiTag();
        String value = wikiNowikiTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- pre ----- */

<NORMAL>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHTML) {
        beginState(PRE);
        return yytext();
    }
    return "&lt;pre&gt;";
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return yytext();
}

<NORMAL, WIKIPRE>^{wikiprestart} {
    logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
        return yytext();
    }
    return yytext();
}

<WIKIPRE>^{wikipreend} {
    logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return yytext();
}

/* ----- templates ----- */

<NORMAL, TEMPLATE>{templatestart} {
    logger.finer("templatestart: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_TEMPLATES)) {
        yypushback(raw.length() - 2);
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
    logger.finer("templateendchar: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    this.templateString += raw;
    this.templateCharCount -= raw.length();
    if (this.templateCharCount == 0) {
        endState();
        String value = new String(this.templateString);
        this.templateString = "";
        try {
            TemplateTag templateTag = new TemplateTag();
            value = templateTag.parse(this.parserInput, this.parserDocument, this.mode, value);
            return value;
        } catch (Exception e) {
            logger.severe("Unable to parse " + this.templateString, e);
            this.templateString = "";
            return value;
        }
    }
    return "";
}

<TEMPLATE>{templatestartchar} {
    logger.finer("templatestartchar: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    this.templateString += raw;
    this.templateCharCount += raw.length();
    return "";
}

<NORMAL>{templateparam} {
    logger.finer("templateparam: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    return raw;
}

<TEMPLATE>{whitespace} {
    // no need to log this
    String raw = yytext();
    this.templateString += raw;
    return "";
}

<TEMPLATE>. {
    // no need to log this
    String raw = yytext();
    this.templateString += raw;
    return "";
}

<NORMAL, TEMPLATE>{includeonlyopen} {
    logger.finer("includeonlyopen: " + yytext() + " (" + yystate() + ")");
    if (this.mode != JFlexParser.MODE_TEMPLATE) {
        yybegin(INCLUDEONLY);
    }
    return "";
}

<NORMAL, TEMPLATE, INCLUDEONLY>{includeonlyclose} {
    logger.finer("includeonlyclose: " + yytext() + " (" + yystate() + ")");
    if (this.mode != JFlexParser.MODE_TEMPLATE) {
        endState();
    }
    return "";
}

<NORMAL, TEMPLATE>{noincludeopen} {
    logger.finer("noincludeopen: " + yytext() + " (" + yystate() + ")");
    if (this.mode == JFlexParser.MODE_TEMPLATE) {
        yybegin(NOINCLUDE);
    }
    return "";
}

<NORMAL, TEMPLATE, NOINCLUDE>{noincludeclose} {
    logger.finer("noincludeclose: " + yytext() + " (" + yystate() + ")");
    if (this.mode == JFlexParser.MODE_TEMPLATE) {
        endState();
    }
    return "";
}

<INCLUDEONLY, NOINCLUDE>{whitespace} {
    // no need to log this
    return "";
}

<INCLUDEONLY, NOINCLUDE>. {
    // no need to log this
    return "";
}

/* ----- wiki links ----- */

<NORMAL>{imagelinkcaption} {
    logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL>{wikilink} {
    logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- signatures ----- */

<NORMAL>{wikisig3} {
    logger.finer("wikisig3: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL>{wikisig4} {
    logger.finer("wikisig4: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL>{wikisig5} {
    logger.finer("wikisig5: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- comments ----- */

<NORMAL>{htmlcomment} {
    logger.finer("htmlcomment: " + yytext() + " (" + yystate() + ")");
    // remove comment
    return "";
}

/* ----- javascript ----- */

<NORMAL>{jsopen} {
    logger.finer("jsopen: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

<JAVASCRIPT>{jsclose} {
    logger.finer("jsclose: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        endState();
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

/* ----- other ----- */

<WIKIPRE, PRE, NORMAL, JAVASCRIPT>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NORMAL, JAVASCRIPT>. {
    // no need to log this
    return yytext();
}
