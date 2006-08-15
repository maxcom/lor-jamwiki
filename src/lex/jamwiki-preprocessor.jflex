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
package org.jamwiki.parser;

import java.util.Hashtable;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
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
    allowHtml = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
    allowJavascript = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT);
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
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
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(JAMWikiPreProcessor.class.getName());
    protected boolean allowHtml = false;
    protected boolean allowJavascript = false;
    protected boolean wikibold = false;
    protected boolean wikih1 = false;
    protected boolean wikih2 = false;
    protected boolean wikih3 = false;
    protected boolean wikih4 = false;
    protected boolean wikih5 = false;
    protected boolean wikiitalic = false;
    protected int nextSection = 0;
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
    protected boolean allowHtml() {
        return (allowHtml && yystate() != PRE && yystate() != NOWIKI && yystate() != WIKIPRE);
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
    private static boolean isListTag(char value) {
        if (value == '*') {
            return true;
        }
        if (value == '#') {
            return true;
        }
        if (value == ':') {
            return true;
        }
        if (value == ';') {
            return true;
        }
        return false;
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
                logger.error("Unknown list tag " + tag);
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
     *
     */
    protected int nextSection() {
    	this.nextSection++;
    	return this.nextSection;
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
    protected String updateToc(String name, String text, int level) {
        String output = "";
        if (this.parserInfo.getTableOfContents().getStatus() == TableOfContents.STATUS_TOC_UNINITIALIZED) {
            output = "__TOC__";
        }
        this.parserInfo.getTableOfContents().addEntry(name, text, level);
        return output;
    }
    
    /**
     *
     */
    public void setParserInfo(ParserInfo parserInfo) throws Exception {
        this.parserInfo = parserInfo;
        // validate parser settings
        boolean validated = true;
        if (this.parserInfo == null) validated = false;
        if (this.parserInfo.getTableOfContents() == null) validated = false;
        if (this.parserInfo.getContext() == null) validated = false;
        if (this.parserInfo.getVirtualWiki() == null) validated = false;
        if (this.parserInfo.getTopicName() == null) validated = false;
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]
inputcharacter     = [^\r\n]
lessthan           = "<"
greaterthan        = ">"
quotation          = "\""
htmltag            = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var

/* non-container expressions */
hr                 = "----"
h1                 = "=" [^=\n]+ ~"="
h1close            = "="
h2                 = "==" [^=\n]+ ~"=="
h2close            = "=="
h3                 = "===" [^=\n]+ ~"==="
h3close            = "==="
h4                 = "====" [^=\n]+ ~"===="
h4close            = "===="
h5                 = "=====" [^=\n]+ ~"====="
h5close            = "====="
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
tablecell          = "|" [^\+\-\}] | "|" [^\+\|\-\}\{\<\r\n] [^\|\r\n(\[\[)]+ "|" [^\|]
tablecells         = "||"
tablecellsstyle    = "||" ([^\|\n]+) "|" ([^|])
tableheading       = "!" | "!" [^\!\|\-\{\<\r\n]+ "|" [^\|]
tableheadings      = "||" | "!!"
tablerow           = "|-" {inputcharacter}* {newline}
tablecaption       = "|+"

/* wiki links */
wikilink           = "[[" [^(\]\])\n\r]+ ~"]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllink           = "[" ({protocol}) ([^\]\n\r]+) ~"]"
htmllinkraw        = ({protocol})  ([^ \n\r\t]+)

%state NORMAL, TABLE, TD, TH, TC, LIST, NOWIKI, PRE, JAVASCRIPT, WIKIPRE

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, TABLE, TD, TH, TC, LIST>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return yytext();
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- pre ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHtml) {
        beginState(PRE);
        return yytext();
    }
    return "&lt;pre&gt;";
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return yytext();
}

<NORMAL, TABLE, TD, TH, TC, LIST, WIKIPRE>^{wikiprestart} {
    logger.debug("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
        return "<pre>";
    }
    return "";
}

<WIKIPRE>^{wikipreend} {
    logger.debug("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return "</pre>\n";
}

/* ----- processing commands ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{notoc} {
    logger.debug("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInfo.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST>{toc} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInfo.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    return yytext();
}

/* ----- wiki links ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{wikilink} {
    logger.debug("wikilink: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildInternalLinkUrl(this.parserInfo.getContext(), this.parserInfo.getVirtualWiki(), yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmllink} {
    logger.debug("htmllink: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildHtmlLink(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmllinkraw} {
    logger.debug("htmllinkraw: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildHtmlLinkRaw(yytext());
}

/* ----- tables ----- */

<NORMAL, TABLE, TD, TH, TC>^{tablestart} {
    logger.debug("tablestart: " + yytext() + " (" + yystate() + ")");
    beginState(TABLE);
    String attributes = yytext().substring(2).trim();
    attributes = ParserUtil.validateHtmlTagAttributes(attributes);
    return ((StringUtils.hasText(attributes)) ? "<table " + attributes + ">" : "<table>");
}

<TABLE, TD, TH, TC>^{tablecaption} {
    logger.debug("tablecaption: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    output.append(closeTable(TC));
    beginState(TC);
    output.append("<caption>");
    return output.toString();
}

<TABLE, TD, TH, TC>^{tableheading} {
    logger.debug("tableheading: " + yytext() + " (" + yystate() + ")");
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
    logger.debug("tableheadings: " + yytext() + " (" + yystate() + ")");
    return "</th><th>";
}

<TABLE, TD, TH, TC>^{tablecell} {
    logger.debug("tablecell: " + yytext() + " (" + yystate() + ")");
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
    logger.debug("tablecells: " + yytext() + " (" + yystate() + ")");
    return "</td><td>";
}

<TD>{tablecellsstyle} {
    logger.debug("tablecellsstyle: " + yytext() + " (" + yystate() + ")");
    // one extra character matched by the pattern, so roll it back
    yypushback(1);
    return "</td>" + openTableCell(yytext(), "td", '|');
}

<TABLE, TD, TH, TC>^{tablerow} {
    logger.debug("tablerow: " + yytext() + " (" + yystate() + ")");
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
    logger.debug("tableend: " + yytext() + " (" + yystate() + ")");
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
    logger.debug("htmlcomment: " + yytext() + " (" + yystate() + ")");
    // remove comment
    return "";
}

/* ----- headings ----- */

<NORMAL>^{hr} {
    logger.debug("hr: " + yytext() + " (" + yystate() + ")");
    return "<hr />\n";
}

<NORMAL>^{h1} {
    logger.debug("h1: " + yytext() + " (" + yystate() + ")");
    String tagText = ParserUtil.stripMarkup(yytext().substring(1, yytext().length() - 1).trim());
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 1);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h1>";
    // pushback to process heading text
    yypushback(yytext().length() - 1);
    this.wikih1 = true;
    return output;
}

<NORMAL>{h1close} {
    logger.debug("h1close: " + yytext() + " (" + yystate() + ")");
    if (this.wikih1) {
        this.wikih1 = false;
        return "</h1>";
    }
    return yytext();
}

<NORMAL>^{h2} {
    logger.debug("h2: " + yytext() + " (" + yystate() + ")");
    String tagText = ParserUtil.stripMarkup(yytext().substring(2, yytext().length() - 2).trim());
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 2);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h2>";
    // pushback to process heading text
    yypushback(yytext().length() - 2);
    this.wikih2 = true;
    return output;
}

<NORMAL>{h2close} {
    logger.debug("h2close: " + yytext() + " (" + yystate() + ")");
    if (this.wikih2) {
        this.wikih2 = false;
        return "</h2>";
    }
    return yytext();
}

<NORMAL>^{h3} {
    logger.debug("h3: " + yytext() + " (" + yystate() + ")");
    String tagText = ParserUtil.stripMarkup(yytext().substring(3, yytext().length() - 3).trim());
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 3);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h3>";
    // pushback to process heading text
    yypushback(yytext().length() - 3);
    this.wikih3 = true;
    return output;
}

