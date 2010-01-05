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
package org.jamwiki.migrate;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.jamwiki.model.Topic;
import org.jamwiki.model.TopicVersion;

/**
 * Interface that controls how topics are imported.
 */
public interface TopicImporter {

	/**
	 * Parse the contents of the file, returning a map of topics and the associated topic
	 * versions for all data contained in the file.
	 *
	 * @param file The file containing all topic data to be imported.
	 * @return A map of topics and all versions for each topic, sorted chronologically from
	 *  oldest to newest.  Note that the exported topic is not required to have a virtual
	 *  wiki set, so the code that processes these values should set one.
	 * @throws MigrationException Thrown if any error occurs during import.
	 */
	public Map<Topic, List<TopicVersion>> importFromFile(File file) throws MigrationException;
}
