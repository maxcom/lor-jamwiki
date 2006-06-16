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

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jmwiki.parser.Lexer;
import org.jmwiki.Environment;
import org.jmwiki.WikiBase;
import org.jmwiki.utils.Utilities;
import org.jmwiki.utils.JSPUtils;

%%

%public
%type String
%unicode
%implements	org.jmwiki.parser.Lexer
%class HTMLLinkLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
	return null;
%eofval}

%{
	protected static Logger logger = Logger.getLogger( HTMLLinkLex.class );
    protected String virtualWiki;

	protected boolean exists( String topic ){
	  try{
	    return WikiBase.getInstance().exists( virtualWiki, topic );
	  }catch( Exception err ){
	    logger.error( err );
	  }
	  return false;
	}

    public void setVirtualWiki( String vWiki ){
	  this.virtualWiki = vWiki;
    }

	protected boolean ignoreWikiname( String name ){
	  return VQWikiParser.doIgnoreWikiname(name);
	}

  protected String getTopicLink(String link, String description)
  {
	  if( exists( link ) )
	  {
	    return "<a class=\"topic\" href=\"" + Utilities.encodeSafeExportFileName(link.trim()) + ".html\">" +
	    description + "</a>";
	  }
	  else{
	    return description;
	  }
  }
	
%}

