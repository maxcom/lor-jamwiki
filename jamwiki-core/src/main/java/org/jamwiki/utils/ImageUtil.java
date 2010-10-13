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
import java.io.FileOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.sf.ehcache.Element;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.WikiImage;
import org.jamwiki.model.WikiFile;

/**
 * Utility methods for readding images from disk, saving images to disk,
 * resizing images, and returning information about images such as width and
 * height.
 */
public class ImageUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageUtil.class.getName());

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
		WikiCache.addToCache(WikiBase.CACHE_IMAGE_DIMENSIONS, key, dimensions);
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
	 *
	 */
	private static int calculateImageIncrement(int maxDimension) {
		int increment = Environment.getIntValue(Environment.PROP_IMAGE_RESIZE_INCREMENT);
		double result = Math.ceil((double)maxDimension / (double)increment) * increment;
		return (int)result;
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
				throw new IOException("JDK is unable to process image file: " + file.getPath());
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
		Element cachedDimensions = WikiCache.retrieveFromCache(WikiBase.CACHE_IMAGE_DIMENSIONS, key);
		return (cachedDimensions != null) ? (ImageDimensions)cachedDimensions.getObjectValue() : null;
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
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(imageFile);
			ImageIO.write(image, imageType, fos);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {}
			}
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
}
