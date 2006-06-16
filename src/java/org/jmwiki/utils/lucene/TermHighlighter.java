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
package org.jmwiki.utils.lucene;


/**
 * Highlights arbitrary terms.
 *
 * @version $Id: TermHighlighter.java 365 2003-10-05 05:07:32Z garethc $
 * @author Maik Schreiber (mailto: bZ@iq-computing.de)
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
