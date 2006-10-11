/*
 * This class implements the MediaWiki syntax (http://meta.wikimedia.org/wiki/Help:Editing).
 * It will also escape any HTML tags that have not been specifically allowed to be
 * present.
 * 
 * Currently supported syntax includes:
 *
 *   Unordered lists: *
 *   Ordered lists: #
 *   Definition lists: ;:
 *   Indents: :
 *   Italics: ''
 *   Bold: '''
 *   h1 heading: =text=
 *   h2 heading: ==text==
 *   h3 level heading: ===text===
 *   h4 level heading: ====text====
 *   Breaking line: ----
 *   Tables: {| |- ! | |}
 *   <nowiki>
 *   __NOTOC__
 *   __TOC__
 *
 * Not yet implemented:
 *
 *   <math>
 *   Templates
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.Environment;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
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
    if (wikibold) {
        wikibold = false;
        output.append("</b>");
    }
    if (wikiitalic) {
        wikiitalic = false;
        output.append( "</i>" );
    }
    // close any open list tags
    if (yystate() == LIST) {
        try {
            WikiListTag wikiListTag = new WikiListTag();
            String value = wikiListTag.parse(this.parserInput, this.parserOutput, this.mode, null);
            output.append(value);
        } catch (Exception e) {
            logger.severe("Unable to close open list", e);
        }
    }
    // close any open tables
    if (yystate() == TD) {
        output.append("</td>");
        endState();
    }
    if (yystate() == TH) {
        output.append("</th>");
        endState();
    }
    if (yystate() == TC) {
        output.append("</caption>");
        endState();
    }
    if (yystate() == TABLE) {
        output.append("</tr></table>");
        endState();
    }
    if (yystate() == PRE || yystate() == WIKIPRE) {
        output.append("</pre>");
        endState();
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiPreProcessor.class.getName());
    protected boolean allowHTML = false;
    protected boolean allowJavascript = false;
    protected boolean wikibold = false;
    protected boolean wikiitalic = false;
    protected int templateCharCount = 0;
    protected String templateString = "";
    
    /**
     *
     */
    protected boolean allowJavascript() {
        return (allowJavascript && yystate() != PRE && yystate() != NOWIKI && yystate() != WIKIPRE);
    }
    
    /**
     *
     */
    protected String closeTable(int currentState) {
        String output = "";
        if (yystate() == TC) output = "</caption>";
        if (yystate() == TH) output = "</th>";
        if (yystate() == TD) output = "</td>";
        if ((yystate() == TC || yystate() == TH || yystate() == TD) && yystate() != currentState) endState();
        return output;
    }
    
    /**
     *
     */
    public void init(ParserInput parserInput, ParserMode mode) throws Exception {
        this.parserInput = parserInput;
        this.mode = mode;
        // validate parser settings
        boolean validated = true;
        if (this.parserInput == null) validated = false;
        if (this.standardMode()) {
            if (this.parserInput.getTableOfContents() == null) validated = false;
            if (this.parserInput.getTopicName() == null) validated = false;
        }
        if (!this.mode.hasMode(ParserMode.MODE_SEARCH)) {
            if (this.parserInput.getContext() == null) validated = false;
            if (this.parserInput.getVirtualWiki() == null) validated = false;
        }
        if (this.mode.hasMode(ParserMode.MODE_SAVE)) {
            if (this.parserInput.getUserIpAddress() == null) validated = false;
        }
        if (!this.standardMode()) {
            endState();
            beginState(PRESAVE);
        }
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
    
    /**
     * Take Wiki text of the form "|" or "| style='foo' |" and convert to
     * and HTML <td> or <th> tag.
     *
     * @param text The text to be parsed.
     * @param tag The HTML tag text, either "td" or "th".
     * @param markup The Wiki markup for the tag, either "|" or "!"
     */
    protected String openTableCell(String text, String tag, char markup) {
        if (text == null) return "";
        text = text.trim();
        int pos = 0;
        while (pos < text.length() && text.charAt(pos) == markup) {
            pos++;
        }
        if (pos >= text.length()) {
            return "<" + tag + ">";
        }
        text = text.substring(pos);
        pos = text.indexOf(markup);
        if (pos != -1) text = text.substring(0, pos);
        String attributes = ParserUtil.validateHtmlTagAttributes(text.trim());
        if (StringUtils.hasText(attributes)) {
            return "<" + tag + " " + attributes + ">";
        }
        return "<" + tag + ">";
    }
    
    /**
     *
     */
    private boolean standardMode() {
        return (!this.mode.hasMode(ParserMode.MODE_SAVE) && !this.mode.hasMode(ParserMode.MODE_SEARCH));
    }
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]
inputcharacter     = [^\r\n]
lessthan           = "<"
greaterthan        = ">"
quotation          = "\""
apostrophe         = "\'"

