/*
 * This class implements the MediaWiki syntax (http://meta.wikimedia.org/wiki/Help:Editing).
 * It will also escape any HTML tags that have not been specifically allowed to be
 * present.
 */
package org.jamwiki.parser.jflex;

import org.apache.commons.lang.StringEscapeUtils;
import org.jamwiki.Environment;
import org.jamwiki.parser.TableOfContents;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

%%

%public
%class JAMWikiProcessor
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
    if (wikiitalic) {
        wikiitalic = false;
        output.append( "</i>" );
    }
    if (wikibold) {
        wikibold = false;
        output.append("</b>");
    }
    if (wikibolditalic) {
        wikibolditalic = false;
        output.append("</i></b>");
    }
    // close any open list tags
    if (yystate() == LIST) {
        WikiListTag parserTag = new WikiListTag();
        output.append(this.parseToken(null, parserTag));
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
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiProcessor.class.getName());
    protected boolean allowHTML = false;
    protected boolean allowJavascript = false;
    protected boolean wikibold = false;
    protected boolean wikibolditalic = false;
    protected boolean wikiitalic = false;
    
    /**
     *
     */
    protected boolean allowJavascript() {
        return (allowJavascript && yystate() != PRE && yystate() != WIKIPRE);
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
listitem           = [\*#\:;]+ [^\*#\:;\r\n]
listend            = [^\*#\:;\r\n]+ (.)+

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ")+ ([^ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* allowed html */
htmlkeyword        = br|b|big|blockquote|caption|center|cite|code|del|div|em|font|hr|i|ins|p|s|small|span|strike|strong|sub|sup|table|td|th|tr|tt|u|var
tableattributes    = align|bgcolor|border|cellpadding|cellspacing|class|colspan|height|nowrap|rowspan|style|valign|width
htmlattributes     = ({tableattributes}) | align|alt|background|bgcolor|border|class|clear|color|face|height|id|size|style|valign|width
htmlattribute      = ([ ]+) {htmlattributes} ([ ]*=[^>\n\r]+[ ]*)*
htmltag            = (<[ ]*[\/]?[ ]*) {htmlkeyword} ({htmlattribute})* ([ ]*[\/]?[ ]*>)

/* javascript */
jsopen             = (<[ ]*) script ({htmlattribute})* ([ ]*[\/]?[ ]*>)
jsclose            = (<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
notoc              = "__NOTOC__"
toc                = "__TOC__"
forcetoc           = "__FORCETOC__"

/* comments */
htmlcomment        = "<!--" ~"-->"

/* tables */
tableattribute     = ([ ]*) {tableattributes} ([ ]*=[^>\n\r\|]+[ ]*)*
tablestart         = "{|" {inputcharacter}* {newline}
tableend           = "|}"
tablecell          = "|" [^\+\-\}] | "|" ({tableattribute})+ "|" [^\|]
tablecells         = "||"
tablecellsstyle    = "||" ({tableattribute})+ "|" ([^|])
tableheading       = "!" | "!" ({tableattribute})+ "|" [^\|]
tableheadings      = "||" | "!!"
tablerow           = "|-" [ ]* ({tableattribute})* {newline}
tablecaption       = "|+"

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

%state NORMAL, TABLE, TD, TH, TC, LIST, PRE, JAVASCRIPT, WIKIPRE

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    WikiNowikiTag parserTag = new WikiNowikiTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- pre ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHTML) {
        beginState(PRE);
    }
    HtmlPreTag parserTag = new HtmlPreTag();
    return this.parseToken(yytext(), parserTag);
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    HtmlPreTag parserTag = new HtmlPreTag();
    return this.parseToken(yytext(), parserTag);
}

<NORMAL, LIST, TABLE, TD, TH, TC, WIKIPRE>^{wikiprestart} {
    logger.finer("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(yytext().length() - 1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
        return "<pre>";
    }
    return "";
}

<WIKIPRE>^{wikipreend} {
    logger.finer("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return  "</pre>\n";
}

/* ----- table of contents ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{notoc} {
    logger.finer("notoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_NO_TOC);
    return "";
}

<NORMAL, LIST, TABLE, TD, TH, TC>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setStatus(TableOfContents.STATUS_TOC_INITIALIZED);
    this.parserInput.getTableOfContents().setForceTOC(true);
    return yytext();
}

<NORMAL, LIST, TABLE, TD, TH, TC>{forcetoc} {
    logger.finer("forcetoc: " + yytext() + " (" + yystate() + ")");
    this.parserInput.getTableOfContents().setForceTOC(true);
    return "";
}

/* ----- wiki links ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{imagelinkcaption} {
    logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    WikiLinkTag parserTag = new WikiLinkTag();
    return this.parseToken(yytext(), parserTag);
}

<NORMAL, LIST, TABLE, TD, TH, TC>{wikilink} {
    logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    WikiLinkTag parserTag = new WikiLinkTag();
    return this.parseToken(yytext(), parserTag);
}

<NORMAL, LIST, TABLE, TD, TH, TC>{htmllink} {
    logger.finer("htmllink: " + yytext() + " (" + yystate() + ")");
    HtmlLinkTag parserTag = new HtmlLinkTag();
    return this.parseToken(yytext(), parserTag);
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
    HtmlCommentTag parserTag = new HtmlCommentTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- headings ----- */

<NORMAL>^{hr} {
    logger.finer("hr: " + yytext() + " (" + yystate() + ")");
    return "<hr />\n";
}

<NORMAL>^{wikiheading} {
    logger.finer("wikiheading: " + yytext() + " (" + yystate() + ")");
    WikiHeadingTag parserTag = new WikiHeadingTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- lists ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>^{listitem} {
    logger.finer("listitem: " + yytext() + " (" + yystate() + ")");
    if (yystate() != LIST) beginState(LIST);
    // one non-list character matched, roll it back
    yypushback(1);
    WikiListTag parserTag = new WikiListTag();
    return this.parseToken(yytext(), parserTag);
}

<LIST>^{listend} {
    logger.finer("listend: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    // roll back any matches to allow re-parsing
    yypushback(raw.length());
    endState();
    WikiListTag parserTag = new WikiListTag();
    return this.parseToken(null, parserTag);
}

/* ----- bold / italic ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{bold} {
    logger.finer("bold: " + yytext() + " (" + yystate() + ")");
    wikibold = !wikibold;
    return (wikibold) ? "<b>" : "</b>";
}

<NORMAL, LIST, TABLE, TD, TH, TC>{bolditalic} {
    logger.finer("bolditalic: " + yytext() + " (" + yystate() + ")");
    wikibolditalic = !wikibolditalic;
    return (wikibolditalic) ? "<b><i>" : "</i></b>";
}

<NORMAL, LIST, TABLE, TD, TH, TC>{italic} {
    logger.finer("italic: " + yytext() + " (" + yystate() + ")");
    wikiitalic = !wikiitalic;
    return (wikiitalic) ? "<i>" : "</i>";
}

/* ----- references ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{reference} {
    logger.finer("reference: " + yytext() + " (" + yystate() + ")");
    WikiReferenceTag parserTag = new WikiReferenceTag();
    return this.parseToken(yytext(), parserTag);
}

<NORMAL, LIST, TABLE, TD, TH, TC>{referencenocontent} {
    logger.finer("referencenocontent: " + yytext() + " (" + yystate() + ")");
    WikiReferenceTag parserTag = new WikiReferenceTag();
    return this.parseToken(yytext(), parserTag);
}

<NORMAL, LIST, TABLE, TD, TH, TC>{references} {
    logger.finer("references: " + yytext() + " (" + yystate() + ")");
    WikiReferencesTag parserTag = new WikiReferencesTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- html ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{htmltag} {
    logger.finer("htmltag: " + yytext() + " (" + yystate() + ")");
    HtmlTag parserTag = new HtmlTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- javascript ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{jsopen} {
    logger.finer("jsopen: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        beginState(JAVASCRIPT);
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return StringEscapeUtils.escapeHtml(yytext());
}

<JAVASCRIPT>{jsclose} {
    logger.finer("jsclose: " + yytext() + " (" + yystate() + ")");
    if (allowJavascript()) {
        endState();
        return ParserUtil.sanitizeHtmlTag(yytext());
    }
    return StringEscapeUtils.escapeHtml(yytext());
}

/* ----- other ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC>{entity} {
    logger.finer("entity: " + yytext() + " (" + yystate() + ")");
    CharacterTag parserTag = new CharacterTag();
    return this.parseToken(yytext(), parserTag);
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT>{whitespace} {
    // no need to log this
    CharacterTag parserTag = new CharacterTag();
    return this.parseToken(yytext(), parserTag);
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT>. {
    // no need to log this
    CharacterTag parserTag = new CharacterTag();
    return this.parseToken(yytext(), parserTag);
}
