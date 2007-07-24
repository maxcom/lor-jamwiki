package org.jamwiki.test.utils;

import org.jamwiki.utils.TiddlyWiki2MediaWikiTranslator;

import junit.framework.TestCase;

public class TiddlyWiki2MediaWikiTranslatorTest extends TestCase {

        public void testHeader() throws Exception {
                TiddlyWiki2MediaWikiTranslator t = new TiddlyWiki2MediaWikiTranslator();
                
                assertEquals("=test=", t.translate("!test"));
                assertEquals("==test==", t.translate("!!test"));
                assertEquals("===test===", t.translate("!!!test"));
                assertEquals("=test=\n=test2=", t.translate("!test\n!test2"));
        }
        
        
        public void testTables() throws Exception {
                TiddlyWiki2MediaWikiTranslator t = new TiddlyWiki2MediaWikiTranslator();
                assertEquals("{|\n|a||b\n|}", t.translate("|a|b|"));
                assertEquals("notab\n{|\n|a||b||c\n|}\nnotab", t.translate("notab\n|a|b|c|\nnotab"));
                //two rows
                assertEquals("{|\n|a||b\n|-\n|d||e\n|}", t.translate("|a|b|\n|d|e|"));
        }
        
        public void testNewLine() throws Exception {
                TiddlyWiki2MediaWikiTranslator t = new TiddlyWiki2MediaWikiTranslator();
                //if first character is letter -> <br/> included
                assertEquals("*first\n*second\n*third", t.translate("*first\\n*second\\n*third"));
                
        }
       
        public void testWikiLinks() throws Exception {
                TiddlyWiki2MediaWikiTranslator t = new TiddlyWiki2MediaWikiTranslator();
                assertEquals("abc [[WikiLink]] def", t.translate("abc WikiLink def"));
        }
        
        public void testInsertBreaks() throws Exception {
                TiddlyWiki2MediaWikiTranslator t = new TiddlyWiki2MediaWikiTranslator();
                //assertEquals("abc<br/>\ndef<br/>\nghi", t.translate("abc\ndef\nghi"));
        }
}
