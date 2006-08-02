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

%%

%public
%class JAMWikiPreProcessor
%implements org.jamwiki.parser.Lexer
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
    /** Member variable used to keep track of the state history for the lexer. */
    protected Stack states = new Stack();
    protected ParserInfo parserInfo = null;;
    protected boolean allowHtml = false;
    protected boolean allowJavascript = false;
    protected boolean wikibold = false;
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
        return (allowHtml && yystate() != PRE && yystate() != NOWIKI);
    }
    
    /**
     *
     */
    protected boolean allowJavascript() {
        return (allowJavascript && yystate() != PRE && yystate() != NOWIKI);
    }

    /**
     * Begin a new state and store the old state onto the stack.
     */
    protected void beginState(int state) {
        // store current state
        Integer current = new Integer(yystate());
        states.push(current);
        // switch to new state
        yybegin(state);
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
     * End processing of a state and switch to the previous state.
     */
    protected void endState() {
        // revert to previous state
        if (states.empty()) {
            logger.warn("Attempt to call endState for an empty stack with text: " + yytext());
            return;
        }
        int next = ((Integer)states.pop()).intValue();
        yybegin(next);
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
    public void setParserInfo(ParserInfo parserInfo) {
        this.parserInfo = parserInfo;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
inputcharacter     = [^\r\n]
whitespace         = {newline} | [ \t\f]
lessthan           = "<"
greaterthan        = ">"
htmltag            = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var

/* non-container expressions */
hr                 = "----"
h1                 = "=" [^=\n]+ ~"="
h2                 = "==" [^=\n]+ ~"=="
h3                 = "===" [^=\n]+ ~"==="
h4                 = "====" [^=\n]+ ~"===="
h5                 = "=====" [^=\n]+ ~"====="
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
tablecell          = "|" [^\+\-\}] | "|" [^\+\|\-\}\{\<\r\n] [^\|\r\n]* "|" [^\|]
tablecells         = "||"
tableheading       = "!" | "!" [^\!\|\-\{\<\r\n]+ "|" [^\|]
tableheadings      = "||" | "!!"
tablerow           = "|-" {inputcharacter}* {newline}
tablecaption       = "|+"

/* wiki links */
wikilink           = "[[" [^(\]\])\n\r]* ~"]]"
htmllink           = "[" [^\]\n\r]* ~"]"
htmllinkraw        = ("https://" [^ \n\r\t]+) | ("http://" [^ \n\r\t]+) | ("mailto://"  [^ \n\r\t]+) | ("ftp://"  [^ \n\r\t]+) | ("file://"  [^ \n\r\t]+)

%state NORMAL, TABLE, TD, TH, TC, LIST, NOWIKI, PRE, JAVASCRIPT

%%

/* ----- nowiki ----- */

<PRE, NORMAL, TABLE, TD, TH, TC, LIST>{nowikistart} {
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
    return "<table " + yytext().substring(2).trim() + ">";
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
        output.append("<th ").append(yytext().substring(start, end).trim()).append(">");
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
    if (yytext().trim().length() > 2) {
        int start = 1;
        int end = yytext().indexOf("|", start+1);
        output.append("<td ").append(yytext().substring(start, end).trim()).append(">");
    } else {
        output.append("<td>");
    }
    // extra character matched by both regular expressions so push it back
    yypushback(1);
    return output.toString();
}

<TD>{tablecells} {
    logger.debug("tablecells: " + yytext() + " (" + yystate() + ")");
    return "</td><td>";
}

<TABLE, TD, TH, TC>^{tablerow} {
    logger.debug("tablerow: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    // if a column was already open, close it
    int oldState = yystate();
    output.append(closeTable(TABLE));
    if (oldState != TABLE) output.append("</tr>");
    if (yytext().trim().length() > 2) {
        output.append("<tr ").append(yytext().substring(2).trim()).append(">");
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
    String tagText = yytext().substring(1, yytext().indexOf("=", 1)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 1);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h1>" + tagText + "</h1>";
    return output;
}

<NORMAL>^{h2} {
    logger.debug("h2: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(2, yytext().indexOf("==", 2)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 2);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h2>" + tagText + "</h2>";
    return output;
}

<NORMAL>^{h3} {
    logger.debug("h3: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(3, yytext().indexOf("===", 3)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 3);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h3>" + tagText + "</h3>";
    return output;
}

<NORMAL>^{h4} {
    logger.debug("h4: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(4, yytext().indexOf("====", 4)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 4);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h4>" + tagText + "</h4>";
    return output;
}

<NORMAL>^{h5} {
    logger.debug("h5: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(5, yytext().indexOf("=====", 5)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 5);
    output += ParserUtil.buildEditLinkUrl(this.parserInfo, nextSection());
    output += "<a name=\"" + tagName + "\"></a><h5>" + tagText + "</h5>";
    return output;
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
    return (allowHtml()) ? ParserUtil.sanitizeHtmlTagAttributes(yytext()) : Utilities.escapeHTML(yytext());
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

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{lessthan} {
    logger.debug("lessthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return "&lt;";
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{greaterthan} {
    logger.debug("greaterthan: " + yytext() + " (" + yystate() + ")");
    // escape html not recognized by above tags
    return "&gt;";
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST, JAVASCRIPT>. {
    // no need to log this
    return yytext();
}
