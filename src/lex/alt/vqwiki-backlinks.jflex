/**
 *
 */
package org.jamwiki.parser.alt;

import java.io.*;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.parser.Lexer;
import org.jamwiki.parser.ParserInfo;

%%

%public
%type String
%unicode
%implements	org.jamwiki.parser.Lexer
%class BackLinkLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
	return null;
%eofval}

%{
	protected static Logger logger = Logger.getLogger( BackLinkLex.class );
	protected ParserInfo parserInfo;
	private List links = new ArrayList();
	
	/**
	 *
	 */
	public void setParserInfo(ParserInfo parserInfo) {
		this.parserInfo = parserInfo;
	}
	
	/**
	 *
	 */
	protected boolean ignoreWikiname(String name) {
		return VQWikiParser.doIgnoreWikiname(name);
	}

	/**
	 *
	 */
	public List getLinks() {
		return this.links;
	}

	/**
	 *
	 */
	private void addLink(String link) {
		logger.debug("adding link: '" + link + "'");
		this.links.add(link);
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
}

<PRE>(<\/pre>) {
  logger.debug( "{newline}x2 leaving pre" );
  yybegin( NORMAL );
}

<NORMAL>{image}	{
  logger.debug( "{image}" );
}

<NORMAL>{hyperlink}	{
  logger.debug( "{hyperlink}" );
}

<NORMAL>{framedhyperlink}	{
  logger.debug( "{framedhyperlink}" );
}

<NORMAL>{prettyhyperlink}	{
  logger.debug( "{prettyhyperlink}" );
}

<NORMAL>{prettytopicsquarebracket}	{
  logger.debug( "{prettytopicsquarebracket} '" + yytext() + "'" );
  String input = yytext();
  int position = input.indexOf('|');
  
  String link = null;
  link = input.substring(2, position).trim();
  
  addLink(link);
}

<NORMAL>{prettytopicsquarebrackettail}
{
  logger.debug( "{prettytopicsquarebrackettail} '" + yytext() + "'" );
  String input = yytext();
  int position = input.indexOf('|');
  
  String link = null;
  link = input.substring(2, position).trim();
  addLink(link);
}

<NORMAL>{topic} {
  logger.debug( "{topic} '" + yytext() + "'" );
  String link = yytext();
  if( !ignoreWikiname( link ) ){
    addLink(link.trim());
  }
}

<NORMAL>{topicbacktick} {
  logger.debug( "{topicbacktick} '" + yytext() + "'" );
  String link = yytext();
  link = link.substring(1);
  link = link.substring( 0, link.length() - 1).trim();
  addLink(link);
}

<NORMAL>{topicbackticktail} {
  logger.debug( "{topicbackticktail} '" + yytext() + "'" );
  String link = yytext();
  link = link.substring( 0, link.indexOf('`')).trim();
  addLink(link);
}

<NORMAL>{topicsquarebracket} {
  logger.debug( "{topicsquarebracket} '" + yytext() + "'");
  String link = yytext();
  link = link.substring(2);
  link = link.substring( 0, link.length() - 2).trim();
  addLink(link);
}

<NORMAL>{topicsquarebrackettail} {
  logger.debug( "{topicsquarebrackettail} '" + yytext() + "'");
  String link = yytext();
  link = link.substring(2);
  link = link.substring( 0, link.indexOf("]]")).trim();
  addLink(link);
}

<NORMAL>{imageattachment2} {
  logger.debug( "{imageattachment2}" );
}

<NORMAL>{imageattachment} {
  logger.debug( "{imageattachment}" );
}

<NORMAL>{attachment2} {
 logger.debug( "{attachment}" );
}


<NORMAL>{attachment} {
 logger.debug( "{attachment}" );
}

<NORMAL>{extlink} {
 logger.debug( "{extlink}" );
}

<NORMAL>{framedextlink} {
 logger.debug( "{extlink2}" );
}

<NORMAL, OFF, PRE, EXTERNAL>{whitespace} {
  return yytext();
}

<NORMAL, OFF, PRE, EXTERNAL>.  {
 //logger.debug( ". (" + yytext() + ")" );
 return yytext();
}
