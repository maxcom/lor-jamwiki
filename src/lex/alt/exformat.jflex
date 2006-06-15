package org.vqwiki.lex.alt;


/*
Very Quick Wiki - WikiWikiWeb clone
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
import org.vqwiki.WikiBase;

%%

%public
%type String
%unicode
%implements org.vqwiki.lex.Lexer
%class ExFormatLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
	return convert.onEOF();
%eofval}

%{
	
	protected ExFormatLexConvert convert = new ExFormatLexConvert();
	protected static Logger cat = Logger.getLogger( ExFormatLex.class );

  public void setVirtualWiki( String vWiki ){
    convert.setVirtualWiki(vWiki);
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
  return "\\__";
}

<NORMAL, PRE>{noformat}	{
  yybegin( OFF );
  return "__";
}

<OFF>{noformat} {
  yybegin( NORMAL );
  return "__";
}

<NORMAL, PRE>{externalstart} {
  yybegin( EXTERNAL );
  return yytext();
}

<EXTERNAL>{externalend} {
  yybegin( NORMAL );
  return yytext();
}

<NORMAL>{nbsp} {
  return convert.onNbsp();
}

<NORMAL>^\!\!\![^\n]+\!\!\!([\t\ ]*){newline} {
  return convert.onHeadlineOne(yytext());
}

<NORMAL>^\!\![^\n]+\!\!([\t\ ]*){newline} {
  return convert.onHeadlineTwo(yytext());
}

<NORMAL>^\![^\n]+\!([\t\ ]*){newline} {
  return convert.onHeadlineThree(yytext());
}

<NORMAL>"'''" {
  return convert.onBold(yytext());
}


<NORMAL>"''"	{
  return convert.onItalic(yytext());
}

<NORMAL>"::"	{
  return convert.onCenter(yytext());
}

<NORMAL>"==="	{
  return convert.onUnderline(yytext());
}

<NORMAL>"{{{" {
  return convert.onCodeBegin();
}

<NORMAL>"}}}" {
  return convert.onCodeEnd();
}

<NORMAL>"@@" {
  return convert.onNewline();
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
  return convert.onEndOfLine(yytext());
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
  return convert.onColorStart(yytext());
}

<NORMAL>{colorend} {
  return convert.onColorEnd(yytext());
}