whitespace = ([\t\ \r\n])
notbacktick_tabcrlf = ([^`\t\r\n])
notsquares_tabcrlf = ([^\]\t\r\n])
notbacktick_notsquares_tabcrlf = ([^`\]\t\r\n])
wikiname = (([:uppercase:]+[:lowercase:]+)([:uppercase:]+[:lowercase:]+)+)
topic = ({wikiname})
topicbacktick = (`{notbacktick_tabcrlf}+`)
topicbackticktail = (`{notbacktick_tabcrlf}+`([:letter:])+)
topicsquarebracket = (\[\[{notsquares_tabcrlf}+\]\])
topicsquarebrackettail = (\[\[{notsquares_tabcrlf}+\]\]([:letter:])+)
prettytopicsquarebracket = (\[\[{notsquares_tabcrlf}+\|{notsquares_tabcrlf}+\]\])
prettytopicsquarebrackettail = (\[\[{notsquares_tabcrlf}+\|{notsquares_tabcrlf}+\]\]([:letter:])+)
protocols = (http|ftp|mailto|news|https|telnet|file)
extlinkchar = ([^\t\ \r\n\<\>])
prettyextlinkchar = ([^\t\r\n\<\>\|])
hyperlink = ({protocols}:{extlinkchar}+)
framedhyperlink = (\[({protocols}:{prettyextlinkchar}+)\])
prettyhyperlink = (\[({protocols}:{prettyextlinkchar}+)\|{notsquares_tabcrlf}+\])
image = ({hyperlink}(\.gif|\.jpg|\.png|\.jpeg|\.GIF|\.JPG|\.PNG|\.JPEG|\.bmp|\.BMP))
extlink = (([:letter:]|[:digit:])+:{extlinkchar}+)
framedextlink = (\[([:letter:]|[:digit:])+:{notbacktick_notsquares_tabcrlf}+\])
noformat = (__)
externalstart = (\[<[A-Za-z]+>\])
externalend = (\[<\/[A-Za-z]+>\])
attachment = (attach:{extlinkchar}+)
attachment2 = (attach:\".+\")
imageattachment = (attach:{extlinkchar}+(\.gif|\.jpg|\.png|\.jpeg|\.GIF|\.JPG|\.PNG|\.JPEG|\.bmp|\.BMP))
imageattachment2 = (attach:\".+(\.gif|\.jpg|\.png|\.jpeg|\.GIF|\.JPG|\.PNG|\.JPEG|\.bmp|\.BMP)\")
// TODO: edit:-topics.

%state NORMAL, OFF, PRE, EXTERNAL

%%
<NORMAL>\\{noformat}	{
  logger.debug( "escaped double backslash" );
  return "__";
}

<NORMAL>{noformat}	{
  logger.debug( "format off" );
  yybegin( OFF );
}

<OFF>{noformat}	{
  logger.debug( "format on" );
  yybegin( NORMAL );
}

<NORMAL, PRE>{externalstart} {
  logger.debug( "external" );
  yybegin( EXTERNAL );
}

<EXTERNAL>{externalend} {
  logger.debug( "external end");
  yybegin( NORMAL );
}

<NORMAL>(<pre>) {
  logger.debug( "@@@@{newline} entering PRE" );
  yybegin( PRE );
  return yytext();
}

<PRE>(<\/pre>) {
  logger.debug( "{newline}x2 leaving pre" );
  yybegin( NORMAL );
  return yytext();
}

<NORMAL>{image}	{
  logger.debug( "{image}" );
  String link = yytext();
  return "<img src=\"" + link.trim() + "\"/>";
}

<NORMAL>{hyperlink}	{
  logger.debug( "{hyperlink}" );
  String link = yytext();
  String punctuation = Utilities.extractTrailingPunctuation(link);

  if(punctuation!=null){
    link = link.substring(0, link.length()-punctuation.length());
  }

  return "<a class=\"externallink\" href=\"" + link.trim() + "\">" +
    link + "</a>" + punctuation;
}

<NORMAL>{framedhyperlink}	{
  logger.debug( "{framedhyperlink}" );
  String link = yytext();

  link = link.substring(1, link.length()-1).trim();

  return "<a class=\"externallink\" href=\"" + link.trim() + "\">" +
    link + "</a>";
}

<NORMAL>{prettyhyperlink}	{
  logger.debug( "{prettyhyperlink}" + yytext() );
  String input = yytext();
  int position = input.indexOf('|');
  
  String link = null;
  String desc = null;
  link = input.substring(1, position).trim();
  desc = input.substring(position + 1, input.length() - 1).trim();
  if (desc.length() == 0)
  {
     desc = link;
  }

  return "<a class=\"externallink\" href=\"" + link.trim() + "\" title= \"" + link + "\" rel=\"nofollow\">" +
    desc + "</a>";
}

<NORMAL>{prettytopicsquarebracket}	{
  logger.debug( "{prettytopicsquarebracket} '" + yytext() + "'" );
  String input = yytext();
  int position = input.indexOf('|');
  
  String link = null;
  String desc = null;
  link = input.substring(2, position).trim();
  desc = input.substring(position + 1, input.length() - 2).trim();
  if (desc.length() == 0)
  {
     desc = link;
  }

  return getTopicLink(link, desc);
}

<NORMAL>{prettytopicsquarebrackettail}
{
  logger.debug( "{prettytopicsquarebrackettail} '" + yytext() + "'" );
  String input = yytext();
  int position = input.indexOf('|');
  
  String link = null;
  String desc = null;
  link = input.substring(2, position).trim();
  desc = input.substring(position + 1, input.indexOf("]]")).trim();
  if (desc.length() == 0)
  {
     desc = link;
  }
  desc = desc + input.substring(input.indexOf("]]") + 2);

  return getTopicLink(link, desc);
}

<NORMAL>{topic} {
  logger.debug( "{topic} '" + yytext() + "'" );
  String link = yytext();
  if( ignoreWikiname( link ) )
    return yytext();
  return getTopicLink(link, link);
}

<NORMAL>{topicbacktick} {
  logger.debug( "{topicbacktick} '" + yytext() + "'" );
  if( !Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_BACK_TICK) ) {
    logger.debug( "No back-tick links allowed" );
    return yytext();
  }
  String link = yytext();
  link = link.substring(1);
  link = link.substring( 0, link.length() - 1).trim();
  return getTopicLink(link, link);
}

<NORMAL>{topicbackticktail} {
  logger.debug( "{topicbackticktail} '" + yytext() + "'" );
  if( !Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_BACK_TICK) ) {
    logger.debug( "No back-tick links allowed" );
    return yytext();
  }
  String link = yytext();
  link = link.substring(1);
  String desc = link.substring(link.indexOf('`') + 1).trim();
  link = link.substring( 0, link.indexOf('`')).trim();
  desc = link + desc;
  return getTopicLink(link, desc);
}

