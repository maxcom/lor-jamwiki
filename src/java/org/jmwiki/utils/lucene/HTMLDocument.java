package org.jmwiki.utils.lucene;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *	notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *	notice, this list of conditions and the following disclaimer in
 *	the documentation and/or other materials provided with the
 *	distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *	if any, must include the following acknowledgment:
 *	   "This product includes software developed by the
 *		Apache Software Foundation (http://www.apache.org/)."
 *	Alternately, this acknowledgment may appear in the software itself,
 *	if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *	"Apache Lucene" must not be used to endorse or promote products
 *	derived from this software without prior written permission. For
 *	written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *	"Apache Lucene", nor may "Apache" appear in their name, without
 *	prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;
import java.io.IOException;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;

/**
 *
 */
public class HTMLDocument {
  /**
   * TODO: Document this field.
   */
  static char dirSep = System.getProperty("file.separator").charAt(0);

  /**
   *Creates a new HTMLDocument.
   */
  private HTMLDocument() {
  }

  /**
   * TODO: Document this method.
   *
   * @param f TODO: Document this parameter.
   * @return TODO: Document the result.
   * @exception IOException TODO: Document this exception.
   * @exception InterruptedException TODO: Document this exception.
   */
  public static Document Document(File f)
	  throws IOException, InterruptedException {
	// make a new, empty document
	Document doc = new Document();

	// Add the url as a field named "url".  Use an UnIndexed field, so
	// that the url is just stored with the document, but is not searchable.
	doc.add(new Field("url", f.getPath().replace(dirSep, '/'), Store.YES, Index.NO));

	// Add the last modified date of the file a field named "modified".  Use a
	// Keyword field, so that it's searchable, but so that no attempt is made
	// to tokenize the field into words.
	doc.add(new Field("modified", DateTools.timeToString(f.lastModified(), Resolution.SECOND), Store.YES, Index.UN_TOKENIZED));

	// Add the uid as a field, so that index can be incrementally maintained.
	// This field is not stored with document, it is indexed, but it is not
	// tokenized prior to indexing.
	doc.add(new Field("uid", uid(f), Store.NO, Index.NO));

	HTMLParser parser = new HTMLParser(f);

	// Add the tag-stripped contents as a Reader-valued Text field so it will
	// get tokenized and indexed.
	doc.add(new Field("contents", parser.getReader()));

	// Add the summary as an UnIndexed field, so that it is stored and returned
	// with hit documents for display.
	doc.add(new Field("summary", parser.getSummary(), Store.NO, Index.NO));

	// Add the title as a separate Text field, so that it can be searched
	// separately.
	doc.add(new Field("title", parser.getTitle(), Store.YES, Index.UN_TOKENIZED));

	// return the document
	return doc;
  }

  /**
   * TODO: Document this method.
   *
   * @param f TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static String uid(File f) {
	// Append path and date into a string in such a way that lexicographic
	// sorting gives the same results as a walk of the file hierarchy.  Thus
	// null (\u0000) is used both to separate directory components and to
	// separate the path from the date.
	return f.getPath().replace(dirSep, '\u0000') + "\u0000" + DateTools.timeToString(f.lastModified(), Resolution.SECOND);
  }

  /**
   * TODO: Document this method.
   *
   * @param uid TODO: Document this parameter.
   * @return TODO: Document the result.
   */
  public static String uid2url(String uid) {
	String url = uid.replace('\u0000', '/');
	// replace nulls with slashes
	return url.substring(0, url.lastIndexOf('/'));
	// remove date from end
  }
}