<NORMAL>{h3close} {
    logger.debug("h3close: " + yytext() + " (" + yystate() + ")");
    if (this.wikih3) {
        this.wikih3 = false;
        return "</h3>";
    }
    return yytext();
}

<NORMAL>^{h4} {
    logger.debug("h4: " + yytext() + " (" + yystate() + ")");
    String tagText = ParserUtil.stripMarkup(yytext().substring(4, yytext().length() - 4).trim());
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 4);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h4>";
    // pushback to process heading text
    yypushback(yytext().length() - 4);
    this.wikih4 = true;
    return output;
}

<NORMAL>{h4close} {
    logger.debug("h4close: " + yytext() + " (" + yystate() + ")");
    if (this.wikih4) {
        this.wikih4 = false;
        return "</h4>";
    }
    return yytext();
}

<NORMAL>^{h5} {
    logger.debug("h5: " + yytext() + " (" + yystate() + ")");
    String tagText = ParserUtil.stripMarkup(yytext().substring(5, yytext().length() - 5).trim());
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 5);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h5>";
    // pushback to process heading text
    yypushback(yytext().length() - 5);
    this.wikih5 = true;
    return output;
}

<NORMAL>{h5close} {
    logger.debug("h5close: " + yytext() + " (" + yystate() + ")");
    if (this.wikih5) {
        this.wikih5 = false;
        return "</h5>";
    }
    return yytext();
}

