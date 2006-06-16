package org.jmwiki.applets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.OutputStream;
import org.apache.lucene.store.RAMDirectory;

import org.jmwiki.AbstractSearchEngine;

/*
Java MediaWiki - WikiWikiWeb clone
Copyright (C) 2001-2002 Gareth Cronin

This program is free software; you can redistribute it and/or modify
it under the terms of the latest version of the GNU Lesser General
Public License as published by the Free Software Foundation;

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program (gpl.txt); if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

/**
 * Search engine, which is used by the search-applet
 *
 * @author $Author: makub $
 */
public class AppletSearchEngine extends AbstractSearchEngine {

	/** Logging */
	private static final Logger logger =
		Logger.getLogger(AppletSearchEngine.class);

	/** The directory to base the search on */
	RAMDirectory ram;

	/**
	 * Init the search engine. Copy all files from the real index
	 * into the RAM, because search can only start from RAM.
	 */
	public AppletSearchEngine()
	{
		AppletSearchEngine.indexPath = "";

		ram = new RAMDirectory();

		StringBuffer contents = new StringBuffer();
		try {
			InputStream in = this.getClass().getResourceAsStream("/lucene/index.dir");
			  while (true) {
				int c = in.read();
				if (c == -1) break;
				contents.append((char) c);
			  }
			  in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
				//split is only in JDK1.4+
			//String[] filenames = contents.toString().split(",");
			StringTokenizer st = new StringTokenizer(contents.toString(),",");
			String[] filenames = new String[st.countTokens()];
			for(int i=0;i<filenames.length;i++) { filenames[i]=st.nextToken(); }

			for (int i=0; i < filenames.length; i++)
			{
				String filename = filenames[i];

				// make place on ram disk
				OutputStream os = ram.createFile(filename);

				// read current file
				InputStream is = this.getClass().getResourceAsStream("/lucene/index/" + filename);

 				// and copy to ram disk
				int len = Math.max(is.available(), 4086);
				byte[] buf = new byte[4086];
				while (len > 0)
				{
					len = is.read(buf, 0, 4086);
					os.writeBytes(buf, len);
				}

				// graceful cleanup
				is.close();
				os.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see jmwiki.AbstractSearchEngine#getFilename(java.lang.String, java.lang.String)
	 */
	protected String getFilename(String currentWiki, String topic) {
		return null;
	}

	/* (non-Javadoc)
	 * @see jmwiki.SearchEngine#getAllTopicNames(java.lang.String)
	 */
	public Collection getAllTopicNames(String virtualWiki) throws Exception {
		return Collections.EMPTY_LIST;
	}

	/**
	 * Get the directory, where the index is stored.
	 * This is the RAMDirectory!
	 */
	protected Directory getIndexDirectory(String indexFilename, boolean create)
		throws IOException
	{
		return ram;
	}

}
