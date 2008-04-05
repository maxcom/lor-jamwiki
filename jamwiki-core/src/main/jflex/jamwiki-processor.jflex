/*
 * This class implements the MediaWiki syntax (http://meta.wikimedia.org/wiki/Help:Editing).
 * It will also escape any HTML tags that have not been specifically allowed to be
 * present.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringEscapeUtils;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiProcessor.class.getName());
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]
inputcharacter     = [^\r\n]
entity             = (&#([0-9]{2,4});) | (&[A-Za-z]{2,6};)

/* non-container expressions */
hr                 = "----"
wikiheading        = [\=]+ ([^\n\=]+|[^\n\=][^\n]+[^\n\=]) [\=]+
bold               = "'''"
bolditalic         = "'''''"
italic             = "''"

/* lists */
listitem           = [\*#\:;]+ [^\*#\:;]
listend            = [^\*#\:;\r\n]+ (.)+
listdt             = ":"

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ")+ ([^ \t\r\n])
wikiprecontinue    = (" ") ([ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* allowed html */
htmlkeyword        = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var
tableattributes    = align|bgcolor|border|cellpadding|cellspacing|class|colspan|height|nowrap|rowspan|style|valign|width
htmlattributes     = ({tableattributes}) | align|alt|background|bgcolor|border|class|clear|color|face|height|id|size|style|valign|width
htmlattribute      = ([ ]+) {htmlattributes} ([ ]*=[^>\n\r]+[ ]*)*
htmltagclose       = (<[ ]*\/[ ]*) {htmlkeyword} ([ ]*>)
htmltagopen        = (<[ ]*) {htmlkeyword} ({htmlattribute})* ([ ]*>)
htmltagnocontent   = (<[ ]*) {htmlkeyword} ({htmlattribute})* ([ ]*\/[ ]*>)

/* javascript */
jsattribute        = ([ ]+) (type|charset|defer|language) ([ ]*=[^>\n\r]+[ ]*)*
jsopen             = (<[ ]*) script ({jsattribute})* ([ ]*[\/]?[ ]*>)
jsclose            = (<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
notoc              = "__NOTOC__"
toc                = "__TOC__"
forcetoc           = "__FORCETOC__"

/* tables */
tableattribute     = ([ ]*) {tableattributes} ([ ]*=[^>\n\r\|]+[ ]*)*
tablestart         = "{|" {inputcharacter}* {newline}
tableend           = "|}"
tablecell          = "|" [^\+\-\}] | "|" ({tableattribute})+ "|" [^\|]
tablecells         = "||" | "!!"
tablecellsstyle    = "||" ({tableattribute})+ "|" ([^|])
tableheading       = "!" | "!" ({tableattribute})+ "|" [^\|]
tablerow           = "|-" [ ]* ({tableattribute})* {newline}
tablecaption       = "|+" | "|+" ({tableattribute})+ "|" [^\|]

/* wiki links */
wikilink           = "[[" [^\]\n\r]+ "]]"
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n\r]+) "]"
htmllinkraw        = ({protocol}) ([^ \n\r\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\r\]\[]* ({wikilink} | {htmllinkwiki}) [^\n\r\]\[]*)+ "]]"

/* references */
reference          = (<[ ]*) "ref" ([ ]+name[ ]*=[^>\/\n\r]+[ ]*)? ([ ]*>) ~(<[ ]*\/[ ]*ref[ ]*>)
referencenocontent = (<[ ]*) "ref" ([ ]+name[ ]*=[^>\/\n\r]+[ ]*) ([ ]*\/[ ]*>)
references         = (<[ ]*) "references" ([ ]*[\/]?[ ]*>)

%state NORMAL, TABLE, LIST, PRE, JAVASCRIPT, WIKIPRE

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    String content = JFlexParserUtil.tagContent(yytext());
    return "<nowiki>" + StringEscapeUtils.escapeHtml(content) + "</nowiki>";
}

/* ----- pre ----- */

<NORMAL, LIST, TABLE>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    beginState(PRE);
    this.pushTag("pre", null);
    return "";
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML() is true, so no need to check here
    endState();
    this.popTag("pre");
    return "";
}

<NORMAL, LIST, TABLE, WIKIPRE>^{wikiprestart} {
    logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(yytext().length() - 1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
        this.pushTag("pre", null);
    }
    return "";
}

