/*
 * This class provides capability for parsing HTML tags of the form <tag attribute="value">.
 */
package org.jamwiki.parser.jflex;

import java.util.LinkedHashMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.jamwiki.utils.WikiLogger;

%%

%public
%class JAMWikiHtmlProcessor
%extends JFlexLexer
%type String
%unicode
%ignorecase

/* code copied verbatim into the generated .java file */
%{
    private static final WikiLogger logger = WikiLogger.getLogger(JAMWikiHtmlProcessor.class.getName());
    private String currentAttributeKey;
    private String html;
    private String tagType;
    private LinkedHashMap<String, String> attributes = new LinkedHashMap<String, String>();

    /**
     *
     */
    private String closeTag(String closeString) {
        String value;
        StringBuilder result = new StringBuilder();
        result.append(this.tagType);
        for (String key : this.attributes.keySet()) {
            result.append(' ').append(key);
            value = this.attributes.get(key);
            if (value != null) {
                result.append('=').append(value);
            }
        }
        result.append(closeString);
        return result.toString();
    }

    /**
     *
     */
    protected String getTagType() {
        return this.tagType;
    }

    /**
     *
     */
    private void initialize() {
        this.attributes.clear();
        this.html = yytext();
        this.tagType = null;
    }
%}

whitespace         = [ \t\f]

inlineTag          = b|big|br|cite|code|del|em|font|i|ins|pre|s|small|span|strike|strong|sub|sup|tt|u|var
blockLevelTag      = blockquote|caption|center|dd|div|dl|dt|hr|li|ol|p|table|tbody|td|tfoot|th|thead|tr|ul
htmlTag            = {inlineTag}|{blockLevelTag}
coreAttributes     = id|class|style|title
i18nAttributes     = lang|xml:lang|dir
alignAttributes    = align
tableAttributes    = align|bgcolor|border|cellpadding|cellspacing|class|colspan|height|nowrap|rowspan|start|style|valign|width
htmlAttributes     = {tableAttributes}|alt|background|clear|color|face|id|size|valign
eventAttributes    = onclick|ondblclick|onmousedown|onmouseup|onmouseover|onmousemove|onmouseout|onkeypress|onkeydown|onkeyupjsattributes
focusAttributes    = accesskey|tabindex|onfocus|onblur
scriptAttributes   = id|charset|type|language|src|defer|xml:space

tagContent         = "<" ({whitespace})* ({htmlTag}) ~">"
tagClose           = "<" ({whitespace})* "/" ({whitespace})* ({htmlTag}) ({whitespace})* ">"
tagCloseContent    = ({whitespace})* ">"
tagCloseNoContent  = "/" ({whitespace})* ">"
tagAttributeKey    = ({htmlAttributes})
tagAttributeValueInQuotes = "\"" ~"\""
tagAttributeValueInSingleQuotes = "'" ~"'"
tagAttributeValueNoQuotes = [^ \t\f]

%state HTML_CLOSE, HTML_OPEN, HTML_ATTRIBUTE_KEY, HTML_ATTRIBUTE_VALUE

%%

<YYINITIAL> {
    {tagClose} {
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml(yytext());
        }
        this.initialize();
        int pos = this.html.indexOf("/");
        yypushback(this.html.length() - (pos + 1));
        beginState(HTML_CLOSE);
        return "</";
    }
    {tagContent} {
        if (!allowHTML()) {
            return StringEscapeUtils.escapeHtml(yytext());
        }
        this.initialize();
        yypushback(this.html.length() - 1);
        beginState(HTML_OPEN);
        return "<";
    }
    /* error fallthrough */
    . {
        throw new IllegalArgumentException("YYINITIAL: Invalid HTML tag: " + yytext());
    }
}

<HTML_CLOSE> {
    {whitespace} {
        // ignore whitespace
        return "";
    }
    {htmlTag} {
        this.tagType = yytext().toLowerCase();
        return "";
    }
    ">" {
        endState();
        return this.closeTag(">");
    }
    . {
        throw new IllegalArgumentException("HTML_CLOSE: Invalid HTML tag: " + this.html);
    }
}

<HTML_OPEN> {
    {whitespace} {
        // ignore whitespace
        return "";
    }
    {htmlTag} {
        endState();
        beginState(HTML_ATTRIBUTE_KEY);
        this.tagType = yytext().toLowerCase();
        return "";
    }
    . {
        throw new IllegalArgumentException("HTML_OPEN: Invalid HTML tag: " + this.html);
    }
}

<HTML_ATTRIBUTE_VALUE, HTML_ATTRIBUTE_KEY> {
    {tagCloseNoContent} {
        // tag close, done
        endState();
        return this.closeTag(" />");
    }
    {tagCloseContent} {
        // tag close, done
        endState();
        return this.closeTag(">");
    }
}

<HTML_ATTRIBUTE_KEY> {
    {tagAttributeKey} {
        this.currentAttributeKey = yytext();
        return "";
    }
    "=" ({whitespace})* {
        if (this.currentAttributeKey != null) {
            endState();
            beginState(HTML_ATTRIBUTE_VALUE);
        }
        return "";
    }
    {whitespace} {
        // ignore whitespace
        return "";
    }
    . {
        // invalid attribute
        return "";
    }
}

<HTML_ATTRIBUTE_VALUE> {
    {tagAttributeValueInQuotes} {
        endState();
        beginState(HTML_ATTRIBUTE_KEY);
        if (!allowJavascript() && yytext().indexOf("javascript") != -1) {
            // potential XSS attack, drop this attribute
            this.attributes.remove(this.currentAttributeKey);
        } else {
            this.attributes.put(this.currentAttributeKey, yytext());
        }
        this.currentAttributeKey = null;
        return "";
    }
    {tagAttributeValueInSingleQuotes} {
        endState();
        beginState(HTML_ATTRIBUTE_KEY);
        if (!allowJavascript() && yytext().indexOf("javascript") != -1) {
            // potential XSS attack, drop this attribute
            this.attributes.remove(this.currentAttributeKey);
        } else {
            // convert apostrophes to quotation marks
            this.attributes.put(this.currentAttributeKey, "\"" + yytext().substring(1, yytext().length() - 1) + "\"");
        }
        this.currentAttributeKey = null;
        return "";
    }
    {tagAttributeValueNoQuotes} {
        endState();
        beginState(HTML_ATTRIBUTE_KEY);
        if (!allowJavascript() && yytext().indexOf("javascript") != -1) {
            // potential XSS attack, drop this attribute
            this.attributes.remove(this.currentAttributeKey);
        } else {
            // add quotes
            this.attributes.put(this.currentAttributeKey, "\"" + yytext() + "\"");
        }
        this.currentAttributeKey = null;
        return "";
    }
    . {
        throw new IllegalArgumentException("HTML_ATTRIBUTE_VALUE: Invalid HTML tag: " + this.html);
    }
}
