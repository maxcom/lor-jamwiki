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
	private ImageBorderEnum border = ImageBorderEnum.NOT_SPECIFIED;
	private String caption = null;
	private ImageHorizontalAlignmentEnum horizontalAlignment = ImageHorizontalAlignmentEnum.NOT_SPECIFIED;
	/**
	 * A value in pixels indicating the maximum width or height value allowed for the image.
	 * Images will be resized so that neither the width or height exceeds this value.
	 */
	private int maxDimension = -1;
	/**
	 * If this value is <code>true</code> then the generated HTML will include the image tag
	 * without a link to the image topic page.
	 */
	boolean suppressLink = false;
	private ImageVerticalAlignmentEnum verticalAlignment = ImageVerticalAlignmentEnum.NOT_SPECIFIED;

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
	public int getMaxDimension() {
		return this.maxDimension;
	}

	/**
	 *
	 */
	public void setMaxDimension(int maxDimension) {
		this.maxDimension = maxDimension;
	}

	/**
	 *
	 */
	public boolean getSuppressLink() {
		return this.suppressLink;
	}

	/**
	 *
	 */
	public void setSuppressLink(boolean suppressLink) {
		this.suppressLink = suppressLink;
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
