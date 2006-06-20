/**
 *
 */
package org.jmwiki;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generic interface for actions
 */
public interface WikiAction {

	/**
	 * Carry out the action resulting from the given request and then pass back the response
	 * @param request request
	 * @param response response
	 * @throws Exception any exception that occurs in the action
	 */
	void doAction(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
