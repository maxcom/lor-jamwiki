/*
 * This class parses items such as signatures which should be converted
 * prior to saving an article.
 */
package org.jamwiki.parser;

import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.LinkUtil;
import org.springframework.util.StringUtils;

%%

%public
%class JAMWikiPreSaveProcessor
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
    protected static Logger logger = Logger.getLogger(JAMWikiPreSaveProcessor.class.getName());
    protected boolean allowHtml = false;
    
    /**
     *
     */
    public void processLink(String raw) {
        String content = ParserUtil.extractLinkContent(raw);
        if (!StringUtils.hasText(content)) {
            // invalid link
            return;
        }
        String url = ParserUtil.extractLinkUrl(content);
        String topic = LinkUtil.extractLinkTopic(url);
        if (!StringUtils.hasText(topic)) {
            return;
        }
        if (topic.startsWith(WikiBase.NAMESPACE_CATEGORY)) {
            String sortKey = ParserUtil.extractLinkText(content);
            this.parserOutput.addCategory(topic, sortKey);
        }
        if (topic.startsWith(":") && topic.length() > 1) {
            // strip opening colon
            topic = topic.substring(1).trim();
        }
        if (StringUtils.hasText(topic)) {
            this.parserOutput.addLink(topic);
        }
    }
    
    /**
     *
     */
    public void setParserInput(ParserInput parserInput) throws Exception {
        this.parserInput = parserInput;
        // validate parser settings
        boolean validated = true;
        if (this.parserInput == null) validated = false;
        if (this.parserInput.getMode() != ParserInput.MODE_SEARCH) {
            if (this.parserInput.getContext() == null) validated = false;
            if (this.parserInput.getVirtualWiki() == null) validated = false;
            if (this.parserInput.getUserIpAddress() == null) validated = false;
        }
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

/* processing commands */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

/* wiki links */
wikilink           = "[[" [^\]\n\r]+ ~"]]"

%state NOWIKI, PRE, WIKIPRE, NORMAL

%%

/* ----- nowiki ----- */

<WIKIPRE, PRE, NORMAL>{nowikistart} {
    logger.debug("nowikistart: " + yytext() + " (" + yystate() + ")");
    beginState(NOWIKI);
    return yytext();
}

<NOWIKI>{nowikiend} {
    logger.debug("nowikiend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- pre ----- */

<NORMAL>{htmlprestart} {
    logger.debug("htmlprestart: " + yytext() + " (" + yystate() + ")");
    if (allowHtml) {
        beginState(PRE);
    }
    return yytext();
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    // state only changes to pre if allowHTML is true, so no need to check here
    endState();
    return yytext();
}

<NORMAL, WIKIPRE>^{wikiprestart} {
    logger.debug("wikiprestart: " + yytext() + " (" + yystate() + ")");
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    if (yystate() != WIKIPRE) {
        beginState(WIKIPRE);
    }
    return yytext();
}

<WIKIPRE>^{wikipreend} {
    logger.debug("wikipreend: " + yytext() + " (" + yystate() + ")");
    endState();
    // rollback the one non-pre character so it can be processed
    yypushback(1);
    return yytext();
}

/* ----- wiki links ----- */

<NORMAL>{wikilink} {
    logger.debug("wikilink: " + yytext() + " (" + yystate() + ")");
    this.processLink(yytext());
    return yytext();
}

/* ----- processing commands ----- */

<NORMAL>{wikisig3} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    if (parserInput.getMode() == ParserInput.MODE_SEARCH) {
        // called from search indexer, no need to parse signatures
        return yytext();
    }
    return ParserUtil.buildWikiSignature(this.parserInput, true, false);
}

<NORMAL>{wikisig4} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    if (parserInput.getMode() == ParserInput.MODE_SEARCH) {
        // called from search indexer, no need to parse signatures
        return yytext();
    }
    return ParserUtil.buildWikiSignature(this.parserInput, true, true);
}

<NORMAL>{wikisig5} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    if (parserInput.getMode() == ParserInput.MODE_SEARCH) {
        // called from search indexer, no need to parse signatures
        return yytext();
    }
    return ParserUtil.buildWikiSignature(this.parserInput, false, true);
}

/* ----- comments ----- */

<NORMAL>{htmlcomment} {
    logger.debug("htmlcomment: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- other ----- */

<WIKIPRE, PRE, NOWIKI, NORMAL>{whitespace} {
    // no need to log this
    return yytext();
}

<WIKIPRE, PRE, NOWIKI, NORMAL>. {
    // no need to log this
    return yytext();
}
