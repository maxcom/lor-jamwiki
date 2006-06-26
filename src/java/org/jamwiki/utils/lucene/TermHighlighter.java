/**
 *
 */
package org.jamwiki.utils.lucene;

/**
 *
 */
public interface TermHighlighter {
  /**
   * Highlight an arbitrary term. For example, an HTML TermHighlighter could simply do:
   *
   * <p><dl><dt></dt><dd><code>return "&lt;b&gt;" + term + "&lt;/b&gt;";</code></dd></dl>
   *
   * @param term term text to highlight
   * @return highlighted term text
   */
  String highlightTerm(String term);
}
