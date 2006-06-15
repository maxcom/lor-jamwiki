/**
 * Note that this only applies to the default format and link lexers
 *
 * @author garethc
 *  29/10/2002 16:23:39
 */
package org.vqwiki.lex;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.StringReader;

public class TestLexer extends TestCase {
  private static final Logger logger = Logger.getLogger(TestLexer.class);

  public TestLexer(String s) {
	super(s);
  }

  public void testFormat() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("Here '''is''' ''the'' '''first''' line");
	inputBuffer.append("\n");
	inputBuffer.append("Here is the second line");
	inputBuffer.append("\n");
	inputBuffer.append("\n");
	inputBuffer.append("Here is the third line following a break");

	StringBuffer expected = new StringBuffer();
	expected.append("Here <strong>is</strong> <em>the</em> <strong>first</strong> line<br/><br/>\nHere is the second line<br/><br/>\n<br/><br/>\nHere is the third line following a break");

	formatLexify(inputBuffer, expected);

	inputBuffer = new StringBuffer();
	inputBuffer.append("Here is the first line");
	inputBuffer.append("@@@@\n");
	inputBuffer.append("this is preformatted");
	inputBuffer.append("\n");
	inputBuffer.append("so is this");
	inputBuffer.append("\n");
	inputBuffer.append("\n");
	inputBuffer.append("this isn't");

	expected = new StringBuffer();
	expected.append("Here is the first line<pre>");
	expected.append("this is preformatted\n");
	expected.append("so is this</pre>\n");
	expected.append("this isn't");

