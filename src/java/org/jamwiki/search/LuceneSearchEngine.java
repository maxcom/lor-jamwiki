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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.VirtualWiki;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

/*
 *
 */
public class LuceneSearchEngine {

	/** Where to log to */
	private static final Logger logger = Logger.getLogger(LuceneSearchEngine.class);
	/** Directory for search index files */
	protected static final String SEARCH_DIR = "search";
	/** Id stored with documents to indicate the searchable topic name */
	protected static final String ITYPE_TOPIC = "topic";
	/** Id stored with documents to indicate the searchable content. */
	protected static final String ITYPE_CONTENT = "content";
	/** Id stored with documents to indicate the raw Wiki markup */
	protected static final String ITYPE_CONTENT_PLAIN = "content_plain";
	/** Id stored with documents to indicate the topic name. */
	protected static final String ITYPE_TOPIC_PLAIN = "topic_plain";
	/** Id stored with the document to indicate the search names of topics linked from the page.  */
	protected static final String ITYPE_TOPIC_LINK = "topic_link";
	/** Temp directory - where to store the indexes (initialized via getInstance method) */
	protected static String indexPath = null;
	/** Index is stored in RAM */
	private static final int RAM_BASED = 0;
	/** Index is stored in the file system */
	private static final int FS_BASED = 1;
	/** where is the index stored */
	private static int fsType = FS_BASED;

	static {
		initSearchEngine();
	}

