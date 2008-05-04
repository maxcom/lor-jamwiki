package org.jamwiki.parser.bliki;

import info.bliki.wiki.addon.model.AddonConfiguration;
import info.bliki.wiki.filter.Encoder;
import info.bliki.wiki.model.AbstractWikiModel;
import info.bliki.wiki.model.ImageFormat;

import java.util.Map;
import java.util.Set;

import org.htmlcleaner.ContentToken;
import org.htmlcleaner.TagNode;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.WikiHeadingTag;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;

/**
 * An IWikiModel model implementation for JAMWiki
 * 
 */
public class JAMWikiModel extends AbstractWikiModel {
	private static WikiLogger logger = WikiLogger.getLogger(WikiHeadingTag.class.getName());

	protected String fExternalImageBaseURL;

	protected String fExternalWikiBaseURL;

	protected ParserInput fParserInput;

	protected ParserOutput fDocument; 
 
	static { 
		TagNode.addAllowedAttribute("style");
	}

	public JAMWikiModel(ParserInput parserInput, ParserOutput document, String imageBaseURL, String linkBaseURL) {
		super(AddonConfiguration.DEFAULT_CONFIGURATION);

		fParserInput = parserInput;
		fDocument = document;
		fExternalImageBaseURL = imageBaseURL;
		fExternalWikiBaseURL = linkBaseURL;
	}

	public void parseInternalImageLink(String imageNamespace, String name) {
		if (fExternalImageBaseURL != null) {
			// see JAMHTMLConverter#imageNodeToText() for the real conversion routine!!!
			ImageFormat imageFormat = ImageFormat.getImageFormat(name, imageNamespace);

			appendInternalImageLink(fExternalWikiBaseURL, fExternalImageBaseURL, imageFormat);
		}

	}

	public void appendInternalLink(String link, String hashSection, String linkText) {
		String hrefLink = fExternalWikiBaseURL;
		String encodedtopic = Encoder.encodeTitleUrl(link);
		hrefLink = StringUtils.replace(hrefLink, "${title}", encodedtopic);
		super.appendInternalLink(hrefLink, hashSection, linkText);
	}

	public void addCategory(String categoryName, String sortKey) {
		fDocument.addCategory(getCategoryNamespace() + NamespaceHandler.NAMESPACE_SEPARATOR + categoryName, sortKey);
	}

	public void addLink(String topic) {
		fDocument.addLink(topic);
	}

	public void addTemplate(String template) {
		fDocument.addTemplate(template);
	}

	public String getRawWikiContent(String namespace, String topicName, Map map) {
		String result = super.getRawWikiContent(namespace, topicName, map);
		if (result != null) {
			return result;
		}
		try {
			topicName = topicName.replaceAll("_", " ");
			Topic topic = WikiBase.getDataHandler().lookupTopic(fParserInput.getVirtualWiki(), namespace + ':' + topicName, false, null);
			if (topic == null) {
				return null;
			}
			return topic.getTopicContent();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void buildEditLinkUrl(int section) {
		if (fParserInput.getAllowSectionEdit()) {
			TagNode divTagNode = new TagNode("div");
			divTagNode.addAttribute("style", "font-size:90%;float:right;margin-left:5px;", false);
			divTagNode.addChild(new ContentToken("["));
			append(divTagNode);

			String url = "";
			try {
				url = LinkUtil.buildEditLinkUrl(fParserInput.getContext(), fParserInput.getVirtualWiki(), fParserInput.getTopicName(),
						null, section);
			} catch (Exception e) {
				logger.severe("Failure while building link for topic " + fParserInput.getVirtualWiki() + " / "
						+ fParserInput.getTopicName(), e);
			}
			TagNode aTagNode = new TagNode("a");
			aTagNode.addAttribute("href", url, false);
			aTagNode.addChild(new ContentToken(Utilities.formatMessage("common.sectionedit", fParserInput.getLocale())));
			divTagNode.addChild(aTagNode);
			divTagNode.addChild(new ContentToken("]"));
		}
	}

	public boolean parseBBCodes() {
		return false;
	}

	public boolean replaceColon() {
		return false;
	}

	public String getCategoryNamespace() {
		return NamespaceHandler.NAMESPACE_CATEGORY;
	}

	public String getImageNamespace() {
		return NamespaceHandler.NAMESPACE_IMAGE;
	}

	public String getTemplateNamespace() {
		return NamespaceHandler.NAMESPACE_TEMPLATE;
	}

	public Set getLinks() {
		return null;
	}

	public void appendInterWikiLink(String namespace, String title, String linkText) {
		// no interwiki link parsing
		return;
	}

	public boolean isTemplateTopic() {
		String topicName = fParserInput.getTopicName();
		int index = topicName.indexOf(':');
		if (index > 0) {
			String namespace = topicName.substring(0, index);
			if (isTemplateNamespace(namespace)) {
				return true;
			}
		}
		return false;
	}

	public boolean isMathtranRenderer() {
		return true;
	}
}
