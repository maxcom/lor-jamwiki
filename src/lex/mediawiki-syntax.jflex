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
 *
 * Not yet implemented:
 *
 *   <math>
 *   Templates
 *   __TOC__
 */
package org.jmwiki.parser;

import java.net.URLEncoder;
import java.util.Hashtable;
import java.util.Stack;
import org.apache.log4j.Logger;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;
import org.jmwiki.servlets.WikiServlet;
import org.jmwiki.utils.Utilities;

%%

%public
%class MediaWikiSyntax
%implements org.jmwiki.parser.Lexer
%type String
%unicode

/* code included in the constructor */
%init{
    allowHtml = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
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
        output.append("</td>");
        endState();
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(MediaWikiSyntax.class.getName());
    /** Member variable used to keep track of the state history for the lexer. */
    protected Stack states = new Stack();
    protected String virtualWiki = null;;
    protected String context = null;
    protected TableOfContents toc = new TableOfContents();
    protected boolean allowHtml = false;
    protected boolean wikibold = false;
    protected boolean wikiitalic = false;
    protected boolean nowiki = false;
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
        int next = ((Integer)states.pop()).intValue();
        yybegin(next);
    }
    
    /**
     *
     */
    protected String listItem(String text) {
        text = text.trim();
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
    protected void setContext(String context) {
    	this.context = context;
    }
    
    /**
     *
     */
    protected String updateToc(String name, String text, int level) {
        String output = "";
        if (this.toc.getStatus() == TableOfContents.STATUS_TOC_UNINITIALIZED) {
            output = TableOfContents.TOC_INSERT_TAG;
        }
        this.toc.addEntry(name, text, level);
        return output;
    }
    
    /**
     *
     */
    public void setVirtualWiki(String vWiki) {
        this.virtualWiki = vWiki;
    }
    
    /**
     *
     */
    public void setTOC(TableOfContents toc) {
        this.toc = toc;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
inputcharacter     = [^\r\n]
whitespace         = {newline} | [ \t\f]
htmltagopen        = "<"
htmltagclose       = ">"
ampersand          = "&"

/* non-container expressions */
hr                 = "----"
h1                 = "=" [^=\n]+ ~"="
h2                 = "==" [^=\n]+ ~"=="
h3                 = "===" [^=\n]+ ~"==="
h4                 = "====" [^=\n]+ ~"===="
h5                 = "=====" [^=\n]+ ~"====="
bold               = "'''"
italic             = "''"

/* container expressions */
liststart          = [\*#\:;]+
listend            = [^\*#\:;\r\n]

/* nowiki */
nowikistart        = "<nowiki>"
nowikiend          = "</nowiki>"

/* pre */
htmlprestart       = (<[ ]*[Pp][Rr][Ee][ ]*>)
htmlpreend         = (<[ ]*\/[ ]*[Pp][Rr][Ee][ ]*>)

/* allowed html */
htmlbreak          = (<[ ]*[Bb][Rr][ ]*[\/]?[ ]*>)
htmlboldstart      = (<[ ]*[Bb][ ]*>)
htmlboldend        = (<[ ]*\/[ ]*[Bb][ ]*>)
htmlcodestart      = (<[ ]*[Cc][Oo][Dd][Ee][ ]*>)
htmlcodeend        = (<[ ]*\/[ ]*[Cc][Oo][Dd][Ee][ ]*>)
htmldivstart       = (<[ ]*[Dd][Ii][Vv][ ]*>)|(<[ ]*[Dd][Ii][Vv][ ]+[^>\/]+>)
htmldivend         = (<[ ]*\/[ ]*[Dd][Ii][Vv][ ]*>)
htmlitalicstart    = (<[ ]*[Ii][ ]*>)
htmlitalicend      = (<[ ]*\/[ ]*[Ii][ ]*>)
htmlspanstart      = (<[ ]*[Ss][Pp][Aa][Nn][ ]*>)|(<[ ]*[Ss][Pp][Aa][Nn][ ]+[^>\/]+>)
htmlspanend        = (<[ ]*\/[ ]*[Ss][Pp][Aa][Nn][ ]*>)
htmlstrikestart    = (<[ ]*[Ss][Tt][Rr][Ii][Kk][Ee][ ]*>)
htmlstrikeend      = (<[ ]*\/[ ]*[Ss][Tt][Rr][Ii][Kk][Ee][ ]*>)
htmlsubstart       = (<[ ]*[Ss][Uu][Bb][ ]*>)
htmlsubend         = (<[ ]*\/[ ]*[Ss][Uu][Bb][ ]*>)
htmlsupstart       = (<[ ]*[Ss][Uu][Pp][ ]*>)
htmlsupend         = (<[ ]*\/[ ]*[Ss][Uu][Pp][ ]*>)
htmltablestart     = (<[ ]*[Tt][Aa][Bb][Ll][Ee][ ]*>)|(<[ ]*[Tt][Aa][Bb][Ll][Ee][ ]+[^>\/]+>)
htmltableend       = (<[ ]*\/[ ]*[Tt][Aa][Bb][Ll][Ee][ ]*>)
htmltdstart        = (<[ ]*[Tt][Dd][ ]*>)|(<[ ]*[Tt][Dd][ ]+[^>\/]+>)
htmltdend          = (<[ ]*\/[ ]*[Tt][Dd][ ]*>)
htmlthstart        = (<[ ]*[Tt][Hh][ ]*>)|(<[ ]*[Tt][Dd][ ]+[^>\/]+>)
htmlthend          = (<[ ]*\/[ ]*[Tt][Hh][ ]*>)
htmltrstart        = (<[ ]*[Tt][Rr][ ]*>)|(<[ ]*[Tt][Rr][ ]+[^>\/]+>)
htmltrend          = (<[ ]*\/[ ]*[Tt][Rr][ ]*>)
htmlttstart        = (<[ ]*[Tt][Tt][ ]*>)
htmlttend          = (<[ ]*\/[ ]*[Tt][Tt][ ]*>)
htmlunderlinestart = (<[ ]*[Uu][ ]*>)
htmlunderlineend   = (<[ ]*\/[ ]*[Uu][ ]*>)

/* processing commands */
notoc              = "__NOTOC__"

/* comments */
htmlcomment        = "<!--" [^(\-\->)]* ~"-->"

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

%state NORMAL, TABLE, TD, TH, TC, LIST, NOWIKI, PRE

%%

/* ----- parsing tags ----- */

<PRE, NORMAL, TABLE, TD, TH, TC, LIST>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return "";
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return "";
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHtml) {
        beginState(PRE);
        return "<pre>";
    }
    return "&lt;pre&gt;";
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return "</pre>";
}

/* ----- processing commands ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{notoc} {
    logger.debug("notoc: " + yytext() + " (" + yystate() + ")");
    this.toc.setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

/* ----- wiki links ----- */

<NORMAL, TABLE, TD, TH, TC, LIST>{wikilink} {
    logger.debug("wikilink: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiLink(this.context, this.virtualWiki, yytext());
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
    return "<table " + yytext().substring(2).trim() + "><tr>";
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
    if (yytext().length() > 1) {
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
    if (yytext().length() > 2) {
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
    output.append(closeTable(TABLE));
    output.append("</tr>");
    if (yytext().length() > 2) {
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
    return output + "<a name=\"" + tagName + "\"></a><h1>" + tagText + "</h1>";
}

<NORMAL>^{h2} {
    logger.debug("h2: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(2, yytext().indexOf("==", 2)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 2);
    return output + "<a name=\"" + tagName + "\"></a><h2>" + tagText + "</h2>";
}

<NORMAL>^{h3} {
    logger.debug("h3: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(3, yytext().indexOf("===", 3)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 3);
    return output + "<a name=\"" + tagName + "\"></a><h3>" + tagText + "</h3>";
}

<NORMAL>^{h4} {
    logger.debug("h4: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(4, yytext().indexOf("====", 4)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 4);
    return output + "<a name=\"" + tagName + "\"></a><h4>" + tagText + "</h4>";
}

<NORMAL>^{h5} {
    logger.debug("h5: " + yytext() + " (" + yystate() + ")");
    String tagText = yytext().substring(5, yytext().indexOf("=====", 5)).trim();
    String tagName = Utilities.encodeURL(tagText);
    String output = updateToc(tagName, tagText, 5);
    return output + "<a name=\"" + tagName + "\"></a><h5>" + tagText + "</h5>";
}

/* ----- lists ----- */

<NORMAL, TABLE, TD, TH, TC>^{liststart} {
    logger.debug("start of list: " + yytext() + " (" + yystate() + ")");
    // switch to list processing mode
    beginState(LIST);
    yypushback(yylength());
    return "";
}

<LIST>^{liststart} {
    logger.debug("list item: " + yytext() + " (" + yystate() + ")");
    // process list item
    return listItem(yytext());
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

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlbreak} {
    logger.debug("htmlbreak: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<br />" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlboldstart} {
    logger.debug("htmlboldstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<b>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlboldend} {
    logger.debug("htmlboldend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</b>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlcodestart} {
    logger.debug("htmlcodestart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<code>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlcodeend} {
    logger.debug("htmlcodeend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</code>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmldivstart} {
    logger.debug("htmldivstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmldivend} {
    logger.debug("htmldivend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</div>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlitalicstart} {
    logger.debug("htmlitalicstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<i>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlitalicend} {
    logger.debug("htmlitalicend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</i>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlspanstart} {
    logger.debug("htmlspanstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlspanend} {
    logger.debug("htmlspanend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</span>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlstrikestart} {
    logger.debug("htmlstrikestart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<strike>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlstrikeend} {
    logger.debug("htmlstrikeend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</strike>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlsubstart} {
    logger.debug("htmlsubstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<sub>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlsubend} {
    logger.debug("htmlsubend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</sub>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlsupstart} {
    logger.debug("htmlsupstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<sup>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlsupend} {
    logger.debug("htmlsupend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</sup>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltablestart} {
    logger.debug("htmltablestart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltableend} {
    logger.debug("htmltableend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</table>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltdstart} {
    logger.debug("htmltdstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltdend} {
    logger.debug("htmltdend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</td>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlthstart} {
    logger.debug("htmlthstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlthend} {
    logger.debug("htmlthend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</th>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltrstart} {
    logger.debug("htmltrstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? yytext() : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmltrend} {
    logger.debug("htmltrend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</tr>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlttstart} {
    logger.debug("htmlttstart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<tt>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlttend} {
    logger.debug("htmlttend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</tt>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlunderlinestart} {
    logger.debug("htmlunderlinestart: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "<u>" : ParserUtil.escapeHtml(yytext());
}

<NORMAL, TABLE, TD, TH, TC, LIST>{htmlunderlineend} {
    logger.debug("htmlunderlineend: " + yytext() + " (" + yystate() + ")");
    return (allowHtml()) ? "</u>" : ParserUtil.escapeHtml(yytext());
}

/* ----- other ----- */

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{ampersand} {
    logger.debug("htmltagopen: " + yytext() + " (" + yystate() + ")");
    // if html not allowed, escape it
    return (allowHtml()) ? yytext() : "&amp;";
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{htmltagopen} {
    logger.debug("htmltagopen: " + yytext() + " (" + yystate() + ")");
    // if html not allowed, escape it
    return (allowHtml()) ? yytext() : "&lt;";
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{htmltagclose} {
    logger.debug("htmltagclose: " + yytext() + " (" + yystate() + ")");
    // if html not allowed, escape it
    return (allowHtml()) ? yytext() : "&gt;";
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>{whitespace} {
    logger.debug("{whitespace}: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<PRE, NOWIKI, NORMAL, TABLE, TD, TH, TC, LIST>. {
    logger.debug("default: " + yytext() + " (" + yystate() + ")");
    return yytext();
}
