package com.alvarosantisteban.pathos.utils;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
/**
 * Runs a small program to configure the ORMLITE
 * 
 * @author Alvaro Santisteban 2013 - alvarosantisteban@gmail.com
 *
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
	public static void main(String[] args) throws Exception {
	    writeConfigFile("ormlite_config.txt");
	}
}