<WIKIPRE>^{wikiprecontinue} {
    // this is a corner-case.  if there is a blank line within a wikipre rollback the first
    // character to prevent extra spaces from being added.
    logger.finer("wikiprecontinue: " + yytext() + " (" + yystate() + ")");
    yypushback(1);
}

<WIKIPRE>^{wikipreend} {
    logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    this.popTag("pre");
    return  "\n";
}

/* ----- table of contents ----- */

<NORMAL, LIST, TABLE>{notoc} {
    logger.finer("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<NORMAL, LIST, TABLE>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    this.parserInput.getTableOfContents().setForceTOC(true);
    return yytext();
}

<NORMAL, LIST, TABLE>{forcetoc} {
    logger.finer("forcetoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setForceTOC(true);
    return "";
}

/* ----- wiki links ----- */

<NORMAL, LIST, TABLE>{imagelinkcaption} {
    logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    WikiLinkTag parserTag = new WikiLinkTag();
    return parserTag.parse(this.parserInput, this.parserOutput, this.mode, yytext());
}

<NORMAL, LIST, TABLE>{wikilink} {
    logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    WikiLinkTag parserTag = new WikiLinkTag();
    return parserTag.parse(this.parserInput, this.parserOutput, this.mode, yytext());
}

<NORMAL, LIST, TABLE>{htmllink} {
    logger.finer("htmllink: " + yytext() + " (" + yystate() + ")");
    HtmlLinkTag parserTag = new HtmlLinkTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

/* ----- tables ----- */

<NORMAL, LIST, TABLE>^{tablestart} {
    logger.finer("tablestart: " + yytext() + " (" + yystate() + ")");
    beginState(TABLE);
    String tagAttributes = yytext().substring(2).trim();
    tagAttributes = JFlexParserUtil.validateHtmlTagAttributes(tagAttributes);
    this.pushTag("table", tagAttributes);
    return "";
}

<TABLE>^{tablecaption} {
    logger.finer("tablecaption: " + yytext() + " (" + yystate() + ")");
    processTableStack();
    if (yytext().length() > 2) {
        // for captions with CSS specified an extra character is matched
        yypushback(1);
    }
    parseTableCell(yytext(), "caption", "|+");
    return "";
}

<TABLE>^{tableheading} {
    logger.finer("tableheading: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    // FIXME - hack!  make sure that a table row is open
    if (!this.peekTag().getTagType().equals("tr")) {
        this.pushTag("tr", null);
    }
    if (yytext().length() > 2) {
        // for headings with CSS specified an extra character is matched
        yypushback(1);
    }
    parseTableCell(yytext(), "th", "!");
    return "";
}

<TABLE>^{tablecell} {
    logger.finer("tablecell: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    // FIXME - hack!  make sure that a table row is open
    if (!this.peekTag().getTagType().equals("tr")) {
        this.pushTag("tr", null);
    }
    // extra character matched by both regular expressions so push it back
    yypushback(1);
    parseTableCell(yytext(), "td", "|");
    return "";
}

<TABLE>{tablecells} {
    logger.finer("tablecells: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("td") && yytext().equals("||")) {
        this.popTag("td");
        this.pushTag("td", null);
        return "";
    }
    if (this.peekTag().getTagType().equals("th")) {
        this.popTag("th");
        this.pushTag("th", null);
        return "";
    }
    return yytext();
}

<TABLE>{tablecellsstyle} {
    logger.finer("tablecellsstyle: " + yytext() + " (" + yystate() + ")");
    if (!this.peekTag().getTagType().equals("td")) {
        return yytext();
    }
    // one extra character matched by the pattern, so roll it back
    yypushback(1);
    this.popTag("td");
    parseTableCell(yytext(), "td", "|");
    return "";
}

<TABLE>^{tablerow} {
    logger.finer("tablerow: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    if (!this.peekTag().getTagType().equals("table") && !this.peekTag().getTagType().equals("caption")) {
        this.popTag("tr");
    }
    String tagType = "tr";
    String attributes = null;
    if (yytext().trim().length() > 2) {
        attributes = yytext().substring(2).trim();
        attributes = JFlexParserUtil.validateHtmlTagAttributes(attributes);
    }
    this.pushTag(tagType, attributes);
    return "";
}

<TABLE>^{tableend} {
    logger.finer("tableend: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    // end TABLE state
    endState();
    this.popTag("tr");
    this.popTag("table");
    return "";
}

/* ----- headings ----- */

<NORMAL>^{hr} {
    logger.finer("hr: " + yytext() + " (" + yystate() + ")");
    return "<hr />\n";
}

<NORMAL>^{wikiheading} {
    logger.finer("wikiheading: " + yytext() + " (" + yystate() + ")");
    WikiHeadingTag parserTag = new WikiHeadingTag();
    return parserTag.parse(this.parserInput, this.parserOutput, this.mode, yytext());
}

/* ----- lists ----- */

<NORMAL, LIST, TABLE>^{listitem} {
    logger.finer("listitem: " + yytext() + " (" + yystate() + ")");
    if (yystate() != LIST) beginState(LIST);
    // one non-list character matched, roll it back
    yypushback(1);
    this.processListStack(yytext());
    return "";
}

<LIST>^{listend} {
    logger.finer("listend: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    // roll back any matches to allow re-parsing
    yypushback(raw.length());
    endState();
    // pop list tags currently on the stack
    int depth = this.currentListDepth();
    this.popListTags(depth);
    return "";
}

<LIST>{listdt} {
    logger.finer("listdt: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("dt")) {
        // special case list of the form "; term : definition"
        this.popTag("dt");
        this.pushTag("dd", null);
        return "";
    }
    return yytext();
}

/* ----- bold / italic ----- */

<NORMAL, LIST, TABLE>{bold} {
    logger.finer("bold: " + yytext() + " (" + yystate() + ")");
    this.processBoldItalic("b");
    return "";
}

<NORMAL, LIST, TABLE>{bolditalic} {
    logger.finer("bolditalic: " + yytext() + " (" + yystate() + ")");
    this.processBoldItalic(null);
    return "";
}

<NORMAL, LIST, TABLE>{italic} {
    logger.finer("italic: " + yytext() + " (" + yystate() + ")");
    this.processBoldItalic("i");
    return "";
}

/* ----- references ----- */

<NORMAL, LIST, TABLE>{reference} {
    logger.finer("reference: " + yytext() + " (" + yystate() + ")");
    WikiReferenceTag parserTag = new WikiReferenceTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

<NORMAL, LIST, TABLE>{referencenocontent} {
    logger.finer("referencenocontent: " + yytext() + " (" + yystate() + ")");
    WikiReferenceTag parserTag = new WikiReferenceTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

<NORMAL, LIST, TABLE>{references} {
    logger.finer("references: " + yytext() + " (" + yystate() + ")");
    WikiReferencesTag parserTag = new WikiReferencesTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

/* ----- html ----- */

<NORMAL, LIST, TABLE>{htmltagopen} {
    logger.finer("htmltagopen: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    String[] tagInfo = JFlexParserUtil.parseHtmlTag(yytext());
    this.pushTag(tagInfo[0], tagInfo[1]);
    return "";
}

<NORMAL, LIST, TABLE>{htmltagclose} {
    logger.finer("htmltagclose: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    String[] tagInfo = JFlexParserUtil.parseHtmlTag(yytext());
    this.popTag(tagInfo[0]);
    return "";
}

<NORMAL, LIST, TABLE>{htmltagnocontent} {
    logger.finer("htmltagnocontent: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    return JFlexParserUtil.validateHtmlTag(yytext());
}

/* ----- javascript ----- */

<NORMAL, LIST, TABLE>{jsopen} {
    logger.finer("jsopen: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        String[] tagInfo = JFlexParserUtil.parseHtmlTag(yytext());
        this.pushTag(tagInfo[0], tagInfo[1]);
        return "";
    }
    return StringEscapeUtils.escapeHtml(yytext());
}

<JAVASCRIPT>{jsclose} {
    logger.finer("jsclose: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        endState();
        String[] tagInfo = JFlexParserUtil.parseHtmlTag(yytext());
        this.popTag(tagInfo[0]);
        return "";
    }
    return StringEscapeUtils.escapeHtml(yytext());
}

/* ----- other ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE>{entity} {
    logger.finer("entity: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    if (Utilities.isHtmlEntity(raw)) {
        return raw;
    }
    return StringEscapeUtils.escapeHtml(raw);
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE, JAVASCRIPT>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE>. {
    // no need to log this
    return StringEscapeUtils.escapeHtml(yytext());
}

<JAVASCRIPT>. {
    // do not escape or otherwise modify Javascript
    return yytext();
}
