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

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiProcessor.class.getName());
%}

/* character expressions */
newline            = "\n"
whitespace         = [ \n\t\f]
entity             = (&#([0-9]{2,4});) | (&[A-Za-z]{2,6};)
emptyline          = ([ \t])* ({newline})

/* non-container expressions */
hr                 = ({newline})? "----" ({newline})
wikiheading6       = "======" (.+) "======"
wikiheading5       = "=====" (.+) "====="
wikiheading4       = "====" (.+) "===="
wikiheading3       = "===" (.+) "==="
wikiheading2       = "==" (.+) "=="
wikiheading1       = "=" (.+) "="
wikiheading        = ({wikiheading6})|({wikiheading5})|({wikiheading4})|({wikiheading3})|({wikiheading2})|({wikiheading1})
bold               = "'''"
bolditalic         = "'''''"
italic             = "''"

/* lists */
listitem           = [\*#\:;]+ [^\*#\:;]
listend            = [^\*#\:;\n]+ (.)+
listdt             = ":"

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
attributeValueInQuotes = "\"" ~"\""
attributeValueInSingleQuotes = "'" ~"'"
attributeValueNoQuotes = [^>\n]+
htmlattribute      = ([ ]+) [a-zA-Z:]+ ([ ]*=[ ]*({attributeValueInQuotes}|{attributeValueInSingleQuotes}|{attributeValueNoQuotes}))*
htmlprestart       = (<[ ]*pre ({htmlattribute})* [ ]* (\/)? [ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ")+ ([^ \t\n])
wikiprecontinue    = (" ") ([ \t\n])
wikipreend         = ([^ ]) | ({newline})

/* allowed html */
inlinetag          = abbr|br|b|big|cite|code|del|em|font|i|ins|s|small|span|strike|strong|sub|sup|tt|u|var
blockleveltag      = blockquote|caption|center|col|colgroup|dd|div|dl|dt|hr|li|ol|p|table|tbody|td|tfoot|th|thead|tr|ul
htmlkeyword        = {inlinetag}|{blockleveltag}
htmlbr             = <[ ]* (\/)? [ ]* br ({htmlattribute})* [ ]* (\/)? [ ]*>
htmlparagraphopen  = <[ ]* p ({htmlattribute})* [ ]* (\/)? [ ]*>
htmlparagraphclose = (<[ ]*\/[ ]*) p ([ ]*>)
inlinetagopen      = <[ ]* ({inlinetag}) ({htmlattribute})* [ ]* (\/)? [ ]*>
blockleveltagopen  = <[ ]* ({blockleveltag}) ({htmlattribute})* [ ]* (\/)? [ ]*>
blockleveltagclose = (<[ ]*\/[ ]*) {blockleveltag} ([ ]*>)
htmltagopen        = <[ ]* ({htmlkeyword}) ({htmlattribute})* [ ]* (\/)? [ ]*>
htmltagclose       = (<[ ]*\/[ ]*) {htmlkeyword} ([ ]*>)
htmltagnocontent   = (<[ ]*) {htmlkeyword} ({htmlattribute})* ([ ]*\/[ ]*>)

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
notoc              = "__NOTOC__"
toc                = "__TOC__"
forcetoc           = "__FORCETOC__"

/* tables */
tableattribute     = ([ ]*) [a-zA-Z:]+ ([ ]*=[^>\n\|]+[ ]*)*
tablestart         = "{|" (.)* {newline}
tableend           = "|}" ({newline})?
tablecell          = "|" [^\+\-\}] | "|" ({tableattribute})+ "|" [^\|]
tablecells         = "||" | "!!"
tablecellsstyle    = "||" ({tableattribute})+ "|" ([^|])
tableheading       = "!" | "!" ({tableattribute})+ "|" [^\|]
tablerow           = "|-" [ ]* ({tableattribute})* {newline}
tablecaption       = "|+" | "|+" ({tableattribute})+ "|" [^\|]

/* wiki links */
wikilink           = "[[" [^\]\n]+ "]]" [a-z]*
protocol           = "http://" | "https://" | "mailto:" | "mailto://" | "ftp://" | "file://"
htmllinkwiki       = "[" ({protocol}) ([^\]\n]+) "]"
htmllinkraw        = ({protocol}) ([^ <'\n\t]+)
htmllink           = ({htmllinkwiki}) | ({htmllinkraw})
/* FIXME - hard-coding of image namespace */
imagelinkcaption   = "[[" ([ ]*) "Image:" ([^\n\]\[]* ({wikilink} | {htmllinkwiki}) [^\n\]\[]*)+ "]]"

/* references */
reference          = (<[ ]*) "ref" ([ ]+name[ ]*=[^>\/\n]+[ ]*)? ([ ]*>) ~(<[ ]*\/[ ]*ref[ ]*>)
referencenocontent = (<[ ]*) "ref" ([ ]+name[ ]*=[^>\/\n]+[ ]*) ([ ]*\/[ ]*>)
references         = (<[ ]*) "references" ([ ]*[\/]?[ ]*>)

/* paragraphs */
/* TODO: this pattern does not match text such as "< is a less than sign" */
startparagraph     = ({emptyline})? ({emptyline})? ([^< \n])|{inlinetagopen}|{imagelinkcaption}|{wikilink}|{htmllink}|{bold}|{bolditalic}|{italic}|{entity}|{nowiki}
paragraphempty     = ({emptyline}) ({emptyline})+
endparagraph1      = ({newline}){1,2} ({hr}|{wikiheading}|{listitem}|{wikiprestart}|{tablestart})
endparagraph2      = (({newline})([ \t]*)){2}
endparagraph3      = {blockleveltagopen}|{htmlprestart}|{blockleveltagclose}
endparagraph       = {endparagraph1}|{endparagraph2}|{endparagraph3}

%state TABLE, LIST, PRE, WIKIPRE, PARAGRAPH

%%

/* ----- paragraphs ----- */

<YYINITIAL>^{startparagraph} {
    if (logger.isFinerEnabled()) logger.finer("startparagraph: " + yytext() + " (" + yystate() + ")");
    this.parseParagraphStart(yytext());
    beginState(PARAGRAPH);
    return "";
}

<YYINITIAL>^{paragraphempty} {
    if (logger.isFinerEnabled()) logger.finer("paragraphempty: " + yytext() + " (" + yystate() + ")");
    this.parseParagraphEmpty(yytext());
    return "";
}

<PARAGRAPH>{endparagraph} {
    if (logger.isFinerEnabled()) logger.finer("endparagraph: " + yytext() + " (" + yystate() + ")");
    this.parseParagraphEnd(yytext());
    endState();
    return "";
}

/* ----- nowiki ----- */

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE, PARAGRAPH>{nowiki} {
    if (logger.isFinerEnabled()) logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    String content = JFlexParserUtil.tagContent(yytext());
    return "<nowiki>" + StringEscapeUtils.escapeHtml(content) + "</nowiki>";
}

/* ----- pre ----- */

<YYINITIAL, LIST, TABLE>{htmlprestart} {
    if (logger.isFinerEnabled()) logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    beginState(PRE);
    this.pushTag("pre", yytext());
    return "";
}

<PRE>{htmlpreend} {
    if (logger.isFinerEnabled()) logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML() is true, so no need to check here
    endState();
    this.popTag("pre");
    return "";
}

<YYINITIAL, WIKIPRE, LIST, TABLE>^{wikiprestart} {
    if (logger.isFinerEnabled()) logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("wikiprecontinue: " + yytext() + " (" + yystate() + ")");
    yypushback(1);
}

<WIKIPRE>^{wikipreend} {
    if (logger.isFinerEnabled()) logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    this.popTag("pre");
    return  "\n";
}

/* ----- table of contents ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{notoc} {
    if (logger.isFinerEnabled()) logger.finer("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{toc} {
    if (logger.isFinerEnabled()) logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    this.parserInput.getTableOfContents().setForceTOC(true);
    return yytext();
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{forcetoc} {
    if (logger.isFinerEnabled()) logger.finer("forcetoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setForceTOC(true);
    return "";
}

/* ----- tables ----- */

<YYINITIAL, TABLE, PARAGRAPH>^{tablestart} {
    if (logger.isFinerEnabled()) logger.finer("tablestart: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("p")) {
        popTag("p");
    }
    if (yystate() == PARAGRAPH) {
        endState();
    }
    beginState(TABLE);
    String tagAttributes = yytext().trim().substring(2).trim();
    this.pushTag("table", "<table " + tagAttributes + ">");
    return "";
}

<TABLE>^{tablecaption} {
    if (logger.isFinerEnabled()) logger.finer("tablecaption: " + yytext() + " (" + yystate() + ")");
    processTableStack();
    if (yytext().length() > 2) {
        // for captions with CSS specified an extra character is matched
        yypushback(1);
    }
    parseTableCell(yytext(), "caption", "|+");
    return "";
}

<TABLE>^{tableheading} {
    if (logger.isFinerEnabled()) logger.finer("tableheading: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("tablecell: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("tablecells: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("tablecellsstyle: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("tablerow: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    if (!this.peekTag().getTagType().equals("table") && !this.peekTag().getTagType().equals("caption")) {
        this.popTag("tr");
    }
    String openTagRaw = null;
    if (yytext().trim().length() > 2) {
        openTagRaw = "<tr " + yytext().substring(2).trim() + ">";
    }
    this.pushTag("tr", openTagRaw);
    return "";
}

<TABLE>^{tableend} {
    if (logger.isFinerEnabled()) logger.finer("tableend: " + yytext() + " (" + yystate() + ")");
    // if a column was already open, close it
    processTableStack();
    // end TABLE state
    endState();
    this.popTag("tr");
    this.popTag("table");
    return "";
}

/* ----- headings ----- */

<YYINITIAL>^{hr} {
    if (logger.isFinerEnabled()) logger.finer("hr: " + yytext() + " (" + yystate() + ")");
    // pushback the closing newline
    yypushback(1);
    return "<hr />";
}

<YYINITIAL, PARAGRAPH> {
    ^{wikiheading6} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 6);
    }
    ^{wikiheading5} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 5);
    }
    ^{wikiheading4} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 4);
    }
    ^{wikiheading3} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 3);
    }
    ^{wikiheading2} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 2);
    }
    ^{wikiheading1} {
        return this.parse(TAG_TYPE_WIKI_HEADING, yytext(), 1);
    }
}

