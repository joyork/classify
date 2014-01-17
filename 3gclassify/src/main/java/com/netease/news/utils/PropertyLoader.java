package com.netease.news.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;


public class PropertyLoader {

	private Properties properties = new Properties();
	
	private String propFile = null;
	
	public PropertyLoader(String propertiesFile){
		this.propFile = propertiesFile;
		this.load();
	}
	
	
	private void load(){
		if(this.propFile!=null){
			try {
				ClassLoader loader = this.getClass().getClassLoader();
				InputStream in=loader.getResourceAsStream(propFile);
				InputStreamReader reader = new InputStreamReader(in,"UTF-8");
				this.properties.load(reader);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public String getValue(String key){
		return properties.getProperty(key);		
	}
	
	public static void main(String[] args){
		PropertyLoader nextStageConf = new PropertyLoader("labelMap.properties");
		System.out.println(nextStageConf.getValue("C000022"));
	}
}
