/*
 * This class provides the capability to slice and splice an article to
 * insert or remove a section of text.  In this case a "section" is
 * defined as a body of text between two heading tags of the same level,
 * such as two &lt;h2&gt; tags.
 */
package org.jamwiki.parser.jflex;

import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiSpliceProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiSpliceProcessor.class.getName());
    protected int section = 0;
    protected int sectionDepth = 0;
    protected int targetSection = 0;
    protected String replacementText = null;
    protected boolean inTargetSection = false;
    
    /**
     *
     */
    protected String processHeading(int level, String headingText, int tagType) {
        this.section++;
        if (inTargetSection && this.sectionDepth >= level) {
            inTargetSection = false;
        } else if (this.targetSection == this.section) {
            this.parse(tagType, headingText, level);
            inTargetSection = true;
            this.sectionDepth = level;
            if (this.mode == JFlexParser.MODE_SPLICE) return this.replacementText;
        }
        return returnText(headingText);
    }
    
    /**
     *
     */
    private String returnText(String text) {
        return ((inTargetSection && this.mode == JFlexParser.MODE_SPLICE) || (!inTargetSection && this.mode == JFlexParser.MODE_SLICE)) ? "" : text;
    }
    
    /**
     *
     */
    public void setReplacementText(String replacementText) {
        // replacementText must end with a newline, otherwise sections get spliced together
        if (replacementText == null) return;
        if (!replacementText.endsWith("\n")) {
            replacementText += "\n";
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
newline            = "\n"
whitespace         = {newline} | [ \t\f]

/* html attributes */
attributeValueInQuotes = "\"" ~"\""
attributeValueInSingleQuotes = "'" ~"'"
attributeValueNoQuotes = [^>\n]+
htmlattribute      = ([ \t]+) [a-zA-Z:]+ ([ \t]*=[ \t]*({attributeValueInQuotes}|{attributeValueInSingleQuotes}|{attributeValueNoQuotes}))*

/* non-container expressions */
wikiheading1       = "=" [^=\n]+ ~"="
wikiheading2       = "==" [^=\n]+ ~"=="
wikiheading3       = "===" [^=\n]+ ~"==="
wikiheading4       = "====" [^=\n]+ ~"===="
wikiheading5       = "=====" [^=\n]+ ~"====="
wikiheading6       = "======" [^=\n]+ ~"======"
h1                 = (<[ \t]*h1 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h1[ \t]*>)
h2                 = (<[ \t]*h2 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h2[ \t]*>)
h3                 = (<[ \t]*h3 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h3[ \t]*>)
h4                 = (<[ \t]*h4 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h4[ \t]*>)
h5                 = (<[ \t]*h5 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h5[ \t]*>)
h6                 = (<[ \t]*h6 ({htmlattribute})* [ \t]*>) ~(<[ \t]*\/[ \t]*h6[ \t]*>)

/* html headings */
nowiki             = (<[ \t]*nowiki[ \t]*>) ~(<[ \t]*\/[ \t]*nowiki[ \t]*>)

/* nowiki */
nowiki             = (<[ \t]*nowiki[ \t]*>) ~(<[ \t]*\/[ \t]*nowiki[ \t]*>)

/* pre */
htmlprestart       = (<[ \t]*pre ({htmlattribute})* [ \t]* (\/)? [ \t]*>)
htmlpreend         = (<[ \t]*\/[ \t]*pre[ \t]*>)

/* comments */
htmlcomment        = "<!--" ~"-->"

%state PRE

%%

/* ----- parsing tags ----- */

<YYINITIAL, PRE>{nowiki} {
    if (logger.isTraceEnabled()) logger.trace("nowiki: " + yytext() + " (" + yystate() + ")");
    return returnText(yytext());
}

/* ----- nowiki ----- */

<YYINITIAL>{htmlprestart} {
    if (allowHTML()) {
        beginState(PRE);
    }
    return returnText(yytext());
}

<PRE>{htmlpreend} {
    // state only changes to pre if allowHTML() is true, so no need to check here
    endState();
    return returnText(yytext());
}

/* ----- comments ----- */

<YYINITIAL>{htmlcomment} {
    return returnText(yytext());
}

/* ----- headings ----- */

<YYINITIAL> {
    ^{wikiheading1} {
        return processHeading(1, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading2} {
        return processHeading(2, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading3} {
        return processHeading(3, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading4} {
        return processHeading(4, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading5} {
        return processHeading(5, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    ^{wikiheading6} {
        return processHeading(6, yytext(), TAG_TYPE_WIKI_HEADING);
    }
    {h1} {
        return processHeading(1, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h2} {
        return processHeading(2, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h3} {
        return processHeading(3, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h4} {
        return processHeading(4, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h5} {
        return processHeading(5, yytext(), TAG_TYPE_HTML_HEADING);
    }
    {h6} {
        return processHeading(6, yytext(), TAG_TYPE_HTML_HEADING);
    }
}

/* ----- default ----- */

<YYINITIAL, PRE>{whitespace} {
    return returnText(yytext());
}

<YYINITIAL, PRE>. {
    return returnText(yytext());
}
