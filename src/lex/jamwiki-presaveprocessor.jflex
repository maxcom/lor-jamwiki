/*
 * This class parses items such as signatures which should be converted
 * prior to saving an article.
 */
package org.jamwiki.parser;

import org.apache.log4j.Logger;

%%

%public
%class JAMWikiPreSaveProcessor
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
    return (output.length() == 0) ? null : output.toString();
%eofval}

/* code copied verbatim into the generated .java file */
%{
    protected static Logger logger = Logger.getLogger(JAMWikiPreSaveProcessor.class.getName());
    
    /**
     *
     */
    public void setParserInput(ParserInput parserInput) throws Exception {
        this.parserInput = parserInput;
        // validate parser settings
        boolean validated = true;
        if (this.parserInput == null) validated = false;
        if (this.parserInput.getContext() == null) validated = false;
        if (this.parserInput.getVirtualWiki() == null) validated = false;
        if (this.parserInput.getUserIpAddress() == null) validated = false;
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

/* javascript */
javascript         = (<[ ]*script[^>]*>) ~(<[ ]*\/[ ]*script[ ]*>)

/* processing commands */
wikisig3           = "~~~"
wikisig4           = "~~~~"
wikisig5           = "~~~~~"

%state NOWIKI, PRE, NORMAL

%%

/* ----- nowiki ----- */

<PRE, NORMAL>{nowikistart} {
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
    beginState(PRE);
    return yytext();
}

<PRE>{htmlpreend} {
    logger.debug("htmlpreend: " + yytext() + " (" + yystate() + ")");
    endState();
    return yytext();
}

/* ----- processing commands ----- */

<NORMAL>{wikisig3} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInput, true, false);
}

<NORMAL>{wikisig4} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInput, true, true);
}

<NORMAL>{wikisig5} {
    logger.debug("toc: " + yytext() + " (" + yystate() + ")");
    return ParserUtil.buildWikiSignature(this.parserInput, false, true);
}

/* ----- javascript ----- */

<NORMAL>{javascript} {
    logger.debug("javascript: " + yytext() + " (" + yystate() + ")");
    return yytext();
}

/* ----- other ----- */

<PRE, NOWIKI, NORMAL>{whitespace} {
    // no need to log this
    return yytext();
}

<PRE, NOWIKI, NORMAL>. {
    // no need to log this
    return yytext();
}
