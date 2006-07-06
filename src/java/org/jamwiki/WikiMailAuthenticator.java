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
package org.jamwiki;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import org.apache.log4j.Logger;
import org.jamwiki.utils.Encryption;

/**
 *
 */
public class WikiMailAuthenticator extends Authenticator {

	private static final Logger logger = Logger.getLogger(WikiMailAuthenticator.class);

	/**
	 *
	 */
	protected PasswordAuthentication getPasswordAuthentication() {
		PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
			Environment.getValue(Environment.PROP_EMAIL_SMTP_USERNAME),
			Encryption.getEncryptedProperty(Environment.PROP_EMAIL_SMTP_PASSWORD)
		);
		logger.debug("Authenticating with: " + passwordAuthentication);
		return passwordAuthentication;
	}
}
