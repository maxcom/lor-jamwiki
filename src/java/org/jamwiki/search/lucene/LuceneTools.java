/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.search.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

/**
 *
 */
public final class LuceneTools {

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
				   ) || (text.charAt(position) != ' ')) {
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
		/*while ( (position>0) && (position < text.length()) && text.charAt(position) == ' ') {
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
	public final static void getTerms(Query query, HashSet terms, boolean prohibited) throws IOException {
		BooleanClause[] queryClauses = ((BooleanQuery)query).getClauses();
		int i;
		for (i = 0; i < queryClauses.length; i++) {
			if (prohibited || !queryClauses[i].isProhibited()) {
				getTerms(queryClauses[i].getQuery(), terms, prohibited);
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
	public final static String[] outputHits(String text, Query query, Analyzer[] analyzer) throws IOException {
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
				stream = analyzer[i].tokenStream("content", new java.io.StringReader(cleanText));
				while ((token = stream.next()) != null) {
					startOffset = token.startOffset();
					endOffset = token.endOffset();
					tokenText = cleanText.substring(startOffset, endOffset);
					// does query contain current token?
					if (terms.contains(token.termText())) {
						// find 10 words before this position
						tenBeforeOffset = LuceneTools.findBefore(cleanText, startOffset);
						if ((tenBeforeOffset != startOffset) && (startOffset > tenBeforeOffset) && (tenBeforeOffset != -1)) {
							result[0] = cleanText.substring(tenBeforeOffset, startOffset);
						}
						result[1] = tokenText;
						// find 10 words after this position
						tenAfterOffset = LuceneTools.findAfter(cleanText, endOffset);
						if ((tenAfterOffset != endOffset) && (endOffset < tenAfterOffset) && (tenAfterOffset != -1)) {
							result[2] = cleanText.substring(endOffset, tenAfterOffset + 1).trim();
						}
						stream.close();
						return result;
					}
				}
			}
			return result;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception e) {
				}
			}
		}
	}
}
