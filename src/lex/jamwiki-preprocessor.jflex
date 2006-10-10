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

import java.util.Hashtable;
import java.util.Stack;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserMode;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
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
    output.append(closeList());
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
    protected Stack listOpenStack = new Stack();
    protected Stack listCloseStack = new Stack();
    protected static Hashtable listOpenHash = new Hashtable();
    protected static Hashtable listCloseHash = new Hashtable();
    protected static Hashtable listItemOpenHash = new Hashtable();

    static {
        listOpenHash.put("*", "<ul>");
        listOpenHash.put("#", "<ol>");
        listOpenHash.put(":", "<dl>");
        listOpenHash.put(";", "<dl>");
        listItemOpenHash.put("*", "<li>");
        listItemOpenHash.put("#", "<li>");
        listItemOpenHash.put(":", "<dd>");
        listItemOpenHash.put(";", "<dt>");
        listCloseHash.put("<ul>", "</ul>");
        listCloseHash.put("<ol>", "</ol>");
        listCloseHash.put("<dl>", "</dl>");
        listCloseHash.put("<li>", "</li>");
        listCloseHash.put("<dd>", "</dd>");
        listCloseHash.put("<dt>", "</dt>");
    }
    
    /**
     *
     */
    protected boolean allowHTML() {
        return (allowHTML && yystate() != PRE && yystate() != NOWIKI && yystate() != WIKIPRE);
    }
    
    /**
     *
     */
    protected boolean allowJavascript() {
        return (allowJavascript && yystate() != PRE && yystate() != NOWIKI && yystate() != WIKIPRE);
    }
    
    /**
     *
     */
    protected String closeList() {
        StringBuffer output = new StringBuffer();
        while (listOpenStack.size() > 0) {
            listOpenStack.pop();
            output.append(listCloseStack.pop());
        }
        return output.toString();
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
     *
     */
    private static boolean isListTag(char character) {
        String value = character + "";
        return (listOpenHash.get(value) != null);
    }
    
    /**
     *
     */
    protected String listItem(String text) {
        int count = 0;
        for (int i=0; i < text.length(); i++) {
            if (!isListTag(text.charAt(i))) break;
            count++;
        }
        // trim all but the list tags
        text = text.substring(0, count);
        StringBuffer output = new StringBuffer();
        // build a stack of html tags based on current values passed to lexer
        Stack currentOpenStack = new Stack();
        for (int i=0; i < text.length(); i++) {
            String tag = "" + text.charAt(i);
            String listOpenTag = (String)listOpenHash.get(tag);
            String listItemOpenTag = (String)listItemOpenHash.get(tag);
            if (listOpenTag == null || listItemOpenTag == null) {
                logger.severe("Unknown list tag " + tag);
                continue;
            }
            currentOpenStack.push(listOpenTag);
            currentOpenStack.push(listItemOpenTag);
        }
        // if list was previously open to a greater depth, close the old list
        while (listOpenStack.size() > currentOpenStack.size()) {
            listOpenStack.pop();
            output.append(listCloseStack.pop());
        }
        // if continuing the same list, process normally
        if (currentOpenStack.equals(listOpenStack)) {
            // get last tag in current stack
            String currentOpenTag = (String)currentOpenStack.elementAt(currentOpenStack.size() - 1);
            String listOpenTag = (String)listOpenStack.elementAt(listOpenStack.size() - 1);
            String closeTag = (String)listCloseHash.get(listOpenTag);
            output.append(closeTag);
            output.append(currentOpenTag);
            return output.toString();
        }
        // look for differences in the old list stack and the new list stack
        int pos = 0;
        while (pos < listOpenStack.size()) {
            if (!listOpenStack.elementAt(pos).equals(currentOpenStack.elementAt(pos))) {
                break;
            }
            pos++;
        }
        // if any differences found process them
        while (listOpenStack.size() > pos) {
            listOpenStack.pop();
            output.append(listCloseStack.pop());
        }
        // continue processing differences
        for (int i=pos; i < currentOpenStack.size(); i++) {
            String currentOpenTag = (String)currentOpenStack.elementAt(i);
            String currentCloseTag = (String)listCloseHash.get(currentOpenTag);
            listOpenStack.push(currentOpenTag);
            listCloseStack.push(currentCloseTag);
            output.append(currentOpenTag);
        }
        return output.toString();
    }
    
    /**
     *
     */
    protected int listTagCount(String text) {
        int count = 0;
        for (int i=0; i < text.length(); i++) {
            if (isListTag(text.charAt(i))) {
                count++;
            } else {
                break;
            }
        }
        return count;
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
htmltag            = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var

/* non-container expressions */
hr                 = "----"
wikiheading        = [\=]+ [^\n\=]* [\=]+
bold               = "'''"
italic             = "''"

/* lists */
/*
  the approach is to match the entire list item, change to a list state,
  parse out the lists tags, and then re-parse the remaining content.
*/
listitem           = [\*#\:;]+ [^\n]* [\n]
listend            = [^\*#\:;\r\n] [^\n]* [\n]

/* nowiki */
nowikistart        = (<[ ]*nowiki[ ]*>)
nowikiend          = (<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ") ([^ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* allowed html */
htmltagopen        = (<[ ]*) {htmltag} ([ ]*[\/]?[ ]*>)
htmltagclose       = (<[ ]*\/[ ]*) {htmltag} ([ ]*>)
htmltagattributes  = (<[ ]*) {htmltag} ([ ]+[^>\/]+[\/]?[ ]*>)

/* javascript */
jsopen             = (<[ ]*script[ ]*[\/]?[ ]*>)
jsclose            = (<[ ]*\/[ ]*script[ ]*>)
jsattributes       = (<[ ]*script[ ]+[^>\/]+[\/]?[ ]*>)

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
includeonlyopen    = (<[ ]*includeonly[ ]*[\/]?[ ]*>)
includeonlyclose   = (<[ ]*\/[ ]*includeonly[ ]*>)
noincludeopen      = (<[ ]*noinclude[ ]*[\/]?[ ]*>)
noincludeclose     = (<[ ]*\/[ ]*noinclude[ ]*>)

/* wiki links */
wikilink           = "[[" [^\]\n\r]+ "]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllink           = "[" ({protocol}) ([^\]\n\r]+) "]"
htmllinkraw        = ({protocol})  ([^ \n\r\t]+)
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\r\]\[]* ({wikilink} | {htmllink}) [^\n\r\]\[]*)+ "]]"

/* templates */
templatestart      = "{{" ([^\{\}]+)
templatestartchar  = "{"
templateendchar    = "}"

/* signatures */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

%state NORMAL, TABLE, TD, TH, TC, LIST, NOWIKI, PRE, JAVASCRIPT, WIKIPRE, PRESAVE, TEMPLATE, NOINCLUDE, INCLUDEONLY

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{nowikistart} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{htmlprestart} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, WIKIPRE, PRESAVE>^{wikiprestart} {
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

<NORMAL, TABLE, TD, TH, TC, LIST>{notoc} {
    logger.finer("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    return yytext();
}

/* ----- templates ----- */

<NORMAL, TABLE, TD, TH, TC, LIST, TEMPLATE>{templatestart} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, TEMPLATE>{includeonlyopen} {
    logger.finer("includeonlyopen: " + yytext() + " (" + yystate() + ")");
    if (!this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        yybegin(INCLUDEONLY);
    }
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST, TEMPLATE, INCLUDEONLY>{includeonlyclose} {
    logger.finer("includeonlyclose: " + yytext() + " (" + yystate() + ")");
    if (!this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        endState();
    }
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST, TEMPLATE>{noincludeopen} {
    logger.finer("noincludeopen: " + yytext() + " (" + yystate() + ")");
    if (this.mode.hasMode(ParserMode.MODE_TEMPLATE)) {
        yybegin(NOINCLUDE);
    }
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST, TEMPLATE, NOINCLUDE>{noincludeclose} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{imagelinkcaption} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{wikilink} {
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

<NORMAL, TABLE, TD, TH, TC, LIST>{htmllink} {
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

<NORMAL, TABLE, TD, TH, TC, LIST>{htmllinkraw} {
    logger.finer("htmllinkraw: " + yytext() + " (" + yystate() + ")");
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{wikisig3} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{wikisig4} {
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

<NORMAL, TABLE, TD, TH, TC, LIST, PRESAVE>{wikisig5} {
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

<NORMAL, TABLE, TD, TH, TC>^{tablestart} {
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

<NORMAL, TABLE, TD, TH, TC>{htmlcomment} {
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

<NORMAL, TABLE, TD, TH, TC>^{listitem} {
    logger.finer("start of list: " + yytext() + " (" + yystate() + ")");
    // switch to list processing mode
    beginState(LIST);
    // now that state is list, push back and re-process this line
    yypushback(yylength());
    return "";
}

<LIST>^{listitem} {
    logger.finer("list item: " + yytext() + " (" + yystate() + ")");
    // process list item content (without the list markup)
    String output = listItem(yytext());
    yypushback(yylength() - listTagCount(yytext()));
    return output;
}

<LIST>^{listend} {
    logger.finer("end of list: " + yytext() + " (" + yystate() + ")");
    // end of list, switch back to normal processing mode
    endState();
    yypushback(yylength());
    return closeList();
}

/* ----- bold / italic ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{bold} {
    logger.finer("bold: " + yytext() + " (" + yystate() + ")");
    wikibold = !wikibold;
    return (wikibold) ? "<b>" : "</b>";
}

<NORMAL, TABLE, TD, TH, TC, LIST>{italic} {
    logger.finer("italic: " + yytext() + " (" + yystate() + ")");
    wikiitalic = !wikiitalic;
    return (wikiitalic) ? "<i>" : "</i>";
}

/* ----- html ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagopen} {
    logger.finer("htmltagopen: " + yytext() + " (" + yystate() + ")");
    return (allowHTML()) ? ParserUtil.sanitizeHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagclose} {
    logger.finer("htmltagclose: " + yytext() + " (" + yystate() + ")");
    return (allowHTML()) ? ParserUtil.sanitizeHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagattributes} {
    logger.finer("htmltagattributes: " + yytext() + " (" + yystate() + ")");
    return (allowHTML()) ? ParserUtil.validateHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

/* ----- javascript ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{jsopen} {
    logger.finer("jsopen: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{jsattributes} {
    logger.finer("jsattributes: " + yytext() + " (" + yystate() + ")");
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

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{lessthan} {
    logger.finer("lessthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&lt;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{greaterthan} {
    logger.finer("greaterthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&gt;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{quotation} {
    logger.finer("quotation: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&quot;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{apostrophe} {
    logger.finer("apostrophe: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return (standardMode()) ? "&#39;" : yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT, PRESAVE>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT, PRESAVE>. {
    // no need to log this
    return yytext();
}
