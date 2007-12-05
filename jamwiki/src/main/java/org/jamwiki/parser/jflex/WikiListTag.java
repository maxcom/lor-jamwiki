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
import java.util.List;
import java.util.Stack;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserTag;
import org.jamwiki.utils.WikiLogger;

/**
 * This class parses wiki list entries of the form <code>* list content</code>,
 * as well as lists using <code>:</code> and <code>#</code> or any combination
 * of the three.
 */
public class WikiListTag implements ParserTag {

	private static final WikiLogger logger = WikiLogger.getLogger(WikiListTag.class.getName());

	private static final String LIST_OPEN_STACK = "WikiListTag.LIST_OPEN_STACK";
	private static final String LIST_CLOSE_STACK = "WikiListTag.LIST_CLOSE_STACK";
	private static Hashtable listOpenHash = new Hashtable();
	private static Hashtable listCloseHash = new Hashtable();
	private static Hashtable listItemOpenHash = new Hashtable();

	static {
		listOpenHash.put("*", "<ul>");
		listOpenHash.put("#", "<ol>");
		listOpenHash.put(":", "<dl>");
		listOpenHash.put(";", "<dl>");
		listItemOpenHash.put("*", "<li>");
		listItemOpenHash.put("#", "<li>");
		listItemOpenHash.put(":", "<dd>");
		listItemOpenHash.put(";", "<dt>");
		listCloseHash.put("<ul>", "</ul>");
		listCloseHash.put("<ol>", "</ol>");
		listCloseHash.put("<dl>", "</dl>");
		listCloseHash.put("<li>", "</li>");
		listCloseHash.put("<dd>", "</dd>");
		listCloseHash.put("<dt>", "</dt>");
	}

	/**
	 *
	 */
	private String closeList(ParserInput parserInput) {
		Stack listOpenStack = retrieveStack(parserInput, LIST_OPEN_STACK);
		Stack listCloseStack = retrieveStack(parserInput, LIST_CLOSE_STACK);
		StringBuffer output = new StringBuffer();
		while (listOpenStack.size() > 0) {
			listOpenStack.pop();
			output.append(listCloseStack.pop());
		}
		updateStack(parserInput, listOpenStack, LIST_OPEN_STACK);
		updateStack(parserInput, listCloseStack, LIST_CLOSE_STACK);
		return output.toString();
	}

	/**
	 *
	 */
	private static boolean isListTag(char character) {
		String value = character + "";
		return (listOpenHash.get(value) != null);
	}

	/**
	 *
	 */
	private String listItem(ParserInput parserInput, String raw) {
		Stack listOpenStack = retrieveStack(parserInput, LIST_OPEN_STACK);
		Stack listCloseStack = retrieveStack(parserInput, LIST_CLOSE_STACK);
		StringBuffer output = new StringBuffer();
		// build a stack of html tags based on current values passed to lexer
		Stack currentOpenStack = tagsToStack(raw);
		String currentItemOpenTag = (String)currentOpenStack.peek();
		String currentItemCloseTag = (String)listCloseHash.get(currentItemOpenTag);
		// if list was previously open to a greater depth, close the old list
		while (listOpenStack.size() > currentOpenStack.size()) {
			listOpenStack.pop();
			output.append(listCloseStack.pop());
		}
		if (this.listStackEquals(currentOpenStack, listOpenStack)) {
			// if continuing same list close previous list item & open new item
			output.append(listCloseStack.pop());
			listCloseStack.push(currentItemCloseTag);
			output.append(currentItemOpenTag);
		} else {
			// look for differences in the old list stack and the new list stack
			int pos = 0;
			while (pos < listOpenStack.size()) {
				if (!this.listStackEquals(currentOpenStack.subList(0, pos+1), listOpenStack.subList(0, pos+1))) {
					break;
				}
				pos++;
			}
			// if any differences found process them
			while (listOpenStack.size() > pos) {
				listOpenStack.pop();
				output.append(listCloseStack.pop());
			}
			// continue processing differences
			for (int i=pos; i < currentOpenStack.size(); i++) {
				String openTag = (String)currentOpenStack.elementAt(i);
				String closeTag = (String)listCloseHash.get(openTag);
				listOpenStack.push(openTag);
				listCloseStack.push(closeTag);
				output.append(openTag);
			}
		}
		updateStack(parserInput, listOpenStack, LIST_OPEN_STACK);
		updateStack(parserInput, listCloseStack, LIST_CLOSE_STACK);
		return output.toString();
	}

	/**
	 *
	 */
	private boolean listStackEquals(List stack1, List stack2) {
		int pos = 0;
		String stack1Tag = "";
		String stack2Tag = "";
		if (stack1.size() != stack2.size()) {
			return false;
		}
		while (pos < stack1.size()) {
			stack1Tag = (String)stack1.get(pos);
			stack2Tag = (String)stack2.get(pos);
			if (!stack1Tag.equals(stack2Tag)) {
				// definition lists are sneaky - <dd> and <dt> must be considered equal
				if ((stack1Tag.equals("<dt>") || stack1Tag.equals("<dd>")) && (stack2Tag.equals("<dd>") || stack2Tag.equals("<dt>"))) {
					pos++;
					continue;
				}
				return false;
			}
			pos++;
		}
		return true;
	}

	/**
	 * Parse a Mediawiki list tag of the form "#*" and return the resulting
	 * HTML output.
	 */
	public String parse(ParserInput parserInput, ParserDocument parserDocument, int mode, String raw) throws Exception {
		if (raw == null || raw.length() == 0 || !isListTag(raw.charAt(0))) {
			return this.closeList(parserInput);
		}
		return listItem(parserInput, raw);
	}

	/**
	 *
	 */
	private Stack retrieveStack(ParserInput parserInput, String stackName) {
		Stack stack = (Stack)parserInput.getTempParams().get(stackName);
		if (stack == null) {
			stack = new Stack();
			parserInput.getTempParams().put(stackName, stack);
		}
		return stack;
	}

	/**
	 *
	 */
	private Stack tagsToStack(String raw) {
		int count = 0;
		for (int i=0; i < raw.length(); i++) {
			if (!isListTag(raw.charAt(i))) {
				break;
			}
			count++;
		}
		String tags = raw.substring(0, count);
		// build a stack of html tags based on current values passed to lexer
		Stack currentOpenStack = new Stack();
		for (int i=0; i < tags.length(); i++) {
			String tag = "" + tags.charAt(i);
			String listOpenTag = (String)listOpenHash.get(tag);
			String listItemOpenTag = (String)listItemOpenHash.get(tag);
			if (listOpenTag == null || listItemOpenTag == null) {
				logger.severe("Unknown list tag " + tag);
				continue;
			}
			currentOpenStack.push(listOpenTag);
			currentOpenStack.push(listItemOpenTag);
		}
		return currentOpenStack;
	}

	/**
	 *
	 */
	private void updateStack(ParserInput parserInput, Stack stack, String stackName) {
		if (stack.empty()) {
			parserInput.getTempParams().remove(stackName);
		} else {
			parserInput.getTempParams().put(stackName, stack);
		}
	}
}
