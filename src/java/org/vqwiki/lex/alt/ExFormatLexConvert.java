package org.vqwiki.lex.alt;

import org.apache.log4j.Logger;

public class ExFormatLexConvert
{
	protected boolean em, strong, underline, center, table, row, cell, allowHtml, code, h1, h2, h3,
			color;

	protected int listLevel;

	protected boolean ordered;

	private String virtualwiki;

	protected static Logger cat = Logger.getLogger(ExFormatLexConvert.class);



	public String onBold(String string)
	{
		cat.debug("'''");
		if (strong)
		{
			strong = false;
			return ("</strong>");
		}
		else
		{
			strong = true;
			return ("<strong>");
		}
	}

	public String onColorEnd(String string)
	{
		if (color)
		{
			cat.debug("color end");
			color = false;
			return ("</font>");
		}
		else
		{
			return string;
		}
	}

	public String onColorStart(String string)
	{
		cat.debug("color start");
		StringBuffer sb = new StringBuffer();
		if (color)
		{
			sb.append("</font>");
		}
		color = true;
		sb.append("<font color=\"").append(string.substring(1, string.length() - 1)).append("\">");
		return sb.toString();
	}

	public String onCenter(String string)
	{
		cat.debug("::");
		if (center)
		{
			center = false;
			return ("</div>");
		}
		else
		{
			center = true;
			return ("<div align=\"center\">");
		}
	}

	public String onHeadlineTwo(String string)
	{
		cat.debug("!!...!!");
		return "<h2>" + string.substring(2, string.substring(2).indexOf('!') + 2) + "</h2>\r";
	}

	public String onHeadlineThree(String string)
	{
		cat.debug("!...!");
		return "<h3>" + string.substring(1, string.substring(1).indexOf('!') + 1) + "</h3>\r";
	}

	public String onNbsp()
	{
		return "&nbsp;";
	}

	public String onItalic(String string)
	{
		cat.debug("''");
		if (em)
		{
			em = false;
			return ("</em>");
		}
		else
		{
			em = true;
			return ("<em>");
		}
	}

	public String onCodeEnd()
	{
		return "</code>";
	}

	public String onUnderline(String string)
	{
		cat.debug("===");
		if (underline)
		{
			underline = false;
			return ("</u>");
		}
		else
		{
			underline = true;
			return ("<u>");
		}
	}

	public String onEndOfLine(String string)
	{
		cat.debug("{newline}");
		if (h1)
		{
			h1 = false;
			return ("</h1>");
		}
		if (h2)
		{
			h2 = false;
			return ("</h2>");
		}
		if (h3)
		{
			h3 = false;
			return ("</h3>");
		}
		return string;
	}

	public String onHeadlineOne(String string)
	{
		cat.debug("!!!...!!!");
		return "<h1>" + string.substring(3, string.substring(3).indexOf('!') + 3) + "</h1>\r";
	}

	public String onCodeBegin()
	{
		return "<code>";
	}

	public String onNewline()
	{
		return "<br/>";
	}

	public String onEOF()
	{
		if (strong)
		{
			strong = false;
			return ("</strong>");
		}
		if (em)
		{
			em = false;
			return ("</em>");
		}
		return null;
	}

	public void setVirtualWiki(String wiki)
	{
		this.virtualwiki = wiki;
	}
}
