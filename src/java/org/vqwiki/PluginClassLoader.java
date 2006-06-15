/**
 * Based on code from The Java(TM) Developers Almanac 1.4,
 * Volume 1: Examples and Quick Reference (4th Edition)
 * by Patrick Chan
 *
 * @author garethc
 *  11/11/2002 12:06:56
 */
package org.vqwiki;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PluginClassLoader extends ClassLoader {

    private static final Logger logger = Logger.getLogger(PluginClassLoader.class);
    private String pluginsDir;

    /**
     *
     */
    public PluginClassLoader(String pluginsDir) {
        this.pluginsDir = pluginsDir;
    }

    /**
     *
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        byte[] b = new byte[0];
        try {
            b = loadClassData(name);
        } catch (Exception e) {
            logger.warn(e);
        }
        return defineClass(name, b, 0, b.length);
    }

    /**
     *
     */
    private byte[] loadClassData(String name) throws Exception {
        File file = new File(this.pluginsDir, name + ".class");
        InputStream is = new FileInputStream(file);
        // Get the size of the file
        long length = file.length();
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}

// $Log$
// Revision 1.6  2006/04/23 07:52:28  wrh2
// Coding style updates (VQW-73).
//
// Revision 1.5  2003/10/05 05:07:30  garethc
// fixes and admin file encoding option + merge with contributions
//
// Revision 1.4  2003/04/15 08:41:31  mrgadget4711
// ADD: Lucene search
// ADD: RSS Stream
//
// Revision 1.3  2003/01/07 03:11:52  garethc
// beginning of big cleanup, taglibs etc
//
// Revision 1.2  2002/12/08 20:58:58  garethc
// 2.3.6 almost ready
//
// Revision 1.1  2002/11/10 23:53:02  garethc
// manual and plugins
//