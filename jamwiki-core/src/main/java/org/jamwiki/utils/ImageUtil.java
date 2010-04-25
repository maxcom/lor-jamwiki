/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.imageio.ImageIO;
import net.sf.ehcache.Element;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.jamwiki.DataAccessException;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Namespace;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicType;
import org.jamwiki.model.TopicVersion;
import org.jamwiki.model.WikiImage;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiFileVersion;
import org.jamwiki.model.WikiUser;
import org.jamwiki.parser.ParserException;
import org.jamwiki.parser.ParserOutput;
import org.jamwiki.parser.ParserUtil;

/**
 * Utility methods for readding images from disk, saving images to disk,
 * resizing images, and returning information about images such as width and
 * height.
 */
public class ImageUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageUtil.class.getName());
	/** Cache name for the cache of image dimensions. */
	private static final String CACHE_IMAGE_DIMENSIONS = "org.jamwiki.utils.ImageUtil.CACHE_IMAGE_DIMENSIONS";

	static {
		// manually set the ImageIO temp directory so that systems with incorrect defaults won't fail
		// when processing images.
		File directory = WikiUtil.getTempDirectory();
		if (directory.exists()) {
			ImageIO.setCacheDirectory(directory);
		}
	}

	/**
	 *
	 */
	private ImageUtil() {
	}

	/**
	 *
	 */
	private static void addToCache(File file, int width, int height) {
		ImageDimensions dimensions = new ImageDimensions(width, height);
		String key = file.getPath();
		WikiCache.addToCache(CACHE_IMAGE_DIMENSIONS, key, dimensions);
	}

	/**
	 * Utility method for building the URL to an image file (NOT the image topic
	 * page).  If the file does not exist then this method will return
	 * <code>null</code>.
	 *
	 * @param context The current servlet context.
	 * @param virtualWiki The virtual wiki for the URL that is being created.
	 * @param topicName The name of the image for which a link is being created.
	 * @return The URL to an image file (not the image topic) or <code>null</code>
	 *  if the file does not exist.
	 * @throws DataAccessException Thrown if any error occurs while retrieving file info.
	 */
	public static String buildImageFileUrl(String context, String virtualWiki, String topicName) throws DataAccessException {
		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		if (wikiFile == null) {
			return null;
		}
		String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + wikiFile.getUrl());
		return FilenameUtils.separatorsToUnix(url);
	}

	/**
	 * Utility method for building an anchor tag that links to an image page
	 * and includes the HTML image tag to display the image.
	 *
	 * @param context The servlet context for the link that is being created.
	 * @param virtualWiki The virtual wiki for the link that is being created.
	 * @param topicName The name of the image for which a link is being
	 *  created.
	 * @param imageMetadata A container for the image display params, such as
	 *  border, alignment, caption, etc.
	 * @param style The CSS class to use with the img HTML tag.  This value
	 *  can be <code>null</code> or empty if no custom style is used.
	 * @param escapeHtml Set to <code>true</code> if the caption should be
	 *  HTML escaped.  This value should be <code>true</code> in any case
	 *  where the caption is not guaranteed to be free from potentially
	 *  malicious HTML code.
	 * @return The full HTML required to display an image enclosed within an
	 *  HTML anchor tag that links to the image topic page.
	 * @throws DataAccessException Thrown if any error occurs while retrieving image
	 *  information.
	 * @throws IOException Thrown if any error occurs while reading image information.
	 */
	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, ImageMetadata imageMetadata, String style, boolean escapeHtml) throws DataAccessException, IOException {
		String url = ImageUtil.buildImageFileUrl(context, virtualWiki, topicName);
		if (url == null) {
			return ImageUtil.buildUploadLink(context, virtualWiki, topicName);
		}
		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		StringBuilder html = new StringBuilder();
		String caption = imageMetadata.getCaption();
		int maxDimension = imageMetadata.getMaxDimension();
		if (topic.getTopicType() == TopicType.FILE) {
			// file, not an image
			if (StringUtils.isBlank(caption)) {
				caption = topicName.substring(Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki).length() + 1);
			}
			html.append("<a href=\"").append(url).append("\">");
			if (escapeHtml) {
				html.append(StringEscapeUtils.escapeHtml(caption));
			} else {
				html.append(caption);
			}
			html.append("</a>");
			return html.toString();
		}
		if (imageMetadata.getBorder() != ImageBorderEnum.THUMB && imageMetadata.getBorder() != ImageBorderEnum.FRAME) {
			// captions are only displayed for thumbnails and framed images
			caption = null;
		}
		WikiImage wikiImage = null;
		try {
			wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension);
		} catch (FileNotFoundException e) {
			// do not log the full exception as the logs can fill up very for this sort of error, and it is generally due to a bad configuration.  instead log a warning message so that the administrator can try to fix the problem
			logger.warning("File not found while parsing image link for topic: " + virtualWiki + " / " + topicName + ".  Make sure that the following file exists and is readable by the JAMWiki installation: " + e.getMessage());
			return ImageUtil.buildUploadLink(context, virtualWiki, topicName);
		}
		String imageWrapperDiv = ImageUtil.buildImageWrapperDivs(imageMetadata, wikiImage.getWidth());
		if (!imageMetadata.getSuppressLink()) {
			html.append("<a class=\"wikiimg\" href=\"").append(LinkUtil.buildTopicUrl(context, virtualWiki, topicName, true)).append("\">");
		}
		if (StringUtils.isBlank(style)) {
			style = "wikiimg";
		}
		html.append("<img class=\"").append(style).append("\" src=\"");
		html.append(url);
		html.append('\"');
		html.append(" width=\"").append(wikiImage.getWidth()).append('\"');
		html.append(" height=\"").append(wikiImage.getHeight()).append('\"');
		String alt = (caption != null) ? caption : topic.getPageName();
		html.append(" alt=\"").append(StringEscapeUtils.escapeHtml(alt)).append('\"');
		if (imageMetadata.getVerticalAlignment() != ImageVerticalAlignmentEnum.NOT_SPECIFIED) {
			html.append(" style=\"vertical-align: ").append(imageMetadata.getVerticalAlignment().toString()).append('\"');
		}
		html.append(" />");
		if (!imageMetadata.getSuppressLink()) {
			html.append("</a>");
		}
		if (!StringUtils.isBlank(caption)) {
			// captions are only displayed for thumbnails and framed images
			html.append("\n<div class=\"thumbcaption\">");
			if (escapeHtml) {
				html.append(StringEscapeUtils.escapeHtml(caption));
			} else {
				html.append(caption);
			}
			html.append("</div>\n");
		}
		return MessageFormat.format(imageWrapperDiv, html.toString());
	}

	/**
	 * Given a file URL and a maximum dimension, return a path for the file.
	 */
	private static String buildImagePath(String currentUrl, int maxDimension) {
		String path = currentUrl;
		int pos = path.lastIndexOf('.');
		if (pos != -1) {
			path = path.substring(0, pos) + "-" + maxDimension + "px" + path.substring(pos);
		} else {
			path += "-" + maxDimension + "px";
		}
		return path;
	}

	/**
	 * Determine the CSS styles to apply to the image wrapper div.
	 */
	private static String buildImageWrapperDivs(ImageMetadata imageMetadata, int width) {
		// CSS and wrappers are processed differently for thumb/frame vs. non-thumb/non-frame
		if (imageMetadata.getBorder() != ImageBorderEnum.THUMB && imageMetadata.getBorder() != ImageBorderEnum.FRAME) {
			if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.LEFT) {
				return "<div class=\"floatleft\">{0}</div>";
			} else if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.RIGHT) {
				return "<div class=\"floatright\">{0}</div>";
			} else if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.CENTER) {
				return "<div class=\"center\">\n<div class=\"floatnone\">{0}</div>\n</div>";
			} else if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.NONE) {
				return "<div class=\"floatnone\">{0}</div>";
			} else {
				return "{0}";
			}
		} else {
			// the inner div must specify a width
			String styleWidth = " style=\"width:" + (width + 2) + "px\"";
			if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.CENTER) {
				return "<div class=\"center\">\n<div class=\"thumb tnone\">\n<div class=\"thumbinner\"" + styleWidth + ">{0}</div>\n</div>\n</div>";
			} else if (imageMetadata.getHorizontalAlignment() == ImageHorizontalAlignmentEnum.LEFT) {
				return "<div class=\"thumb tleft\">\n<div class=\"thumbinner\"" + styleWidth + ">{0}</div>\n</div>";
			} else {
				return "<div class=\"thumb tright\">\n<div class=\"thumbinner\"" + styleWidth + ">{0}</div>\n</div>";
			}
		}
	}

	/**
	 *
	 */
	private static String buildUploadLink(String context, String virtualWiki, String topicName) throws DataAccessException {
		WikiLink uploadLink = LinkUtil.parseWikiLink(virtualWiki, "Special:Upload?topic=" + topicName);
		return LinkUtil.buildInternalLinkHtml(context, virtualWiki, uploadLink, topicName, "edit", null, true);
	}

	/**
	 *
	 */
	private static int calculateImageIncrement(int maxDimension) {
		int increment = Environment.getIntValue(Environment.PROP_IMAGE_RESIZE_INCREMENT);
		double result = Math.ceil((double)maxDimension / (double)increment) * increment;
		return (int)result;
	}

	/**
	 * Given a filename, generate the URL to use to store the file on the filesystem.
	 */
	public static String generateFileUrl(String filename, Date date) throws WikiException {
		String url = filename;
		if (StringUtils.isBlank(url)) {
			throw new WikiException(new WikiMessage("upload.error.filename"));
		}
		// file is appended with a timestamp of DDHHMMSS
		GregorianCalendar cal = new GregorianCalendar();
		if (date != null) {
			cal.setTime(date);
		}
		String day = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
		if (day.length() == 1) {
			day = "0" + day;
		}
		String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
		if (hour.length() == 1) {
			hour = "0" + hour;
		}
		String minute = Integer.toString(cal.get(Calendar.MINUTE));
		if (minute.length() == 1) {
			minute = "0" + minute;
		}
		String second = Integer.toString(cal.get(Calendar.SECOND));
		if (second.length() == 1) {
			second = "0" + second;
		}
		String suffix = "-" + day + hour + minute + second;
		int pos = url.lastIndexOf('.');
		url = (pos == -1) ? url + suffix : url.substring(0, pos) + suffix + url.substring(pos);
		// now pre-pend the file system directory
		// subdirectory is composed of year/month
		String year = Integer.toString(cal.get(Calendar.YEAR));
		String month = Integer.toString(cal.get(Calendar.MONTH) + 1);
		String subdirectory = "/" + year + "/" + month;
		File directory = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), subdirectory);
		if (!directory.exists() && !directory.mkdirs()) {
			throw new WikiException(new WikiMessage("upload.error.directorycreate", directory.getAbsolutePath()));
		}
		return subdirectory + "/" + url;
	}

	/**
	 * Given an image file name, generate the appropriate topic name for the image.
	 */
	public static String generateFileTopicName(String virtualWiki, String filename) {
		String topicName = Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki) + Namespace.SEPARATOR;
		topicName += Utilities.decodeAndEscapeTopicName(filename, true);
		return topicName;
	}

	/**
	 * Convert a Java Image object to a Java BufferedImage object.
	 */
	private static BufferedImage imageToBufferedImage(Image image) {
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB );
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.dispose();
		return bufferedImage;
	}

	/**
	 * Given a virtualWiki and WikiFIle that correspond to an existing image,
	 * return the WikiImage object.  In addition, an optional maxDimension
	 * parameter may be specified, in which case a resized version of the image
	 * may be created.
	 *
	 * @param wikiFile Given a WikiFile object, use it to initialize a
	 *  WikiImage object.
	 * @param maxDimension The maximum width or height for the initialized
	 *  WikiImage object.  Setting this value to 0 or less will cause the
	 *  value to be ignored.
	 * @return An initialized WikiImage object.
	 * @throws IOException Thrown if an error occurs while initializing the
	 *  WikiImage object.
	 */
	public static WikiImage initializeImage(WikiFile wikiFile, int maxDimension) throws IOException {
		if (wikiFile == null) {
			throw new IllegalArgumentException("wikiFile may not be null");
		}
		WikiImage wikiImage = new WikiImage(wikiFile);
		if (maxDimension > 0) {
			ImageDimensions dimensions = ImageUtil.resizeImage(wikiImage, maxDimension);
			setScaledDimensions(dimensions.getWidth(), dimensions.getHeight(), wikiImage, maxDimension);
		} else {
			File file = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), wikiImage.getUrl());
			ImageDimensions dimensions = retrieveFromCache(file);
			if (dimensions != null) {
				wikiImage.setWidth(dimensions.getWidth());
				wikiImage.setHeight(dimensions.getHeight());
			} else {
				BufferedImage imageObject = ImageUtil.loadImage(file);
				wikiImage.setWidth(imageObject.getWidth());
				wikiImage.setHeight(imageObject.getHeight());
				addToCache(file, imageObject.getWidth(), imageObject.getHeight());
			}
		}
		return wikiImage;
	}

	/**
	 * Utility method for determining if a file name corresponds to a file type that is allowed
	 * for this wiki instance.
	 *
	 * @param filename The file name.
	 * @return <code>true</code> if the file type has not been blacklisted and is allowed for upload.
	 */
	public static boolean isFileTypeAllowed(String filename) {
		String extension = FilenameUtils.getExtension(filename);
		int blacklistType = Environment.getIntValue(Environment.PROP_FILE_BLACKLIST_TYPE);
		if (blacklistType == WikiBase.UPLOAD_ALL) {
			return true;
		}
		if (blacklistType == WikiBase.UPLOAD_NONE) {
			return false;
		}
		if (StringUtils.isBlank(extension)) {
			// FIXME - should non-extensions be represented in the whitelist/blacklist?
			return true;
		}
		extension = extension.toLowerCase();
		List list = WikiUtil.retrieveUploadFileList();
		if (blacklistType == WikiBase.UPLOAD_BLACKLIST) {
			return !list.contains(extension);
		}
		if (blacklistType == WikiBase.UPLOAD_WHITELIST) {
			return list.contains(extension);
		}
		return false;
	}

	/**
	 * Given a File object, determine if the file is an image or if it is some
	 * other type of file.  Note that this method will read in the entire file,
	 * so there are performance implications for large files.
	 *
	 * @param file The File object for the file that is being examined.
	 * @return Returns <code>true</code> if the file is an image object.
	 */
	public static boolean isImage(File file) {
		try {
			return (ImageUtil.loadImage(file) != null);
		} catch (IOException x) {
			return false;
		}
	}

	/**
	 * Given a file that corresponds to an existing image, return a
	 * BufferedImage object.
	 */
	private static BufferedImage loadImage(File file) throws IOException {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			BufferedImage image = ImageIO.read(fis);
			if (image == null) {
				throw new IOException("JDK is unable to process image file: " + file.getAbsolutePath());
			}
			return image;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {}
			}
		}
	}

	/**
	 * Resize an image, using a maximum dimension value.  Image dimensions will
	 * be constrained so that the proportions are the same, but neither the width
	 * or height exceeds the value specified.
	 */
	private static ImageDimensions resizeImage(WikiImage wikiImage, int maxDimension) throws IOException {
		String newUrl = buildImagePath(wikiImage.getUrl(), maxDimension);
		File newImageFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), newUrl);
		ImageDimensions dimensions = retrieveFromCache(newImageFile);
		if (dimensions != null) {
			return dimensions;
		}
		File imageFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), wikiImage.getUrl());
		BufferedImage original = ImageUtil.loadImage(imageFile);
		maxDimension = calculateImageIncrement(maxDimension);
		int increment = Environment.getIntValue(Environment.PROP_IMAGE_RESIZE_INCREMENT);
		if (increment <= 0 || (maxDimension > original.getWidth() && maxDimension > original.getHeight())) {
			// let the browser scale the image
			addToCache(imageFile, original.getWidth(), original.getHeight());
			return new ImageDimensions(original.getWidth(), original.getHeight());
		}
		wikiImage.setUrl(newUrl);
		if (newImageFile.exists()) {
			BufferedImage result = ImageUtil.loadImage(newImageFile);
			addToCache(newImageFile, result.getWidth(), result.getHeight());
			return new ImageDimensions(result.getWidth(), result.getHeight());
		}
		int width = -1;
		int height = -1;
		if (original.getWidth() >= original.getHeight()) {
			width = maxDimension;
		} else {
			height = maxDimension;
		}
		Image resized = null;
		try {
			resized = original.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		} catch (Throwable t) {
			logger.severe("Unable to resize image.  This problem sometimes occurs due to dependencies between Java and X on UNIX systems.  Consider enabling an X server or setting the java.awt.headless parameter to true for your JVM.", t);
			resized = original;
		}
		BufferedImage bufferedImage = null;
		if (resized instanceof BufferedImage) {
			bufferedImage = (BufferedImage)resized;
		} else {
			bufferedImage = ImageUtil.imageToBufferedImage(resized);
		}
		ImageUtil.saveImage(bufferedImage, newImageFile);
		addToCache(newImageFile, bufferedImage.getWidth(), bufferedImage.getHeight());
		return new ImageDimensions(bufferedImage.getWidth(), bufferedImage.getHeight());
	}

	/**
	 * Determine if image information is available in the cache.  If so return it,
	 * otherwise return <code>null</code>.
	 */
	private static ImageDimensions retrieveFromCache(File file) {
		String key = file.getPath();
		Element cachedDimensions = WikiCache.retrieveFromCache(CACHE_IMAGE_DIMENSIONS, key);
		return (cachedDimensions != null) ? (ImageDimensions)cachedDimensions.getObjectValue() : null;
	}

	/**
	 * Given a file name that might correspond to an absolute URL, strip any directories
	 * and convert spaces in the name to underscores.
	 *
	 * @param filename The file name (path) to be sanitized.
	 * @return A sanitized version of the file name.
	 */
	public static String sanitizeFilename(String filename) {
		if (StringUtils.isBlank(filename)) {
			return null;
		}
		// some browsers set the full path, so strip to just the file name
		filename = FilenameUtils.getName(filename);
		filename = StringUtils.replace(filename.trim(), " ", "_");
		return filename;
	}

	/**
	 * Save an image to a specified file.
	 */
	private static void saveImage(BufferedImage image, File file) throws IOException {
		String filename = file.getName();
		int pos = filename.lastIndexOf('.');
		if (pos == -1 || (pos + 1) >= filename.length()) {
			throw new IOException("Unknown image file type " + filename);
		}
		String imageType = filename.substring(pos + 1);
		File imageFile = new File(file.getParent(), filename);
		boolean result = ImageIO.write(image, imageType, imageFile);
		if (!result) {
			throw new IOException("No appropriate writer found when writing image: " + filename);
		}
	}

	/**
	 * Set the width and height of a WikiImage to match the specified dimensions, with a
	 * maximum width/height value specified by maxDimension.  Thus if an image is
	 * 800x400 and maxDimension is 200 the result will be 200x100.
	 */
	private static void setScaledDimensions(int width, int height, WikiImage wikiImage, int maxDimension) {
		if (width >= height) {
			height = (int)Math.floor(((double)maxDimension / (double)width) * (double)height);
			width = maxDimension;
		} else {
			width = (int)Math.floor(((double)maxDimension / (double)height) * (double)width);
			height = maxDimension;
		}
		wikiImage.setWidth(width);
		wikiImage.setHeight(height);
	}

	/**
	 *
	 */
	public static Topic writeImageTopic(String virtualWiki, String topicName, String contents, WikiUser user, boolean isImage, String ipAddress) throws DataAccessException, ParserException, WikiException {
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		int charactersChanged = 0;
		if (topic == null) {
			topic = new Topic(virtualWiki, topicName);
			topic.setTopicContent(contents);
			charactersChanged = StringUtils.length(contents);
		}
		if (isImage) {
			topic.setTopicType(TopicType.IMAGE);
		} else {
			topic.setTopicType(TopicType.FILE);
		}
		TopicVersion topicVersion = new TopicVersion(user, ipAddress, contents, topic.getTopicContent(), charactersChanged);
		topicVersion.setEditType(TopicVersion.EDIT_UPLOAD);
		ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki, topicName);
		WikiBase.getDataHandler().writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
		return topic;
	}

	/**
	 *
	 */
	public static WikiFile writeWikiFile(Topic topic, WikiUser user, String ipAddress, String filename, String url, String contentType, long fileSize) throws DataAccessException, WikiException {
		WikiFileVersion wikiFileVersion = new WikiFileVersion();
		wikiFileVersion.setUploadComment(topic.getTopicContent());
		wikiFileVersion.setAuthorDisplay(ipAddress);
		Integer authorId = null;
		if (user != null && user.getUserId() > 0) {
			authorId = user.getUserId();
		}
		wikiFileVersion.setAuthorId(authorId);
		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(topic.getVirtualWiki(), topic.getName());
		if (wikiFile == null) {
			wikiFile = new WikiFile();
			wikiFile.setVirtualWiki(topic.getVirtualWiki());
		}
		wikiFile.setFileName(filename);
		wikiFile.setUrl(url);
		wikiFileVersion.setUrl(url);
		wikiFileVersion.setMimeType(contentType);
		wikiFile.setMimeType(contentType);
		wikiFileVersion.setFileSize(fileSize);
		wikiFile.setFileSize(fileSize);
		wikiFile.setTopicId(topic.getTopicId());
		WikiBase.getDataHandler().writeFile(wikiFile, wikiFileVersion);
		return wikiFile;
	}
}
