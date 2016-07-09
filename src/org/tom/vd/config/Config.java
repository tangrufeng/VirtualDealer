package org.tom.vd.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * <p>
 * Title: Config.java
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2014
 * </p>
 * <p>
 * Company: jc-yt.com
 * </p>
 * 
 * @author tang
 * @date 2016-1-10 ����2:46:53
 * @version 1.0
 * 
 */
public class Config {

	Properties prop = new Properties();
	private static Config cfg;

	private Config() {
		init();
	}

	public static Config getCfg() {
		if (cfg == null) {
			cfg = new Config();

		}
		return cfg;
	}

	private void init() {
		System.out.println("init properties........");
		InputStream in = null;
		try {
			String path = URLDecoder.decode(this.getClass()
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath(), "UTF-8");
			File file = new File(path);
			String configPorp = file.getParentFile().getAbsolutePath()
					+ File.separator + "config.properties";
			System.out.println(configPorp);
			in = new FileInputStream(new File(configPorp));
			prop.load(in);
			String configPorp1 = file.getParentFile().getAbsolutePath()
					+ File.separator + "db.properties";
			System.out.println(configPorp1);
			in = new FileInputStream(new File(configPorp1));
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public String getString(String name,String defaultValue){
		return prop.getProperty(name,defaultValue);
	}

	public String getString(String name){
		return prop.getProperty(name);
	}
	
	
}
