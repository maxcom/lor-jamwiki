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
 * along with this program (gpl.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jmwiki.parser;

/**
 * This interface can be implemented in any way you like, it doesn't have to be for
 * a JLex generated lexer. As long as the implementing class implements a constructor
 * that takes a single InputStream or Reader parameter and returns one token at a
 * time from the yylex() method, it will work.
 */
public interface Lexer {

	/**
	 *
	 */
	public String yylex() throws java.io.IOException;

	/**
	 *
	 */
	public void setVirtualWiki(String vWiki);
}