/* non-container expressions */
hr                 = "----"
wikiheading        = [\=]+ [^\n\=]* [\=]+
bold               = "'''"
italic             = "''"

/* lists */
listitem           = [\*#\:;]+ [^\*#\:;\r\n]
listend            = [^\*#\:;\r\n]+ (.)+

/* nowiki */
nowikistart        = (<[ ]*nowiki[ ]*>)
nowikiend          = (<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ") ([^ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* allowed html */
htmlkeyword        = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var
htmltag            = (<[ ]*[\/]?[ ]*) {htmlkeyword} ([ ]+[^>\/]+)* ([ ]*[\/]?[ ]*>)

/* javascript */
jsopen             = (<[ ]*) script ([ ]+[^>\/]+)* ([ ]*[\/]?[ ]*>)
jsclose            = (<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
notoc              = "__NOTOC__"
toc                = "__TOC__"

/* comments */
htmlcomment        = "<!--" ~"-->"

/* tables */
tablestart         = "{|" {inputcharacter}* {newline}
tableend           = "|}"
/*
FIXME - the tablecell and tablecellsstyle patterns must include "[" to account
for links of the form "[[Topic|Text]]", but in the process this breaks any
style that might (for some reason) include a "["
*/
tablecell          = "|" [^\+\-\}] | "|" [^\+\|\-\}\{\<\r\n] [^\|\r\n\[]+ "|" [^\|]
tablecells         = "||"
tablecellsstyle    = "||" ([^\|\r\n\[]+) "|" ([^|])
tableheading       = "!" | "!" [^\!\|\-\{\<\r\n]+ "|" [^\|]
tableheadings      = "||" | "!!"
tablerow           = "|-" {inputcharacter}* {newline}
tablecaption       = "|+"

/* wiki links */
wikilink           = "[[" [^\]\n\r]+ "]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n\r]+) "]"
htmllinkraw        = ({protocol}) ([^ \n\r\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\r\]\[]* ({wikilink} | {htmllinkwiki}) [^\n\r\]\[]*)+ "]]"

/* templates */
templatestart      = "{{" ([^\{\}]+)
templatestartchar  = "{"
templateendchar    = "}"
includeonlyopen    = (<[ ]*includeonly[ ]*[\/]?[ ]*>)
includeonlyclose   = (<[ ]*\/[ ]*includeonly[ ]*>)
noincludeopen      = (<[ ]*noinclude[ ]*[\/]?[ ]*>)
noincludeclose     = (<[ ]*\/[ ]*noinclude[ ]*>)

/* signatures */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

%state NORMAL, TABLE, TD, TH, TC, LIST, NOWIKI, PRE, JAVASCRIPT, WIKIPRE, PRESAVE, TEMPLATE, NOINCLUDE, INCLUDEONLY

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{nowikistart} {
    logger.finer("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return yytext();
}

<NOWIKI>{nowikiend} {
    logger.finer("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- pre ----- */

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHTML || !standardMode()) {
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

<NORMAL, LIST, TABLE, TD, TH, TC, WIKIPRE, PRESAVE>^{wikiprestart} {
    logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
        return (standardMode()) ? "<pre>" : yytext();
    }
    return (standardMode()) ? "" : yytext();
}

<WIKIPRE>^{wikipreend} {
    logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return  (standardMode()) ? "</pre>\n" : yytext();
}

/* ----- processing commands ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{notoc} {
    logger.finer("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<NORMAL, LIST, TABLE, TD, TH, TC>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    return yytext();
}

/* ----- templates ----- */

<NORMAL, LIST, TABLE, TD, TH, TC, TEMPLATE>{templatestart} {
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
            value = templateTag.parse(this.parserInput, this.parserOutput, this.mode, value);
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

<NORMAL, LIST, TABLE, TD, TH, TC, TEMPLATE>{includeonlyopen} {
    logger.finer("includeonlyopen: " + yytext() + " (" + yystate() + ")");
    if (!this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        yybegin(INCLUDEONLY);
    }
    return "";
}

<NORMAL, LIST, TABLE, TD, TH, TC, TEMPLATE, INCLUDEONLY>{includeonlyclose} {
    logger.finer("includeonlyclose: " + yytext() + " (" + yystate() + ")");
    if (!this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        endState();
    }
    return "";
}

<NORMAL, LIST, TABLE, TD, TH, TC, TEMPLATE>{noincludeopen} {
    logger.finer("noincludeopen: " + yytext() + " (" + yystate() + ")");
    if (this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        yybegin(NOINCLUDE);
    }
    return "";
}

<NORMAL, LIST, TABLE, TD, TH, TC, TEMPLATE, NOINCLUDE>{noincludeclose} {
    logger.finer("noincludeclose: " + yytext() + " (" + yystate() + ")");
    if (this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
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

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{imagelinkcaption} {
    logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{wikilink} {
    logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC>{htmllink} {
    logger.finer("htmllink: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        HtmlLinkTag htmlLinkTag = new HtmlLinkTag();
        String value = htmlLinkTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- signatures ----- */

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{wikisig3} {
    logger.finer("wikisig3: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{wikisig4} {
    logger.finer("wikisig4: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC, PRESAVE>{wikisig5} {
    logger.finer("wikisig5: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiSignatureTag wikiSignatureTag = new WikiSignatureTag();
        String value = wikiSignatureTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- tables ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>^{tablestart} {
    logger.finer("tablestart: " + yytext() + " (" + yystate() + ")");
    beginState(TABLE);
    String attributes = yytext().substring(2).trim();
    attributes = ParserUtil.validateHtmlTagAttributes(attributes);
    return ((StringUtils.hasText(attributes)) ? "<table " + attributes + ">" : "<table>");
}

<TABLE, TD, TH, TC>^{tablecaption} {
    logger.finer("tablecaption: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    output.append(closeTable(TC));
    beginState(TC);
    output.append("<caption>");
    return output.toString();
}

<TABLE, TD, TH, TC>^{tableheading} {
    logger.finer("tableheading: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    // if a column was already open, close it
    output.append(closeTable(TH));
    if (yystate() != TH) beginState(TH);
    if (yytext().trim().length() > 1) {
        int start = 1;
        int end = yytext().indexOf("|", start+1);
        String attributes = yytext().substring(start, end).trim();
        attributes = ParserUtil.validateHtmlTagAttributes(attributes);
        String tag = "<th>";
        if (StringUtils.hasText(attributes)) {
            tag = "<th " + attributes + ">";
        }
        output.append(tag);
        // extra character matched by regular expression so push it back
        yypushback(1);
    } else {
        output.append("<th>");
    }
    return output.toString();
}

<TH>{tableheadings} {
    logger.finer("tableheadings: " + yytext() + " (" + yystate() + ")");
    return "</th><th>";
}

<TABLE, TD, TH, TC>^{tablecell} {
    logger.finer("tablecell: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    // if a column was already open, close it
    output.append(closeTable(TD));
    if (yystate() != TD) beginState(TD);
    // extra character matched by both regular expressions so push it back
    yypushback(1);
    output.append(openTableCell(yytext(), "td", '|'));
    return output.toString();
}

<TD>{tablecells} {
    logger.finer("tablecells: " + yytext() + " (" + yystate() + ")");
    return "</td><td>";
}

<TD>{tablecellsstyle} {
    logger.finer("tablecellsstyle: " + yytext() + " (" + yystate() + ")");
    // one extra character matched by the pattern, so roll it back
    yypushback(1);
    return "</td>" + openTableCell(yytext(), "td", '|');
}

<TABLE, TD, TH, TC>^{tablerow} {
    logger.finer("tablerow: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    // if a column was already open, close it
    int oldState = yystate();
    output.append(closeTable(TABLE));
    if (oldState != TABLE) output.append("</tr>");
    if (yytext().trim().length() > 2) {
        String attributes = yytext().substring(2).trim();
        attributes = ParserUtil.validateHtmlTagAttributes(attributes);
        String tag = "<tr>";
        if (StringUtils.hasText(attributes)) {
            tag = "<tr " + attributes + ">";
        }
        output.append(tag);
    } else {
        output.append("<tr>");
    }
    return output.toString();
}

<TABLE, TD, TH, TC>^{tableend} {
    logger.finer("tableend: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    // if a column was already open, close it
    output.append(closeTable(TABLE));
    // end TABLE state
    endState();
    output.append("</tr></table>\n");
    return output.toString();
}

/* ----- comments ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{htmlcomment} {
    logger.finer("htmlcomment: " + yytext() + " (" + yystate() + ")");
    // remove comment
    return "";
}

/* ----- headings ----- */

<NORMAL>^{hr} {
    logger.finer("hr: " + yytext() + " (" + yystate() + ")");
    return "<hr />\n";
}

<NORMAL>^{wikiheading} {
    logger.finer("wikiheading: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiHeadingTag wikiHeadingTag = new WikiHeadingTag();
        String value = wikiHeadingTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- lists ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>^{listitem} {
    logger.finer("listitem: " + yytext() + " (" + yystate() + ")");
    if (yystate() != LIST) beginState(LIST);
    // one non-list character matched, roll it back
    yypushback(1);
    String raw = yytext();
    try {
        WikiListTag wikiListTag = new WikiListTag();
        String value = wikiListTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return raw;
    }
}

<LIST>^{listend} {
    logger.finer("listend: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    // roll back any matches to allow re-parsing
    yypushback(raw.length());
    endState();
    try {
        WikiListTag wikiListTag = new WikiListTag();
        // close open list tags
        String value = wikiListTag.parse(this.parserInput, this.parserOutput, this.mode, null);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        return "";
    }
}

/* ----- bold / italic ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{bold} {
    logger.finer("bold: " + yytext() + " (" + yystate() + ")");
    wikibold = !wikibold;
    return (wikibold) ? "<b>" : "</b>";
}

<NORMAL, LIST, TABLE, TD, TH, TC>{italic} {
    logger.finer("italic: " + yytext() + " (" + yystate() + ")");
    wikiitalic = !wikiitalic;
    return (wikiitalic) ? "<i>" : "</i>";
}

/* ----- html ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{htmltag} {
    logger.finer("htmltag: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        HtmlTag htmlTag = new HtmlTag();
        String value = htmlTag.parse(this.parserInput, this.parserOutput, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.severe("Unable to parse " + raw, e);
        // FIXME - what should be returned? escaped html?
        return "";
    }
}

/* ----- javascript ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{jsopen} {
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

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC>{lessthan} {
    logger.finer("lessthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&lt;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC>{greaterthan} {
    logger.finer("greaterthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&gt;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC>{quotation} {
    logger.finer("quotation: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&quot;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC>{apostrophe} {
    logger.finer("apostrophe: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&#39;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT, PRESAVE>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT, PRESAVE>. {
    // no need to log this
    return yytext();
}
