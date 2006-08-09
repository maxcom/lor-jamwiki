/*
 *
 */
package org.jamwiki.parser;

import java.util.Stack;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

%%

%public
%class JAMWikiSearchIndexer
%implements org.jamwiki.parser.Lexer
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
    protected static Logger logger = Logger.getLogger(JAMWikiPreProcessor.class.getName());
    protected boolean allowHtml = false;
    protected ParserInfo parserInfo = null;;
    /** Member variable used to keep track of the state history for the lexer. */
    protected Stack states = new Stack();
    protected Vector topicLinks = new Vector();

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
    protected Vector getTopicLinks() {
        return this.topicLinks;
    }
    
    /**
     *
     */
    public void processLink(String raw) {
        String content = ParserUtil.extractLinkContent(raw);
        if (!StringUtils.hasText(content)) {
            // invalid link
            return;
        }
        String topic = ParserUtil.extractLinkTopic(content);
        if (StringUtils.hasText(topic)) {
            this.topicLinks.add(topic);
        }
    }
    
    /**
     *
     */
    public void setParserInfo(ParserInfo parserInfo) throws Exception {
        this.parserInfo = parserInfo;
        // validate parser settings
        boolean validated = true;
        if (this.parserInfo == null) validated = false;
        if (!validated) {
            throw new Exception("Parser info not properly initialized");
        }
    }
%}

/* character expressions */
newline            = ((\r\n) | (\n))
whitespace         = {newline} | [ \t\f]

/* nowiki */
nowikistart        = (<[ ]*nowiki[ ]*>)
nowikiend          = (<[ ]*\/[ ]*nowiki[ ]*>)

/* pre */
htmlprestart       = (<[ ]*pre[ ]*>)
htmlpreend         = (<[ ]*\/[ ]*pre[ ]*>)
wikiprestart       = (" ") ([^ \t\r\n])
wikipreend         = ([^ ]) | ({newline})

/* comments */
htmlcomment        = "<!--" ~"-->"

/* wiki links */
wikilink           = "[[" [^(\]\])\n\r]+ ~"]]"

%state NORMAL, NOWIKI, PRE, WIKIPRE

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return "";
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return "";
}

/* ----- pre ----- */

<NORMAL>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHtml) {
        beginState(PRE);
    }
    return "";
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return "";
}

<NORMAL, WIKIPRE>^{wikiprestart} {
    logger.debug("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
    }
    return "";
}

<WIKIPRE>^{wikipreend} {
    logger.debug("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return "";
}

/* ----- wiki links ----- */

<NORMAL>{wikilink} {
    logger.debug("wikilink: " + yytext() + " (" + yystate() + ")");
    this.processLink(yytext());
    return "";
}

/* ----- comments ----- */

<NORMAL>{htmlcomment} {
    logger.debug("htmlcomment: " + yytext() + " (" + yystate() + ")");
    // remove comment
    return "";
}

/* ----- other ----- */

<WIKIPRE, PRE, NOWIKI, NORMAL>{whitespace} {
    // no need to log this
    return "";
}

<WIKIPRE, PRE, NOWIKI, NORMAL>. {
    // no need to log this
    return "";
}