<NORMAL>{topicsquarebracket} {
  logger.debug( "{topicsquarebracket} '" + yytext() + "'");
  String link = yytext();
  link = link.substring( 2, link.length() - 2).trim();
  return getTopicLink(link, link);
}

<NORMAL>{topicsquarebrackettail} {
  logger.debug( "{topicsquarebrackettail} '" + yytext() + "'");
  String link = yytext();
  link = link.substring(2);
  String desc = link.substring(link.indexOf("]]") + 2);
  link = link.substring( 0, link.indexOf("]]")).trim();
  desc = link + desc;
  return getTopicLink(link, desc);
}

<NORMAL>{imageattachment2} {
  logger.debug( "{imageattachment2}" );
  String displayLink = yytext();
  int firstQuotePosition = displayLink.indexOf("\"");
  String attachmentName = displayLink.substring(firstQuotePosition+1, displayLink.length()-1);
  String link = JSPUtils.encodeURL( attachmentName );
  return "<img src=\"" + link.trim() + "\"/>";
}

<NORMAL>{imageattachment} {
  logger.debug( "{imageattachment}" );
  String displayLink = yytext();
  String attachmentName = displayLink.substring(7);
  String link = JSPUtils.encodeURL( attachmentName );
  return "<img src=\"" + link.trim() + "\"/>";
}

<NORMAL>{attachment2} {
 logger.debug( "{attachment2}" );
 String displayLink = yytext();
 int firstQuotePosition = displayLink.indexOf("\"");
 String attachmentName = displayLink.substring(firstQuotePosition+1, displayLink.length()-1);
 String link = JSPUtils.encodeURL( attachmentName );
  StringBuffer buffer = new StringBuffer();
  buffer.append( "<a class=\"attachmentlink\"" );
  if( Environment.getValue( Environment.PROP_ATTACH_TYPE ).equals( "inline" ) )
    buffer.append( " target=\"_blank\"" );
  buffer.append( " href=\"" );
  buffer.append(link);
  buffer.append( "\" >att:" );
  buffer.append( attachmentName );
  buffer.append( "</a>" );
  return buffer.toString();
}


<NORMAL>{attachment} {
 logger.debug( "{attachment}" );
 String displayLink = yytext();
 String attachmentName = displayLink.substring(7);
 String link = JSPUtils.encodeURL( attachmentName );
  StringBuffer buffer = new StringBuffer();
  buffer.append( "<a class=\"attachmentlink\"" );
  if( Environment.getValue( Environment.PROP_ATTACH_TYPE ).equals( "inline" ) )
    buffer.append( " target=\"_blank\"" );
  buffer.append( " href=\"" );
  buffer.append(link);
  buffer.append( "\" >att:" );
  buffer.append( attachmentName );
  buffer.append( "</a>" );
  return buffer.toString();
}

<NORMAL>{extlink} {
  logger.debug("{extlink}");
  String text = yytext();
  try{
    return LinkExtender.generateLink(
      text.substring( 0, text.indexOf( ':' ) ),
      text.substring( text.indexOf( ':' ) + 1 ),
      text,
      true
    );
  }catch( Exception err ){
    logger.error( "error generating link from extender", err );
    return text;
  }
}

<NORMAL>{framedextlink} {
  logger.debug("{framedextlink}");
  String text = yytext();
  // trim off the square brackets
  text = text.substring(1, text.length()-1);
  try{
    return LinkExtender.generateLink(
      text.substring( 0, text.indexOf( ':' ) ),
      text.substring( text.indexOf( ':' ) + 1 ),
      text,
      true
    );
  }catch( Exception err ){
    logger.error( "error generating link from extender", err );
    return text;
  }
}

<NORMAL, OFF, PRE, EXTERNAL>{whitespace} {
  return yytext();
}

<NORMAL, OFF, PRE, EXTERNAL>.  {
 //logger.debug( ". (" + yytext() + ")" );
 return yytext();
}
