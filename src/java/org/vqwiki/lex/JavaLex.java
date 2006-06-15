package org.vqwiki.lex;

import de.java2html.Java2Html;
import de.java2html.converter.Java2HtmlConversionOptions;

/**

 * @author garethc
 * Date: Jan 6, 2003
 * @author <a href="mailto:markus@jave.de">Markus Gebhard</a>
 * Date: Apr 12, 2003
 */
public class JavaLex implements ExternalLex {

  public String process(String text) {

	Java2HtmlConversionOptions options =
		Java2HtmlConversionOptions.getDefault();

	options.setShowTableBorder(true);

	options.setShowLineNumbers(false);
	return
		Java2Html.convertToHtml(text, options);
  }
}