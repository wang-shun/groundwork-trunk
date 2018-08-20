package com.groundworkopensource.tomcat.nagios.plugin;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Shell {

	public static void main(String[] args) {
		Monitor mon = null;
		if (args.length > 0 && args[0].equalsIgnoreCase("-f")
				|| args[0].equalsIgnoreCase("-file")) {
			if (args.length != 2) {
				print(new Exception("Bad argument " + args[1]));
				System.exit(Monitor.UNKNOWN_STATUS);
			}
			String[] argv = null;
			File inFile = new File(args[1]);
			if (inFile.exists() == false) {
				print(new Exception("File " + args[1] + " does not exist"));
				System.exit(Monitor.UNKNOWN_STATUS);
			}
			byte[] bytes = new byte[(int) inFile.length()];
			try {
				FileInputStream fstream = new FileInputStream(inFile);
				DataInputStream in = new DataInputStream(fstream);
				in.read(bytes);
				in.close();
				String s = new String(bytes);
				args = s.split("\n");
				for (int i=0;i<args.length;i++) {
					args[i] = args[i].replace("\r", "");
				}
			} catch (Exception e) {
				print(e);
				System.exit(Monitor.UNKNOWN_STATUS);
			}
		}

		try {
			mon = new Monitor(args);
		} catch (Exception e) {
			print(e);
			System.exit(mon.getRC());
		}
	}
	
	public static void print(Exception e) {
		String chopped = e.toString().substring(
				e.toString().indexOf(":") + 2);
		System.out.println(chopped);
	}
}
