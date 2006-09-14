/**
 *
 */
package org.jamwiki.parser.alt;

import java.io.*;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.parser.AbstractLexer;
import org.jamwiki.parser.ParserInput;

%%

%public
%type String
%unicode
%extends AbstractLexer
%class VQWikiLayoutLex

%init{
	yybegin( NORMAL );
%init}

%eofval{
    logger.fine( "Ending at list level: " + listLevel );
    if (yystate() == LIST ){
      yybegin( NORMAL );
      StringBuffer buffer = new StringBuffer();
      for( int i = listLevel; i > 0; i-- ){
        buffer.append( "</li>" );
        if( ordered )
          buffer.append( "</ol>" );
        else
          buffer.append( "</ul>" );
        buffer.append("\n" );
      }
    return buffer.toString();
    }
    if (yystate() == TABLE ){
      yybegin( NORMAL );
      return "</tr></table>";
    }

	return null;
%eofval}

%{
	protected boolean allowHtml;
	protected int listLevel;
	protected boolean ordered;
	protected static WikiLogger logger = WikiLogger.getLogger( VQWikiLayoutLex.class.getName() );
	
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
	
	/**
	 *
	 */
	protected String getListEntry(String text) {
		logger.fine( "first list item: " + text );
		int count = 0;
		ordered = false;
		logger.fine( "First char='" + text.charAt(0) + "'" );
		if (text.charAt( 0 ) == '#') ordered = true;
		listLevel = 1;
		StringBuffer buffer = new StringBuffer();
		if (ordered) {
			buffer.append( "<ol>" );
		} else {
			buffer.append( "<ul>" );
		}
		buffer.append( "<li>" );
		buffer.append( text.substring( count + 1 ).trim() );
		return buffer.toString();
	}
	
	/**
	 *
	 */
	protected String getListSingleEntry(String text) {
		logger.fine( "another list item: " + text );
		boolean tabs = false;
		int count = 0;
		if (text.charAt(0) == '\t') {
			tabs = true;
			for( int i = 0; i < text.length(); i++ ){
				if( text.charAt( i ) == '\t' ) {
					count++;
				} else {
					break;
				}
			}
		} else {
			tabs = false;
			for (int i = 0; i < text.length(); i +=  3) {
				if ((i + 3 <= text.length()) && (text.substring( i, i + 3 ).equals("   "))) {
					count++;
				} else {
					break;
				}
			}
		}
		StringBuffer buffer = new StringBuffer();
		if (count > listLevel) {
			for (int i=count-listLevel;i>0;i--) {
				if( ordered ) {
					buffer.append( "\n<ol>" );
				} else {
					buffer.append( "\n<ul>" );
				}
			}
			listLevel = count;
		} else if (count < listLevel) {
			buffer.append( "</li>" );
			for (int i = listLevel - count ; i > 0 ; i--) {
				if( ordered ) {
					buffer.append( "</ol>" );
				} else {
					buffer.append( "</ul>" );
				}
			}
			listLevel = count;
			buffer.append( "\n" );
		} else {
			buffer.append( "</li>\n" );
		}
		buffer.append( "<li>" );
		if (tabs) {
			buffer.append(text.substring( count + 1 ).trim());
		} else {
			buffer.append(text.substring((count * 3) + 1).trim());
		}
		return buffer.toString();
	}

%}

newline	= (\n|\r\n)
whitespace = ([\t\ \r\n])
hr = (----)
noformat	=	(__)
tableboundary = (####{newline})
tablecell =	([^#\n]+##)
listtabentry = (\t+[*#].+{newline})|({newline}\t+[*#].+{newline})
listtabsingleentry = \t+[*#].+{newline}
listspaceentry = (([\ ]{3})+[*#].+{newline})|({newline}([\ ]{3})+[*#].+{newline})
listspacesingleentry = (([\ ]{3})+[*#].+{newline})
externalstart = (\[<[:letter:]+>\])
externalend = (\[<\/[:letter:]+>\])

%state NORMAL, OFF, PRE, LIST, TABLE, EXTERNAL

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

<EXTERNAL>{externalend} {
  logger.fine( "external end");
  yybegin( NORMAL );
  return yytext();
}

<NORMAL>({hr}{newline}) {
  logger.fine( "{hr}" );
  return "\n<hr>\n";
}

<NORMAL>^{listspaceentry}
{
  yybegin( LIST );
  return getListEntry(yytext().trim());
}

<NORMAL>{listtabentry}
{
  yybegin( LIST );
  return getListEntry(yytext().trim());
}

<LIST>^{listspacesingleentry} {
  return getListSingleEntry(yytext());
}

<LIST>{listtabsingleentry} {
  return getListSingleEntry(yytext());
}

<LIST>{newline} {
  logger.fine( "end of list" );
  yybegin( NORMAL );
  StringBuffer buffer = new StringBuffer();
  for( int i = listLevel; i > 0; i-- ){
    buffer.append( "</li>" );
    if( ordered )
      buffer.append( "</ol>" );
    else
      buffer.append( "</ul>" );
    buffer.append("\n" );
  }
  return buffer.toString();
}

<NORMAL>{tableboundary} {
  logger.fine( "table start" );
  yybegin( TABLE );
  return "<table class=\"wikitable\" border=\"1\"><tr>";
}

<TABLE>{tablecell}{newline}{tableboundary} {
  logger.fine( "table end" );
  yybegin( NORMAL );
  String text = yytext().trim();
  StringBuffer buffer = new StringBuffer();
  buffer.append( "<td>" );
  buffer.append( text.substring( 0, text.indexOf("##") ) );
  buffer.append( "</td>" );
  buffer.append( "</tr></table>\n" );
  return buffer.toString();
}

<TABLE>{newline} {
  logger.fine( "tablerowend" );
  return "</tr><tr>";
}

<TABLE>{tablecell} {
  logger.fine( "tablecell" );
  String text = yytext();
  StringBuffer buffer = new StringBuffer();
  buffer.append( "<td>" );
  buffer.append( text.substring( 0, text.length() - 2 ) );
  buffer.append( "</td>" );
  return buffer.toString();
}

<PRE>{newline}{newline} {
  logger.fine( "{newline}x2 leaving pre" );
	yybegin( NORMAL );
  return "</pre>\n";
}

<NORMAL, OFF>{newline} {
  logger.fine( "{newline}" );
  StringBuffer buffer = new StringBuffer();
  buffer.append( "<br/>" );
  buffer.append( "\n" );
  return buffer.toString();
}

<NORMAL>(@@@@{newline}) {
  logger.fine( "@@@@{newline} entering PRE" );
  yybegin( PRE );
  return "<pre>";
}

<NORMAL, OFF, LIST, TABLE>{whitespace} {
  String text = yytext();
  StringBuffer buffer = new StringBuffer();
  for( int i = 0; i < text.length(); i++ ){
    buffer.append( (int)text.charAt(i) );
  }
  logger.fine( "{whitespace} " + buffer.toString() );
  return " ";
}

<PRE, EXTERNAL>{whitespace} {
  logger.fine( "PRE, EXTERNAL {whitespace}" );
  return yytext();
}

<NORMAL, PRE, OFF, LIST, TABLE, EXTERNAL>. {
  logger.fine( ". (" + yytext() + ")" );
  return yytext();
}
