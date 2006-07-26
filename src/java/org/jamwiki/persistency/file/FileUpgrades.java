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
package org.jamwiki.persistency.file;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jamwiki.WikiBase;
import org.jamwiki.model.VirtualWiki;

/**
 *
 */
public class FileUpgrades {

	private static final Logger logger = Logger.getLogger(FileUpgrades.class);

	/**
	 *
	 */
	public static Vector upgrade010(Vector messages) throws Exception {
		String VIRTUAL_WIKI_LIST = "virtualwikis";
		File file = FileHandler.getPathFor("", null, VIRTUAL_WIKI_LIST);
		if (file.exists()) {
			List lines = FileUtils.readLines(file, "UTF-8");
			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
				String line = (String)iterator.next();
				VirtualWiki virtualWiki = new VirtualWiki();
				virtualWiki.setName(line);
				virtualWiki.setDefaultTopicName(WikiBase.DEFAULT_VWIKI);
				WikiBase.getHandler().writeVirtualWiki(virtualWiki);
			}
			file.delete();
		}
		// FIXME - hard coding
		messages.add("Updated virtual wiki files");
		return messages;
	}
}
