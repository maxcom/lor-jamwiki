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
package org.jamwiki.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.jamwiki.Environment;

/**
 * Provide capability for encrypting and decrypting values.  Inspired by an
 * example from http://www.devx.com/assets/sourcecode/10387.zip.
 */
public class Encryption {

	private static Logger logger = Logger.getLogger(Encryption.class);
	public static final String DES_ALGORITHM = "DES";
	public static final String ENCRYPTION_KEY = "JAMWiki Key 12345";

	/**
	 * Hide the constructor by making it private.
	 */
	private Encryption() {
	}

	/**
	 * Encrypt a String value using the DES encryption algorithm.
	 *
	 * @param unencryptedString The unencrypted String value that is to be encrypted.
	 * @return An encrypted version of the String that was passed to this method.
	 */
	public static String encrypt(String unencryptedString) throws Exception {
		if (unencryptedString == null || unencryptedString.trim().length() == 0) {
			return unencryptedString;
		}
		try {
			SecretKey key = createKey();
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] unencryptedBytes = unencryptedString.getBytes("UTF8");
			byte[] encryptedBytes = Base64.encodeBase64(cipher.doFinal(unencryptedBytes));
			return bytes2String(encryptedBytes);
		} catch (Exception e) {
			logger.error("Encryption error while processing value '" + unencryptedString + "'", e);
			throw e;
		}
	}

	/**
	 * Unencrypt a String value using the DES encryption algorithm.
	 *
	 * @param encryptedString The encrypted String value that is to be unencrypted.
	 * @return An unencrypted version of the String that was passed to this method.
	 */
	public static String decrypt(String encryptedString) {
		if (encryptedString == null || encryptedString.trim().length() <= 0) {
			return encryptedString;
		}
		try {
			SecretKey key = createKey();
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] encryptedBytes = encryptedString.getBytes("UTF8");
			byte[] unencryptedBytes = cipher.doFinal(Base64.decodeBase64(encryptedBytes));
			return bytes2String(unencryptedBytes);
		} catch (Exception e) {
			logger.error("Decryption error while processing value '" + encryptedString + "'", e);
			// FIXME - should this throw the exception - caues issues upstream.
			return null;
		}
	}

	/**
	 * Convert a byte array to a String value.
	 *
	 * @param bytes The byte array that is to be converted.
	 * @return A String value created from the byte array that was passed to this method.
	 */
	private static String bytes2String(byte[] bytes) {
		StringBuffer buffer = new StringBuffer();
		for (int i=0; i < bytes.length; i++) {
			buffer.append((char)bytes[i]);
		}
		return buffer.toString();
	}

	/**
	 * Create the encryption key value.
	 *
	 * @return An encryption key value implementing the DES encryption algorithm.
	 */
	private static SecretKey createKey() throws Exception {
		byte[] bytes = ENCRYPTION_KEY.getBytes("UTF8");
		DESKeySpec spec = new DESKeySpec(bytes);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES_ALGORITHM);
		return keyFactory.generateSecret(spec);
	}

	/**
	 * If a property value is encrypted, return the unencrypted value.
	 *
	 * @param name The name of the encrypted property being retrieved.
	 * @return The unencrypted value of the property.
	 */
	public static String getEncryptedProperty(String name) {
		if (Environment.getBooleanValue(Environment.PROP_BASE_ENCODE_PASSWORDS)) {
			return Encryption.decrypt(Environment.getValue(name));
		}
		return Environment.getValue(name);
	}

	/**
	 * Encrypt and set a property value.
	 *
	 * @param name The name of the encrypted property being retrieved.
	 * @value The enencrypted value of the property.
	 */
	public static void setEncryptedProperty(String name, String value) throws Exception {
		if (Environment.getBooleanValue(Environment.PROP_BASE_ENCODE_PASSWORDS)) {
			value = Encryption.encrypt(value);
		}
		Environment.setValue(name, value);
	}

	/**
	 * Change whether or not passwords are encrypted in property files.
	 *
	 * @param encrypt Set to <code>true</code> if passwords should be
	 *  encrypted in property files.
	 */
	public static void togglePropertyEncryption(boolean encrypt) throws Exception {
		// get passwords prior to changing encryption
		String adminPassword = getEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD);
		String dbPassword = getEncryptedProperty(Environment.PROP_DB_PASSWORD);
		String smtpPassword = getEncryptedProperty(Environment.PROP_EMAIL_SMTP_PASSWORD);
		String userGroupPassword = getEncryptedProperty(Environment.PROP_USERGROUP_PASSWORD);
		// change encryption
		Environment.setBooleanValue(Environment.PROP_BASE_ENCODE_PASSWORDS, encrypt);
		// re-set passwords with changed encryption
		setEncryptedProperty(Environment.PROP_BASE_ADMIN_PASSWORD, adminPassword);
		setEncryptedProperty(Environment.PROP_DB_PASSWORD, dbPassword);
		setEncryptedProperty(Environment.PROP_EMAIL_SMTP_PASSWORD, smtpPassword);
		setEncryptedProperty(Environment.PROP_USERGROUP_PASSWORD, userGroupPassword);
	}
}