	/**
	 * Adds to the in-memory table. Does not remove indexed items that are
	 * no longer valid due to deletions, edits etc.
	 */
	public static synchronized void addToIndex(Topic topic, Collection links) {
		String virtualWiki = topic.getVirtualWiki();
		String topicName = topic.getName();
		String contents = topic.getTopicContent();
		String indexFilename = getSearchIndexPath(virtualWiki);
		IndexWriter writer = null;
		IndexReader reader = null;
		try {
			Directory directory = getIndexDirectory(indexFilename, false);
			// delete the current document
			try {
				reader = IndexReader.open(directory);
				reader.deleteDocuments(new Term(ITYPE_TOPIC_PLAIN, topicName));
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (Exception e) {}
				}
			}
			directory.close();
			// add new document
			try {
				writer = new IndexWriter(directory, new StandardAnalyzer(), false);
				KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
				writer.optimize();
				Document standardDocument = createStandardDocument(topic);
				if (standardDocument != null) writer.addDocument(standardDocument);
				Document keywordDocument = createKeywordDocument(topic, links);
				if (keywordDocument != null) writer.addDocument(keywordDocument, keywordAnalyzer);
			} finally {
				try {
					if (writer != null) {
						writer.optimize();
					}
				} catch (Exception e) {}
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (Exception e) { logger.fatal(e); }
			}
		} catch (Exception e) {
			logger.error("Exception while adding topic " + topicName, e);
		}
	}

	/**
	 * Copy an index from RAM to file
	 * @param ram The index in RAM
	 * @param indexFile The index on disc
	 * @throws IOException
	 */
	private static void copyRamIndexToFileIndex(RAMDirectory ram, File indexFile) throws IOException {
		Directory index = getIndexDirectory(indexFile, true);
		try {
			if (IndexReader.isLocked(index)) {
				// FIXME - huh?
				// wait up to ten seconds until unlocked
				int count = 0;
				while (IndexReader.isLocked(index) && count < 20) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						; // do nothing
					}
					count++;
				}
				// if still locked, force to unlock it
				if (IndexReader.isLocked(index)) {
					IndexReader.unlock(index);
					logger.fatal("Unlocking search index by force");
				}
			}
			IndexWriter indexWriter = new IndexWriter(index, null, true);
			indexWriter.close();
		} catch (Exception e) {
			logger.fatal("Cannot create empty directory: ", e);
			// delete all files in the temp directory
			if (fsType == FS_BASED) {
				File[] files = indexFile.listFiles();
				for (int i = 0; i < files.length; i++) {
					files[i].delete();
				}
			}
		}
		// actually copy files
		String[] ar = ram.list();
		for (int i = 0; i < ar.length; i++) {
			// make place on ram disk
			IndexOutput os = index.createOutput(ar[i]);
			// read current file
			IndexInput is = ram.openInput(ar[i]);
			// and copy to ram disk
			int len = (int) is.length();
			byte[] buf = new byte[len];
			is.readBytes(buf, 0, len);
			os.writeBytes(buf, len);
			// graceful cleanup
			is.close();
			os.close();
		}
	}

	/**
	 * Create a basic Lucene document to add to the index that does treats
	 * the topic content as a single keyword and does not tokenize it.
	 */
	private static Document createKeywordDocument(Topic topic, Collection links) throws Exception {
		String topicContent = topic.getTopicContent();
		if (topicContent == null) topicContent = "";
		Document doc = new Document();
		// store topic name for later retrieval
		doc.add(new Field(ITYPE_TOPIC_PLAIN, topic.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		if (links == null) {
			links = new Vector();
		}
		// index topic links for search purposes
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			String linkTopic = (String)iter.next();
			doc.add(new Field(ITYPE_TOPIC_LINK, linkTopic, Field.Store.NO, Field.Index.UN_TOKENIZED));
		}
		return doc;
	}

	/**
	 * Create a basic Lucene document to add to the index.  This document
	 * is suitable to be parsed with the StandardAnalyzer.
	 */
	private static Document createStandardDocument(Topic topic) throws Exception {
		String topicContent = topic.getTopicContent();
		if (topicContent == null) topicContent = "";
		Document doc = new Document();
		// store topic name and content for later retrieval
		doc.add(new Field(ITYPE_TOPIC_PLAIN, topic.getName(), Field.Store.YES, Field.Index.UN_TOKENIZED));
		doc.add(new Field(ITYPE_CONTENT_PLAIN, topicContent, Field.Store.YES, Field.Index.NO));
		// index topic name and content for search purposes
		doc.add(new Field(ITYPE_TOPIC, new StringReader(topic.getName())));
		doc.add(new Field(ITYPE_CONTENT, new StringReader(topicContent)));
		return doc;
	}

	/**
	 *
	 */
	public static synchronized void deleteFromIndex(Topic topic) {
	}

	/**
	 *
	 */
	public static Collection findLinkedTo(String virtualWiki, String topicName) throws Exception {
		if (indexPath == null) {
			return Collections.EMPTY_LIST;
		}
		String indexFilename = getSearchIndexPath(virtualWiki);
		KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
		Collection results = new ArrayList();
		IndexSearcher searcher = null;
		try {
			PhraseQuery query = new PhraseQuery();
			Term term = new Term(ITYPE_TOPIC_LINK, topicName);
			query.add(term);
			searcher = new IndexSearcher(getIndexDirectory(indexFilename, false));
			// actually perform the search
			Hits hits = searcher.search(query);
			for (int i = 0; i < hits.length(); i++) {
				SearchResultEntry result = new SearchResultEntry();
				result.setRanking(hits.score(i));
				result.setTopic(hits.doc(i).get(LuceneSearchEngine.ITYPE_TOPIC_PLAIN));
				results.add(result);
			}
		} catch (Exception e) {
			logger.fatal("Exception while searching for " + topicName, e);
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (Exception e) {}
			}
		}
		return results;
	}

	/**
	 * Find topics that contain any of the space delimited terms.
	 * Note: Use this method for full text search.
	 *
	 * @param virtualWiki The virtual wiki to use
	 * @param text The text to find
	 *
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	public static Collection findMultiple(String virtualWiki, String text) {
		if (indexPath == null) {
			return Collections.EMPTY_LIST;
		}
		String indexFilename = getSearchIndexPath(virtualWiki);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		Collection results = new ArrayList();
		logger.debug("search text: " + text);
		IndexSearcher searcher = null;
		try {
			BooleanQuery query = new BooleanQuery();
			QueryParser qp;
			qp = new QueryParser(ITYPE_TOPIC, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			qp = new QueryParser(ITYPE_CONTENT, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			searcher = new IndexSearcher(getIndexDirectory(indexFilename, false));
			// rewrite the query to expand it - required for wildcards to work with highlighter
			Query rewrittenQuery = searcher.rewrite(query);
			// actually perform the search
			Hits hits = searcher.search(rewrittenQuery);
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), new SimpleHTMLEncoder(), new QueryScorer(rewrittenQuery));
			for (int i = 0; i < hits.length(); i++) {
				String summary = retrieveResultSummary(hits.doc(i), highlighter, analyzer);
				SearchResultEntry result = new SearchResultEntry();
				result.setRanking(hits.score(i));
				result.setTopic(hits.doc(i).get(LuceneSearchEngine.ITYPE_TOPIC_PLAIN));
				result.setSummary(summary);
				results.add(result);
			}
		} catch (Exception e) {
			logger.fatal("Exception while searching for " + text, e);
		} finally {
			if (searcher != null) {
				try {
					searcher.close();
				} catch (Exception e) {}
			}
		}
		return results;
	}

	/**
	 * @param indexFile
	 */
	protected static Directory getIndexDirectory(File indexFile, boolean create) throws IOException {
		if (fsType == FS_BASED) {
			return FSDirectory.getDirectory(indexFile, create);
		} else {
			return null;
		}
	}

	/**
	 * @param indexFilename
	 */
	protected static Directory getIndexDirectory(String indexFilename, boolean create) throws IOException {
		if (fsType == FS_BASED) {
			return FSDirectory.getDirectory(indexFilename, create);
		} else {
			return null;
		}
	}

	/**
	 * Get the path, which holds all index files
	 */
	public static String getSearchIndexPath(String virtualWiki) {
		return indexPath + File.separator + "index" + virtualWiki;
	}

	/**
	 * @param indexPath
	 */
	protected static void initSearchEngine() {
		try {
			String dir = Environment.getValue(Environment.PROP_BASE_FILE_DIR) + File.separator + SEARCH_DIR;
			File tmpDir = new File(dir);
			indexPath = tmpDir.getPath();
		} catch (Exception e) {
			logger.warn("Undefined or invalid temp directory, using java.io.tmpdir", e);
			indexPath = System.getProperty("java.io.tmpdir");
		}
	}

	/**
	 * Trawls all the files in the wiki directory and indexes them
	 */
	public static synchronized void refreshIndex() throws Exception {
		Collection allWikis = WikiBase.getHandler().getVirtualWikiList();
		Topic topic;
		for (Iterator iterator = allWikis.iterator(); iterator.hasNext();) {
			long start = System.currentTimeMillis();
			int count = 0;
			VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
			String currentWiki = virtualWiki.getName();
			File indexFile = new File(indexPath, "index" + currentWiki);
			// initially create index in ram
			RAMDirectory ram = new RAMDirectory();
			StandardAnalyzer analyzer = new StandardAnalyzer();
			KeywordAnalyzer keywordAnalyzer = new KeywordAnalyzer();
			IndexWriter writer = new IndexWriter(ram, analyzer, true);
			try {
				Collection topicNames = WikiBase.getHandler().getAllTopicNames(currentWiki);
				for (Iterator iter = topicNames.iterator(); iter.hasNext();) {
					String topicName = (String)iter.next();
					topic = WikiBase.getHandler().lookupTopic(currentWiki, topicName);
					Document standardDocument = createStandardDocument(topic);
					if (standardDocument != null) writer.addDocument(standardDocument);
					// FIXME - parsing all documents will be intolerably slow with even a
					// moderately large Wiki
					ParserInput parserInput = new ParserInput();
					parserInput.setMode(ParserInput.MODE_SEARCH);
					ParserOutput parserOutput = Utilities.parsePreSave(parserInput, topic.getTopicContent());
					Document keywordDocument = createKeywordDocument(topic, parserOutput.getLinks());
					if (keywordDocument != null) writer.addDocument(keywordDocument, keywordAnalyzer);
					count++;
				}
			} catch (IOException ex) {
				logger.error(ex);
			} finally {
				try {
					if (writer != null) {
						writer.optimize();
					}
				} catch (IOException ioe) {
					logger.fatal("IOException during optimize", ioe);
				}
				try {
					if (writer != null) {
						writer.close();
					}
				} catch (IOException ioe) {
					logger.fatal("IOException during close", ioe);
				}
				writer = null;
			}
			// write back to disc
			copyRamIndexToFileIndex(ram, indexFile);
			logger.info("Rebuilt search index for " + virtualWiki.getName() + " (" + count + " documents) in " + ((System.currentTimeMillis() - start) / 1000.000) + " seconds");
		}
	}

	/**
	 *
	 */
	private static String retrieveResultSummary(Document document, Highlighter highlighter, StandardAnalyzer analyzer) throws Exception {
		String content = document.get(ITYPE_CONTENT_PLAIN);
		TokenStream tokenStream = analyzer.tokenStream(ITYPE_CONTENT_PLAIN, new StringReader(content));
		String summary = highlighter.getBestFragments(tokenStream, content, 3, "...");
		if (!StringUtils.hasText(summary) && StringUtils.hasText(content)) {
			summary = Utilities.escapeHTML(content.substring(0, Math.min(200, content.length())));
			if (Math.min(200, content.length()) == 200) summary += "...";
		}
		return summary;
	}
}
