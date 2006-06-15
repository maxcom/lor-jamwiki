package org.vqwiki.lex;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import org.apache.log4j.Logger;

import org.vqwiki.lex.ParserInfo;

/**
 * Abstract class to be used when implementing new lexers.  New lexers
 * should extend this class and override any methods that need to be
 * implemented differently.
 */
public abstract class AbstractParser {

    private static final Logger logger = Logger.getLogger(AbstractParser.class);
    private ParserInfo parserInfo;

    /**
     * Sets the basics for this parser.
     *
     * @param parserInfo General information about this parser.
     */
    protected AbstractParser(ParserInfo parserInfo) {
        this.parserInfo = parserInfo;
    }

    /**
     * For getting general information about this parser.
     *
     * @return General information about this parser.
     */
    public ParserInfo getParserInfo() {
        return parserInfo;
    }

    /**
     * Returns a HTML representation of the given wiki raw text for online representation.
     *
     * @param raw The raw Wiki syntax to be converted into HTML.
     * @param virtualwiki A virtual wiki prefix (if any).
     * @return HTML representation of the text for online.
     */
    public abstract String parseHTML(String raw, String virtualwiki) throws IOException;

    /**
     * Returns a HTML representation of the given wiki raw text for the HTML-exporter.
     * This has mainly effect to the links. All links will be on HTML-files and not
     * on a Wiki-Servlet. The HTML-files will all be stored flat into the same directory.
     *
     * @param raw The raw Wiki syntax to be converted into HTML suitable for export.
     * @param virtualwiki A virtual wiki prefix (if any).
     * @return HTML representation of the text for HTML export.
     */
    public abstract String parseExportHTML(String raw, String virtualwiki) throws IOException;

    /**
     * Get a list of Topics from a raw text (used for backlinks, todo-Topics)
     *
     * @param raw The raw Wiki syntax to be converted into HTML.
     * @param virtualwiki A virtual wiki prefix (if any).
     * @return a List of all Topic names found in the text.
     */
    public abstract List getTopics(String rawtext, String virtualwiki) throws Exception;
}