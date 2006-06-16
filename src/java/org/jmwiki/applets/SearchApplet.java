package org.jmwiki.applets;

import java.applet.Applet;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.jmwiki.AbstractSearchEngine;
import org.jmwiki.SearchResultEntry;

/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

/**
 * Applet to perform a search using lucene
 *
 * @author $Author: studer $
 */
public class SearchApplet extends Applet {

	/**
	 *
	 */
	private static final long serialVersionUID = "$Id: SearchApplet.java 606 2006-04-07 23:10:51Z studer $".hashCode();

	/** The last search result */
	private ArrayList result = null;

	/** Reference to the search Engine */
	private  AbstractSearchEngine se = null;


	/** Init the applet and the search engine.
	 * @see java.applet.Applet#init()
	 */
	public void init() {
		super.init();

		// configure log4j
		BasicConfigurator.configure();
		Logger.getLogger("org.apache").setLevel(Level.WARN);

		se = new AppletSearchEngine();
	}

	/**
	 * Actually perform a search
	 * @param term The word to find
	 * @return A String containing the number of hits found
	 */
 	public String doSearch(String term)
 	{
 		result = new ArrayList(se.findMultiple("", term, true));

 		return String.valueOf(result.size());
 	}

	/**
	 * Actually perform a search
	 * @param term The word to find
	 * @param template The template to fill in
	 * @return A String containing the number of hits found
	 */
	public String doSearch(String term, String template)
	{
		result = new ArrayList(se.findMultiple("", term, true));
 		StringBuffer out = new StringBuffer();
 		for (Iterator iter = result.iterator(); iter.hasNext();) {
			SearchResultEntry searchresultentry = (SearchResultEntry) iter.next();
			out.append(replaceString(
				replaceString(
				replaceString(
				replaceString(
				replaceString(
				template,
				"##FOUNDWORD##", searchresultentry.getFoundWord()),
				"##TEXTAFTER##", searchresultentry.getTextAfter()),
				"##TEXTBEFORE##", searchresultentry.getTextBefore()),
				"##TOPIC##", searchresultentry.getTopic()),
				"##TOPICHREF##", safename(searchresultentry.getTopic())));
		}
		return out.toString();
	}

 	/**
 	 * Get a topic for a specific hit
 	 * @param i Number of hit
 	 * @return Topic for this hit or empty string.
 	 */
 	public String getTopic(int i)
 	{
 		if (result == null || i >= result.size())
 			return "";
 		return ((SearchResultEntry)result.get(i)).getTopic();
 	}

	/**
	 * Get a text before word actually found for a specific hit
	 * @param i Number of hit
	 * @return Text before word actually found for this hit or empty string.
	 */
	public String getTextBefore(int i)
	{
		if (result == null || i >= result.size())
			return "";
		return ((SearchResultEntry)result.get(i)).getTextBefore();
	}

	/**
	 * Get a text after word actually found for a specific hit
	 * @param i Number of hit
	 * @return Text after word actually found for this hit or empty string.
	 */
	public String getTextAfter(int i)
	{
		if (result == null || i >= result.size())
			return "";
		return ((SearchResultEntry)result.get(i)).getTextAfter();
	}

	/**
	 * Get a the word actually found for a specific hit
	 * @param i Number of hit
	 * @return Word actually found for this hit or empty string.
	 */
	public String getFoundWord(int i)
	{
		if (result == null || i >= result.size())
			return "";
		return ((SearchResultEntry)result.get(i)).getFoundWord();
	}

	/**
	 * Replaces occurences of the find string with the replace string in the given text
	 * @param text
	 * @param find
	 * @param replace
	 * @return the altered string
	 */
	public static String replaceString(String text, String find, String replace) {
	  int findLength = find.length();
	  StringBuffer buffer = new StringBuffer();
	  int i;
	  for (i = 0; i < text.length() - find.length() + 1; i++) {
		String substring = text.substring(i, i + findLength);
		if (substring.equals(find)) {
		  buffer.append(replace);
		  i += find.length() - 1;
		}
		else {
		  buffer.append(text.charAt(i));
		}
	  }
	  buffer.append(text.substring(text.length() - (text.length() - i)));
	  return buffer.toString();
	}

	/**
	 * Create a safe name of this topic for the file system.
	 * If the topic need to be converted, the hashmap of the original topic
	 * is appended to ensure that the file name is unique.
	 * NOTE: This is a copy of the code in Utilities encodeSafeExportFileName
	 * @param topic
	 *			The original topic name
	 * @return The safe topic name
	 */
	private String safename(String topic) {
		 StringTokenizer st = new StringTokenizer(topic,"%"+File.separator,true);
		 StringBuffer sb = new StringBuffer(topic.length());
		 try {
			 while (st.hasMoreTokens()) {
				 String token = st.nextToken();
				 if(File.separator.equals(token)||"%".equals(token)) {
					sb.append(token);
				 } else {
					sb.append(URLEncoder.encode(token,"utf-8"));
				 }
			 }
		 } catch (java.io.UnsupportedEncodingException ex) {
			 return "java.io.UnsupportedEncodingException";
		 }


		 for (int i=0 ; i < sb.length() ; i++)
		 {
			 if (sb.charAt(i) == '%')
			 {
				 sb.setCharAt(i, '-');
			 }
		 }

		 return sb.toString();
	}

}
