package org.jamwiki.parser.bliki;

import info.bliki.htmlcleaner.TagNode;
import info.bliki.htmlcleaner.Utils;
import info.bliki.wiki.filter.HTMLConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;

import java.io.IOException;
import java.util.Map;

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

	@Override
	public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer, IWikiModel model)
			throws IOException {
		String imageName = imageFormat.getFilename();
		imageName = imageName.replaceAll("_", " ");
		Map<String, String> map = imageTagNode.getAttributes();
		String caption = imageFormat.getCaption();
		String alt = null;
		if (caption != null && caption.length() > 0) {
			alt = imageFormat.getAlt();
			caption = Utils.escapeXml(caption, true, false, true);
		}
		if (alt == null) {
			alt = "";
		}
		String location = imageFormat.getLocation();
		String type = imageFormat.getType();
		int pxWidth = imageFormat.getWidth();
		int pxHeight = imageFormat.getHeight();

		boolean frame = type == null ? false : type.equals("frame");
		boolean thumb = type == null ? false : type.equals("thumb");
		if (thumb && pxWidth <= 0) {
			pxWidth = DEFAULT_THUMBNAIL_SIZE;
		}

		try {
			resultBuffer.append(LinkUtil.buildImageLinkHtml(fParserInput.getContext(), fParserInput.getVirtualWiki(), model
					.getImageNamespace()
					+ NamespaceHandler.NAMESPACE_SEPARATOR + imageName, frame, thumb, imageFormat.getLocation(), imageFormat.getCaption(),
					pxWidth, false, null, false));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
