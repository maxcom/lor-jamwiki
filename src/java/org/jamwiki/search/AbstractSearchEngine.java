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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
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
import org.jamwiki.utils.Utilities;
import org.springframework.util.StringUtils;

/*
 *
 */
public abstract class AbstractSearchEngine implements SearchEngine {

	/** Where to log to */
	private static final Logger logger = Logger.getLogger(AbstractSearchEngine.class);
	/** Directory for search index files */
	protected static final String SEARCH_DIR = "search";
	/** Index type "File" */
	protected static final String ITYPE_FILE = "file";
	/** Index type "topic" */
	protected static final String ITYPE_TOPIC = "topic";
	/** Index type "content" */
	protected static final String ITYPE_CONTENT = "content";
	/** Index type "content plain" */
	protected static final String ITYPE_CONTENT_PLAIN = "content_plain";
	/** Index type "topic plain" */
	protected static final String ITYPE_TOPIC_PLAIN = "topic_plain";
	/** Index type "topic plain" */
	protected static final String ITYPE_TOPIC_LINK = "topic_link";
	/** File separator */
	protected static String sep = System.getProperty("file.separator");
	/** Temp directory - where to store the indexes (initialized via getInstance method) */
	protected static String indexPath = null;
	/** Index is stored in RAM */
	private static final int RAM_BASED = 0;
	/** Index is stored in the file system */
	private static final int FS_BASED = 1;
	/** where is the index stored */
	private transient int fsType = FS_BASED;

	/**
	 * Index the given text for the search engine database
	 */
	public void indexText(String virtualWiki, String topic, String text) throws IOException {
		// put keywords into index db - ignore particles etc
		add(virtualWiki, topic, text);
	}

