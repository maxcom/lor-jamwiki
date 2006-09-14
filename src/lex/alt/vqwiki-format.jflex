/**
 *
 */
package org.jamwiki.parser.alt;

import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInput;

%%

%public
%type String
%unicode
%extends AbstractLexer
%class VQWikiFormatLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
	if( strong ){
	  strong = false;
	  return( "</strong>" );
	}
	if( em ){
	  em = false;
	  return( "</em>" );
	}
	return null;
%eofval}

%{
	protected boolean em, strong, underline, center, table, row, cell, allowHtml, code, h1, h2, h3, color;
	protected int listLevel;
	protected boolean ordered;
	protected static WikiLogger logger = WikiLogger.getLogger( VQWikiFormatLex.class.getName() );
	
	/**
	 *
	 */
	protected boolean exists(String topic) {
		try {
			return WikiBase.exists(this.parserInput.getVirtualWiki(), topic);
		} catch (Exception err) {
			logger.severe("Error while looking up topic " + topic, err);
		}
		return false;
	}
	
	/**
	 *
	 */
	public void setParserInput(ParserInput parserInput) throws Exception {
		this.parserInput = parserInput;
	}

%}

newline	= (\n|\r\n)
whitespace = ([\t\ \r\n])
noformat	=	(__)
externalstart = (\[<[:letter:]+>\])
externalend = (\[<\/[:letter:]+>\])
entity = (&#[:digit:]+;)
colorstart = (\%([a-zA-Z]+|\#[0-9a-fA-F]{6})\%)
colorend = (\%\%)
nbsp=_&

%state NORMAL, OFF, PRE, EXTERNAL

%%

<NORMAL, PRE>\\{noformat}	{
  logger.fine( "escaped double backslash" );
  return "\\__";
}

<NORMAL, PRE>{noformat}	{
  logger.fine( "Format off" );
  yybegin( OFF );
  return "__";
}

<OFF>{noformat} {
  logger.fine( "Format on" );
  yybegin( NORMAL );
  return "__";
}

<NORMAL, PRE>{externalstart} {
  logger.fine( "external" );
  yybegin( EXTERNAL );
  return yytext();
}

<NORMAL>{nbsp} {
  return "&nbsp;";
}

<EXTERNAL>{externalend} {
  logger.fine( "external end");
  yybegin( NORMAL );
  return yytext();
}

<NORMAL>^\!\!\![^\n]+\!\!\!{newline} {
  logger.fine("!!!...!!!");
  return "<h1>" + yytext().substring(3, yytext().substring(3).indexOf('!')+3) + "</h1>";
}

<NORMAL>^\!\![^\n]+\!\!{newline} {
  logger.fine("!!...!!");
  return "<h2>" + yytext().substring(2, yytext().substring(2).indexOf('!')+2) + "</h2>";
}

<NORMAL>^\![^\n]+\!{newline} {
  logger.fine("!...!");
  return "<h3>" + yytext().substring(1,yytext().substring(1).indexOf('!')+1) + "</h3>";
}

<NORMAL>"'''" {
  logger.fine( "'''" );
  if( strong ){
    strong = false;
    return( "</strong>" );
  }
  else{
    strong = true;
    return( "<strong>" );
  }
}


<NORMAL>"''"	{
  logger.fine( "''" );
  if( em ){
    em = false;
    return( "</em>" );
  }
  else{
    em = true;
    return( "<em>" );
  }
}

<NORMAL>"::"	{
  logger.fine( "::" );
  if( center ){
    center = false;
    return( "</div>" );
  }
  else{
    center = true;
    return( "<div align=\"center\">" );
  }
}

<NORMAL>"==="	{
  logger.fine( "===" );
  if( underline ){
    underline = false;
    return( "</u>" );
  }
  else{
    underline = true;
    return( "<u>" );
  }
}

<NORMAL>"{{{" {
  return "<code>";
}

<NORMAL>"}}}" {
  return "</code>";
}

<NORMAL>"@@" {
  return "<br />";
}

<NORMAL, OFF, PRE>{entity}  {
  return yytext();
}
<NORMAL, OFF, PRE>"&lt;"  {
  return "&amp;lt;";
}
<NORMAL, OFF, PRE>"&gt;" {
  return "&amp;gt;";
}
<NORMAL, OFF, PRE>"&amp;"	{
  return "&amp;amp;";
}
<NORMAL, OFF, PRE>"<"	{
  return "&lt;";
}
<NORMAL, OFF, PRE>">" {
  return "&gt;";
}
<NORMAL, OFF, PRE>"&"	{
  return "&amp;";
}

<PRE>{newline}{newline} {
  logger.fine( "{newline}x2 leaving pre" );
	yybegin( NORMAL );
  return yytext();
}

<NORMAL, OFF>{newline} {
  logger.fine( "{newline}" );
  if( h1 ){
    h1 = false;
    return( "</h1>" );
  }
  if( h2 ){
    h2 = false;
    return( "</h2>" );
  }
  if( h3 ){
    h3 = false;
    return( "</h3>" );
  }
  return yytext();
}

<NORMAL>(@@@@{newline}) {
  logger.fine( "@@@@{newline} entering PRE" );
  yybegin( PRE );
  return yytext();
}

<NORMAL, OFF, EXTERNAL>{whitespace} {
  logger.fine( "{whitespace}" );
  return yytext();
}

<PRE>{whitespace} {
  logger.fine( "PRE {whitespace}" );
  return yytext();
}

<NORMAL, PRE, OFF, EXTERNAL>. {
  logger.fine( ". (" + yytext() + ")" );
  return yytext();
}

<NORMAL>{colorstart} {
  logger.fine( "color start" );
  
  StringBuffer sb = new StringBuffer() ;
  if( color ){
    sb.append( "</font>" );
  }  
  color = true;
  sb.append( "<font color=\"")
    .append( yytext().substring( 1,yytext().length()-1) )
    .append( "\">" );
  
  return sb.toString();
}

<NORMAL>{colorend} {
  if( color ){
    logger.fine( "color end" );
  
    color = false ;
    return( "</font>" );
  }
  else {
    return yytext();
  }
  
}
