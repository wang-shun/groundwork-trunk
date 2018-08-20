/*
 * 
 * Copyright 2010 GroundWork Open Source, Inc. ("GroundWork") All rights
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
package com.groundwork.agents.appservers.utils;
/**
 * This class sends NSCA
 * 
 * @author Arul Shanmugam
 * 
 */
import java.io.IOException;

import com.googlecode.jsendnsca.Level;
import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosException;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;

public class SendNSCA {
	private NagiosSettings nagiosSettings = null;

	public SendNSCA(String nagiosHostName, int nagiosPort, int encryption, String encryptionKey) {
		Encryption encriptionType = Encryption.NONE;
		if (encryption == 0)
			encriptionType = Encryption.NONE;
		if (encryption == 1)
			encriptionType = Encryption.XOR;
		if (encryption == 3)
			encriptionType = Encryption.TRIPLE_DES;
		if (encryptionKey== null || encryptionKey.equals(""))
		nagiosSettings = new NagiosSettingsBuilder().withNagiosHost(
				nagiosHostName).withPort(nagiosPort).withEncryption(encriptionType).create();
		else
		nagiosSettings = new NagiosSettingsBuilder().withNagiosHost(
				nagiosHostName).withPort(nagiosPort).withEncryption(encriptionType).withPassword(encryptionKey).create();
	}

	public void send(String hostName, Level level, String serviceName,
			String message) {
		NagiosPassiveCheckSender sender = new NagiosPassiveCheckSender(
				nagiosSettings);

		MessagePayload payload = new MessagePayloadBuilder().withHostname(
				hostName).withLevel(level).withServiceName(serviceName)
				.withMessage(message).create();
		try {
			sender.send(payload);
		} catch (NagiosException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
