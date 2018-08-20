/*
 * Copyright 2012 GroundWork Open Source, Inc. ("GroundWork") All rights
 * reserved. This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.groundwork.agents.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class SharedSecretProtector {

	private static final byte[] SALT = { (byte) 0xde, (byte) 0x33, (byte) 0x10,
			(byte) 0x12, (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12, };
	private static final String ALGORITHM = "PBEWithMD5AndDES";
	
	// This passphrase is used by both encryptor and decryptor to generate a common shared key
	private static final String passPhrase = "gwoskey67";

	
	
	/**
	 * Encrypts the property
	 * @param property
	 * @return
	 * @throws java.security.GeneralSecurityException
	 */
	public static String encrypt(String property)
			throws GeneralSecurityException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(ALGORITHM);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(passPhrase
				.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
		pbeCipher
				.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return base64Encode(pbeCipher.doFinal(property.getBytes()));
	}

	private static String base64Encode(byte[] bytes) {
			return new BASE64Encoder().encode(bytes);
	}

	/**
	 * Decrypts the password
	 * @param property
	 * @return
	 * @throws java.security.GeneralSecurityException
	 * @throws java.io.IOException
	 */
	public static String decrypt(String property)
			throws GeneralSecurityException, IOException {
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance(ALGORITHM);
		SecretKey key = keyFactory.generateSecret(new PBEKeySpec(passPhrase
				.toCharArray()));
		Cipher pbeCipher = Cipher.getInstance(ALGORITHM);
		pbeCipher
				.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
		return new String(pbeCipher.doFinal(base64Decode(property)));
	}

	private static byte[] base64Decode(String property) throws IOException {		
		return new BASE64Decoder().decodeBuffer(property);
	}

}
