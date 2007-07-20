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
import javax.imageio.ImageIO;
import net.sf.ehcache.Element;
import org.jamwiki.Environment;
import org.jamwiki.model.WikiImage;
import org.jamwiki.model.WikiFile;

/**
 * Utility methods for readding images from disk, saving images to disk,
 * resizing images, and returning information about images such as width and
 * height.
 */
public class ImageUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageUtil.class.getName());
	private static final String CACHE_IMAGES = "org.jamwiki.utils.ImageUtils.CACHE_IMAGES";

	/**
	 *
	 */
	private ImageUtil() {
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
	private static BufferedImage imageToBufferedImage(Image image) throws Exception {
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
	 * @throws Exception Thrown if an error occurs while initializing the
	 *  WikiImage object.
	 */
	public static WikiImage initializeImage(WikiFile wikiFile, int maxDimension) throws Exception {
		if (wikiFile == null) {
			logger.warning("No image found for " + wikiFile.getVirtualWiki() + " / " + wikiFile.getFileName());
			return null;
		}
		WikiImage wikiImage = new WikiImage(wikiFile);
		BufferedImage imageObject = null;
		if (maxDimension > 0) {
			imageObject = ImageUtil.resizeImage(wikiImage, maxDimension);
			setScaledDimensions(imageObject, wikiImage, maxDimension);
		} else {
			File imageFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), wikiImage.getUrl());
			imageObject = ImageUtil.loadImage(imageFile);
			wikiImage.setWidth(imageObject.getWidth());
			wikiImage.setHeight(imageObject.getHeight());
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
	 * @throws Exception Thrown if any error occurs while reading the file.
	 */
	public static boolean isImage(File file) throws Exception {
		return (ImageUtil.loadImage(file) != null);
	}

	/**
	 * Given a file that corresponds to an existing image, return a
	 * BufferedImage object.
	 */
	private static BufferedImage loadImage(File file) throws Exception {
		BufferedImage image = null;
		String key = file.getPath();
		Element cacheElement = WikiCache.retrieveFromCache(CACHE_IMAGES, key);
		if (cacheElement != null) {
			return (BufferedImage)cacheElement.getObjectValue();
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			image = ImageIO.read(fis);
			WikiCache.addToCache(CACHE_IMAGES, key, image);
			return image;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {}
			}
		}
	}

	/**
	 * Resize an image, using a maximum dimension value.  Image dimensions will
	 * be constrained so that the proportions are the same, but neither the width
	 * or height exceeds the value specified.
	 */
	private static BufferedImage resizeImage(WikiImage wikiImage, int maxDimension) throws Exception {
		File imageFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), wikiImage.getUrl());
		BufferedImage original = ImageUtil.loadImage(imageFile);
		maxDimension = calculateImageIncrement(maxDimension);
		int increment = Environment.getIntValue(Environment.PROP_IMAGE_RESIZE_INCREMENT);
		if (increment <= 0 || (maxDimension > original.getWidth() && maxDimension > original.getHeight())) {
			// let the browser scale the image
			return original;
		}
		String newUrl = wikiImage.getUrl();
		int pos = newUrl.lastIndexOf('.');
		if (pos > -1) {
			newUrl = newUrl.substring(0, pos) + "-" + maxDimension + "px" + newUrl.substring(pos);
		} else {
			newUrl += "-" + maxDimension + "px";
		}
		wikiImage.setUrl(newUrl);
		File newImageFile = new File(Environment.getValue(Environment.PROP_FILE_DIR_FULL_PATH), newUrl);
		if (newImageFile.exists()) {
			return ImageUtil.loadImage(newImageFile);
		}
		int width = -1;
		int height = -1;
		if (original.getWidth() >= original.getHeight()) {
			width = maxDimension;
		} else {
			height = maxDimension;
		}
		Image resized = original.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
		BufferedImage bufferedImage = null;
		if (resized instanceof BufferedImage) {
			bufferedImage = (BufferedImage)resized;
		} else {
			bufferedImage = ImageUtil.imageToBufferedImage(resized);
		}
		ImageUtil.saveImage(bufferedImage, newImageFile);
		return bufferedImage;
	}

	/**
	 * Save an image to a specified file.
	 */
	private static void saveImage(BufferedImage image, File file) throws Exception {
		String filename = file.getName();
		int pos = filename.lastIndexOf('.');
		if (pos == -1 || (pos + 1) >= filename.length()) {
			throw new Exception("Unknown image type " + filename);
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
				} catch (Exception e) {}
			}
		}
	}

	/**
	 *
	 */
	private static void setScaledDimensions(BufferedImage bufferedImage, WikiImage wikiImage, int maxDimension) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();
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
