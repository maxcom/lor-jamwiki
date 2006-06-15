package org.vqwiki.lex;

/*
Very Quick Wiki - WikiWikiWeb clone
Copyright (C) 2001 Gareth Cronin
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import org.apache.log4j.Category;
import org.vqwiki.Environment;
import org.vqwiki.WikiBase;
import org.vqwiki.utils.JSPUtils;


public class Yylex implements org.vqwiki.lex.Lexer {
  private final int YY_BUFFER_SIZE = 512;
  private final int YY_F = -1;
  private final int YY_NO_STATE = -1;
  private final int YY_NOT_ACCEPT = 0;
  private final int YY_START = 1;
  private final int YY_END = 2;
  private final int YY_NO_ANCHOR = 4;
  private final int YY_BOL = 256;
  private final int YY_EOF = 257;

  protected boolean em, strong, unordered, ordered, table, row, pre, cell;
  protected static Category cat = Category.getInstance(Yylex.class);
  protected String virtualWiki;
  protected final static String mskb = "http://support.microsoft.com/default.aspx?scid=KB;EN-US;";

  protected boolean exists(String topic) {
	try {
	  return WikiBase.getInstance().exists(virtualWiki, topic);
	}
	catch (Exception err) {
	  cat.error(err);
	}
	return false;
  }

  public void setVirtualWiki(String vWiki) {
	this.virtualWiki = vWiki;
  }

  private java.io.BufferedReader yy_reader;
  private int yy_buffer_index;
  private int yy_buffer_read;
  private int yy_buffer_start;
  private int yy_buffer_end;
  private char yy_buffer[];
  private boolean yy_at_bol;
  private int yy_lexical_state;

  public Yylex(java.io.Reader reader) {
	this();
	if (null == reader) {
	  throw (new Error("Error: Bad input stream initializer."));
	}
	yy_reader = new java.io.BufferedReader(reader);
  }

  public Yylex(java.io.InputStream instream) {
	this();
	if (null == instream) {
	  throw (new Error("Error: Bad input stream initializer."));
	}
	yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
  }

  private Yylex() {
	yy_buffer = new char[YY_BUFFER_SIZE];
	yy_buffer_read = 0;
	yy_buffer_index = 0;
	yy_buffer_start = 0;
	yy_buffer_end = 0;
	yy_at_bol = true;
	yy_lexical_state = YYINITIAL;

	boolean allowHtml =
		Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML);
	if (allowHtml) {
	  yybegin(ALLOWHTML);
	}
	else {
	  yybegin(NORMAL);
	}
  }

  private boolean yy_eof_done = false;
  private final int ALLOWHTML = 2;
  private final int YYINITIAL = 0;
  private final int PRE = 3;
  private final int NORMAL = 1;
  private final int yy_state_dtrans[] = {
	0,
	34,
	118,
	119
  };

  private void yybegin(int state) {
	yy_lexical_state = state;
  }

  private int yy_advance()
	  throws java.io.IOException {
	int next_read;
	int i;
	int j;

	if (yy_buffer_index < yy_buffer_read) {
	  return yy_buffer[yy_buffer_index++];
	}

	if (0 != yy_buffer_start) {
	  i = yy_buffer_start;
	  j = 0;
	  while (i < yy_buffer_read) {
		yy_buffer[j] = yy_buffer[i];
		++i;
		++j;
	  }
	  yy_buffer_end = yy_buffer_end - yy_buffer_start;
	  yy_buffer_start = 0;
	  yy_buffer_read = j;
	  yy_buffer_index = j;
	  next_read = yy_reader.read(yy_buffer,
								 yy_buffer_read,
								 yy_buffer.length - yy_buffer_read);
	  if (-1 == next_read) {
		return YY_EOF;
	  }
	  yy_buffer_read = yy_buffer_read + next_read;
	}

	while (yy_buffer_index >= yy_buffer_read) {
	  if (yy_buffer_index >= yy_buffer.length) {
		yy_buffer = yy_double(yy_buffer);
	  }
	  next_read = yy_reader.read(yy_buffer,
								 yy_buffer_read,
								 yy_buffer.length - yy_buffer_read);
	  if (-1 == next_read) {
		return YY_EOF;
	  }
	  yy_buffer_read = yy_buffer_read + next_read;
	}
	return yy_buffer[yy_buffer_index++];
  }

  private void yy_move_end() {
	if (yy_buffer_end > yy_buffer_start &&
		'\n' == yy_buffer[yy_buffer_end - 1])
	  yy_buffer_end--;
	if (yy_buffer_end > yy_buffer_start &&
		'\r' == yy_buffer[yy_buffer_end - 1])
	  yy_buffer_end--;
  }

  private boolean yy_last_was_cr = false;

  private void yy_mark_start() {
	yy_buffer_start = yy_buffer_index;
  }

  private void yy_mark_end() {
	yy_buffer_end = yy_buffer_index;
  }

  private void yy_to_mark() {
	yy_buffer_index = yy_buffer_end;
	yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		('\r' == yy_buffer[yy_buffer_end - 1] ||
		'\n' == yy_buffer[yy_buffer_end - 1] ||
		2028/*LS*/ == yy_buffer[yy_buffer_end - 1] ||
		2029/*PS*/ == yy_buffer[yy_buffer_end - 1]);
  }

  private java.lang.String yytext() {
	return (new java.lang.String(yy_buffer,
								 yy_buffer_start,
								 yy_buffer_end - yy_buffer_start));
  }

  private int yylength() {
	return yy_buffer_end - yy_buffer_start;
  }

  private char[] yy_double(char buf[]) {
	int i;
	char newbuf[];
	newbuf = new char[2 * buf.length];
	for (i = 0; i < buf.length; ++i) {
	  newbuf[i] = buf[i];
	}
	return newbuf;
  }

  private final int YY_E_INTERNAL = 0;
  private final int YY_E_MATCH = 1;
  private java.lang.String yy_error_string[] = {
	"Error: Internal error.\n",
	"Error: Unmatched input.\n"
  };

  private void yy_error(int code, boolean fatal) {
	java.lang.System.out.print(yy_error_string[code]);
	java.lang.System.out.flush();
	if (fatal) {
	  throw new Error("Fatal Error.\n");
	}
  }

  private int[][] unpackFromString(int size1, int size2, String st) {
	int colonIndex = -1;
	String lengthString;
	int sequenceLength = 0;
	int sequenceInteger = 0;

	int commaIndex;
	String workString;

	int res[][] = new int[size1][size2];
	for (int i = 0; i < size1; i++) {
	  for (int j = 0; j < size2; j++) {
		if (sequenceLength != 0) {
		  res[i][j] = sequenceInteger;
		  sequenceLength--;
		  continue;
		}
		commaIndex = st.indexOf(',');
		workString = (commaIndex == -1) ? st :
			st.substring(0, commaIndex);
		st = st.substring(commaIndex + 1);
		colonIndex = workString.indexOf(':');
		if (colonIndex == -1) {
		  res[i][j] = Integer.parseInt(workString);
		  continue;
		}
		lengthString =
			workString.substring(colonIndex + 1);
		sequenceLength = Integer.parseInt(lengthString);
		workString = workString.substring(0, colonIndex);
		sequenceInteger = Integer.parseInt(workString);
		res[i][j] = sequenceInteger;
		sequenceLength--;
	  }
	}
	return res;
  }

  private int yy_acpt[] = {
	/* 0 */ YY_NOT_ACCEPT,
			/* 1 */ YY_NO_ANCHOR,
			/* 2 */ YY_NO_ANCHOR,
			/* 3 */ YY_NO_ANCHOR,
			/* 4 */ YY_NO_ANCHOR,
			/* 5 */ YY_NO_ANCHOR,
			/* 6 */ YY_NO_ANCHOR,
			/* 7 */ YY_NO_ANCHOR,
			/* 8 */ YY_NO_ANCHOR,
			/* 9 */ YY_NO_ANCHOR,
			/* 10 */ YY_NO_ANCHOR,
			/* 11 */ YY_NO_ANCHOR,
			/* 12 */ YY_NO_ANCHOR,
			/* 13 */ YY_NO_ANCHOR,
			/* 14 */ YY_NO_ANCHOR,
			/* 15 */ YY_NO_ANCHOR,
			/* 16 */ YY_NO_ANCHOR,
			/* 17 */ YY_NO_ANCHOR,
			/* 18 */ YY_NO_ANCHOR,
			/* 19 */ YY_NO_ANCHOR,
			/* 20 */ YY_NO_ANCHOR,
			/* 21 */ YY_NO_ANCHOR,
			/* 22 */ YY_NO_ANCHOR,
			/* 23 */ YY_NO_ANCHOR,
			/* 24 */ YY_NO_ANCHOR,
			/* 25 */ YY_NO_ANCHOR,
			/* 26 */ YY_NO_ANCHOR,
			/* 27 */ YY_NO_ANCHOR,
			/* 28 */ YY_NO_ANCHOR,
			/* 29 */ YY_NO_ANCHOR,
			/* 30 */ YY_NO_ANCHOR,
			/* 31 */ YY_NO_ANCHOR,
			/* 32 */ YY_NO_ANCHOR,
			/* 33 */ YY_NO_ANCHOR,
			/* 34 */ YY_NOT_ACCEPT,
			/* 35 */ YY_NO_ANCHOR,
			/* 36 */ YY_NO_ANCHOR,
			/* 37 */ YY_NO_ANCHOR,
			/* 38 */ YY_NO_ANCHOR,
			/* 39 */ YY_NO_ANCHOR,
			/* 40 */ YY_NO_ANCHOR,
			/* 41 */ YY_NO_ANCHOR,
			/* 42 */ YY_NO_ANCHOR,
			/* 43 */ YY_NO_ANCHOR,
			/* 44 */ YY_NO_ANCHOR,
			/* 45 */ YY_NOT_ACCEPT,
			/* 46 */ YY_NO_ANCHOR,
			/* 47 */ YY_NO_ANCHOR,
			/* 48 */ YY_NO_ANCHOR,
			/* 49 */ YY_NO_ANCHOR,
			/* 50 */ YY_NO_ANCHOR,
			/* 51 */ YY_NO_ANCHOR,
			/* 52 */ YY_NOT_ACCEPT,
			/* 53 */ YY_NO_ANCHOR,
			/* 54 */ YY_NO_ANCHOR,
			/* 55 */ YY_NO_ANCHOR,
			/* 56 */ YY_NO_ANCHOR,
			/* 57 */ YY_NO_ANCHOR,
			/* 58 */ YY_NOT_ACCEPT,
			/* 59 */ YY_NO_ANCHOR,
			/* 60 */ YY_NO_ANCHOR,
			/* 61 */ YY_NOT_ACCEPT,
			/* 62 */ YY_NO_ANCHOR,
			/* 63 */ YY_NOT_ACCEPT,
			/* 64 */ YY_NO_ANCHOR,
			/* 65 */ YY_NOT_ACCEPT,
			/* 66 */ YY_NO_ANCHOR,
			/* 67 */ YY_NOT_ACCEPT,
			/* 68 */ YY_NO_ANCHOR,
			/* 69 */ YY_NOT_ACCEPT,
			/* 70 */ YY_NO_ANCHOR,
			/* 71 */ YY_NOT_ACCEPT,
			/* 72 */ YY_NO_ANCHOR,
			/* 73 */ YY_NOT_ACCEPT,
			/* 74 */ YY_NO_ANCHOR,
			/* 75 */ YY_NOT_ACCEPT,
			/* 76 */ YY_NO_ANCHOR,
			/* 77 */ YY_NOT_ACCEPT,
			/* 78 */ YY_NO_ANCHOR,
			/* 79 */ YY_NOT_ACCEPT,
			/* 80 */ YY_NOT_ACCEPT,
			/* 81 */ YY_NOT_ACCEPT,
			/* 82 */ YY_NOT_ACCEPT,
			/* 83 */ YY_NOT_ACCEPT,
			/* 84 */ YY_NOT_ACCEPT,
			/* 85 */ YY_NOT_ACCEPT,
			/* 86 */ YY_NOT_ACCEPT,
			/* 87 */ YY_NOT_ACCEPT,
			/* 88 */ YY_NOT_ACCEPT,
			/* 89 */ YY_NOT_ACCEPT,
			/* 90 */ YY_NOT_ACCEPT,
			/* 91 */ YY_NOT_ACCEPT,
			/* 92 */ YY_NOT_ACCEPT,
			/* 93 */ YY_NOT_ACCEPT,
			/* 94 */ YY_NOT_ACCEPT,
			/* 95 */ YY_NOT_ACCEPT,
			/* 96 */ YY_NOT_ACCEPT,
			/* 97 */ YY_NOT_ACCEPT,
			/* 98 */ YY_NOT_ACCEPT,
			/* 99 */ YY_NOT_ACCEPT,
			/* 100 */ YY_NOT_ACCEPT,
			/* 101 */ YY_NOT_ACCEPT,
			/* 102 */ YY_NOT_ACCEPT,
			/* 103 */ YY_NOT_ACCEPT,
			/* 104 */ YY_NOT_ACCEPT,
			/* 105 */ YY_NOT_ACCEPT,
			/* 106 */ YY_NOT_ACCEPT,
			/* 107 */ YY_NOT_ACCEPT,
			/* 108 */ YY_NOT_ACCEPT,
			/* 109 */ YY_NOT_ACCEPT,
			/* 110 */ YY_NOT_ACCEPT,
			/* 111 */ YY_NOT_ACCEPT,
			/* 112 */ YY_NOT_ACCEPT,
			/* 113 */ YY_NOT_ACCEPT,
			/* 114 */ YY_NOT_ACCEPT,
			/* 115 */ YY_NOT_ACCEPT,
			/* 116 */ YY_NOT_ACCEPT,
			/* 117 */ YY_NOT_ACCEPT,
			/* 118 */ YY_NOT_ACCEPT,
			/* 119 */ YY_NOT_ACCEPT,
			/* 120 */ YY_NOT_ACCEPT,
			/* 121 */ YY_NOT_ACCEPT,
			/* 122 */ YY_NOT_ACCEPT,
			/* 123 */ YY_NOT_ACCEPT,
			/* 124 */ YY_NOT_ACCEPT,
			/* 125 */ YY_NO_ANCHOR,
			/* 126 */ YY_NO_ANCHOR,
			/* 127 */ YY_NO_ANCHOR,
			/* 128 */ YY_NO_ANCHOR,
			/* 129 */ YY_NOT_ACCEPT,
			/* 130 */ YY_NO_ANCHOR,
			/* 131 */ YY_NOT_ACCEPT,
			/* 132 */ YY_NO_ANCHOR,
			/* 133 */ YY_NOT_ACCEPT,
			/* 134 */ YY_NO_ANCHOR,
			/* 135 */ YY_NOT_ACCEPT,
			/* 136 */ YY_NOT_ACCEPT,
			/* 137 */ YY_NOT_ACCEPT,
			/* 138 */ YY_NOT_ACCEPT,
			/* 139 */ YY_NOT_ACCEPT,
			/* 140 */ YY_NOT_ACCEPT,
			/* 141 */ YY_NOT_ACCEPT,
			/* 142 */ YY_NO_ANCHOR,
			/* 143 */ YY_NOT_ACCEPT,
			/* 144 */ YY_NO_ANCHOR,
			/* 145 */ YY_NOT_ACCEPT,
			/* 146 */ YY_NO_ANCHOR,
			/* 147 */ YY_NOT_ACCEPT,
			/* 148 */ YY_NOT_ACCEPT,
			/* 149 */ YY_NOT_ACCEPT
  };
  private int yy_cmap[] = unpackFromString(1, 258,
										   "20:9,2,4,20:2,5,20:18,45,41,40,42,41:2,26,25,41:2,43,41:2,3,22,21,34:2,36,3" +
										   "4:7,19,27,28,41,29,41,44,30:16,39,30:9,41:4,1,33,11,37,35,31,16,9,23,6,12,2" +
										   "4,38,13,10,15,14,8,31:2,18,7,31:2,17,31:3,20:101,32:23,20:2,32:5,20,32,0:2")[0];

  private int yy_rmap[] = unpackFromString(1, 150,
										   "0,1,2,3,4,1:4,5,1,6,7,1:2,8,9,1:2,10,11,12,1,13,14,15,16,17,18,1,19,15,1,20" +
										   ",21,22,23,24,25,1:3,26,27,28,29,30,31,32,33,34,35,36,37,32,1,38,1,39,40,41," +
										   "42,1,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,25,61,62,63,64,6" +
										   "5,66,67,68,69,70,71,11,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,8" +
										   "9,16,90,91,92,93,19,94,95,96,97,35,98,28,99,100,60,97,101,102,103,104,105,1" +
										   "06,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121")[0];

  private int yy_nxt[][] = unpackFromString(122, 46,
											"1,-1:92,45,-1:48,9,36,-1:51,73,-1,143,-1:9,145,-1:26,80,54,-1:65,13,-1:24,1" +
											"5,37,-1:36,92,-1:8,37,-1:36,138,-1:8,55,-1:46,19:13,-1:4,19:2,-1:5,91,19:2," +
											"-1:2,19,-1,19:2,91,-1:7,20,129:2,-1:2,129:40,-1:5,39,-1:45,40,-1:45,41,-1:4" +
											"1,106,-1,106,-1:2,25:13,106:2,25,139,25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1," +
											"106:4,-1:35,26,-1,26,-1:15,27:13,-1:4,27:2,-1:5,113,27:2,-1:2,27,-1,27:2,11" +
											"3,-1:12,28:13,-1:4,28:2,-1:5,115,28:2,-1:2,28,-1,28:2,115,-1:7,30,-1,30,-1:" +
											"2,30:14,-1,30:11,-1:2,30:12,-1:5,57,-1:40,1,2,35,46,3,125,53,59,62,132,64,1" +
											"46,62:3,134,62:5,66,62:3,68,4,62,5,6,70,62:2,72,62,74,62:3,70,62:2,76,62,78" +
											",62,-1:42,7,8,-1:6,60,54,-1:82,138,-1:7,16,55,-1:41,106,-1,106,-1:2,25:13,1" +
											"06:2,25,139,31,25,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-1:5,120,130,-1:44" +
											",33,57,-1:41,129:3,-1:2,129:40,-1:3,52,-1:46,80,81,-1:44,127,38,-1:41,106,-" +
											"1,106,-1:2,25:3,31,25:9,106:2,25,139,25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1," +
											"106:4,-1:5,120,141,-1:44,128,44,-1:43,131,-1:49,58,-1:39,106,-1,106,-1:2,25" +
											":10,42,25:2,106:2,25,139,31,25,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-1:8," +
											"135,-1:54,61,-1:33,127,48,-1:53,82,-1:40,136,-1:48,65,-1:6,67,-1:18,69,-1:2" +
											"0,83,-1:54,10,-1:62,84,-1:32,11,-1:39,85,-1:32,75:13,-1:4,75:2,-1:6,75:2,-1" +
											":2,75,-1,75:2,-1:24,87,-1:34,77:13,-1:4,77:2,-1:5,77:2,-1:2,77:6,-1:16,88,-" +
											"1:71,149,-1:15,75:13,-1:4,75:2,-1:5,91,75:2,-1:2,75,-1,75:2,91,-1:48,12,-1:" +
											"9,77:13,-1:4,77:2,-1:5,77:2,-1,14,77:6,-1:50,79,-1:45,137,-1:5,16,38,-1:55," +
											"96,-1:43,98,-1:69,99,-1:38,100,-1:8,100,-1:17,101,-1:52,136,-1:35,102,-1:64" +
											",17,-1:45,18,-1:24,19:13,-1:4,19:2,-1:6,19:2,-1:2,19,-1,19:2,-1:49,103,-1:7" +
											",21,39,-1:58,136,97,-1:42,105,-1:30,106,-1,106,-1:2,106:19,-1,106:2,-1:2,10" +
											"6:10,-1,106:4,-1:8,107,-1:57,108,-1:32,109:13,-1:4,109:2,-1:6,109:2,-1:2,10" +
											"9,-1,109:2,-1:42,110,-1:37,22,-1:22,23,40,-1:44,24,41,-1:47,136,-1:39,106,-" +
											"1,106,-1:2,25:13,106:2,25,106,25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-" +
											"1:15,136,-1:70,112,-1:12,109:13,-1:4,109:2,-1:5,113,109:2,-1:2,109,-1,109:2" +
											",113,-1:12,114,-1:81,116,-1:9,27:13,-1:4,27:2,-1:6,27:2,-1:2,27,-1,27:2,-1:" +
											"26,117,-1:32,28:13,-1:4,28:2,-1:6,28:2,-1:2,28,-1,28:2,-1:49,29,-1:3,1,2,35" +
											",46,3,125,53,59,62,132,64,146,62:3,134,62:5,66,62:3,68,62:4,70,62:2,72,62,7" +
											"4,62:3,70,62:2,76,62,78,62,1,62,32,62,43,50,62:39,32,-1:4,121,122,-1:44,33," +
											"44,-1:44,128,51,-1:44,9,47,-1:41,106,-1,106,-1:2,25:9,42,25:3,106:2,25,139," +
											"25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-1:2,93,129:2,-1:2,129:40,-1:4," +
											"123,122,-1:43,94,-1:49,63,-1:45,86,-1:54,71,-1:37,95,-1:56,97,-1:70,104,-1:" +
											"43,111,-1:4,106,-1,106,-1:2,25:2,126,25:10,106:2,25,106,142,144,-1,106:2,-1" +
											":2,25:2,106:2,25:6,-1,106:4,-1:7,140:13,-1:4,140:2,-1:5,115,140:2,-1:2,140," +
											"-1,140:2,115,-1:10,121,124,-1:41,106,-1,106,-1:2,25:6,49,25:6,106:2,25,139," +
											"25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-1:8,89,-1:39,106,-1,106,-1:2,2" +
											"5:2,56,25:10,106:2,25,139,25:2,-1,106:2,-1:2,25:2,106:2,25:6,-1,106:4,-1:8," +
											"90,-1:45,133,-1:44,140:13,-1:4,140:2,-1:6,140:2,-1:2,140,-1,140:2,-1:37,147" +
											",-1:8,147,-1:25,148,-1:26");

  public String yylex()
	  throws java.io.IOException {
	int yy_lookahead;
	int yy_anchor = YY_NO_ANCHOR;
	int yy_state = yy_state_dtrans[yy_lexical_state];
	int yy_next_state = YY_NO_STATE;
	int yy_last_accept_state = YY_NO_STATE;
	boolean yy_initial = true;
	int yy_this_accept;

	yy_mark_start();
	yy_this_accept = yy_acpt[yy_state];
	if (YY_NOT_ACCEPT != yy_this_accept) {
	  yy_last_accept_state = yy_state;
	  yy_mark_end();
	}
	while (true) {
	  if (yy_initial && yy_at_bol)
		yy_lookahead = YY_BOL;
	  else
		yy_lookahead = yy_advance();
	  yy_next_state = YY_F;
	  yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
	  if (YY_EOF == yy_lookahead && true == yy_initial) {

		if (ordered) {
		  ordered = false;
		  return "</ol>";
		}
		if (unordered) {
		  unordered = false;
		  return "</ul>";
		}
		if (strong) {
		  strong = false;
		  return ("</strong>");
		}
		if (em) {
		  em = false;
		  return ("</em>");
		}
		if (pre) {
		  return ("</pre>");
		}
		return null;
	  }
	  if (YY_F != yy_next_state) {
		yy_state = yy_next_state;
		yy_initial = false;
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
		  yy_last_accept_state = yy_state;
		  yy_mark_end();
		}
	  }
	  else {
		if (YY_NO_STATE == yy_last_accept_state) {
		  throw (new Error("Lexical Error: Unmatched Input."));
		}
		else {
		  yy_anchor = yy_acpt[yy_last_accept_state];
		  if (0 != (YY_END & yy_anchor)) {
			yy_move_end();
		  }
		  yy_to_mark();
		  switch (yy_last_accept_state) {
			case 1:

			case -2:
			  break;
			case 2:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -3:
			  break;
			case 3:
			  {
				cat.debug("{newline}");
				return " ";
			  }
			case -4:
			  break;
			case 4:
			  {
				return "&amp;";
			  }
			case -5:
			  break;
			case 5:
			  {
				return "&lt;";
			  }
			case -6:
			  break;
			case 6:
			  {
				return "&gt;";
			  }
			case -7:
			  break;
			case 7:
			  {
				cat.debug("{tab}#");
				if (!ordered) {
				  ordered = true;
				  return "\n<ol>\n<li>";
				}
				return "\n<li>";
			  }
			case -8:
			  break;
			case 8:
			  {
				cat.debug("{newline}{tab}*");
				if (!unordered) {
				  unordered = true;
				  return "\n<ul>\n<li>";
				}
				return "\n<li>";
			  }
			case -9:
			  break;
			case 9:
			  {
				cat.debug("{newline}{newline}");
				if (!ordered && !unordered)
				  return "\n<br/><br/>\n";
			  }
			case -10:
			  break;
			case 10:
			  {
				if (cell) return "<br/>";
				return "//";
			  }
			case -11:
			  break;
			case 11:
			  {
				cat.debug("''");
				if (em) {
				  em = false;
				  return ("</em>");
				}
				else {
				  em = true;
				  return ("<em>");
				}
			  }
			case -12:
			  break;
			case 12:
			  {
				cat.debug("tablecellboundary");
				if (cell) {
				  return "</td><td>";
				}
				cell = true;
				return "<td>";
			  }
			case -13:
			  break;
			case 13:
			  {
				cat.debug("'''");
				if (strong) {
				  strong = false;
				  return ("</strong>");
				}
				else {
				  strong = true;
				  return ("<strong>");
				}
			  }
			case -14:
			  break;
			case 14:
			  {
				cat.debug("{link2}");
				if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_BACK_TICK)) {
				  cat.debug("No back-tick links allowed");
				  return yytext();
				}
				String link = yytext();
				link = link.substring(1);
				link = link.substring(0, link.length() - 1);
				if (exists(link)) {
				  return "<a href=\"Wiki?" + link.trim() + "\">" +
					  link + "</a>";
				}
				else {
				  return link + "<a href=\"Wiki?topic=" + link.trim() +
					  "&action=action_edit\">?</a>";
				}
			  }
			case -15:
			  break;
			case 15:
			  {
				cat.debug("tablerowend");
				return "</td></tr><tr><td>";
			  }
			case -16:
			  break;
			case 16:
			  {
				if (unordered) {
				  unordered = false;
				  return "</ul>\n<br/>\n";
				}
				if (ordered) {
				  ordered = false;
				  return "</ol>\n<br/>\n";
				}
				else
				  return "\n<br/><br/>\n";
			  }
			case -17:
			  break;
			case 17:
			  {
				return "&amp;lt;";
			  }
			case -18:
			  break;
			case 18:
			  {
				return "&amp;gt;";
			  }
			case -19:
			  break;
			case 19:
			  {
				cat.debug("{link} '" + yytext() + "'");
				String link = yytext();
				if (exists(link.trim())) {
				  return "<a href=\"Wiki?" + link.trim() + "\">" +
					  link + "</a>";
				}
				else {
				  return link + "<a href=\"Wiki?topic=" + link.trim() +
					  "&action=action_edit\">?</a>";
				}
			  }
			case -20:
			  break;
			case 20:
			  {
				return yytext().substring(2, yytext().length() - 2);
			  }
			case -21:
			  break;
			case 21:
			  {
				cat.debug("{hr}");
				return "\n<hr>\n";
			  }
			case -22:
			  break;
			case 22:
			  {
				return "&amp;amp;";
			  }
			case -23:
			  break;
			case 23:
			  {
				cat.debug("tableboundary");
				if (table) {
				  table = false;
				  cell = false;
				  return "</td></tr></table>";
				}
				table = true;
				return "<table border=\"1\"><tr><td>";
			  }
			case -24:
			  break;
			case 24:
			  {
				cat.debug("@@@@{newline} entering PRE");
				yybegin(PRE);
				return "<pre>";
			  }
			case -25:
			  break;
			case 25:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -26:
			  break;
			case 26:
			  {
				cat.debug("{mblink}");
				if (yytext().length() < 6) return "[bad Microsoft KB link]";
				String link = yytext().substring(5);
				return "<a href=\"" + mskb + link.trim() + "\">MicrosoftKB:" + link + "</a>";
			  }
			case -27:
			  break;
			case 27:
			  {
				cat.debug("{mblink}");
				if (yytext().length() < 4) return "[bad Meatball WikiLink]";
				String link = yytext().substring(3);
				return "<a href=\"http://usemod.com/cgi-bin/mb.pl?" + link.trim() + "\">MeatballWiki:" + link + "</a>";
			  }
			case -28:
			  break;
			case 28:
			  {
				cat.debug("{c2link}");
				if (yytext().length() < 4) return "[bad C2 WikiLink]";
				String link = yytext().substring(3);
				return "<a href=\"http://c2.com/cgi/wiki?" + link.trim() + "\">c2Wiki:" + link + "</a>";
			  }
			case -29:
			  break;
			case 29:
			  {
				cat.debug("tablerowend,tableboundary");
				table = false;
				cell = false;
				return "</td></tr></table>";
			  }
			case -30:
			  break;
			case 30:
			  {
				cat.debug("{attachment}");
				String displayLink = yytext();
				String attachmentName = displayLink.substring(7);
				String link = "Wiki?action=action_view_attachment&attachment=" +
					JSPUtils.encodeURL(attachmentName);
				return "<a href=\"" + link +
					"\" target=\"_blank\">att:" + attachmentName + "</a>";
			  }
			case -31:
			  break;
			case 31:
			  {
				cat.debug("{image}");
				String link = yytext();
				return "<img src=\"" + link.trim() + "\">";
			  }
			case -32:
			  break;
			case 32:
			  {
				return yytext();
			  }
			case -33:
			  break;
			case 33:
			  {
				cat.debug("{newline}x2 leaving pre");
				if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
				  yybegin(ALLOWHTML);
				}
				else {
				  yybegin(NORMAL);
				}
				return "</pre>";
			  }
			case -34:
			  break;
			case 35:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -35:
			  break;
			case 36:
			  {
				cat.debug("{newline}{newline}");
				if (!ordered && !unordered)
				  return "\n<br/><br/>\n";
			  }
			case -36:
			  break;
			case 37:
			  {
				cat.debug("tablerowend");
				return "</td></tr><tr><td>";
			  }
			case -37:
			  break;
			case 38:
			  {
				if (unordered) {
				  unordered = false;
				  return "</ul>\n<br/>\n";
				}
				if (ordered) {
				  ordered = false;
				  return "</ol>\n<br/>\n";
				}
				else
				  return "\n<br/><br/>\n";
			  }
			case -38:
			  break;
			case 39:
			  {
				cat.debug("{hr}");
				return "\n<hr>\n";
			  }
			case -39:
			  break;
			case 40:
			  {
				cat.debug("tableboundary");
				if (table) {
				  table = false;
				  cell = false;
				  return "</td></tr></table>";
				}
				table = true;
				return "<table border=\"1\"><tr><td>";
			  }
			case -40:
			  break;
			case 41:
			  {
				cat.debug("@@@@{newline} entering PRE");
				yybegin(PRE);
				return "<pre>";
			  }
			case -41:
			  break;
			case 42:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -42:
			  break;
			case 43:
			  {
				return yytext();
			  }
			case -43:
			  break;
			case 44:
			  {
				cat.debug("{newline}x2 leaving pre");
				if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
				  yybegin(ALLOWHTML);
				}
				else {
				  yybegin(NORMAL);
				}
				return "</pre>";
			  }
			case -44:
			  break;
			case 46:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -45:
			  break;
			case 47:
			  {
				cat.debug("{newline}{newline}");
				if (!ordered && !unordered)
				  return "\n<br/><br/>\n";
			  }
			case -46:
			  break;
			case 48:
			  {
				if (unordered) {
				  unordered = false;
				  return "</ul>\n<br/>\n";
				}
				if (ordered) {
				  ordered = false;
				  return "</ol>\n<br/>\n";
				}
				else
				  return "\n<br/><br/>\n";
			  }
			case -47:
			  break;
			case 49:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -48:
			  break;
			case 50:
			  {
				return yytext();
			  }
			case -49:
			  break;
			case 51:
			  {
				cat.debug("{newline}x2 leaving pre");
				if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
				  yybegin(ALLOWHTML);
				}
				else {
				  yybegin(NORMAL);
				}
				return "</pre>";
			  }
			case -50:
			  break;
			case 53:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -51:
			  break;
			case 54:
			  {
				cat.debug("{newline}{newline}");
				if (!ordered && !unordered)
				  return "\n<br/><br/>\n";
			  }
			case -52:
			  break;
			case 55:
			  {
				if (unordered) {
				  unordered = false;
				  return "</ul>\n<br/>\n";
				}
				if (ordered) {
				  ordered = false;
				  return "</ol>\n<br/>\n";
				}
				else
				  return "\n<br/><br/>\n";
			  }
			case -53:
			  break;
			case 56:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -54:
			  break;
			case 57:
			  {
				cat.debug("{newline}x2 leaving pre");
				if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
				  yybegin(ALLOWHTML);
				}
				else {
				  yybegin(NORMAL);
				}
				return "</pre>";
			  }
			case -55:
			  break;
			case 59:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -56:
			  break;
			case 60:
			  {
				cat.debug("{newline}{newline}");
				if (!ordered && !unordered)
				  return "\n<br/><br/>\n";
			  }
			case -57:
			  break;
			case 62:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -58:
			  break;
			case 64:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -59:
			  break;
			case 66:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -60:
			  break;
			case 68:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -61:
			  break;
			case 70:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -62:
			  break;
			case 72:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -63:
			  break;
			case 74:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -64:
			  break;
			case 76:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -65:
			  break;
			case 78:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -66:
			  break;
			case 125:
			  {
				cat.debug("{newline}");
				return " ";
			  }
			case -67:
			  break;
			case 126:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -68:
			  break;
			case 127:
			  {
				if (unordered) {
				  unordered = false;
				  return "</ul>\n<br/>\n";
				}
				if (ordered) {
				  ordered = false;
				  return "</ol>\n<br/>\n";
				}
				else
				  return "\n<br/><br/>\n";
			  }
			case -69:
			  break;
			case 128:
			  {
				cat.debug("{newline}x2 leaving pre");
				if (Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_HTML)) {
				  yybegin(ALLOWHTML);
				}
				else {
				  yybegin(NORMAL);
				}
				return "</pre>";
			  }
			case -70:
			  break;
			case 130:
			  {
				return yytext();
			  }
			case -71:
			  break;
			case 132:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -72:
			  break;
			case 134:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -73:
			  break;
			case 142:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -74:
			  break;
			case 144:
			  {
				cat.debug("{hyperlink}");
				String link = yytext();
				return "<a href=\"" + link.trim() + "\">" +
					link + "</a>";
			  }
			case -75:
			  break;
			case 146:
			  {
				cat.debug(". (" + yytext() + ")");
				return yytext();
			  }
			case -76:
			  break;
			default:
			  yy_error(YY_E_INTERNAL, false);
			case -1:
		  }
		  yy_initial = true;
		  yy_state = yy_state_dtrans[yy_lexical_state];
		  yy_next_state = YY_NO_STATE;
		  yy_last_accept_state = YY_NO_STATE;
		  yy_mark_start();
		  yy_this_accept = yy_acpt[yy_state];
		  if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		  }
		}
	  }
	}
  }
}
