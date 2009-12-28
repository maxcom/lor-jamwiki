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

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.jamwiki.utils.WikiUtil;

/**
 * <code>TemplateTag</code> parses Mediawiki template syntax, which allows
 * programmatic structures to be embedded in wiki syntax.
 */
public class TemplateTag implements JFlexParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(TemplateTag.class.getName());
	protected static final String TEMPLATE_INCLUSION = "template-inclusion";
	private static Pattern PARAM_NAME_VALUE_PATTERN = Pattern.compile("[\\s]*([A-Za-z0-9_\\ \\-]+)[\\s]*\\=([\\s\\S]*)");

	private final HashMap<String, String> parameterValues = new HashMap<String, String>();

	/**
	 * Once the template call has been parsed and the template values have been
	 * determined, parse the template body and apply those template values.
	 * Parameters may be embedded or have default values, so there is some
	 * voodoo magic that happens here to first parse any embedded values, and
	 * to apply default values when no template value has been set.
	 */
	private String applyParameter(ParserInput parserInput, String param) throws ParserException {
		if (this.parameterValues == null) {
			return param;
		}
		String content = param.substring("{{{".length(), param.length() - "}}}".length());
		// re-parse in case of embedded templates or params
		content = this.parseTemplateBody(parserInput, content);
		String name = this.parseParamName(content);
		String defaultValue = this.parseParamDefaultValue(parserInput, content);
		String value = this.parameterValues.get(name);
		if (value == null && defaultValue == null) {
			return param;
		}
		return (value == null) ? defaultValue : value;
	}

	/**
	 * Parse a call to a Mediawiki template of the form "{{template|param1|param2}}"
	 * and return the resulting template output.
	 */
	public String parse(JFlexLexer lexer, String raw, Object... args) {
		try {
			lexer.getParserInput().incrementTemplateDepth();
			// validate and extract the template content
			if (StringUtils.isBlank(raw)) {
				throw new Exception("Empty template text");
			}
			if (!raw.startsWith("{{") || !raw.endsWith("}}")) {
				throw new Exception ("Invalid template text: " + raw);
			}
			String templateContent = raw.substring("{{".length(), raw.length() - "}}".length());
			// parse for nested templates, signatures, etc.
			templateContent = JFlexParserUtil.parseFragment(lexer.getParserInput(), templateContent, lexer.getMode());
			// update the raw value to handle cases such as a signature in the template content
			raw = "{{" + templateContent + "}}";
			// check for magic word or parser function
			String[] parserFunctionInfo = ParserFunctionUtil.parseParserFunctionInfo(templateContent);
			if (MagicWordUtil.isMagicWord(templateContent) || parserFunctionInfo != null) {
				if (lexer.getMode() <= JFlexParser.MODE_MINIMAL) {
					lexer.getParserInput().decrementTemplateDepth();
					return raw;
				}
				lexer.getParserInput().decrementTemplateDepth();
				if (MagicWordUtil.isMagicWord(templateContent)) {
					return MagicWordUtil.processMagicWord(lexer.getParserInput(), templateContent);
				} else {
					return ParserFunctionUtil.processParserFunction(lexer.getParserInput(), parserFunctionInfo[0], parserFunctionInfo[1]);
				}
			}
			// extract the template name
			String name = this.parseTemplateName(templateContent);
			boolean inclusion = false;
			if (name.startsWith(NamespaceHandler.NAMESPACE_SEPARATOR)) {
				name = name.substring(1);
				inclusion = true;
			}
			// get the parsed template body
			Topic templateTopic = WikiBase.getDataHandler().lookupTopic(lexer.getParserInput().getVirtualWiki(), name, false, null);
			this.processTemplateMetadata(lexer.getParserInput(), lexer.getParserOutput(), templateTopic, raw, name);
			if (lexer.getMode() <= JFlexParser.MODE_MINIMAL) {
				lexer.getParserInput().decrementTemplateDepth();
				return raw;
			}
			// make sure template was not redirected
			if (templateTopic != null && templateTopic.getTopicType() == Topic.TYPE_REDIRECT) {
				templateTopic = WikiUtil.findRedirectedTopic(templateTopic, 0);
				name = templateTopic.getName();
			}
			if (templateTopic != null && templateTopic.getTopicType() == Topic.TYPE_REDIRECT) {
				// redirection target does not exist
				templateTopic = null;
			}
			if (inclusion) {
				String output = this.processTemplateInclusion(lexer.getParserInput(), lexer.getParserOutput(), lexer.getMode(), templateTopic, raw, name);
				lexer.getParserInput().decrementTemplateDepth();
				return output;
			}
			String output = this.processTemplateContent(lexer.getParserInput(), lexer.getParserOutput(), templateTopic, templateContent, name);
			lexer.getParserInput().decrementTemplateDepth();
			return output;
		} catch (Throwable t) {
			logger.info("Unable to parse " + raw, t);
			return raw;
		}
	}

	/**
	 * Given template parameter content of the form "name" or "name|default",
	 * return the default value if it exists.
	 */
	private String parseParamDefaultValue(ParserInput parserInput, String raw) throws ParserException {
		List<String> tokens = JFlexParserUtil.tokenizeParamString(raw);
		if (tokens.size() < 2) {
			return null;
		}
		// table elements mess up default processing, so just return anything after
		// the first parameter to avoid having to implement special table logic
		String param1 = tokens.get(0);
		String value = raw.substring(param1.length() + 1);
		return JFlexParserUtil.parseFragment(parserInput, value, JFlexParser.MODE_PREPROCESS);
	}

	/**
	 * Given template parameter content of the form "name" or "name|default",
	 * return the parameter name.
	 */
	private String parseParamName(String raw) throws ParserException {
		int pos = raw.indexOf('|');
		String name = ((pos != -1) ? raw.substring(0, pos) : raw).trim();
		if (StringUtils.isBlank(name)) {
			// FIXME - no need for an exception
			throw new ParserException("No parameter name specified");
		}
		return name;
	}

	/**
	 * After template parameter values have been set, process the template body
	 * and replace parameters with parameter values or defaults, processing any
	 * embedded parameters or templates.
	 */
	private String parseTemplateBody(ParserInput parserInput, String content) throws ParserException {
		StringBuilder output = new StringBuilder();
		int pos = 0;
		while (pos < content.length()) {
			String substring = content.substring(pos);
			if (substring.startsWith("{{{")) {
				// template
				int endPos = Utilities.findMatchingEndTag(content, pos, "{{{", "}}}");
				if (endPos != -1) {
					String param = content.substring(pos, endPos);
					output.append(this.applyParameter(parserInput, param));
				}
				pos = endPos;
			} else {
				output.append(content.charAt(pos));
				pos++;
			}
		}
		return JFlexParserUtil.parseFragment(parserInput, output.toString(), JFlexParser.MODE_PREPROCESS);
	}

	/**
	 * Given a template call of the form "{{template|param|param}}", return
	 * the template name.
	 */
	private String parseTemplateName(String raw) throws ParserException {
		String name = raw;
		int pos = raw.indexOf('|');
		if (pos != -1) {
			name = name.substring(0, pos);
		}
		name = Utilities.decodeTopicName(name.trim(), true);
		if (StringUtils.isBlank(name)) {
			// FIXME - no need for an exception
			throw new ParserException("No template name specified");
		}
		if (name.startsWith(NamespaceHandler.NAMESPACE_SEPARATOR)) {
			if (name.length() == 1) {
				// FIXME - no need for an exception
				throw new ParserException("No template name specified");
			}
		} else if (!name.startsWith(NamespaceHandler.NAMESPACE_TEMPLATE + NamespaceHandler.NAMESPACE_SEPARATOR)) {
			name = NamespaceHandler.NAMESPACE_TEMPLATE + NamespaceHandler.NAMESPACE_SEPARATOR + StringUtils.capitalize(name);
		}
		return name;
	}

	/**
	 * Given a template call of the form "{{name|param=value|param=value}}"
	 * parse the parameter names and values.
	 */
	private void parseTemplateParameterValues(ParserInput parserInput, String templateContent) throws ParserException {
		List<String> tokens = JFlexParserUtil.tokenizeParamString(templateContent);
		if (tokens.isEmpty()) {
			throw new ParserException("No template name found in " + templateContent);
		}
		int count = -1;
		for (String token : tokens) {
			count++;
			if (count == 0) {
				// first token is template name
				continue;
			}
			String[] nameValue = this.tokenizeNameValue(token);
			String name = (StringUtils.isBlank(nameValue[0]) ? Integer.toString(count) : nameValue[0].trim());
			String value = (nameValue[1] == null) ? null : nameValue[1].trim();
			this.parameterValues.put(name, value);
		}
	}

	/**
	 * Given a template call of the form "{{name|param|param}}" return the
	 * parsed output.
	 */
	private String processTemplateContent(ParserInput parserInput, ParserOutput parserOutput, Topic templateTopic, String templateContent, String name) throws ParserException {
		if (templateTopic == null) {
			return "[[" + name + "]]";
		}
		// set template parameter values
		this.parseTemplateParameterValues(parserInput, templateContent);
		return this.parseTemplateBody(parserInput, templateTopic.getTopicContent());
	}

	/**
	 * Given a template call of the form "{{:name}}" parse the template
	 * inclusion.
	 */
	private String processTemplateInclusion(ParserInput parserInput, ParserOutput parserOutput, int mode, Topic templateTopic, String raw, String name) throws ParserException {
		if (templateTopic == null) {
			return "[[" + name + "]]";
		}
		// FIXME - disable section editing
		parserInput.getTempParams().put(TEMPLATE_INCLUSION, "true");
		return (StringUtils.isBlank(templateTopic.getTopicContent())) ? templateTopic.getTopicContent() : JFlexParserUtil.parseFragment(parserInput, templateTopic.getTopicContent(), mode);
	}

	/**
	 * Process template values, setting link and other metadata output values.
	 */
	private void processTemplateMetadata(ParserInput parserInput, ParserOutput parserOutput, Topic templateTopic, String raw, String name) {
		name = (templateTopic != null) ? templateTopic.getName() : name;
		parserOutput.addLink(name);
		parserOutput.addTemplate(name);
	}

	/**
	 *
	 */
	private String[] tokenizeNameValue(String content) {
		String[] results = new String[2];
		results[0] = null;
		results[1] = content;
		Matcher m = PARAM_NAME_VALUE_PATTERN.matcher(content);
		if (m.matches()) {
			results[0] = m.group(1);
			results[1] = m.group(2);
		}
		return results;
	}
}
