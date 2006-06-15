/*
 * $Id: AbstractSearchEngine.java 644 2006-04-23 07:52:28Z wrh2 $
 *
 * Filename  : AbstractSearchEngine.java
 * Project   : vqwiki-classic
 */
package org.vqwiki;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.InputStream;
import org.apache.lucene.store.OutputStream;
import org.apache.lucene.store.RAMDirectory;
import org.vqwiki.lex.BackLinkLex;
import org.vqwiki.utils.Utilities;
import org.vqwiki.utils.lucene.HTMLParser;
import org.vqwiki.utils.lucene.LuceneTools;
import org.vqwiki.utils.lucene.SimpleKeepNumbersAnalyzer;


/**
 * Abstract class to do the search.
 *
 * This class was created on 09:59:41 04.08.2003
 *
 * @author tobsch
 */
public abstract class AbstractSearchEngine implements SearchEngine {

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
	/** Where to log to */
	private static final Logger logger = Logger.getLogger(AbstractSearchEngine.class);
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
	/** Can we parse HTML files? */
	private transient boolean canParseHTML = false;
	/** Can we parse PDF files? */
	private transient boolean canParsePDF = false;

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
	 * Find topics that contain the given term.
	 * Note: Use this method ONLY to search for topics!
	 *
	 * @param virtualWiki The virtual wiki to use
	 * @param text The text to find
	 * @param fuzzy true, if fuzzy search should be used, false otherwise
	 *
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	public Collection find(String virtualWiki, String text, boolean doTextBeforeAndAfterParsing) {
		return doSearch(virtualWiki, text, false, doTextBeforeAndAfterParsing);
	}

