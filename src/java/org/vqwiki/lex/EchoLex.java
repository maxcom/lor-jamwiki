package org.vqwiki.lex;


/**
 * @author garethc
 * Date: Jan 7, 2003
 */
public class EchoLex implements ExternalLex {
  public String process(String text) {
    return text;
  }
}
