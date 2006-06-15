/**
 * @author garethc
 * Date: Jan 15, 2003
 */
package org.vqwiki;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.vqwiki.utils.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// FIXME - is this class used anywhere???
public class ParameterValidator {

	private static ParameterValidator instance;
	private static final Logger logger = Logger.getLogger(ParameterValidator.class);
	private Map definitions;
	private static final String DEFINITIONS_FILENAME = "/parametervalidation.xml";
	private static final String TAG_PARAMETER = "parameter";

	/**
	 *
	 */
	public synchronized static ParameterValidator getInstance() {
		if (instance == null) {
			instance = new ParameterValidator();
		}
		return instance;
	}

	/**
	 *
	 */
	private ParameterValidator() {
		definitions = new HashMap();
		try {
			String fileName = getClass().getResource(DEFINITIONS_FILENAME).getFile();
			Document definitionDocument = Utilities.parseDocumentFromFile(fileName);
			NodeList parameters = definitionDocument.getElementsByTagName(TAG_PARAMETER);
			for (int i = 0; i < parameters.getLength(); i++) {
				Element parameterElement = (Element) parameters.item(i);
				ParameterValidationDefinition definition = new ParameterValidationDefinition();
			}
		} catch (Exception e) {
			logger.warn(e);
		}
	}
}

class ParameterValidationDefinition {

	private String action;
	private String name;
	private List types;
	private boolean duplicates;
	private String regularExpression;

	/**
	 *
	 */
	public String getAction() {
		return action;
	}

	/**
	 *
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 *
	 */
	public String getName() {
		return name;
	}

	/**
	 *
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 */
	public List getTypes() {
		return types;
	}

	/**
	 *
	 */
	public void setTypes(List types) {
		this.types = types;
	}

	/**
	 *
	 */
	public boolean isDuplicates() {
		return duplicates;
	}

	/**
	 *
	 */
	public void setDuplicates(boolean duplicates) {
		this.duplicates = duplicates;
	}

	/**
	 *
	 */
	public String getRegularExpression() {
		return regularExpression;
	}

	/**
	 *
	 */
	public void setRegularExpression(String regularExpression) {
		this.regularExpression = regularExpression;
	}
}
