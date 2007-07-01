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
package org.jamwiki.servlets;

import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jamwiki.WikiBase;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.model.Role;
import org.jamwiki.utils.Utilities;
import org.jamwiki.utils.WikiLogger;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 */
public class RolesServlet extends JAMWikiServlet {

	private static WikiLogger logger = WikiLogger.getLogger(RolesServlet.class.getName());
	protected static final String JSP_ADMIN_ROLES = "admin-roles.jsp";

	/**
	 * This method handles the request after its parent class receives control.
	 *
	 * @param request - Standard HttpServletRequest object.
	 * @param response - Standard HttpServletResponse object.
	 * @return A <code>ModelAndView</code> object to be handled by the rest of the Spring framework.
	 */
	protected ModelAndView handleJAMWikiRequest(HttpServletRequest request, HttpServletResponse response, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String function = request.getParameter("function");
		if (!StringUtils.hasText(function)) {
			view(request, next, pageInfo);
		} else if (function.equals("modifyRole")) {
			modifyRole(request, next, pageInfo);
		} else if (function.equals("assignRole")) {
			// FIXME - enable this
			view(request, next, pageInfo);
		}
		return next;
	}

	/**
	 *
	 */
	private void modifyRole(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		String updateRole = request.getParameter("updateRole");
		Role role = null;
		if (StringUtils.hasText(request.getParameter("Submit"))) {
			try {
				role = new Role();
				// once created a role name cannot be modified, so the text field
				// will be disabled in the form.
				String roleName = (StringUtils.hasText(request.getParameter("roleName"))) ? request.getParameter("roleName") : updateRole;
				role.setName(roleName);
				role.setDescription(request.getParameter("roleDescription"));
				Utilities.validateRole(role);
				WikiBase.getDataHandler().writeRole(role, null);
				if (StringUtils.hasText(updateRole) && updateRole.equals(role.getName())) {
					next.addObject("message", new WikiMessage("roles.message.roleupdated", role.getName()));
				} else {
					next.addObject("message", new WikiMessage("roles.message.roleadded", role.getName()));
				}
			} catch (WikiException e) {
				next.addObject("message", e.getWikiMessage());
			} catch (Exception e) {
				logger.severe("Failure while adding role", e);
				next.addObject("message", new WikiMessage("roles.message.rolefail", e.getMessage()));
			}
		} else if (StringUtils.hasText(updateRole)) {
			// FIXME - use a cached list of roles instead of iterating
			// load details for the selected role
			Collection roles = WikiBase.getDataHandler().getAllRoles();
			Iterator roleIterator = roles.iterator();
			while (roleIterator.hasNext()) {
				Role tempRole = (Role)roleIterator.next();
				if (tempRole.getName().equals(updateRole)) {
					role = tempRole;
				}
			}
		}
		if (role != null) {
			next.addObject("roleName", role.getName());
			next.addObject("roleDescription", role.getDescription());
		}
		this.view(request, next, pageInfo);
	}

	/**
	 *
	 */
	private void view(HttpServletRequest request, ModelAndView next, WikiPageInfo pageInfo) throws Exception {
		Collection roles = WikiBase.getDataHandler().getAllRoles();
		next.addObject("roles", roles);
		next.addObject("roleCount", new Integer(roles.size()));
		pageInfo.setAdmin(true);
		pageInfo.setContentJsp(JSP_ADMIN_ROLES);
		pageInfo.setPageTitle(new WikiMessage("roles.title"));
	}
}
