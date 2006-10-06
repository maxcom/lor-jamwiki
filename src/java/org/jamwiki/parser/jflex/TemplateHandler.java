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

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 *
 */
public class TemplateHandler {

	private static WikiLogger logger = WikiLogger.getLogger(TemplateHandler.class.getName());
	private static Pattern LINK_PATTERN = null;
	private static Pattern TEMPLATE_PATTERN = null;
	private static Pattern NAME_PATTERN = null;
	private static Pattern TABLE_PATTERN = null;
	private Hashtable parameterValues = new Hashtable();

	static {
		try {
			LINK_PATTERN = Pattern.compile("(\\[{2}(.)+\\]{2})(.*)");
			TEMPLATE_PATTERN = Pattern.compile("(\\{{2,3}(.+)\\}{2,3})(.*)");
			NAME_PATTERN = Pattern.compile("(([^\\[\\{\\=]+)=)(.*)");
			TABLE_PATTERN = Pattern.compile("^((\\{\\|)(.+)^(\\|\\}))(.*)");
		} catch (Exception e) {
			logger.severe("Unable to compile pattern", e);
		}
	}

	/**
	 *
	 */
	public String applyParameter(ParserInput parserInput, ParserOutput parserOutput, String raw) throws Exception {
		this.parameterValues = parserInput.getTemplateParameterValues();
		if (parameterValues == null) return raw;
		String name = this.parseParamName(raw);
		String defaultValue = this.parseParamDefaultValue(parserInput, raw);
		String value = (String)this.parameterValues.get(name);
		if (value == null && defaultValue == null) return raw;
		return (value != null) ? value : defaultValue;
	}

	/**
	 *
	 */
	public String parse(ParserInput parserInput, ParserOutput parserOutput, String raw) throws Exception {
		if (!StringUtils.hasText(raw)) {
			throw new Exception("Empty template text");
		}
		if (!raw.startsWith("{{") || !raw.endsWith("}}")) {
			throw new Exception ("Invalid template text");
		}
		// extract the template name
		String name = this.parseTemplateName(raw);
		// set template parameter values
		this.parseTemplateParameterValues(parserInput, raw);
		return this.parseTemplateBody(parserInput, name);
	}

	/**
	 *
	 */
	private String parseParamDefaultValue(ParserInput parserInput, String raw) throws Exception {
		int pos = raw.indexOf("|");
		String defaultValue = null;
		if (pos == -1) {
			return null;
		}
		defaultValue = raw.substring(pos + 1, raw.length() - "}}}".length());
		return ParserUtil.parseFragment(parserInput, defaultValue);
	}

	/**
	 *
	 */
	private String parseParamName(String raw) throws Exception {
		int pos = raw.indexOf("|");
		String name = null;
		if (pos != -1) {
			name = raw.substring("{{{".length(), pos);
		} else {
			name = raw.substring("{{{".length(), raw.length() - "}}}".length());
		}
		name = name.trim();
		if (!StringUtils.hasText(name)) {
			// FIXME - no need for an exception
			throw new Exception("No parameter name specified");
		}
		return name;
	}

	/**
	 *
	 */
	private String parseTemplateBody(ParserInput parserInput, String name) throws Exception {
		// get the parsed template body
		Topic templateTopic = WikiBase.getHandler().lookupTopic(parserInput.getVirtualWiki(), name, false);
		if (templateTopic == null) {
			// FIXME - no need for an exception
			throw new Exception("Template " + name + " does not yet exist");
		}
		// FIXME - need check to avoid infinite nesting
		ParserInput input = new ParserInput(parserInput);
		input.setTemplateParameterValues(this.parameterValues);
		return ParserUtil.parseFragment(input, templateTopic.getTopicContent());
	}

	/**
	 *
	 */
	private String parseTemplateName(String raw) throws Exception {
		int pos = raw.indexOf("|");
		String name = null;
		if (pos != -1) {
			name = raw.substring("{{".length(), pos);
		} else {
			name = raw.substring("{{".length(), raw.length() - "}}".length());
		}
		name = name.trim();
		if (!StringUtils.hasText(name)) {
			// FIXME - no need for an exception
			throw new Exception("No template name specified");
		}
		if (!name.startsWith(WikiBase.NAMESPACE_TEMPLATE + WikiBase.NAMESPACE_SEPARATOR)) {
			name = WikiBase.NAMESPACE_TEMPLATE + WikiBase.NAMESPACE_SEPARATOR + name;
		}
		return name;
	}

	/**
	 *
	 */
	private void parseTemplateParameterValues(ParserInput parserInput, String raw) throws Exception {
		String content = raw.substring("{{".length(), raw.length() - "}}".length());
		// strip the template name
		int pos = content.indexOf("|");
		if (pos == -1) return;
		pos++;
		Matcher nameMatcher = null;
		Matcher linkMatcher = null;
		Matcher templateMatcher = null;
		Matcher tableMatcher = null;
		int count = 1;
		String value = "";
		String name = "";
		while (pos < content.length()) {
			if (!StringUtils.hasText(name)) {
				nameMatcher = NAME_PATTERN.matcher(content.substring(pos));
				if (nameMatcher.matches()) {
					name = nameMatcher.group(2);
					logger.severe("matched name " + name);
				} else {
					name = new Integer(count).toString();
				}
				pos += nameMatcher.group(1).length();
				continue;
			}
			linkMatcher = LINK_PATTERN.matcher(content.substring(pos));
			if (linkMatcher.matches()) {
				value += linkMatcher.group(1);
				pos += linkMatcher.group(1).length();
				logger.severe("matched link " + linkMatcher.group(1));
				continue;
			}
			templateMatcher = TEMPLATE_PATTERN.matcher(content.substring(pos));
			if (templateMatcher.matches()) {
				value += templateMatcher.group(1);
				pos += templateMatcher.group(1).length();
				logger.severe("matched template " + templateMatcher.group(1));
				continue;
			}
			tableMatcher = TABLE_PATTERN.matcher(content.substring(pos));
			if (tableMatcher.matches()) {
				value += tableMatcher.group(1);
				pos += tableMatcher.group(1).length();
				logger.severe("matched table " + tableMatcher.group(1));
				continue;
			}
			if (content.charAt(pos) == '|') {
				value = ParserUtil.parseFragment(parserInput, value);
				this.parameterValues.put(name, value);
				count++;
				name = "";
				value = "";
				pos++;
				continue;
			}
			value += content.charAt(pos);
			pos++;
		}
		if (StringUtils.hasText(name) && StringUtils.hasText(value)) {
			value = ParserUtil.parseFragment(parserInput, value);
			this.parameterValues.put(name, value);
		}
	}
}
