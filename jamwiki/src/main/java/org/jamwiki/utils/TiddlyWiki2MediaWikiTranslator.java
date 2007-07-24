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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * This class translates TiddlyWiki markup to MediaWiki markup.
 * @author Michael Greifeneder mikegr@gmx.net
 */
public class TiddlyWiki2MediaWikiTranslator {
        private static final WikiLogger logger = WikiLogger.getLogger(TiddlyWiki2MediaWikiTranslator.class.getName());
        
            public static String newline = System.getProperty("line.separator");
            
            public String translate(String wikicode) throws IOException {
                    String replaced = wikicode.replaceAll("\\\\n", "\n");
                    replaced = insertBreaks(replaced);
                    logger.info("Content with breaks?:  " + replaced);
                    
                    replaced = tables(headers(wikiLinks(replaced)));
                    return replaced;
            }
            
            private String tables(String wikicode) throws IOException {
                    BufferedReader reader = new BufferedReader(new StringReader(wikicode));
                    StringBuffer output = new StringBuffer();
                    boolean inTable = false;
                    String line = reader.readLine();
                    while ( line != null) {
                            if (inTable) { 
                                    if (line.startsWith("|")) {
                                           output.append("|-"); //new row
                                           output.append(newline);
                                           output.append(translateTableLine(line));
                                    }
                                    else {
                                            output.append("|}");
                                            output.append(newline);
                                            output.append(line);
                                            inTable = false;
                                    }
                                    
                            }
                            else {
                                    if (line.startsWith("|")) { 
                                            output.append("{|");
                                            output.append(newline);
                                            inTable = true;
                                            output.append(translateTableLine(line));
                                    }
                                    else {
                                            output.append(line);
                                    }
                            }
                            line = reader.readLine();
                            if (line != null) {
                                    output.append(newline);        
                            }
                    }
                    if (inTable) {
                            output.append(newline);
                            output.append("|}");
                    }
                    return output.toString();
            }
            
            private String translateTableLine(String line) {
                    StringTokenizer tokenizer = new StringTokenizer(line, "|");
                    StringBuffer output = new StringBuffer();
                    output.append("|");
                    while(tokenizer.hasMoreTokens()) { 
                            String token = tokenizer.nextToken();
                            if (token.startsWith("!")) { //headers bold
                                    output.append("'''");
                            }
                            output.append(token);
                            if (token.startsWith("!")) {
                                    output.append("'''");
                            }
                            if (tokenizer.hasMoreTokens()) {
                                    output.append("||");
                            }
                    }
                    return output.toString();
            }
            
            private String headers(String wikicode) throws IOException {
                    BufferedReader reader = new BufferedReader(new StringReader(wikicode));
                    String line = null;
                    StringBuffer output = new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                           int i = 0;
                           while (line.length()>i && line.charAt(i) == '!') {
                                   output.append('=');
                                   i++;
                           }
                           output.append(line.substring(i));
                           for (int j = 0; j < i; j++) {
                                   output.append('=');
                           }
                           output.append(newline);
                    }
                    return output.toString();
            }
        
            public String wikiLinks(String wikicode) {
                    StringBuffer output = new StringBuffer();
                    StringTokenizer tokenizer = new StringTokenizer(wikicode, " \t\n\r\f<>", true);
                    while (tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();
                            if (token.length() > 0 && Character.isUpperCase(token.charAt(0))) {
                                    String rest = token.substring(1);
                                    if (!rest.toLowerCase().equals(rest)) {
                                         output.append("[[");
                                         output.append(token);
                                         output.append("]]");
                                    }
                                    else {
                                            output.append(token);
                                    }
                            }
                            else {
                                    output.append(token);
                            }
                    }
                    return output.toString();
            }
            
            public String insertBreaks(String wikicode) throws IOException {
                    BufferedReader reader = new BufferedReader(new StringReader(wikicode));
                    
                    StringBuffer output = new StringBuffer();
                    boolean isLetterLast = false;
                    boolean isLetterNow = false;
                    String line = reader.readLine();
                    String lastLine = null;
                    while (line != null) {
                            isLetterLast = isLetterNow;
                            if (line !=null && line.length()>0 && Character.isLetterOrDigit(line.charAt(0))){
                                    isLetterNow = true;
                            }
                            else {
                                    isLetterNow = false;
                            }
                            if (lastLine != null) output.append(lastLine);
                            if (isLetterLast && isLetterNow) {
                                    output.append("<br/>");
                            }
                            if (lastLine != null) output.append(newline);
                            lastLine = line;
                            line = reader.readLine();
                            if (line == null) {
                                    output.append(lastLine);
                            }
                    }
                    return output.toString();
            }
 
            public static void main(String[] args) {
                String t = new String("abc\\nabc");
                String r = t.replaceAll("\\\\n", "DUMMY");
                
        }
}
