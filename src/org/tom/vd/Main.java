package org.tom.vd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.log4j.PropertyConfigurator;
import org.tom.vd.mina.ApCommand;
import org.tom.vd.mina.MinaGameInterface;

import com.effecia.mina.MinaCommandPo;

/**
 * <p>
 * Title: VirtualDealer.java
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
 * @date 2016-1-10 下午3:42:44
 * @version 1.0
 * 
 */
public class Main {
	

	private void initLog4j() {
		System.out.println("初始化log4j........");
		Properties prop = new Properties();
		InputStream in = null;
		try {
			String path = URLDecoder.decode(this.getClass()
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath(), "UTF-8");
			File file = new File(path);
			String log4jPorp = file.getParentFile().getAbsolutePath()
					+ File.separator + "log4j.properties";
			System.out.println(log4jPorp);
			in = new FileInputStream(new File(log4jPorp));
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
		PropertyConfigurator.configure(prop);
		System.out.println("初始化log4j结束........");
	}

	private void initMina() {
		System.out.println("初始话Mina，并注册..........");
		new MinaGameInterface();
		MinaCommandPo regiestPo = new MinaCommandPo();
		regiestPo.setModuleType("PLATFORM");
		regiestPo.setRequestType("REQUEST");
		regiestPo.setCommandType(ApCommand.TABLE_INIT.toString());
		MinaGameInterface.ioSession.write(regiestPo);
		System.out.println("初始话Mina，并注册结束..........");
	}

	private void init() {
		initLog4j();
		initMina();
	}
	
//	public void start(){
//		new VirtualDealer().start();
//	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("every thing is begin.....");
		new Main().init();
		VirtualDealer.getVD().start();
	}

}
