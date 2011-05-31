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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jamwiki.model.Namespace;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.ImageBorderEnum;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLink;
import org.jamwiki.utils.WikiLogger;

/**
 * Handle image galleries of the form <gallery>...</gallery>.
 */
public class GalleryTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(GalleryTag.class.getName());
	private static Pattern IMAGE_DIMENSION_PATTERN = Pattern.compile("([0-9]+)[ ]*(px)?", Pattern.CASE_INSENSITIVE);
	private static final int DEFAULT_IMAGES_PER_ROW = 4;
	private static final int DEFAULT_THUMBNAIL_MAX_DIMENSION = 120;

	/**
	 * Given a list of image links to display in the gallery, generate the
	 * gallery HTML.
	 */
	private String generateGalleryHtml(ParserInput parserInput, String raw, List<String> imageLinks) throws ParserException {
		if (imageLinks.isEmpty()) {
			// empty gallery tag
			return "";
		}
		// process the open tag to generate a list of attributes
		String openTag = raw.substring(0, raw.indexOf(">") + 1);
		HtmlTagItem htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(openTag);
		int width = this.retrieveDimension(htmlTagItem, "widths", DEFAULT_THUMBNAIL_MAX_DIMENSION);
		int height = this.retrieveDimension(htmlTagItem, "heights", DEFAULT_THUMBNAIL_MAX_DIMENSION);
		int perRow = NumberUtils.toInt(Utilities.getMapValueCaseInsensitive(htmlTagItem.getAttributes(), "perrow"), DEFAULT_IMAGES_PER_ROW);
		int count = 0;
		StringBuilder result = new StringBuilder("{| class=\"gallery\" cellspacing=\"0\" cellpadding=\"0\"\n");
		String caption = Utilities.getMapValueCaseInsensitive(htmlTagItem.getAttributes(), "caption");
		if (!StringUtils.isBlank(caption)) {
			result.append("|+ ").append(caption.trim()).append("\n");
		}
		result.append("|-\n");
		for (String imageLink : imageLinks) {
			count++;
			if (count != 1 && count % perRow == 1) {
				// new row
				result.append("|-\n");
			}
			result.append("| [[");
			result.append(imageLink).append('|');
			result.append(ImageBorderEnum._GALLERY).append('|');
			result.append(width).append('x').append(height).append("px");
			result.append("]]\n");
		}
		// add any blank columns that are necessary to fill out the last row
		if ((count % perRow) != 0) {
			for (int i = (perRow - (count % perRow)); i > 0; i--) {
				result.append("| &#160;\n");
			}
		}
		result.append("|}");
		return result.toString();
	}

	/**
	 * Process the contents of the gallery tag into a list of wiki link objects
	 * for the images in the gallery.  This method also updates the topic metadata,
	 * including any "link to" records, in the ParserOutput object.
	 */
	private List<String> generateImageLinks(ParserInput parserInput, ParserOutput parserOutput, int mode, String raw) throws ParserException {
		// get the tag contents as a list of wiki syntax for image thumbnails
		String content = JFlexParserUtil.tagContent(raw);
		List<String> imageLinks = new ArrayList<String>();
		if (!StringUtils.isBlank(content)) {
			String[] lines = content.split("\n");
			String imageLinkText;
			WikiLink wikiLink;
			for (String line : lines) {
				imageLinkText = "[[" + line.trim() + "]]";
				try {
					wikiLink = JFlexParserUtil.parseWikiLink(parserInput, null, imageLinkText);
				} catch (ParserException e) {
					// failure while parsing, the user may have entered invalid text
					logger.info("Invalid gallery entry " + line);
					continue;
				}
				if (!wikiLink.getNamespace().getId().equals(Namespace.FILE_ID)) {
					// not an image
					continue;
				}
				imageLinks.add(line);
			}
		}
		return imageLinks;
	}

	/**
	 * Parse a gallery tag of the form <gallery>...</gallery> and return the
	 * resulting HTML output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) throws ParserException {
		if (lexer.getMode() < JFlexParser.MODE_CUSTOM) {
			return raw;
		}
		// get the tag contents as a list of wiki syntax for image thumbnails.
		List<String> imageLinks = this.generateImageLinks(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), raw);
		// generate the gallery wiki text
		return this.generateGalleryHtml(lexer.getParserInput(), raw, imageLinks);
	}

	/**
	 * Utility method for converting a dimension of the form "50px" to an integer.
	 */
	private int retrieveDimension(HtmlTagItem htmlTagItem, String key, int defaultValue) {
		String value = Utilities.getMapValueCaseInsensitive(htmlTagItem.getAttributes(), key);
		if (StringUtils.isBlank(value)) {
			return defaultValue;
		}
		Matcher matcher = IMAGE_DIMENSION_PATTERN.matcher(value.trim());
		if (matcher.find()) {
			value = matcher.group(1);
		}
		return NumberUtils.toInt(value, defaultValue);
	}
}
