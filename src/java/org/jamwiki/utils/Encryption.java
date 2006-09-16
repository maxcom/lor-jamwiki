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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.jamwiki.Environment;
import org.springframework.util.StringUtils;

/**
 * Provide capability for encrypting and decrypting values.  Inspired by an
 * example from http://www.devx.com/assets/sourcecode/10387.zip.
 */
public class Encryption {

	private static WikiLogger logger = WikiLogger.getLogger(Encryption.class.getName());
	public static final String DES_ALGORITHM = "DES";
	public static final String ENCRYPTION_KEY = "JAMWiki Key 12345";
	private static final char[] hexDigits = {
		'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'
	};

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
	public static String encrypt64(String unencryptedString) throws Exception {
		if (!StringUtils.hasText(unencryptedString)) {
			return unencryptedString;
		}
		byte[] unencryptedBytes = unencryptedString.getBytes("UTF8");
		return encrypt64(unencryptedBytes);
	}

	/**
	 * Encrypt a String value using the DES encryption algorithm.
	 *
	 * @param unencryptedString The unencrypted String value that is to be encrypted.
	 * @return An encrypted version of the String that was passed to this method.
	 */
	public static String encrypt64(byte[] unencryptedBytes) throws Exception {
		try {
			SecretKey key = createKey();
			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] encryptedBytes = Base64.encodeBase64(cipher.doFinal(unencryptedBytes));
			return bytes2String(encryptedBytes);
		} catch (Exception e) {
			logger.severe("Encryption error while processing value '" + bytes2String(unencryptedBytes) + "'", e);
			throw e;
		}
	}

	/**
	 *
	 */
	public static String encrypt(String unencryptedString) throws Exception {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			logger.severe("JDK does not support the SHA-512 encryption algorithm");
			throw e;
		}
		try {
			md.update(unencryptedString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// this should never happen
			logger.severe("Unsupporting encoding UTF-8");
			throw e;
		}
		byte raw[] = md.digest();
		return encrypt64(raw);
    }

	/**
	 * Unencrypt a String value using the DES encryption algorithm.
	 *
	 * @param encryptedString The encrypted String value that is to be unencrypted.
	 * @return An unencrypted version of the String that was passed to this method.
	 */
	public static String decrypt64(String encryptedString) {
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
			logger.severe("Decryption error while processing value '" + encryptedString + "'", e);
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
	public static String getEncryptedProperty(String name, Properties props) {
		if (props != null) {
			return Encryption.decrypt64(props.getProperty(name));
		}
		return Encryption.decrypt64(Environment.getValue(name));
	}

	/**
	 * Encrypt and set a property value.
	 *
	 * @param name The name of the encrypted property being retrieved.
	 * @value The enencrypted value of the property.
	 */
	public static void setEncryptedProperty(String name, String value, Properties props) throws Exception {
		value = Encryption.encrypt64(value);
		if (value == null) value = "";
		if (props != null) {
			props.setProperty(name, value);
		} else {
			Environment.setValue(name, value);
		}
	}
}
