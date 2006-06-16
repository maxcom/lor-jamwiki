package org.jmwiki.parser.alt;


/*
Java MediaWiki - WikiWikiWeb clone
Copyright (C) 2001-2003 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import org.apache.log4j.Logger;
import org.jmwiki.WikiBase;

%%

%public
%type String
%unicode
%implements org.jmwiki.parser.Lexer
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
	protected static Logger cat = Logger.getLogger( VQWikiFormatLex.class );

  protected String virtualWiki;

	protected boolean exists( String topic ){
	  try{
	    return WikiBase.getInstance().exists( virtualWiki, topic );
	  }catch( Exception err ){
	    cat.error( err );
	  }
	  return false;
	}

  public void setVirtualWiki( String vWiki ){
    this.virtualWiki = vWiki;
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
  cat.debug( "escaped double backslash" );
  return "\\__";
}

<NORMAL, PRE>{noformat}	{
  cat.debug( "Format off" );
  yybegin( OFF );
  return "__";
}

<OFF>{noformat} {
  cat.debug( "Format on" );
  yybegin( NORMAL );
  return "__";
}

<NORMAL, PRE>{externalstart} {
  cat.debug( "external" );
  yybegin( EXTERNAL );
  return yytext();
}

<NORMAL>{nbsp} {
  return "&nbsp;";
}

<EXTERNAL>{externalend} {
  cat.debug( "external end");
  yybegin( NORMAL );
  return yytext();
}

<NORMAL>^\!\!\![^\n]+\!\!\!{newline} {
  cat.debug("!!!...!!!");
  return "<h1>" + yytext().substring(3, yytext().substring(3).indexOf('!')+3) + "</h1>";
}

<NORMAL>^\!\![^\n]+\!\!{newline} {
  cat.debug("!!...!!");
  return "<h2>" + yytext().substring(2, yytext().substring(2).indexOf('!')+2) + "</h2>";
}

<NORMAL>^\![^\n]+\!{newline} {
  cat.debug("!...!");
  return "<h3>" + yytext().substring(1,yytext().substring(1).indexOf('!')+1) + "</h3>";
}

<NORMAL>"'''" {
  cat.debug( "'''" );
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
  cat.debug( "''" );
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
  cat.debug( "::" );
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
  cat.debug( "===" );
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
  return "<br>";
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
  cat.debug( "{newline}x2 leaving pre" );
	yybegin( NORMAL );
  return yytext();
}

<NORMAL, OFF>{newline} {
  cat.debug( "{newline}" );
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
  cat.debug( "@@@@{newline} entering PRE" );
  yybegin( PRE );
  return yytext();
}

<NORMAL, OFF, EXTERNAL>{whitespace} {
  cat.debug( "{whitespace}" );
  return yytext();
}

<PRE>{whitespace} {
  cat.debug( "PRE {whitespace}" );
  return yytext();
}

<NORMAL, PRE, OFF, EXTERNAL>. {
  cat.debug( ". (" + yytext() + ")" );
  return yytext();
}

<NORMAL>{colorstart} {
  cat.debug( "color start" );
  
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
    cat.debug( "color end" );
  
    color = false ;
    return( "</font>" );
  }
  else {
    return yytext();
  }
  
}
