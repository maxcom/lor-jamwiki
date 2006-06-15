/*
Lucene-Highlighting � Lucene utilities to highlight terms in texts
Copyright (C) 2001 Maik Schreiber
This library is free software; you can redistribute it and/or modify it
under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation; either version 2.1 of the License, or
(at your option) any later version.
This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
License for more details.
You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
*/
package org.vqwiki.utils.lucene;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;


/**
 * Contains miscellaneous utility methods for use with Lucene.
 *
 * @version $Id: LuceneTools.java 365 2003-10-05 05:07:32Z garethc $
 * @author Maik Schreiber (mailto: bZ@iq-computing.de)
 */
public final class LuceneTools {
  /**
   * the log4j category/logger for this class
   */
  private final static Logger log = Logger.getLogger(LuceneTools.class.getName());

  /**
   * LuceneTools must not be instantiated directly.
   */
  private LuceneTools() {
  }

  /**
   * TODO: Document this method.
   *
   * @param text TODO: Document this parameter.
   * @param position TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static int findAfter(String text, int position) {
	return findAfter(text, position, 15);
  }

  /**
   * TODO: Document this method.
   *
   * @param text TODO: Document this parameter.
   * @param position TODO: Document this parameter.
   * @param howmany TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static int findAfter(String text, int position, int howmany) {
	int counter = 0;
	int foundPos = -1;
	int lastcharwidth = 1;
	// first find a valid character
	while ((position > 0) && (position < text.length()) && text.charAt(position) == ' ') {
	  position++;
	}

	while ((counter <= howmany) && (position > 0) && (position < text.length())) {
	  lastcharwidth = 1;
	  if (text.charAt(position) == '\r') {
		break;
	  }
	  if (text.charAt(position) == '\t') {
		break;
	  }
	  if (text.charAt(position) == '\u00A0') {
		break;
	  }
	  if ((text.charAt(position) == ' ') ||
		  ((position + 5) < text.length() &&
		  text.charAt(position) == '&' &&
		  text.charAt(position + 1) == 'n' &&
		  text.charAt(position + 2) == 'b' &&
		  text.charAt(position + 3) == 's' &&
		  text.charAt(position + 4) == 'p' &&
		  text.charAt(position + 5) == ';'
		  )) {
		if ((!(((position + 2) < text.length()) &&
			(text.charAt(position + 2) == ' ') &&
			(text.charAt(position + 1) >= 'A') &&
			(text.charAt(position + 1) <= 'Z'))
			) ||
			(text.charAt(position) != ' ')) {
		  counter++;
		}
		if (text.charAt(position) != ' ') {
		  position += 5;
		  lastcharwidth = 6;
		}
	  }

	  position++;

	}
	position -= lastcharwidth;
	return position;
  }

  /**
   * TODO: Document this method.
   *
   * @param text TODO: Document this parameter.
   * @param position TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static int findBefore(String text, int position) {
	int counter = 0;
	int foundPos = -1;
	int lastspacePos = 0;

	// first find a valid character
	/*while ( (position>0) && (position < text.length()) && text.charAt(position) == ' ')
{
position++;
}*/
	while ((counter < 16) && (position > 0) && (position < text.length())) {
	  position--;
	  if (text.charAt(position) == '\n') {
		break;
	  }
	  if (text.charAt(position) == '\t') {
		break;
	  }
	  if (text.charAt(position) == '\u00A0') {
		break;
	  }
	  if (text.charAt(position) == ' ') {
		if (!(((position - 2) >= 0) &&
			(text.charAt(position - 2) == ' ') &&
			(text.charAt(position - 1) >= 'A') &&
			(text.charAt(position - 1) <= 'Z'))
		) {
		  counter++;
		}
	  }
	}
	if (text.charAt(position) == ' ') {
	  position++;
	}
	//log.debug("Returning position " + position);
	return position;
  }

  /**
   * Extracts all term texts of a given Query. Term texts will be returned in lower-case.
   *
   * @param query Query to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   * @exception IOException TODO: Document this exception.
   */
  public final static void getTerms(Query query, HashSet terms, boolean prohibited)
	  throws IOException {
	if (query instanceof BooleanQuery) {
	  getTermsFromBooleanQuery((BooleanQuery) query, terms, prohibited);
	}
	else if (query instanceof PhraseQuery) {
	  getTermsFromPhraseQuery((PhraseQuery) query, terms);
	}
	else if (query instanceof TermQuery) {
	  getTermsFromTermQuery((TermQuery) query, terms);
	}
	else if (query instanceof PrefixQuery) {
	  getTermsFromPrefixQuery((PrefixQuery) query, terms, prohibited);
	}
	else if (query instanceof RangeQuery) {
	  getTermsFromRangeQuery((RangeQuery) query, terms, prohibited);
	}
	else if (query instanceof MultiTermQuery) {
	  getTermsFromMultiTermQuery((MultiTermQuery) query, terms, prohibited);
	}
  }

  /**
   * Extracts all term texts of a given BooleanQuery. Term texts will be returned in lower-case.
   *
   * @param query BooleanQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   * @exception IOException TODO: Document this exception.
   */
  private final static void getTermsFromBooleanQuery(BooleanQuery query, HashSet terms,
													 boolean prohibited)
	  throws IOException {
	BooleanClause[] queryClauses = query.getClauses();
	int i;

	for (i = 0; i < queryClauses.length; i++) {
	  if (prohibited || !queryClauses[i].prohibited) {
		getTerms(queryClauses[i].query, terms, prohibited);
	  }
	}
  }

  /**
   * Extracts all term texts of a given MultiTermQuery. Term texts will be returned in lower-case.
   *
   * @param query MultiTermQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   * @exception IOException TODO: Document this exception.
   */
  private final static void getTermsFromMultiTermQuery(MultiTermQuery query, HashSet terms,
													   boolean prohibited)
	  throws IOException {
	getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts all term texts of a given PhraseQuery. Term texts will be returned in lower-case.
   *
   * @param query PhraseQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   */
  private final static void getTermsFromPhraseQuery(PhraseQuery query, HashSet terms) {
	Term[] queryTerms = query.getTerms();
	int i;

	for (i = 0; i < queryTerms.length; i++) {
	  terms.add(getTermsFromTerm(queryTerms[i]));
	}
  }

  /**
   * Extracts all term texts of a given PrefixQuery. Term texts will be returned in lower-case.
   *
   * @param query PrefixQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   * @exception IOException TODO: Document this exception.
   */
  private final static void getTermsFromPrefixQuery(PrefixQuery query, HashSet terms,
													boolean prohibited)
	  throws IOException {
	getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts all term texts of a given RangeQuery. Term texts will be returned in lower-case.
   *
   * @param query RangeQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   * @param prohibited <code>true</code> to extract "prohibited" terms, too
   * @exception IOException TODO: Document this exception.
   */
  private final static void getTermsFromRangeQuery(RangeQuery query, HashSet terms,
												   boolean prohibited)
	  throws IOException {
	getTerms(query.getQuery(), terms, prohibited);
  }

  /**
   * Extracts the term of a given Term. The term will be returned in lower-case.
   *
   * @param term Term to extract term from
   * @return the Term's term text
   */
  private final static String getTermsFromTerm(Term term) {
	return term.text().toLowerCase();
  }

  /**
   * Extracts all term texts of a given TermQuery. Term texts will be returned in lower-case.
   *
   * @param query TermQuery to extract term texts from
   * @param terms HashSet where extracted term texts should be put into (Elements: String)
   */
  private final static void getTermsFromTermQuery(TermQuery query, HashSet terms) {
	terms.add(getTermsFromTerm(query.getTerm()));
  }

  /**
   * TODO: Document this method.
   *
   * @param term TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static String highlightTerm(String term) {
	return "<B style=\"color:black;background-color:#ffff66\">" + term + "</B>";
  }

  /**
   * Highlights a text in accordance to a given query.
   *
   * @param text text to highlight terms in
   * @param highlighter TermHighlighter to use to highlight terms in the text
   * @param query Query which contains the terms to be highlighted in the text
   * @param analyzer Analyzer used to construct the Query
   * @return highlighted text
   * @exception IOException TODO: Document this exception.
   */
  public final static String highlightTerms(String text, TermHighlighter highlighter, Query query,
											Analyzer analyzer)
	  throws IOException {
	StringBuffer newText = new StringBuffer();
	TokenStream stream = null;

	try {
	  HashSet terms = new HashSet();
	  org.apache.lucene.analysis.Token token;
	  String tokenText;
	  int startOffset;
	  int endOffset;
	  int lastEndOffset = 0;

	  // get terms in query
	  getTerms(query, terms, false);

	  boolean foundBodyStart = false;

	  stream = analyzer.tokenStream(null, new StringReader(text));
	  while ((token = stream.next()) != null) {
		if (!token.termText().equalsIgnoreCase("body") && !foundBodyStart) {
		  continue;
		}
		else {
		  if (!foundBodyStart) {
			token = stream.next();
		  }
		  foundBodyStart = true;
		}

		startOffset = token.startOffset();
		endOffset = token.endOffset();
		tokenText = text.substring(startOffset, endOffset);

		// append text between end of last token (or beginning of text) and start of current token
		if (startOffset > lastEndOffset) {
		  newText.append(text.substring(lastEndOffset, startOffset));
		}

		// does query contain current token?
		if (terms.contains(token.termText())) {
		  newText.append(highlightTerm(tokenText));
		}
		else {
		  newText.append(tokenText);
		}

		lastEndOffset = endOffset;
	  }

	  // append text after end of last token
	  if (lastEndOffset < text.length()) {
		newText.append(text.substring(lastEndOffset));
	  }

	  return newText.toString();
	}
	finally {
	  if (stream != null) {
		try {
		  stream.close();
		}
		catch (Exception e) {
		}
	  }
	}
  }

  /**
   * Give the text, which is before the highlighted text, the highlighted text
   * and the text, which is afterwards
   *
   * @param text The source text
   * @param query The query containing the string searched for
   * @param analyzer Some analyzer
   * @return An array of 3 strings, with the ten words before (as pos 0), the keyword (as pos 1) and the ten words after (as pos 2)
   * @throws IOException The stream can throw an IOException
   */
  public final static String[] outputHits(
	  String text,
	  Query query,
	  Analyzer[] analyzer)
	  throws IOException {
	HTMLParser htmlparser = new HTMLParser(new StringReader(text));

	Reader in = htmlparser.getReader();
	StringBuffer buffer = new StringBuffer();

	int ch;

	while ((ch = in.read()) > -1) {
	  buffer.append((char) ch);
	}

	in.close();

	String cleanText = buffer.toString();

	TokenStream stream = null;

	String[] result = new String[3];
	result[0] = "";
	result[1] = "";
	result[2] = "";

	try {
	  HashSet terms = new HashSet();
	  org.apache.lucene.analysis.Token token;
	  String tokenText;
	  int startOffset;
	  int endOffset;
	  int tenBeforeOffset;
	  int tenAfterOffset;

	  // get terms in query
	  LuceneTools.getTerms(query, terms, false);
	  log.debug("Terms: " + terms);

	  for (int i = 0; i < analyzer.length; i++) {
		stream =
			analyzer[i].tokenStream(
				"content",
				new java.io.StringReader(cleanText));

		while ((token = stream.next()) != null) {
		  startOffset = token.startOffset();
		  endOffset = token.endOffset();
		  tokenText = cleanText.substring(startOffset, endOffset);

		  // does query contain current token?
		  if (terms.contains(token.termText())) {
			// find 10 words before this position
			tenBeforeOffset =
				LuceneTools.findBefore(cleanText, startOffset);

			if ((tenBeforeOffset != startOffset)
				&& (startOffset > tenBeforeOffset)
				&& (tenBeforeOffset != -1)) {
			  //log.debug("Before: " + tenBeforeOffset + " / " + startOffset );
			  result[0] =
				  cleanText.substring(tenBeforeOffset, startOffset);
			}

			result[1] = tokenText;

			// find 10 words after this position
			tenAfterOffset =
				LuceneTools.findAfter(cleanText, endOffset);

			if ((tenAfterOffset != endOffset)
				&& (endOffset < tenAfterOffset)
				&& (tenAfterOffset != -1)) {
			  //log.debug("After: " + endOffset + " / " + tenAfterOffset);
			  result[2] =
				  cleanText.substring(endOffset, tenAfterOffset + 1).trim();
			}

			stream.close();
			return result;
		  }
		}
	  }
	  return result;
	}
	finally {
	  if (stream != null) {
		try {
		  stream.close();
		}
		catch (Exception e) {
		  ;
		}
	  }
	}
  }

}
