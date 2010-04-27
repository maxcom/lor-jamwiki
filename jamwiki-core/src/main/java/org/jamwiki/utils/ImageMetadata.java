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

/**
 * Utility method used to model image metadata including alignment, max dimension, etc.
 */
public class ImageMetadata {

	private static final WikiLogger logger = WikiLogger.getLogger(ImageMetadata.class.getName());
	private String alt = null;
	private ImageBorderEnum border = ImageBorderEnum.NOT_SPECIFIED;
	private String caption = null;
	private ImageHorizontalAlignmentEnum horizontalAlignment = ImageHorizontalAlignmentEnum.NOT_SPECIFIED;
	/**
	 * Destination to link to when the image is clicked.  May be either a topic or a URL.  If
	 * this value is an empty string then no link is generated.
	 */
	private String link = null;
	/**
	 * A value in pixels indicating the maximum height value allowed for the image.  If this
	 * value is greater than zero then the image will be resized so that the height does not
	 * exceed this value.
	 */
	private int maxHeight = -1;
	/**
	 * A value in pixels indicating the maximum width value allowed for the image.  If this
	 * value is greater than zero then the image will be resized so that the width does not
	 * exceed this value.
	 */
	private int maxWidth = -1;
	private ImageVerticalAlignmentEnum verticalAlignment = ImageVerticalAlignmentEnum.NOT_SPECIFIED;

	/**
	 *
	 */
	public String getAlt() {
		return this.alt;
	}

	/**
	 *
	 */
	public void setAlt(String alt) {
		this.alt = alt;
	}

	/**
	 *
	 */
	public ImageBorderEnum getBorder() {
		return this.border;
	}

	/**
	 *
	 */
	public void setBorder(ImageBorderEnum border) {
		this.border = border;
	}

	/**
	 *
	 */
	public String getCaption() {
		return this.caption;
	}

	/**
	 *
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 *
	 */
	public ImageHorizontalAlignmentEnum getHorizontalAlignment() {
		return this.horizontalAlignment;
	}

	/**
	 *
	 */
	public void setHorizontalAlignment(ImageHorizontalAlignmentEnum horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	/**
	 *
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 *
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 *
	 */
	public int getMaxHeight() {
		return this.maxHeight;
	}

	/**
	 *
	 */
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	/**
	 *
	 */
	public int getMaxWidth() {
		return this.maxWidth;
	}

	/**
	 *
	 */
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	/**
	 *
	 */
	public ImageVerticalAlignmentEnum getVerticalAlignment() {
		return this.verticalAlignment;
	}

	/**
	 *
	 */
	public void setVerticalAlignment(ImageVerticalAlignmentEnum verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}
}
