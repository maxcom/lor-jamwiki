/*
 * This class adds paragraph tags as appropriate.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiPostProcessor
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
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiPostProcessor.class.getName());
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]
inputcharacter     = ([^ \n\r\t])

/* nowiki */
nowiki             = (<[ ]*nowiki[ ]*>) ~(<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
toc                = "__TOC__"

/* references */
references         = (<[ ]*) "references" ([ ]*[\/]?[ ]*>)

/* paragraph */
noparagraph        = "<hr" ~">"
specialstart       = (((<[ ]*) td ([^/>]*>)) ([^\n])+ ([\n])? ((<[ ]*\/[ ]*) td ([ ]*>)))
specialend         = ((<[ ]*\/[ ]*) td ([ ]*>))
noninlinetag       = dl|div|h1|h2|h3|h4|h5|ol|p|table|ul
noninlinetagstart  = ((<[ ]*) {noninlinetag} ([^/>]*>))
noninlinetagend    = ((<[ ]*\/[ ]*) {noninlinetag} ([ ]*>))
emptyline          = ({newline} {newline})
nonparagraphstart  = ({noninlinetagstart}) | ((<[ ]*\/[ ]*) td ([ ]*>))
nonparagraphend    = ({noninlinetagend}) | ((<[ ]*) td ([^/>]*>))
anchorname         = (<[ ]*a[ ]*name[ ]*=[^/]+\/[ ]*[a]?[ ]*>)
break              = (<[ ]*) br ([ ]*[\/]?[ ]*>)
paragraphend       = ({newline})
paragraphstart     = ({inputcharacter})

%state PRE, NORMAL, P, NONPARAGRAPH, SPECIAL

%%

/* ----- nowiki ----- */

<PRE, NORMAL, P, NONPARAGRAPH, SPECIAL>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    return JFlexParserUtil.tagContent(yytext());
}

/* ----- pre ----- */

<NORMAL, P, NONPARAGRAPH, SPECIAL>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<NORMAL, P, NONPARAGRAPH, SPECIAL>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInput.getTableOfContents().attemptTOCInsertion();
}

/* ----- references ----- */

<NORMAL, P, NONPARAGRAPH, SPECIAL>{references} {
    logger.finer("references: " + yytext() + " (" + yystate() + ")");
    WikiReferencesTag parserTag = new WikiReferencesTag();
    return parserTag.parse(this.parserInput, this.mode, yytext());
}

/* ----- javascript ----- */

<NORMAL, P, NONPARAGRAPH, SPECIAL>{javascript} {
    logger.finer("javascript: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- layout ----- */

<NORMAL, NONPARAGRAPH, SPECIAL>{noparagraph} {
    // <hr> should be ignored for the sake of paragraph parsing
    logger.finer("noparagraph: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, NONPARAGRAPH>{specialstart} {
    // <td> tags _with no newlines_ should be ignored for the sake of paragraph parsing
    logger.finer("specialstart: " + yytext() + " (" + yystate() + ")");
    beginState(SPECIAL);
    // push back to the start of the opening td tag
    String raw = yytext();
    int pos = raw.indexOf(">") + 1;
    yypushback(raw.length() - pos);
    return yytext();
}

<SPECIAL>{specialend} {
    logger.finer("specialend: " + yytext() + " (" + yystate() + ")");
    if (yystate() != SPECIAL) {
        logger.warning("Ending SPECIAL state while current state is not SPECIAL");
    }
    endState();
    return yytext();
}

<NORMAL, P>^{emptyline} {
    logger.finer("emptyline: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        this.popTag("p");
        endState();
    }
    return output.toString() + "\n<p><br /></p>";
}

<NORMAL, P, NONPARAGRAPH, SPECIAL>{anchorname} {
    // for layout purposes and <a name="foo"></a> link should be returned without
    // changes, but should not affect paragraph layout in any way.
    logger.finer("anchorname: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, P, NONPARAGRAPH, SPECIAL>{break} {
    // for layout purposes <br> tags should not affect paragraph layout in any way.
    logger.finer("break: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, P, NONPARAGRAPH, SPECIAL>{nonparagraphstart} {
    logger.finer("nonparagraphstart: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        this.popTag("p");
        endState();
    }
    beginState(NONPARAGRAPH);
    return output.toString() + yytext();
}

<NORMAL, P, NONPARAGRAPH, SPECIAL>{nonparagraphend} {
    logger.finer("nonparagraphend: " + yytext() + " (" + yystate() + ")");
    if (yystate() == NONPARAGRAPH) {
        endState();
    } else {
        logger.warning("Attempt to end nonparagraph state while state is not nonparagraph for text: " + yytext());
    }
    return yytext();
}

<NORMAL>{paragraphstart} {
    logger.finer("paragraphstart: " + yytext() + " (" + yystate() + ")");
    beginState(P);
    this.pushTag("p", null);
    return yytext();
}

<P>^{paragraphend} {
    logger.finer("end of paragraph: " + yytext() + " (" + yystate() + ")");
    endState();
    this.popTag("p");
    return "";
}

/* ----- other ----- */

<PRE, NORMAL, NONPARAGRAPH, P, SPECIAL>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NORMAL, NONPARAGRAPH, P, SPECIAL>. {
    // no need to log this
    return yytext();
}