	/**
	 * Should be called by a monitor thread at regular intervals, rebuilds the
	 * entire seach index to account for removed items. Due to the additive rather
	 * than subtractive nature of a Wiki, it probably only needs to be called once
	 * or twice a day
	 */
	public void refreshIndex() throws Exception {
		rebuild();
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
	public Collection findMultiple(String virtualWiki, String text) {
		return doSearch(virtualWiki, text);
	}

	/**
	 * @param indexPath
	 */
	protected void initSearchEngine() throws Exception {
		// FIXME - need a unique temp directory even if multiple wiki installations
		// running on the same system.
		try {
			String dir = Environment.getValue(Environment.PROP_BASE_FILE_DIR) + File.separator + SEARCH_DIR;
			File tmpDir = new File(dir);
			indexPath = tmpDir.getPath();
		} catch (Exception e) {
			logger.warn("Undefined or invalid temp directory, using java.io.tmpdir", e);
			indexPath = System.getProperty("java.io.tmpdir");
		}
		refreshIndex();
	}

	/**
	 * @param indexPath
	 */
	protected void initSearchEngine(String iP) throws Exception {
		indexPath = iP;
		refreshIndex();
	}

	/**
	 *
	 */
	public Collection findLinkedTo(String virtualWiki, String topic) throws Exception {
		if (indexPath == null) {
			return Collections.EMPTY_LIST;
		}
		String indexFilename = getSearchIndexPath(virtualWiki);
		Analyzer analyzer = new StandardAnalyzer();
		Collection results = new ArrayList();
		try {
			BooleanQuery query = new BooleanQuery();
			QueryParser qp;
			qp = new QueryParser(ITYPE_TOPIC_LINK, analyzer);
			query.add(qp.parse(topic), Occur.MUST);
			Searcher searcher = new IndexSearcher(getIndexDirectory(indexFilename, false));
			// actually perform the search
			Hits hits = searcher.search(query);
			for (int i = 0; i < hits.length(); i++) {
				SearchResultEntry result = new SearchResultEntry();
				result.setRanking(hits.score(i));
				result.setTopic(hits.doc(i).get(AbstractSearchEngine.ITYPE_TOPIC_PLAIN));
				results.add(result);
			}
		} catch (IOException e) {
			logger.warn("Error (IOExcpetion) while searching for " + topic + "; Refreshing search index", e);
			SearchRefreshThread.refreshNow();
		} catch (Exception e) {
			logger.fatal("Exception while searching for " + topic, e);
		}
		return results;
	}

	/**
	 * Actually perform the search.
	 *
	 * @param virtualWiki The virtual wiki to use
	 * @param text The text to find
	 *
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	protected Collection doSearch(String virtualWiki, String text) {
		if (indexPath == null) {
			return Collections.EMPTY_LIST;
		}
		String indexFilename = getSearchIndexPath(virtualWiki);
		Analyzer analyzer = new StandardAnalyzer();
		Collection results = new ArrayList();
		logger.debug("search text: " + text);
		try {
			BooleanQuery query = new BooleanQuery();
			QueryParser qp;
			qp = new QueryParser(ITYPE_TOPIC, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			qp = new QueryParser(ITYPE_CONTENT, analyzer);
			query.add(qp.parse(text), Occur.SHOULD);
			Searcher searcher = new IndexSearcher(getIndexDirectory(indexFilename, false));
			// rewrite the query to expand it - required for wildcards to work with highlighter
			Query rewrittenQuery = searcher.rewrite(query);
			// actually perform the search
			Hits hits = searcher.search(rewrittenQuery);
			Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"highlight\">", "</span>"), new SimpleHTMLEncoder(), new QueryScorer(rewrittenQuery));
			for (int i = 0; i < hits.length(); i++) {
				String summary = this.retrieveResultSummary(hits.doc(i), highlighter, analyzer);
				SearchResultEntry result = new SearchResultEntry();
				result.setRanking(hits.score(i));
				result.setTopic(hits.doc(i).get(AbstractSearchEngine.ITYPE_TOPIC_PLAIN));
				result.setSummary(summary);
				results.add(result);
			}
		} catch (IOException e) {
			logger.warn("Error (IOExcpetion) while searching for " + text + "; Refreshing search index", e);
			SearchRefreshThread.refreshNow();
		} catch (Exception e) {
			logger.fatal("Exception while searching for " + text, e);
		}
		return results;
	}

	/**
	 *
	 */
	private String retrieveResultSummary(Document document, Highlighter highlighter, Analyzer analyzer) throws Exception {
		String content = document.get(ITYPE_CONTENT_PLAIN);
		TokenStream tokenStream = analyzer.tokenStream(ITYPE_CONTENT_PLAIN, new StringReader(content));
		String summary = highlighter.getBestFragments(tokenStream, content, 3, "...");
		if (!StringUtils.hasText(summary) && StringUtils.hasText(content)) {
			summary = Utilities.escapeHTML(content.substring(0, Math.min(200, content.length())));
			if (Math.min(200, content.length()) == 200) summary += "...";
		}
		return summary;
	}

	/**
	 * Adds to the in-memory table. Does not remove indexed items that are
	 * no longer valid due to deletions, edits etc.
	 */
	public synchronized void add(String virtualWiki, String topic, String contents)
		throws IOException {
		String indexFilename = getSearchIndexPath(virtualWiki);
		try {
			Directory directory = getIndexDirectory(indexFilename, false);
			if (IndexReader.isLocked(directory)) {
				// wait up to ten seconds until unlocked
				int count = 0;
				while (IndexReader.isLocked(directory) && count < 20) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						; // do nothing
					}
					count++;
				}
				// if still locked, force to unlock it
				if (IndexReader.isLocked(directory)) {
					IndexReader.unlock(directory);
					logger.fatal("Unlocking search index by force");
				}
			}
			// delete the current document
			IndexReader reader = IndexReader.open(directory);
			reader.deleteDocuments(new Term(ITYPE_TOPIC_PLAIN, topic));
			reader.close();
			directory.close();
			// add new document
			IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer(), false);
			writer.optimize();
			Document doc = createDocument(virtualWiki, topic);
			try {
				writer.addDocument(doc);
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
					logger.fatal("IOException during closing", ioe);
				}
				writer = null;
			}
		} catch (IOException e) {
			logger.fatal("Excpetion while adding topic " + topic + "; Refreshing search index", e);
			SearchRefreshThread.refreshNow();
		} catch (Exception e) {
			logger.error("Excpetion while adding topic " + topic, e);
		}
	}

	/**
	 * Trawls all the files in the wiki directory and indexes them
	 */
	public synchronized void rebuild() throws Exception {
		logger.info("Building index");
		Collection allWikis = WikiBase.getHandler().getVirtualWikiList();
		for (Iterator iterator = allWikis.iterator(); iterator.hasNext();) {
			VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
			String currentWiki = virtualWiki.getName();
			logger.debug("indexing virtual wiki " + currentWiki);
			File indexFile = new File(indexPath, "index" + currentWiki);
			logger.debug("Index file path = " + indexFile);
			int retrycounter = 0;
			do {
				// initially create index in ram
				RAMDirectory ram = new RAMDirectory();
				Analyzer analyzer = new StandardAnalyzer();
				IndexWriter writer = new IndexWriter(ram, analyzer, true);
				try {
					Collection topics = WikiBase.getHandler().getAllTopicNames(currentWiki);
					for (Iterator iter = topics.iterator(); iter.hasNext();) {
						String topic = (String) iter.next();
						Document doc = createDocument(currentWiki, topic);
						if (doc != null) writer.addDocument(doc);
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
							retrycounter = 999;
						}
					} catch (IOException ioe) {
						logger.fatal("IOException during close", ioe);
					}
					writer = null;
				}
				// write back to disc
				copyRamIndexToFileIndex(ram, indexFile);
				retrycounter++;
			} while (retrycounter < 1);
		}
	}

	/**
	 * Copy an index from RAM to file
	 * @param ram The index in RAM
	 * @param indexFile The index on disc
	 * @throws IOException
	 */
	private void copyRamIndexToFileIndex(RAMDirectory ram, File indexFile)
		throws IOException {
		Directory index = getIndexDirectory(indexFile, true);
		try {
			if (IndexReader.isLocked(index)) {
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
	 * @param indexFile
	 */
	protected Directory getIndexDirectory(File indexFile, boolean create)
		throws IOException {
		if (fsType == FS_BASED) {
			return FSDirectory.getDirectory(indexFile, create);
		} else {
			return null;
		}
	}

	/**
	 * @param indexFilename
	 */
	protected Directory getIndexDirectory(String indexFilename, boolean create)
		throws IOException {
		if (fsType == FS_BASED) {
			return FSDirectory.getDirectory(indexFilename, create);
		} else {
			return null;
		}
	}

	/**
	 * Create a document to add to the search index
	 * @param currentWiki Name of this wiki
	 * @param topic Name of the topic to add
	 * @return The document to add
	 */
	protected Document createDocument(String virtualWiki, String topicName) throws Exception {
		// get content
		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
		if (topic == null) return null;
		String topicContent = topic.getTopicContent();
		if (topicContent == null) topicContent = "";
		StringBuffer contents = new StringBuffer(topicContent);
		// add document information to search index
		String fileName = getFilename(virtualWiki, topicName);
		if (fileName != null) {
			logger.debug("Indexing topic " + topicName + " in file " + fileName);
		} else {
			logger.debug("Indexing topic " + topicName);
		}
		Document doc = new Document();
		doc.add(new Field(ITYPE_TOPIC, new StringReader(topicName)));
		doc.add(new Field(ITYPE_TOPIC_PLAIN, topicName, Store.YES, Index.UN_TOKENIZED));
		if (fileName != null) {
			doc.add(new Field(ITYPE_FILE, fileName, Store.YES, Index.NO));
		}
		doc.add(new Field(ITYPE_CONTENT, new StringReader(contents.toString())));
		doc.add(new Field(ITYPE_CONTENT_PLAIN, contents.toString(), Store.YES, Index.NO));
		Collection links = Utilities.parseForSearch(topicContent, topicName);
		for (Iterator iter = links.iterator(); iter.hasNext();) {
			String linkTopic = (String)iter.next();
			doc.add(new Field(ITYPE_TOPIC_LINK, new StringReader(linkTopic)));
		}
		return doc;
	}

	/**
	 * @param currentWiki
	 * @param topic
	 * @return
	 */
	protected abstract String getFilename(String currentWiki, String topic);

	/**
	 * Get the path, which holds all index files
	 */
	public String getSearchIndexPath(String virtualWiki) {
		return indexPath + sep + "index" + virtualWiki;
	}
}
