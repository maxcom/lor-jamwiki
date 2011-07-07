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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Utility methods that wrap native Java image processing functionality to allow
 * functionality such as resizing, reading, saving and otherwise performing
 * common image operations.
 */
public class ImageProcessor {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageProcessor.class.getName());

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
	private ImageProcessor() {
	}

	/**
	 * Given a file that corresponds to an existing image, return a
	 * BufferedImage object.
	 */
	private static BufferedImage loadImage(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
		}
		// use a FileInputStream and make sure it gets closed to prevent unclosed file
		// errors on some operating systems
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			BufferedImage image = ImageIO.read(fis);
			if (image == null) {
				throw new IOException("JDK is unable to process image file, possibly indicating file corruption: " + file.getAbsolutePath());
			}
			return image;
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	/**
	 * Convenience method that returns a scaled instance of the provided image.
	 * This method never resizes by more than 50% since resizing by more than that
	 * amount causes quality issues with the BICUBIC and BILINEAR algorithms.
	 *
	 * Based on examples from the GraphicsUtilities sample from the book "Filthy
	 * Rich Clients" by Chet Haase and Romain Guy (http://filthyrichclients.org/).
	 * That source is dual licensed: LGPL (Sun and Romain Guy) and BSD (Romain Guy).
	 *
	 * @param imageFile The file path for the original image to be scaled.
	 * @param targetWidth the desired width of the scaled instance in pixels.
	 * @param targetHeight the desired height of the scaled instance in pixels.
	 * @return a scaled version of the original {@code BufferedImage}
	 */
	public static BufferedImage resizeImage(File imageFile, int targetWidth, int targetHeight) throws IOException {
		long start = System.currentTimeMillis();
		BufferedImage tmp = ImageProcessor.loadImage(imageFile);
		int type = (tmp.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		int width = tmp.getWidth();
		int height = tmp.getHeight();
		BufferedImage resized = tmp;
		do {
			width /= 2;
			if (width < targetWidth) {
				width = targetWidth;
			}
			height /= 2;
			if (height < targetHeight) {
				height = targetHeight;
			}
			tmp = new BufferedImage(width, height, type);
			Graphics2D g2 = tmp.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g2.drawImage(resized, 0, 0, width, height, null);
			g2.dispose();
			resized = tmp;
		} while (width != targetWidth || height != targetHeight);
		if (logger.isDebugEnabled()) {
			long current = System.currentTimeMillis();
			String message = "Image resize time (" + ((current - start) / 1000.000) + " s), dimensions: " + targetWidth + "x" + targetHeight + " for file: " + imageFile.getAbsolutePath();
			logger.debug(message);
		}
		return resized;
	}

	/**
	 * Retrieve image dimensions.  This method simply reads headers so it should perform
	 * relatively fast.
	 */
	protected static Dimension retrieveImageDimensions(File imageFile) throws IOException {
		if (!imageFile.exists()) {
			logger.info("No file found while determining image dimensions: " + imageFile.getAbsolutePath());
			return null;
		}
		ImageInputStream iis = null;
		Dimension dimensions = null;
		ImageReader reader = null;
		// use a FileInputStream and make sure it gets closed to prevent unclosed file
		// errors on some operating systems
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(imageFile);
			iis = ImageIO.createImageInputStream(fis);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				reader = readers.next();
				reader.setInput(iis, true);
				dimensions = new Dimension(reader.getWidth(0), reader.getHeight(0));
			}
		} finally {
			if (reader != null) {
				reader.dispose();
			}
			if (iis != null) {
				try {
					iis.close();
				} catch (IOException e) {
					// ignore
				}
			}
			IOUtils.closeQuietly(fis);
		}
		return dimensions;
	}

	/**
	 * Save an image to a specified file.
	 */
	protected static void saveImage(BufferedImage image, File file) throws IOException {
		String filename = file.getName();
		int pos = filename.lastIndexOf('.');
		if (pos == -1 || (pos + 1) >= filename.length()) {
			throw new IOException("Unknown image file type " + filename);
		}
		String imageType = filename.substring(pos + 1);
		File imageFile = new File(file.getParent(), filename);
		// use a FileOutputStream and make sure it gets closed to prevent unclosed file
		// errors on some operating systems
		FileOutputStream fos = null;
		try {
			// use the FileUtils utility method to ensure parent directories are created
			// if necessary
			fos = FileUtils.openOutputStream(imageFile);
			boolean result = ImageIO.write(image, imageType, fos);
			if (!result) {
				throw new IOException("No appropriate writer found when writing image: " + filename);
			}
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