	formatLexify(inputBuffer, expected);

  }

  public void testUnformat() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("__Here '''is'''__ ''the'' __'''first'''__ line");

	StringBuffer expected = new StringBuffer();
	expected.append("Here '''is''' <em>the</em> '''first''' line");

	formatLexify(inputBuffer, expected);
  }

  public void testList() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("No list here");
	inputBuffer.append("\n");
	inputBuffer.append("\t*item one");
	inputBuffer.append("\n");
	inputBuffer.append("\t*item two");
	inputBuffer.append("\n");
	inputBuffer.append("\t\t*subitem two-one");
	inputBuffer.append("\n");
	inputBuffer.append("\t\t*subitem two-two");
	inputBuffer.append("\n");
	inputBuffer.append("\t*item three");
	inputBuffer.append("\n");
	inputBuffer.append("\n");
	inputBuffer.append("No list here");
	inputBuffer.append("\n");

	StringBuffer expected = new StringBuffer();
	expected.append("No list here<br/><br/>\n<ul><li>item one");
	expected.append("</li>\n");
	expected.append("<li>item two\n");
	expected.append("<ul><li>subitem two-one");
	expected.append("</li>\n");
	expected.append("<li>subitem two-two");
	expected.append("</li></ul>\n");
	expected.append("<li>item three");
	expected.append("</li></ul>\n");
	expected.append("No list here<br/><br/>\n");

	formatLexify(inputBuffer, expected);
  }

  public void testTables() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("No table here");
	inputBuffer.append("\n");
	inputBuffer.append("####");
	inputBuffer.append("\n");
	inputBuffer.append("0,0");
	inputBuffer.append("##");
	inputBuffer.append("0,1");
	inputBuffer.append("##");
	inputBuffer.append("\n");
	inputBuffer.append("1,0");
	inputBuffer.append("##");
	inputBuffer.append("1,1");
	inputBuffer.append("##");
	inputBuffer.append("####");
	inputBuffer.append("\n");

	StringBuffer expected = new StringBuffer();
	expected.append("No table here<br/><br/>\n<table border=\"1\"><tr><td>0,0</td><td>0,1</td></tr><tr><td>1,0</td><td>1,1</td></tr></table>");

	formatLexify(inputBuffer, expected);
  }

  public void testWikiNames() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("Here is a line with a standard WikiName in it");
	inputBuffer.append("\n");
	inputBuffer.append("Here is a line with a broken Wiki2Name in it");
	inputBuffer.append("\n");
	inputBuffer.append("Here is a `backtick` WikiName");

	StringBuffer expected = new StringBuffer();
	expected.append("Here is a line with a standard WikiName<a href=\"Wiki?topic=WikiName&action=action_edit\">?</a> in it");
	expected.append("\n");
	expected.append("Here is a line with a broken Wiki2Name in it");
	expected.append("\n");
	expected.append("Here is a backtick<a href=\"Wiki?topic=backtick&action=action_edit\">?</a> WikiName<a href=\"Wiki?topic=WikiName&action=action_edit\">?</a>");
	linkLexify(inputBuffer, expected);
  }

  public void testHyperlinks() throws Exception {
	StringBuffer inputBuffer = new StringBuffer();
	inputBuffer.append("Here is a link: http://www.www.com/something and the end");
	inputBuffer.append("\n");
	inputBuffer.append("link: (http://www.www.com/something) and the end");
	inputBuffer.append("\n");
	inputBuffer.append("link: (http://www.www.com/something/) and the end");
	inputBuffer.append("\n");
	inputBuffer.append("link: <b>http://www.www.com/something/</b> and the end");
	inputBuffer.append("\n");
	inputBuffer.append("image: http://www.www.com/something/2.jpg and the end");
	inputBuffer.append("\n");
	inputBuffer.append("c2:SomeTopic and mskb:Q1234567 and other:Thing");

	StringBuffer expected = new StringBuffer();
	expected.append("Here is a link: <a href=\"http://www.www.com/something\">http://www.www.com/something</a> and the end");
	expected.append("\n");
	expected.append("link: (<a href=\"http://www.www.com/something\">http://www.www.com/something</a>) and the end");
	expected.append("\n");
	expected.append("link: (<a href=\"http://www.www.com/something/\">http://www.www.com/something/</a>) and the end");
	expected.append("\n");
	expected.append("link: <b><a href=\"http://www.www.com/something/\">http://www.www.com/something/</a></b> and the end");
	expected.append("\n");
	expected.append("image: <img src=\"http://www.www.com/something/2.jpg\"/> and the end");
	expected.append("\n");
	expected.append("<a href=\"http://c2.com/cgi/wiki?SomeTopic\">c2:SomeTopic</a> and <a href=\"http://support.microsoft.com/default.aspx?scid=KB;EN-US;Q1234567\">mskb:Q1234567</a> and other:Thing");
	linkLexify(inputBuffer, expected);
  }

  private void linkLexify(StringBuffer inputBuffer, StringBuffer expected) throws Exception {
	String text = inputBuffer.toString();
	StringReader in = new StringReader(text);
	LinkLex lex = new LinkLex(in);
	lex.setVirtualWiki("jsp");
	StringBuffer outputBuffer = new StringBuffer();
	while (true) {
	  String out = lex.yylex();
	  if (out == null) break;
	  outputBuffer.append(out);
	  System.out.print(out);
	}
	System.out.println();
	assertEquals(expected.toString(), outputBuffer.toString());
	in.close();
  }

  private void formatLexify(StringBuffer inputBuffer, StringBuffer expected) throws Exception {
	String text = inputBuffer.toString();
	StringReader in = new StringReader(text);
	FormatLex lex = new FormatLex(in);
	StringBuffer outputBuffer = new StringBuffer();
	while (true) {
	  String out = lex.yylex();
	  if (out == null) break;
	  outputBuffer.append(out);
	  System.out.print(out);
	}
	System.out.println();
	assertEquals(expected.toString(), outputBuffer.toString());
	in.close();
  }


  public static void main(String[] args) throws Exception {
	TestLexer test = new TestLexer("lexer");
	test.testFormat();
	test.testUnformat();
	test.testList();
	test.testTables();
	test.testWikiNames();
	test.testHyperlinks();
	System.exit(0);
  }
}

// $Log$
// Revision 1.3  2003/10/05 05:07:31  garethc
// fixes and admin file encoding option + merge with contributions
//
// Revision 1.2  2003/04/15 23:11:03  garethc
// lucene fixes
//
// Revision 1.1  2003/04/09 20:44:25  garethc
// package org
//
// Revision 1.7  2003/01/07 03:11:53  garethc
// beginning of big cleanup, taglibs etc
//
// Revision 1.6  2002/12/08 20:58:58  garethc
// 2.3.6 almost ready
//
// Revision 1.5  2002/11/15 03:31:44  garethc
// small fixes
//
// Revision 1.4  2002/11/07 23:18:14  garethc
// lex unit test update
//
// Revision 1.3  2002/11/07 22:51:31  garethc
// new two pass lexer working
//
// Revision 1.2  2002/11/07 21:47:45  garethc
// part way through 2 part lex
//
// Revision 1.1  2002/11/01 03:12:43  garethc
// starting work on new two pass lexer
//