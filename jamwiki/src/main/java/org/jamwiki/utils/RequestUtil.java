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

import java.io.File;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Provides utility methods useful for processing and interpreting
 * ServletRequest objects.
 */
public class RequestUtil {

	/** Logger */
	public static final WikiLogger logger = WikiLogger.getLogger(RequestUtil.class.getName());

	/**
	 *
	 */
	private RequestUtil() {
	}

	/**
	 * Duplicate the functionality of the request.getRemoteAddr() method, but
	 * for IPv6 addresses strip off any local interface information (anything
	 * following a "%").
	 *
	 * @param request the HTTP request object.
	 * @return The IP address that the request originated from, or 0.0.0.0 if
	 *  the originating address cannot be determined.
	 */
	public static String getIpAddress(HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request object cannot be null");
		}
		String ipAddress = request.getRemoteAddr();
		int pos = ipAddress.indexOf("%");
		if (pos != -1) {
			ipAddress = ipAddress.substring(0, pos);
		}
		if (!Utilities.isIpAddress(ipAddress)) {
			logger.info("Invalid IP address found in request: " + ipAddress);
			ipAddress = "0.0.0.0";
		}
		return ipAddress;
	}

	/**
	 * Utility method for parsing a multipart servlet request.  This method returns
	 * an iterator of FileItem objects that corresponds to the request.
	 *
	 * @param request The servlet request containing the multipart request.
	 * @param uploadDirectory The directory into which files will be uploaded.
	 * @param maxFileSize The maximum allowed file size in bytes.
	 * @return Returns an iterator of FileItem objects the corresponds to the request.
	 * @throws Exception Thrown if any problems occur while processing the request.
	 */
	public static Iterator processMultipartRequest(HttpServletRequest request, String uploadDirectory, long maxFileSize) throws Exception {
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setRepository(new File(uploadDirectory));
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setHeaderEncoding("UTF-8");
		upload.setSizeMax(maxFileSize);
		return upload.parseRequest(request).iterator();
	}
}
