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

import java.io.*;
import org.vqwiki.lex.Lexer;

%%

%public
%type String
%unicode
%implements org.vqwiki.lex.Lexer
%class ExLayoutLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
    if( yy_lexical_state == LIST ){
      yybegin( NORMAL );
      return convert.onListEOF();
    }
    if( yy_lexical_state == TABLE ){
      yybegin( NORMAL );
      return convert.onTableEOF();
    }
	if( yy_lexical_state == MULTITABLE)
	{
	  yybegin( NORMAL );
	  return convert.onMultiTableEOF();
	}

	return null;
%eofval}

%{

	ExLayoutLexConvert convert = new ExLayoutLexConvert();

  	public void setVirtualWiki( String vWiki ){
    	convert.setVirtualWiki(vWiki);
  	}

%}

newline	= (\n|\r\n)
whitespace = ([\t\ \r\n])
hr = (----)
noformat	=	(__)
tablecell = (\|\|((.*)\|\|{newline}))
tableheader = (\|\!((.*)\|\|{newline}))
multitablecell = (\|\|((.*){newline}))
multitableheader = (\|\!((.*){newline}))
multitablerow = (\|-(([\t\ ]*){newline}))
externalstart = (\[<[:letter:]+>\])
externalend = (\[<\/[:letter:]+>\])

%state NORMAL, OFF, PRE, LIST, TABLE, MULTITABLE, EXTERNAL

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

<NORMAL>({hr}{newline}) {
  return convert.onHorizontalRuler();
}

<NORMAL>^([*#]+.+{newline})|({newline}[*#]+.+{newline}) {
  // since the originalauthor removes \n after headers,
  // we can't indicate at the beginning of the line.
  // so we need to change headers also with \r at the end.
  yybegin( LIST );
  return convert.onListBegin(yytext());
}

<LIST>^[*#]+.+{newline} {
  return convert.onListLine(yytext());
}

<LIST>{newline} {
  yybegin( NORMAL );
  return convert.onListEnd();
}

<NORMAL>^{tablecell} {
  yybegin( TABLE );
  return convert.onTableStart(yytext());
}

<NORMAL>^{tableheader} {
  yybegin( TABLE );
  return convert.onTableStartWithHeader(yytext());
}

<TABLE>^{tablecell} {
  return convert.onTableCell(yytext());
}

<TABLE>^[^\|] {
  yypushback(1);
  yybegin( NORMAL );
  return convert.onTableEnd();
}

<NORMAL>^{multitablecell} {
  yybegin( MULTITABLE );
  return convert.onMultiTableStart(yytext());
}

<NORMAL>^{multitableheader} {
  yybegin( MULTITABLE );
  return convert.onMultiTableStartWithHeader(yytext());
}

<MULTITABLE>^{multitablecell} {
  return convert.onMultiTableCell(yytext());
}

<MULTITABLE>^{multitablerow} {
  return convert.onMultiTableRow(yytext());
}

<MULTITABLE>^[^\|] {
  yypushback(1);
  yybegin( NORMAL );
  return convert.onMultiTableEnd();
}

<MULTITABLE>^[\|][^\|] {
  yypushback(2);
  yybegin( NORMAL );
  return convert.onMultiTableEnd();
}

<MULTITABLE>^[\|][^-] {
  yypushback(2);
  yybegin( NORMAL );
  return convert.onMultiTableEnd();
}


<PRE>{newline}{newline} {
  yybegin( NORMAL );
  return convert.onPreFormatEnd();
}

<NORMAL, OFF>{newline} {
  return convert.onNewLine();
}

<NORMAL>(@@@@{newline}) {
  yybegin( PRE );
  return convert.onPreFormatBegin();
}

<NORMAL, OFF, LIST, TABLE>{whitespace} {
  return convert.onRemoveWhitespace(yytext());
}

<PRE, EXTERNAL>{whitespace} {
  return convert.onKeepWhitespace(yytext());
}

<NORMAL, PRE, OFF, LIST, TABLE, EXTERNAL>. {
  return yytext();
}
