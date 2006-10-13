/*
 * This class adds paragraph tags as appropriate.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiPostProcessor
%extends AbstractLexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        endState();
        output.append("</p>");
    }
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static WikiLogger logger = WikiLogger.getLogger(JAMWikiPostProcessor.class.getName());
    
    /**
     *
     */
    public void init(ParserInput parserInput, ParserDocument parserDocument, int mode) throws Exception {
        this.parserInput = parserInput;
        this.parserDocument = parserDocument;
        this.mode = mode;
        // validate parser settings
        boolean validated = true;
        if (this.mode != JFlexParser.MODE_LAYOUT) validated = false;
        if (this.parserInput == null) validated = false;
        if (this.parserInput.getTableOfContents() == null) validated = false;
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
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


/* paragraph */
noparagraph        = (((<[ ]*) td ([^/>]*>)) ([^\n])+ ([\n])? ((<[ ]*\/[ ]*) td ([ ]*>))) | "<" [ ]* "hr" ~">"
emptyline          = ({newline} {newline} {newline})
nonparagraphtag    = table|div|h1|h2|h3|h4|h5|ul|dl|ol|span|p
nonparagraphstart  = ((<[ ]*) {nonparagraphtag} ([^/>]*>)) | ((<[ ]*\/[ ]*) td ([ ]*>))
nonparagraphend    = ((<[ ]*\/[ ]*) {nonparagraphtag} ([ ]*>)) | ((<[ ]*) td ([^/>]*>))
anchorname         = (<[ ]*a[ ]*name[ ]*=[^/]+\/[ ]*[a]?[ ]*>)
break              = (<[ ]*) br ([ ]*[\/]?[ ]*>)
paragraphend       = ({newline} {newline})
paragraphstart     = ({inputcharacter})

%state PRE, NORMAL, P, NONPARAGRAPH

%%

/* ----- nowiki ----- */

<PRE, NORMAL, P, NONPARAGRAPH>{nowiki} {
    logger.finer("nowiki: " + yytext() + " (" + yystate() + ")");
    WikiNowikiTag parserTag = new WikiNowikiTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- pre ----- */

<NORMAL, P, NONPARAGRAPH>{htmlprestart} {
    logger.finer("htmlprestart: " + yytext() + " (" + yystate() + ")");
    beginState(PRE);
    HtmlPreTag parserTag = new HtmlPreTag();
    return this.parseToken(yytext(), parserTag);
}

<PRE>{htmlpreend} {
    logger.finer("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    HtmlPreTag parserTag = new HtmlPreTag();
    return this.parseToken(yytext(), parserTag);
}

/* ----- processing commands ----- */

<NORMAL, P, NONPARAGRAPH>{toc} {
    logger.finer("toc: " + yytext() + " (" + yystate() + ")");
    return this.parserInput.getTableOfContents().attemptTOCInsertion();
}

/* ----- javascript ----- */

<NORMAL, P, NONPARAGRAPH>{javascript} {
    logger.finer("javascript: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- layout ----- */

<NORMAL, NONPARAGRAPH>{noparagraph} {
    // <hr> and <td> tags _with no newlines_ should be ignored for the sake of paragraph parsing
    logger.finer("noparagraph: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, P>{emptyline} {
    logger.finer("emptyline: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        output.append("</p>");
        endState();
    }
    return output.toString() + "\n<p><br /></p>";
}

<NORMAL, P, NONPARAGRAPH>{anchorname} {
    // for layout purposes and <a name="foo"></a> link should be returned without
    // changes, but should not affect paragraph layout in any way.
    logger.finer("anchorname: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, P, NONPARAGRAPH>{break} {
    // for layout purposes <br> tags should not affect paragraph layout in any way.
    logger.finer("break: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

<NORMAL, P, NONPARAGRAPH>{nonparagraphstart} {
    logger.finer("nonparagraphstart: " + yytext() + " (" + yystate() + ")");
    StringBuffer output = new StringBuffer();
    if (yystate() == P) {
        output.append("</p>");
        endState();
    }
    beginState(NONPARAGRAPH);
    return output.toString() + yytext();
}

<NORMAL, P, NONPARAGRAPH>{nonparagraphend} {
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
    return "<p>" + yytext();
}

<P>{paragraphend} {
    logger.finer("end of paragraph: " + yytext() + " (" + yystate() + ")");
    endState();
    return "</p>" + yytext();
}

/* ----- other ----- */

<PRE, NORMAL, NONPARAGRAPH, P>{whitespace} {
    // no need to log this
    CharacterTag parserTag = new CharacterTag();
    return this.parseToken(yytext(), parserTag);
}

<PRE, NORMAL, NONPARAGRAPH, P>. {
    // no need to log this
    CharacterTag parserTag = new CharacterTag();
    return this.parseToken(yytext(), parserTag);
}