/* ----- lists ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>^{listitem} {
    if (logger.isFinerEnabled()) logger.finer("listitem: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("p")) {
        popTag("p");
    }
    if (yystate() == PARAGRAPH) {
        endState();
    }
    if (yystate() != LIST) beginState(LIST);
    // one non-list character matched, roll it back
    yypushback(1);
    this.processListStack(yytext());
    return "";
}

<LIST>^{listend} {
    if (logger.isFinerEnabled()) logger.finer("listend: " + yytext() + " (" + yystate() + ")");
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
    if (logger.isFinerEnabled()) logger.finer("listdt: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("dt")) {
        // special case list of the form "; term : definition"
        this.popTag("dt");
        this.pushTag("dd", null);
        return "";
    }
    return yytext();
}

/* ----- wiki links ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{imagelinkcaption} {
    if (logger.isFinerEnabled()) logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{wikilink} {
    if (logger.isFinerEnabled()) logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    return this.parse(TAG_TYPE_WIKI_LINK, yytext());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmllinkraw} {
    return this.parse(TAG_TYPE_HTML_LINK, yytext());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmllinkwiki} {
    String raw = yytext();
    // strip the opening and closing brackets
    return this.parse(TAG_TYPE_HTML_LINK, raw.substring(1, raw.length() - 1));
}

/* ----- bold / italic ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{bold} {
    return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), "b");
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{bolditalic} {
    return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), (String)null);
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{italic} {
    return this.parse(TAG_TYPE_WIKI_BOLD_ITALIC, yytext(), "i");
}

/* ----- references ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{reference} {
    return this.parse(TAG_TYPE_WIKI_REFERENCE, yytext());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{referencenocontent} {
    return this.parse(TAG_TYPE_WIKI_REFERENCE, yytext());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{references} {
    logger.finer("references: " + yytext() + " (" + yystate() + ")");
    if (this.peekTag().getTagType().equals("p")) {
        // if a paragraph is already opened, close it
        this.popTag("p");
    }
    return this.parse(TAG_TYPE_WIKI_REFERENCES, yytext());
}

/* ----- html ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmlbr} {
    if (logger.isFinerEnabled()) logger.finer("htmlbr: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    // <br> may have attributes, so check for them
    HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(yytext());
    // FIXME - clean this up
    if (htmlTagItem == null) {
        return "";
    }
    int start = htmlTagItem.getHtml().indexOf(" ");
    if (start == -1) {
        return "<br />\n";
    }
    int end = htmlTagItem.getHtml().lastIndexOf("/>");
    if (end == -1) {
        end = htmlTagItem.getHtml().lastIndexOf(">");
    }
    String attributes = htmlTagItem.getHtml().substring(start, end).trim();
    return (attributes.length() > 0) ? "<br " + attributes + " />\n" : "<br />\n";
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmlparagraphopen} {
    if (logger.isFinerEnabled()) logger.finer("htmlparagraphopen: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    if (this.peekTag().getTagType().equals("p")) {
        // if a paragraph is already opened, close it before opening a new paragraph
        this.popTag("p");
    }
    this.pushTag("p", yytext());
    if (yystate() != PARAGRAPH) {
        beginState(PARAGRAPH);
    }
    return "";
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmlparagraphclose} {
    if (logger.isFinerEnabled()) logger.finer("htmlparagraphclose: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    if (this.peekTag().getTagType().equals("p")) {
        // only perform processing if a paragraph is open.  otherwise just suppress this tag.
        this.popTag("p");
    }
    if (yystate() == PARAGRAPH) {
        endState();
    }
    return "";
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmltagnocontent} {
    if (logger.isFinerEnabled()) logger.finer("htmltagnocontent: " + yytext() + " (" + yystate() + ")");
    HtmlTagItem tagItem = JFlexParserUtil.sanitizeHtmlTag(yytext());
    return ((tagItem == null) ? "" : tagItem.getHtml());
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmltagopen} {
    if (logger.isFinerEnabled()) logger.finer("htmltagopen: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    this.pushTag(null, yytext());
    return "";
}

<YYINITIAL, LIST, TABLE, PARAGRAPH>{htmltagclose} {
    if (logger.isFinerEnabled()) logger.finer("htmltagclose: " + yytext() + " (" + yystate() + ")");
    if (!allowHTML()) {
        return StringEscapeUtils.escapeHtml(yytext());
    }
    this.popTag(null, yytext());
    return "";
}

/* ----- javascript ----- */

<YYINITIAL, LIST, TABLE, PARAGRAPH>{javascript} {
    return this.parse(TAG_TYPE_JAVASCRIPT, yytext());
}

/* ----- other ----- */

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE, PARAGRAPH>{entity} {
    if (logger.isFinerEnabled()) logger.finer("entity: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    return (Utilities.isHtmlEntity(raw)) ? raw : StringEscapeUtils.escapeHtml(raw);
}

<YYINITIAL>^{emptyline} {
    // suppress superfluous empty lines
    return "";
}

<PARAGRAPH>{newline} {
    // convert newlines within paragraphs to spaces
    return " ";
}

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE, PARAGRAPH>{whitespace} {
    // no need to log this
    return yytext();
}

<YYINITIAL, WIKIPRE, PRE, LIST, TABLE, PARAGRAPH>. {
    // no need to log this
    return StringEscapeUtils.escapeHtml(yytext());
}
