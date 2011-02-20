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
package org.jamwiki.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Method;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jamwiki.Environment;
import org.jamwiki.SearchEngine;
import org.jamwiki.WikiBase;
import org.jamwiki.model.SearchResultEntry;
import org.jamwiki.model.Topic;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.utils.WikiLogger;

/**
 * An implementation of {@link org.jamwiki.SearchEngine} that uses
 * <a href="http://rankingalgorithm.tgels.com">RankingAlgorithm</a> to perform searches of
 * Wiki content.
 */
public class RankingAlgorithmSearchEngine extends  LuceneSearchEngine {

	/** Where to log to */
	protected static final WikiLogger logger = WikiLogger.getLogger(RankingAlgorithmSearchEngine.class.getName());

	/**
	 * Find all documents that contain a specific search term, ordered by relevance.
	 * This method supports all Lucene search query syntax.
	 *
	 * @param virtualWiki The virtual wiki for the topic.
	 * @param text The search term being searched for.
	 * @return A list of SearchResultEntry objects for all documents that
	 *  contain the search term.
	 */
	public List<SearchResultEntry> findResults(String virtualWiki, String text) {
		StandardAnalyzer analyzer = new StandardAnalyzer(USE_LUCENE_VERSION);
		List<SearchResultEntry> results = new ArrayList<SearchResultEntry>();
		logger.trace("search text: " + text);
		try {
			BooleanQuery query = new BooleanQuery();
			QueryParser qp;
			qp = new QueryParser(USE_LUCENE_VERSION, ITYPE_TOPIC, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			qp = new QueryParser(USE_LUCENE_VERSION, ITYPE_CONTENT, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			Searcher searcher = this.retrieveIndexSearcher(virtualWiki);
			// rewrite the query to expand it - required for wildcards to work with highlighter
			Query rewrittenQuery = searcher.rewrite(query);
			// actually perform the search
			TopScoreDocCollector collector = TopScoreDocCollector.create(MAXIMUM_RESULTS_PER_SEARCH, true);
		
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), new SimpleHTMLEncoder(), new QueryScorer(rewrittenQuery));
		
			try {
				IndexSearcher is = (IndexSearcher)searcher;
				Class classRQ = Class.forName("com.transaxtions.search.rankingalgorithm.RankingQuery");
				Class classQuery = Class.forName("org.apache.lucene.search.Query");
				Object rq = classRQ.newInstance();
				Class classArray[] = new Class[2];
				classArray[0] = classQuery;
				classArray[1] = is.getClass();
				Object args[] = new Object[2];
				args[0] = rewrittenQuery;
				args[1] = is;
				Method methodRQ_search = classRQ.getMethod("search", classArray);				
				Object hitsobject = methodRQ_search.invoke(rq, args); 
				Class classRH = hitsobject.getClass();
				classArray = new Class[1];
				classArray[0] = int.class;
				Method methodRH_length = classRH.getMethod("length", null);
				Method methodRH_docid = classRH.getMethod("docid", classArray);
				Method methodRH_score = classRH.getMethod("score", classArray);
				Object lenobject = methodRH_length.invoke(hitsobject);
				int length = ((Integer)lenobject).intValue();
				for (int i = 0; i < length; i++) {
					args = new Object[1];
					args[0] = new Integer(i);
					Object docobject = methodRH_docid.invoke(hitsobject, args);
					int docId = ((Integer)docobject).intValue();
					Document doc = searcher.doc(docId);
					String summary = retrieveResultSummary(doc, highlighter, analyzer);
					SearchResultEntry result = new SearchResultEntry();
					Object scoreobject = methodRH_score.invoke(hitsobject, args);
					float score = ((Float)scoreobject).floatValue();
					result.setRanking(score);
					result.setTopic(doc.get(ITYPE_TOPIC_PLAIN));
					result.setSummary(summary);
					results.add(result);
				}
			} catch(Throwable t) {t.printStackTrace();}
			
		} catch (Exception e) {
			logger.error("Exception while searching for " + text, e);
		}
		return results;
	}
}
