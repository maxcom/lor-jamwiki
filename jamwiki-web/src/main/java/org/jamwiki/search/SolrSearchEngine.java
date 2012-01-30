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

import org.jamwiki.SearchEngine;
import org.jamwiki.model.SearchResultEntry;
import org.jamwiki.model.Topic;
import org.jamwiki.utils.WikiLogger;

import java.io.IOException;
import java.util.List;

/**
 * An implementation of {@link org.jamwiki.SearchEngine} that uses
 * <a href="http://lucene.apache.org/solr/">Apache Solr</a> to perform searches of
 * Wiki content.
 */
public class SolrSearchEngine implements SearchEngine {
  private static final WikiLogger logger = WikiLogger.getLogger(LuceneSearchEngine.class.getName());
  
  public void addToIndex(Topic topic) {
    
  }
  
  public void commit(String virtualWiki) {
    
  }
  
  public void deleteFromIndex(Topic topic) {

  }

  public List<SearchResultEntry> findResults(String virtualWiki, String text, List<Integer> namespaces) {
    return null;
  }

  public void refreshIndex() throws Exception {

  }

  public void setAutoCommit(boolean autoCommit) {

  }

  public void shutdown() throws IOException {

  }

  public void updateInIndex(Topic topic) {

  }
}
