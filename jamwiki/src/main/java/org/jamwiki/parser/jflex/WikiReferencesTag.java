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
package org.jamwiki.parser.jflex;

import java.util.Vector;
import org.jamwiki.model.WikiReference;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * This class parses nowiki tags of the form <code>&lt;references /&gt;</code>.
 */
public class WikiReferencesTag implements ParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiReferencesTag.class.getName());

	/**
	 *
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		if (mode < JFlexParser.MODE_LAYOUT) {
			return raw;
		}
		// retrieve all references, then loop through in order, building an HTML
		// reference list for display.  While looping, if there are multiple citations
		// for the same reference then include those in the output as well.
		Vector references = this.retrieveReferences(parserInput);
		String html = (!references.isEmpty()) ? "<ol class=\"references\">" : "";
		while (!references.isEmpty()) {
			WikiReference reference = (WikiReference)references.elementAt(0);
			references.removeElementAt(0);
			html += "<li id=\"" + reference.getNotationName() + "\">";
			html += "<sup>";
			int pos = 0;
			Vector citations = new Vector();
			while (pos < references.size()) {
				WikiReference temp = (WikiReference)references.elementAt(pos);
				if (temp.getName() != null && reference.getName() != null && reference.getName().equals(temp.getName())) {
					citations.add(temp);
					if (!StringUtils.hasText(reference.getContent()) && StringUtils.hasText(temp.getContent())) {
						reference.setContent(temp.getContent());
					}
					references.removeElementAt(pos);
					continue;
				}
				pos++;
			}
			if (!citations.isEmpty()) {
				html += "<a href=\"#" + reference.getReferenceName() + "\" title=\"\">";
				html += reference.getCitation() + "." + reference.getCount() + "</a>&#160;";
				while (!citations.isEmpty()) {
					WikiReference citation = (WikiReference)citations.elementAt(0);
					html += "&#160;<a href=\"#" + citation.getReferenceName() + "\" title=\"\">";
					html += citation.getCitation() + "." + citation.getCount() + "</a>&#160;";
					citations.removeElementAt(0);
				}
			} else {
				html += "<a href=\"#" + reference.getReferenceName() + "\" title=\"\">";
				html += reference.getCitation() + "</a>&#160;";
			}
			html += "</sup>";
			html += ParserUtil.parseFragment(parserInput, reference.getContent(), JFlexParser.MODE_PROCESS);
			html += "</li>";
		}
		html += (!references.isEmpty()) ? "</ol>" : "";
		return html;
	}

	/**
	 *
	 */
	private Vector retrieveReferences(ParserInput parserInput) {
		Vector references = (Vector)parserInput.getTempParams().get(WikiReferenceTag.REFERENCES_PARAM);
		if (references == null) {
			references = new Vector();
			parserInput.getTempParams().put(WikiReferenceTag.REFERENCES_PARAM, references);
		}
		return references;
	}
}
