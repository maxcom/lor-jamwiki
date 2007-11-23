package org.jamwiki.servlets;

import junit.framework.TestCase;

public class WikiPageInfoTest extends TestCase {

	public void testGetNamespace() {
		WikiPageInfo p = new WikiPageInfo();

		assertEquals(p.getTopicName(),"",p.getTopicName());

		p.setTopicName("Main");
		assertEquals("", p.getNamespace());

		p.setTopicName("User:FooBar");
		assertEquals("User", p.getNamespace());
		
		p.setTopicName("Special:Contributions");
		p.setSpecial(true);
		assertEquals("Special", p.getNamespace());
	}

	public void testGetPagename() {
		WikiPageInfo p = new WikiPageInfo();

		assertEquals(p.getTopicName(),"",p.getPagename());

		p.setTopicName("Main");
		assertEquals("Main", p.getPagename());

		p.setTopicName("User:FooBar");
		assertEquals("FooBar", p.getPagename());
		
		p.setTopicName("Special:Contributions");
		p.setSpecial(true);
		assertEquals("Contributions", p.getPagename());
	}
}
