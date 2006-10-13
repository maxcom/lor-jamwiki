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
 *   Templates
 *
 * Not yet implemented:
 *
 *   <math>
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
            String value = wikiListTag.parse(this.parserInput, this.parserDocument, this.mode, null);
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
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiProcessor.class.getName());
    protected boolean allowHTML = false;
    protected boolean allowJavascript = false;
    protected boolean wikibold = false;
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
     *
     */
    public void init(ParserInput parserInput, ParserDocument parserDocument, int mode) throws Exception {
        this.parserInput = parserInput;
        this.parserDocument = parserDocument;
        this.mode = mode;
        // validate parser settings
        boolean validated = true;
        if (this.mode != JFlexParser.MODE_PROCESS) validated = false;
        if (this.parserInput.getTableOfContents() == null) validated = false;
        if (this.parserInput.getTopicName() == null) validated = false;
        if (this.parserInput.getContext() == null) validated = false;
        if (this.parserInput.getVirtualWiki() == null) validated = false;
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
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]
inputcharacter     = [^\r\n]
entity             = (&#([0-9]{2,4});) | (&[A-Za-z]{3,6};)

/* non-container expressions */
hr                 = "----"
wikiheading        = [\=]+ [^\n\=]* [\=]+
bold               = "'''"
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

%state NORMAL, TABLE, TD, TH, TC, LIST, PRE, JAVASCRIPT, WIKIPRE

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiNowikiTag wikiNowikiTag = new WikiNowikiTag();
        String value = wikiNowikiTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
}

/* ----- pre ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHTML) {
        beginState(PRE);
    }
    String raw = yytext();
    try {
        HtmlPreTag htmlPreTag = new HtmlPreTag();
        String value = htmlPreTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    String raw = yytext();
    try {
        HtmlPreTag htmlPreTag = new HtmlPreTag();
        String value = htmlPreTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
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

/* ----- wiki links ----- */

<NORMAL, LIST, TABLE, TD, TH, TC>{imagelinkcaption} {
    logger.finer("imagelinkcaption: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC>{wikilink} {
    logger.finer("wikilink: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        WikiLinkTag wikiLinkTag = new WikiLinkTag();
        String value = wikiLinkTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
}

<NORMAL, LIST, TABLE, TD, TH, TC>{htmllink} {
    logger.finer("htmllink: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        HtmlLinkTag htmlLinkTag = new HtmlLinkTag();
        String value = htmlLinkTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
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
    String raw = yytext();
    try {
        HtmlCommentTag htmlCommentTag = new HtmlCommentTag();
        String value = htmlCommentTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        return raw;
    }
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
        String value = wikiHeadingTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
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
        String value = wikiListTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
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
        String value = wikiListTag.parse(this.parserInput, this.parserDocument, this.mode, null);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
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
        String value = htmlTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
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

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC>{entity} {
    logger.finer("entity: " + yytext() + " (" + yystate() + ")");
    String raw = yytext();
    try {
        CharacterTag characterTag = new CharacterTag();
        String value = characterTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        // FIXME - what to return here?
        return "";
    }
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT>{whitespace} {
    // no need to log this
    String raw = yytext();
    try {
        CharacterTag characterTag = new CharacterTag();
        String value = characterTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        // FIXME - what to return here?
        return "";
    }
}

<WIKIPRE, PRE, NORMAL, LIST, TABLE, TD, TH, TC, JAVASCRIPT>. {
    // no need to log this
    String raw = yytext();
    try {
        CharacterTag characterTag = new CharacterTag();
        String value = characterTag.parse(this.parserInput, this.parserDocument, this.mode, raw);
        return value;
    } catch (Exception e) {
        logger.info("Unable to parse " + raw, e);
        // FIXME - what to return here?
        return "";
    }
}
