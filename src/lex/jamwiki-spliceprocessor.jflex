/*
 * This class provides the capability to slice and splice an article to
 * insert or remove a section of text.  In this case a "section" is
 * defined as a body of text between two heading tags of the same level,
 * such as two &lt;h2&gt; tags.
 */
package org.jamwiki.parser;

import org.apache.log4j.Logger;
import org.jamwiki.Environment;

%%

%public
%class JAMWikiSpliceProcessor
%extends AbstractLexer
%type String
%unicode
%ignorecase

/* code included in the constructor */
%init{
    allowHtml = Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
    yybegin(NORMAL);
    states.add(new Integer(yystate()));
%init}

/* code called after parsing is completed */
%eofval{
    StringBuffer output = new StringBuffer();
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(JAMWikiSpliceProcessor.class.getName());
    protected boolean allowHtml = false;
    protected int section = 0;
    protected int sectionDepth = 0;
    protected int targetSection = 0;
    protected String replacementText = null;
    protected boolean inTargetSection = false;
    
    /**
     *
     */
    protected String processHeading(int level, String headingText) {
        this.section++;
        if (inTargetSection && this.sectionDepth >= level) {
            inTargetSection = false;
        } else if (this.targetSection == this.section) {
            inTargetSection = true;
            this.sectionDepth = level;
            if (this.parserInfo.getMode() == ParserInfo.MODE_SPLICE) return this.replacementText;
        }
        return returnText(headingText);
    }
    
    /**
     *
     */
    private String returnText(String text) {
        return (inTargetSection && this.parserInfo.getMode() == ParserInfo.MODE_SPLICE || !inTargetSection && this.parserInfo.getMode() == ParserInfo.MODE_SLICE) ? "" : text;
    }
    
    /**
     *
     */
    public void setParserInfo(ParserInfo parserInfo) throws Exception {
        this.parserInfo = parserInfo;
        // validate parser settings
        boolean validated = true;
        if (this.parserInfo == null) validated = false;
        if (this.parserInfo.getMode() != ParserInfo.MODE_SPLICE && this.parserInfo.getMode() != ParserInfo.MODE_SLICE) validated = false;
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
    
    /**
     *
     */
    public void setReplacementText(String replacementText) {
        // replacementText must end with a newline, otherwise sections get spliced together
        if (replacementText == null) return;
        if (!replacementText.endsWith("\n") && !replacementText.endsWith("\r")) {
            replacementText += "\r\n";
        }
        this.replacementText = replacementText;
    }
    
    /**
     *
     */
    public void setTargetSection(int targetSection) {
        this.targetSection = targetSection;
    }
%}

/* character expressions */
newline            = \r|\n|\r\n
whitespace         = {newline} | [ \t\f]

/* non-container expressions */
h1                 = "=" [^=\n]+ ~"="
h2                 = "==" [^=\n]+ ~"=="
h3                 = "===" [^=\n]+ ~"==="
h4                 = "====" [^=\n]+ ~"===="
h5                 = "=====" [^=\n]+ ~"====="

/* nowiki */
nowikistart        = (<[ ]*nowiki[ ]*>)
nowikiend          = (<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)

/* comments */
htmlcomment        = "<!--" ~"-->"

%state NORMAL, NOWIKI, PRE

%%

/* ----- parsing tags ----- */

<PRE, NORMAL>{nowikistart} {
    beginState(NOWIKI);
    return returnText(yytext());
}

<NOWIKI>{nowikiend} {
    endState();
    return returnText(yytext());
}

/* ----- nowiki ----- */

<NORMAL>{htmlprestart} {
    if (allowHtml) {
        beginState(PRE);
        return returnText(yytext());
    }
    return returnText("&lt;pre&gt;");
}

<PRE>{htmlpreend} {
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return returnText(yytext());
}

/* ----- comments ----- */

<NORMAL>{htmlcomment} {
    return returnText(yytext());
}

/* ----- headings ----- */

<NORMAL>^{h1} {
    return processHeading(1, yytext());
}

<NORMAL>^{h2} {
    return processHeading(2, yytext());
}

<NORMAL>^{h3} {
    return processHeading(3, yytext());
}

<NORMAL>^{h4} {
    return processHeading(4, yytext());
}

<NORMAL>^{h5} {
    return processHeading(5, yytext());
}

/* ----- default ----- */

<PRE, NOWIKI, NORMAL>{whitespace} {
    return returnText(yytext());
}

<PRE, NOWIKI, NORMAL>. {
    return returnText(yytext());
}
