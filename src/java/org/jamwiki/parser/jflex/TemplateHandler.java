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
	private Hashtable parameterValues = new Hashtable();

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
		this.parseTemplateParameterValues(raw);
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
		defaultValue = raw.substring(pos, raw.length() - "}}}".length());
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
		Topic templateTopic = WikiBase.getHandler().lookupTopic(parserInput.getVirtualWiki(), name);
		if (templateTopic == null) {
			// FIXME - no need for an exception
			throw new Exception("Template " + name + " does not yet exist");
		}
		// FIXME - need check to avoid infinite nesting
		parserInput.setTemplateParameterValues(this.parameterValues);
		return ParserUtil.parseFragment(parserInput, templateTopic.getTopicContent());
		// FIXME - reset template parameter values
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
	private void parseTemplateParameterValues(String raw) throws Exception {
		raw = raw.substring("{{".length(), raw.length() - "}}".length());
		// strip template name
		int pos = raw.indexOf("|");
		if (pos == -1) {
			return;
		}
		raw = raw.substring(pos + 1);
		// any pipe after the name is a param
		pos = raw.indexOf("|");
		int count = 1;
		do {
			String name = new Integer(count).toString();
			String value = (pos != -1) ? raw.substring(0, pos) : raw;
			if (value.indexOf("=") != -1) {
				name = value.substring(0, value.indexOf("="));
				value = value.substring(value.indexOf("=") + 1);
			}
			this.parameterValues.put(name, value);
			if (pos != -1) raw = raw.substring(pos + 1);
			count++;
		} while ((pos = raw.indexOf("|")) != -1);
	}
}
