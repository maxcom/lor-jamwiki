package org.jamwiki.parser.bliki;

import java.io.IOException;

import info.bliki.wiki.filter.HTMLConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;

import org.htmlcleaner.TagNode;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.utils.LinkUtil;
import org.jamwiki.utils.NamespaceHandler;

public class JAMHTMLConverter extends HTMLConverter {
	private static final int DEFAULT_THUMBNAIL_SIZE = 180;

	private ParserInput fParserInput; 

	public JAMHTMLConverter(ParserInput parserInput) {
		super();
		fParserInput = parserInput;
	}
	
	public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer, IWikiModel model) throws IOException {
//	public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, StringBuffer resultBuffer, IWikiModel model) {
		String imageName = imageFormat.getFilename();
		imageName = imageName.replaceAll("_", " ");
		int maxDimension = imageFormat.getSize();
		String type = imageFormat.getType();
		boolean frame = type == null ? false : type.equals("frame");
		boolean thumb = type == null ? false : type.equals("thumb");
		if (thumb && maxDimension <= 0) {
			maxDimension = DEFAULT_THUMBNAIL_SIZE;
		}
		try {
			resultBuffer.append(LinkUtil.buildImageLinkHtml(fParserInput.getContext(), fParserInput.getVirtualWiki(), model
					.getImageNamespace()
					+ NamespaceHandler.NAMESPACE_SEPARATOR + imageName, frame, thumb, imageFormat.getLocation(), imageFormat.getCaption(),
					maxDimension, false, null, false));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
