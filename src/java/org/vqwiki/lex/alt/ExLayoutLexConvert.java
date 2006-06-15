package org.vqwiki.lex.alt;

import org.apache.log4j.Logger;
import org.vqwiki.Environment;

public class ExLayoutLexConvert
{
	private boolean even = false;

	private boolean list_ul = false;

	private int list_level = 0;

	private boolean multitableheader = false;

	protected static Logger cat = Logger.getLogger(ExLayoutLexConvert.class);

	protected String virtualWiki;

	public void setVirtualWiki(String vWiki)
	{
		this.virtualWiki = vWiki;
	}

	public String onTableCell(String text)
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<tr " + (even ? "class=\"even\"" : "class=\"odd\"") + ">");
		even = !even;
		int startindex = 0;
		int endindex = 0;
		while ((startindex = text.indexOf("||", startindex)) != -1)
		{
			endindex = text.indexOf("||", startindex + 2);
			if (endindex != -1)
			{
				sbuf.append("<td>");
				sbuf.append(text.substring(startindex + 2, endindex));
				sbuf.append("</td>");
				startindex = endindex;
			}
			else
			{
				startindex += 2;
			}
		}
		sbuf.append("</tr>\n");
		return sbuf.toString();
	}

	public String onTableEnd()
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</tbody></table>\n");
		return sbuf.toString();
	}

	public String onTableEOF()
	{
		return "</tbody></table>\n";
	}

	public String onTableStart(String text)
	{
		even = false;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<table><tbody>\n");
		sbuf.append(onTableCell(text));
		return sbuf.toString();
	}

	public String onTableStartWithHeader(String text)
	{
		even = false;
		StringBuffer sbuf2 = new StringBuffer(text);
		int index = 0;
		while (-1 != (index = sbuf2.indexOf("|!")))
		{
			sbuf2.replace(index, index + 2, "||");
		}
		text = sbuf2.toString();
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<table>\n");
		sbuf.append("<thead>\n");
		sbuf.append(onTableCell(text));
		sbuf.append("</thead><tbody>\n");
		return sbuf.toString();
	}

	public String onListBegin(String text)
	{
		text = text.trim();
		StringBuffer sbuf = new StringBuffer();
		// decide ordered or unordered list.
		if (text.charAt(0) == '*')
		{
			list_ul = true;
		}
		else
		{
			list_ul = false;
		}
		list_level = getListLevel(text);
		for (int i = list_level; i > 0; i--)
		{
			sbuf.append(list_ul ? "<ul>\n" : "<ol>\n");
		}
		sbuf.append("<li>");
		sbuf.append(text.substring(list_level));
		return sbuf.toString();
	}

	public String onListEnd()
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</li>");
		for (int i = list_level; i > 0; i--)
		{
			sbuf.append(list_ul ? "</ul>\n" : "</ol>\n");
		}
		return sbuf.toString();
	}

	public String onListEOF()
	{
		return onListEnd();
	}

	public String onListLine(String text)
	{
		text = text.trim();
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</li>");
		int count = getListLevel(text);
		int diff = count - list_level;
		if (diff != 0)
		{
			String html;
			// First decide to open or close lists.
			if (diff > 0)
			{
				html = list_ul ? "<ul>\n" : "<ol>\n";
			}
			else
			{
				html = list_ul ? "</ul>\n" : "</ol>\n";
			}
			// make the diff positive for the for-loop.
			diff = diff > 0 ? diff : -diff;
			// Print open or closing lists.
			while (diff-- > 0)
			{
				sbuf.append(html);
			}
			// change List-level global.
			list_level = count;
		}
		sbuf.append("<li>");
		sbuf.append(text.substring(list_level));
		return sbuf.toString();
	}

	private int getListLevel(String text)
	{
		// get the level of the list entry.
		int count = 0;
		while ((text.charAt(count) == '*') || (text.charAt(count) == '#'))
		{
			count++;
			if (count >= text.length())
				break;
		}
		return count;
	}

	public String onNewLine()
	{
		cat.debug("{newline}");
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < Environment.getIntValue(
				Environment.PROP_PARSER_NEW_LINE_BREAKS); i++)
			buffer.append("<br/>");
		buffer.append("\n");
		return buffer.toString();
	}

	public String onPreFormatBegin()
	{
		cat.debug("@@@@{newline} entering PRE");
		return "<pre>";
	}

	public String onPreFormatEnd()
	{
		cat.debug("{newline}x2 leaving pre");
		return "</pre>\n";
	}

	public String onRemoveWhitespace(String text)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < text.length(); i++)
		{
			buffer.append((int) text.charAt(i));
		}
		cat.debug("{whitespace} " + buffer.toString());
		return " ";
	}

	public String onKeepWhitespace(String text)
	{
		cat.debug("PRE, EXTERNAL {whitespace}");
		return text;
	}

	public String onHorizontalRuler()
	{
		cat.debug("{hr}");
		return "\n<hr>\n";
	}

	public String onMultiTableStart(String text)
	{
		multitableheader = false;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<table><tbody>\n");
		sbuf.append("<tr class=\"odd\">");
		// because the first row is odd, we need to set this
		// to even for the next row.
		even = true;
		sbuf.append(onMultiTableCell(text));
		return sbuf.toString();
	}

	public String onMultiTableRow(String text)
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</tr>\n");
		if (multitableheader)
		{
			sbuf.append("</thead>\n");
			sbuf.append("<tbody>\n");
			multitableheader = false;
		}
		sbuf.append("<tr " + (even ? "class=\"even\"" : "class=\"odd\"") + ">");
		even = !even;
		return sbuf.toString();
	}

	public String onMultiTableCell(String text)
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<td>");
		sbuf.append(text.substring(2));
		sbuf.append("</td>");
		return sbuf.toString();
	}

	public String onMultiTableEnd()
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("</tr>");
		if (multitableheader)
		{
			sbuf.append("</thead>\n");
		}
		else
		{
			sbuf.append("</tbody>\n");
		}
		sbuf.append("</table>\n");
		return sbuf.toString();
	}

	public String onMultiTableStartWithHeader(String text)
	{
		multitableheader = true;
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("<table>\n");
		sbuf.append("<thead>\n");
		sbuf.append("<tr class=\"odd\">");
		// because the first row is odd, we need to set this
		// to even for the next row.
		even = true;
		sbuf.append(onMultiTableCell(text));
		return sbuf.toString();
	}

	public String onMultiTableEOF()
	{
		return onMultiTableEnd();
	}
}