	/**
	 * Find topics that contain a link to the given topic name
	 * @param virtualWiki the virtual wiki to look in
	 * @param topicName the topic being searched for
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	public Collection findLinkedTo(String virtualWiki, String topicName) throws Exception {
		// create a set to hold the valid back linked topics
		Set results = new HashSet();
		// find all topics that actually mention the name of the topic in the text somewhere
		Collection all = doSearch(virtualWiki, topicName, false, false);
		// iterate the results from the general search
		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
			SearchResultEntry searchResultEntry = (SearchResultEntry) iterator.next();
			// the topic where the hit was is the topic that will potentially contain a link back to our topicName
			String topicFoundIn = searchResultEntry.getTopic();
			if (!topicName.equalsIgnoreCase(topicFoundIn)) {
				logger.debug("checking links in topic " + topicFoundIn + " to " + topicName);
				// read the raw content of the topic the hit was in
				String topicContents = WikiBase.getInstance().readRaw(virtualWiki, topicFoundIn);
				StringReader reader = new StringReader(topicContents);
				BackLinkLex backLinkLex = new BackLinkLex(reader);
				// lex the whole file with a back link lexer that simply catalogues all the valid intrawiki links
				while (backLinkLex.yylex() != null) ;
				reader.close();
				// get the intrawiki links
				List backLinks = backLinkLex.getLinks();
				logger.debug("links: " + backLinks);
				if (Utilities.containsStringIgnoreCase(backLinks, topicName)) {
					// only add the topic if there is an actual link
					results.add(searchResultEntry);
					logger.debug("'" + topicFoundIn + "' does contain a link to '" + topicName + "'");
				} else {
					logger.debug("'" + topicFoundIn + "' contains no link to '" + topicName + "'");
				}
			} else {
				// the topic itself does not count as a back link
				logger.debug("the topic itself is not a back link");
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
	 * @param fuzzy true, if fuzzy search should be used, false otherwise
	 *
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	public Collection findMultiple(String virtualWiki, String text, boolean fuzzy) {
		return doSearch(virtualWiki, text, true, true);
	}

	/**
	 * @param indexPath
	 */
	protected void initSearchEngine(ServletContext ctx) throws Exception {
		// Initialize the temp directory used to store search indexes.
		// In order to avoid collisions in the case of multiple deployments
		// of this very application, the temp directory supplied by the
		// servlet container (which is required to be private per servlet
		// context by ?? 3.7.1 of the Java Servlet Specification) is used
		// rather than the global temp directory as defined in the system
		// property 'java.io.tmpdir'.
		try {
			File tmpDir = (File) ctx.getAttribute("javax.servlet.context.tempdir");
			indexPath = tmpDir.getPath();
		} catch (Throwable t) {
			logger.warn("'javax.servlet.context.tempdir' attribute undefined or invalid, using java.io.tmpdir", t);
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
	 * Actually perform the search.
	 *
	 * @param virtualWiki The virtual wiki to use
	 * @param text The text to find
	 * @param caseInsensitiveSearch true, if case does not matter in search, false otherwise
	 *
	 * @return A collection of SearchResultEntry, containing the search results
	 */
	protected Collection doSearch(String virtualWiki, String text,
		boolean caseInsensitiveSearch, boolean doTextBeforeAndAfterParsing) {
		if (indexPath == null) {
			return Collections.EMPTY_LIST;
		}
		String indexFilename = getSearchIndexPath(virtualWiki);
		Analyzer analyzer = new SimpleKeepNumbersAnalyzer();
		Collection result = new ArrayList();
		logger.debug("search text: " + text);
		try {
			BooleanQuery query = new BooleanQuery();
			if (caseInsensitiveSearch) {
				query.add(QueryParser.parse(text, ITYPE_TOPIC, analyzer), false, false);
				query.add(QueryParser.parse(text, ITYPE_CONTENT, analyzer), false, false);
			} else {
				query.add(QueryParser.parse("\"" + text + "\"", ITYPE_TOPIC, analyzer), false, false);
				query.add(QueryParser.parse("\"" + text + "\"", ITYPE_CONTENT, analyzer), false, false);
			}
			Searcher searcher = new IndexSearcher(getIndexDirectory(indexFilename, false));
			// actually perform the search
			Hits hits = searcher.search(query);
			for (int i = 0; i < hits.length(); i++) {
				SearchResultEntry entry = new SearchResultEntry();
				entry.setTopic(hits.doc(i).get(ITYPE_TOPIC_PLAIN));
				entry.setRanking(hits.score(i));
				boolean canBeAdded = true;
				boolean found = false;
				if (doTextBeforeAndAfterParsing) {
					String content = hits.doc(i).get(ITYPE_CONTENT_PLAIN);
					if (content != null) {
						if (!caseInsensitiveSearch) {
							if (content.indexOf(text) != -1) {
								found = true;
							}
						} else {
							if (content.toLowerCase().indexOf(text.toLowerCase()) != -1) {
								found = true;
							}
							if (!found) {
								HashSet terms = new HashSet();
								LuceneTools.getTerms(query, terms, false);
								Token token;
								TokenStream stream = new SimpleKeepNumbersAnalyzer().tokenStream(ITYPE_CONTENT,
									new java.io.StringReader(content));
								while ((token = stream.next()) != null) {
									// does query contain current token?
									if (terms.contains(token.termText())) {
										found = true;
									}
								}
							}
							if (!found) {
								// we had a keyword hit
								int firstword = LuceneTools.findAfter(content, 1, 0);
								if (firstword == -1) {
									firstword = 0;
								}
								entry.setTextBefore("");
								entry.setFoundWord(content.substring(0, firstword));
								if ((firstword + 1) < content.length()) {
									firstword++;
								}
								int lastword = LuceneTools.findAfter(content, 1, 19);
								if (lastword < 0) {
									lastword = content.length();
								}
								if (firstword < 0) {
									firstword = 0;
								}
								entry.setTextAfter(content.substring(Math.min(firstword, lastword), Math.max(firstword, lastword)) + " ...");
							} else {
								// we had a regular hit
								String[] tempresult = LuceneTools.outputHits(hits.doc(i).get(ITYPE_CONTENT_PLAIN),
									query,
									new Analyzer[] {
										new SimpleKeepNumbersAnalyzer(),
										new SimpleKeepNumbersAnalyzer()
									}
								);
								entry.setTextBefore("... " + tempresult[0]);
								entry.setTextAfter(tempresult[2] + " ...");
								entry.setFoundWord(tempresult[1]);
							}
						}
					}
					if (!caseInsensitiveSearch && !found) {
						canBeAdded = false;
					}
				} else {
					canBeAdded = true;
					entry.setTextBefore("");
					entry.setTextAfter("");
					entry.setFoundWord(entry.getTopic());
				}
				if (canBeAdded) {
					result.add(entry);
				}
			}
		} catch (IOException e) {
			logger.warn("Error (IOExcpetion) while searching for " + text + "; Refreshing search index");
			SearchRefreshThread.refreshNow();
		} catch (Exception e) {
			logger.fatal("Excpetion while searching for " + text, e);
		}
		return result;
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
			reader.delete(new Term(ITYPE_TOPIC_PLAIN, topic));
			reader.close();
			directory.close();
			// add new document
			IndexWriter writer = new IndexWriter(directory, new SimpleKeepNumbersAnalyzer(), false);
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
		Collection allWikis = WikiBase.getInstance().getVirtualWikiList();
		if (!allWikis.contains(WikiBase.DEFAULT_VWIKI)) {
			allWikis.add(WikiBase.DEFAULT_VWIKI);
		}
		try {
			// check, if classes are here:
			Class.forName("vqwiki.utils.lucene.HTMLParser");
			canParseHTML = true;
		} catch (ClassNotFoundException e) {
			canParseHTML = false;
		}
		try {
			// check, if classes are here:
			Class.forName("org.pdfbox.pdfparser.PDFParser");
			canParsePDF = true;
		} catch (ClassNotFoundException e) {
			canParsePDF = false;
		}
		for (Iterator iterator = allWikis.iterator(); iterator.hasNext();) {
			String currentWiki = (String) iterator.next();
			logger.debug("indexing virtual wiki " + currentWiki);
			File indexFile = new File(indexPath, "index" + currentWiki);
			logger.debug("Index file path = " + indexFile);
			if (currentWiki.equals(WikiBase.DEFAULT_VWIKI)) {
				currentWiki = "";
			}
			int retrycounter = 0;
			do {
				// initially create index in ram
				RAMDirectory ram = new RAMDirectory();
				Analyzer analyzer = new SimpleKeepNumbersAnalyzer();
				IndexWriter writer = new IndexWriter(ram, analyzer, true);
				try {
					Collection topics = getAllTopicNames(currentWiki);
					for (Iterator iter = topics.iterator(); iter.hasNext();) {
						String topic = (String) iter.next();
						Document doc = createDocument(currentWiki, topic);
						writer.addDocument(doc);
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
			OutputStream os = index.createFile(ar[i]);
			// read current file
			InputStream is = ram.openFile(ar[i]);
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
	protected Document createDocument(String currentWiki, String topic) throws Exception {
		// get content
		StringBuffer contents = new StringBuffer(WikiBase.getInstance().getHandler().read(currentWiki,
			topic));
		// find attachments
		List attachments = extractByKeyword(contents, "attach:", true);
		// find links
		List links = new ArrayList();
		List linksNonsecure = extractByKeyword(contents, "http://", false);
		for (Iterator iter = linksNonsecure.iterator(); iter.hasNext();) {
			links.add("http://" + (String)iter.next());
		}
		List linksSecure = extractByKeyword(contents, "https://", false);
		for (Iterator iter = linksSecure.iterator(); iter.hasNext();) {
			links.add("https://" + (String)iter.next());
		}
		if (Environment.getBooleanValue(Environment.PROP_SEARCH_ATTACHMENT_INDEXING_ENABLED)) {
			for (Iterator iter = attachments.iterator(); iter.hasNext();) {
				String attachmentFileName = (String) iter.next();
				String extension = "";
				if (attachmentFileName.lastIndexOf('.') != -1) {
					extension = attachmentFileName.substring(attachmentFileName.lastIndexOf('.') + 1).toLowerCase();
				}
				File attachmentFile = Utilities.uploadPath(currentWiki, attachmentFileName);
				if ("txt".equals(extension) || "asc".equals(extension)) {
					StringBuffer textFileBuffer = Utilities.readFile(attachmentFile);
					contents.append(" ").append(textFileBuffer);
				}
				if (canParseHTML && ("htm".equals(extension) || "html".equals(extension))) {
					HTMLParser parser = new HTMLParser(attachmentFile);
					// Add the tag-stripped contents as a Reader-valued Text field so it will
					// get tokenized and indexed.
					contents.append(" ");
					Reader inStream = parser.getReader();
					while (true) {
						int read = inStream.read();
						if (read == -1) {
							break;
						}
						contents.append((char) read);
					}
					inStream.close();
				}
				if (canParsePDF && ("pdf".equals(extension))) {
					try {
						Class pdfclass = Class.forName("vqwiki.utils.lucene.PDFDocument");
						Object pdfdocument = pdfclass.newInstance();
						Method method = pdfclass.getMethod("getContentOfPDFFile", new Class[]{String.class, File.class});
						Object result = method.invoke(pdfdocument, new Object[]{attachmentFileName, attachmentFile});
						if (result instanceof StringBuffer) {
							contents.append((StringBuffer) result);
						}
					} catch (SecurityException e) {
						// Actually do nothing
					} catch (IllegalArgumentException e) {
						// Actually do nothing
					} catch (ClassNotFoundException e) {
						// Actually do nothing
					} catch (InstantiationException e) {
						// Actually do nothing
					} catch (IllegalAccessException e) {
						// Actually do nothing
					} catch (NoSuchMethodException e) {
						// Actually do nothing
					} catch (InvocationTargetException e) {
						// Actually do nothing
					}
				}
				// otherwise we cannot index it -> ignore it!
			}
			if (canParseHTML && Environment.getBooleanValue(Environment.PROP_SEARCH_EXTLINKS_INDEXING_ENABLED)) {
				for (Iterator iter = links.iterator(); iter.hasNext();) {
					try {
						String link = (String) iter.next();
						// get page
						HttpClient client = new HttpClient();
						//			establish a connection within 15 seconds
						client.setConnectionTimeout(15000);
						client.setTimeout(15000);
						HttpMethod method = new GetMethod(link);
						method.setFollowRedirects(true);
						client.executeMethod(method);
						HTMLParser parser = new HTMLParser(method.getResponseBodyAsStream());
						// Add the tag-stripped contents as a Reader-valued Text field so it will
						// get tokenized and indexed.
						contents.append(" ");
						Reader inStream = parser.getReader();
						while (true) {
							int read = inStream.read();
							if (read == -1) {
								break;
							}
							contents.append((char) read);
						}
						inStream.close();
					} catch (HttpException e) {
						// Actually do nothing
					} catch (IOException e) {
						// Actually do nothing
					} catch (IllegalArgumentException e) {
						// Actually do nothing
					}
				}
			}
		}
		// add remaining information
		String fileName = getFilename(currentWiki, topic);
		if (fileName != null) {
			logger.debug("Indexing topic " + topic + " in file " + fileName);
		} else {
			logger.debug("Indexing topic " + topic);
		}
		Document doc = new Document();
		doc.add(Field.Text(ITYPE_TOPIC, new StringReader(topic)));
		doc.add(Field.Keyword(ITYPE_TOPIC_PLAIN, topic));
		if (fileName != null) {
			doc.add(Field.UnIndexed(ITYPE_FILE, fileName));
		}
		doc.add(Field.Text(ITYPE_CONTENT, new StringReader(contents.toString())));
		doc.add(Field.UnIndexed(ITYPE_CONTENT_PLAIN, contents.toString()));
		return doc;
	}

	/**
	 * Get a list of all keywords in a given text. The list returned contains all words
	 * following the keyword. For example if the keyword is "attach:" all attachments
	 * are returned.
	 * @param contents The content to search
	 * @param keyword  The keyword to search
	 * @return A list of all words
	 */
	private ArrayList extractByKeyword(StringBuffer contents, String keyword, boolean possibleQuoted) {
		ArrayList returnList = new ArrayList();
		int attPos = contents.toString().indexOf(keyword);
		while (attPos != -1) {
			int endPos = attPos + keyword.length() + 1;
			boolean beginQuote = contents.charAt(attPos + keyword.length()) == '\"';
			while (endPos < contents.length()) {
				// attach: can have quotes, so we need a special handling if there are
				// begin and end quotes.
				if (possibleQuoted && beginQuote) {
					if (contents.charAt(endPos) == '\"' ||
						contents.charAt(endPos) == '\n' ||
						contents.charAt(endPos) == '\r') {
						attPos++;
						break;
					}
				} else if (contents.charAt(endPos) == ' ' ||
					contents.charAt(endPos) == ')' ||
					contents.charAt(endPos) == '|' ||
					contents.charAt(endPos) == '\"' ||
					contents.charAt(endPos) == '\n' ||
					contents.charAt(endPos) == '\r' ||
					contents.charAt(endPos) == '\t') {
					break;
				}
				endPos++;
			}
			returnList.add(contents.substring(attPos + keyword.length(), endPos));
			attPos = contents.toString().indexOf(keyword, endPos);
		}
		return returnList;
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

/*
 * Log:
 *
 * $Log$
 * Revision 1.23  2006/04/23 07:52:28  wrh2
 * Coding style updates (VQW-73).
 *
 * Revision 1.22  2006/04/20 01:32:17  wrh2
 * Use standard variable name for logger (VQW-73).
 *
 * Revision 1.21  2006/03/15 00:23:21  studer
 * Fixing bug http://www.vqwiki.org/jira/browse/VQW-26
 * Adding new parameter on the admin console to switch off indexing of http:-resources
 *
 * Revision 1.20  2006/03/01 17:34:57  studer
 * fixing http://www.vqwiki.org/jira/browse/VQW-51
 *
 * Revision 1.19  2006/02/28 21:23:40  studer
 * Humm.. a guess for the VQW-26 bug. Maybe it is something with the jdk and maybe it helps Pete...
 *
 * Revision 1.18  2006/01/31 21:57:13  studer
 * http://www.vqwiki.org/jira/browse/VQW-26
 * Fixed definitively a bug. Quoted attach:-files wheren't indexed at all. But I guess it's not the one which got us some problems?!
 *
 * Revision 1.17  2004/07/14 04:58:51  garethc
 * fix
 *
 * Revision 1.16  2004/06/28 09:42:06  mrgadget4711
 * Fix with searching referred HTML pages
 *
 * Revision 1.15  2004/06/24 18:55:22  mrgadget4711
 * ADD: The search engine now also searches external links
 *
 * Revision 1.14  2004/04/02 12:51:54  mrgadget4711
 * ADD: Ignore numbers, when doing a search
 *
 * Revision 1.13  2004/02/28 04:05:42  garethc
 * General bug fixes, panic on admin console
 *
 * Revision 1.12  2003/11/29 23:40:12  mrgadget4711
 * MOD: Using the PDFDocument by dynamic lookup. By this, you can
 * delete it if you need JDK 1.3.x compatibility.
 *
 * Revision 1.11  2003/11/29 21:53:08  mrgadget4711
 * MOD: In case, search throws an IOException, only give a warning
 * and do not dump the whole stacktrace.
 *
 * Revision 1.10  2003/11/29 21:24:26  mrgadget4711
 * MOD: catching a null pointer exception in PDFparser.close();
 *
 * Revision 1.9  2003/11/27 01:57:11  garethc
 * fixes
 *
 * Revision 1.8  2003/10/05 05:07:30  garethc
 * fixes and admin file encoding option + merge with contributions
 *
 * Revision 1.7  2003/09/12 14:06:43  makub
 * Made code JDK1.3 compatible by removing calls to StringBuffer.indexOf() and STring.split()
 *
 * Revision 1.6  2003/08/22 12:34:34  mrgadget4711
 * Search extendes, so that text attachments,
 * html attachments and pdf attachments are searched as well
 *
 * Revision 1.5  2003/08/20 20:45:45  mrgadget4711
 * Avoid scanning of result, if not needed
 *
 * Revision 1.3  2003/08/05 05:41:45  mrgadget4711
 * MOD: Wait up to 10 seconds until a lock is released
 * ADD: More specific log information
 *
 * Revision 1.2  2003/08/04 17:23:58  mrgadget4711
 * MOD: Use RAM to build up index, then copy it by brute force into the file system
 *
 * Revision 1.1  2003/08/04 09:06:47  mrgadget4711
 * MOD: Extracted all core search engine functionality into an AbstractSearchEngine
 * MOD: Try really hard to delete a lock
 *
 * ------------END------------
 */