/* ----- lists ----- */

<NORMAL, TABLE, TD, TH, TC>^{listitem} {
    logger.debug("start of list: " + yytext() + " (" + yystate() + ")");
    // switch to list processing mode
    beginState(LIST);
    // now that state is list, push back and re-process this line
    yypushback(yylength());
    return "";
}

<LIST>^{listitem} {
    logger.debug("list item: " + yytext() + " (" + yystate() + ")");
    // process list item content (without the list markup)
    String output = listItem(yytext());
    yypushback(yylength() - listTagCount(yytext()));
    return output;
}

<LIST>^{listend} {
    logger.debug("end of list: " + yytext() + " (" + yystate() + ")");
    // end of list, switch back to normal processing mode
    endState();
    yypushback(yylength());
    return closeList();
}

/* ----- bold / italic ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{bold} {
    logger.debug("bold: " + yytext() + " (" + yystate() + ")");
    wikibold = !wikibold;
    return (wikibold) ? "<b>" : "</b>";
}

<NORMAL, TABLE, TD, TH, TC, LIST>{italic} {
    logger.debug("italic: " + yytext() + " (" + yystate() + ")");
    wikiitalic = !wikiitalic;
    return (wikiitalic) ? "<i>" : "</i>";
}

/* ----- html ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagopen} {
    logger.debug("htmltagopen: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? ParserUtil.sanitizeHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagclose} {
    logger.debug("htmltagclose: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? ParserUtil.sanitizeHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltagattributes} {
    logger.debug("htmltagattributes: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? ParserUtil.validateHtmlTag(yytext()) : Utilities.escapeHTML(yytext());
}

/* ----- javascript ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{jsopen} {
    logger.debug("jsopen: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{jsattributes} {
    logger.debug("jsattributes: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

<JAVASCRIPT>{jsclose} {
    logger.debug("jsclose: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        endState();
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return Utilities.escapeHTML(yytext());
}

/* ----- other ----- */

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{lessthan} {
    logger.debug("lessthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return "&lt;";
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{greaterthan} {
    logger.debug("greaterthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return "&gt;";
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{quotation} {
    logger.debug("quotation: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return "&quot;";
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT>. {
    // no need to log this
    return yytext();
}
