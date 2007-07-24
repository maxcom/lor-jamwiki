package org.jamwiki.test.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.utils.TiddlyWikiParser;

import junit.framework.TestCase;

public class TiddlyWikiParserTest extends TestCase {

        private static final String NAME = "MyTopic";
        private static final String DATUM  = "200701020304";
        private static final String CONTENT  = "Content"; 
        public void testParse() throws Exception {
                Handler[] h = Logger.getLogger("").getHandlers();
                for (int i = 0; i < h.length; i++) {
                        h[i].setLevel(Level.ALL);
                }
                Logger.getLogger("").setLevel(Level.ALL);
                
                WikiBaseMock mock = new WikiBaseMock();
                TiddlyWikiParser parser = new TiddlyWikiParser("myvirtual", null, "", mock);
                
                String testLine = "<div tiddler=\""+ NAME + "\" modified=\"" + DATUM + "\">" + CONTENT+ "</div>";
                        
                parser.parse(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(testLine.getBytes()))));
                
                assertEquals(1, mock.topics.size());
                Topic result =  (Topic) mock.topics.get(0);
                
                assertEquals(NAME, result.getName());
                assertEquals(CONTENT, result.getTopicContent());
                
                assertEquals(1, mock.versions.size());
                TopicVersion version = (TopicVersion) mock.versions.get(0);
                
                SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmm");
                
                Timestamp t = new Timestamp(fmt.parse(DATUM).getTime());
                assertEquals(t, version.getEditDate());
        }

        private class WikiBaseMock implements TiddlyWikiParser.WikiBaseFascade {
                
                public ArrayList topics = new ArrayList();
                public ArrayList versions = new ArrayList();
                
                public void writeTopic(Topic topic, TopicVersion topicVersion, ParserDocument parserDocument, boolean userVisible, Object transactionObject) throws Exception {
                        topics.add(topic);
                        versions.add(topicVersion);
                }
           
        }
}
