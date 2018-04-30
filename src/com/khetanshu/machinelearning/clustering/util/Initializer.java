package com.khetanshu.machinelearning.clustering.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import com.khetanshu.machinelearning.clustering.main.ClusteringAlgorithms;

public class Initializer {
	public static void initialize(String propertiesFileName) {
		File file = new File(propertiesFileName);
		FileInputStream fileInput;
		try {
			fileInput = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileInput);
			fileInput.close();
			
			Enumeration<Object> enuKeys = properties.keys();
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				
				switch(key) {
				case "NO_OF_CLUSTERS":
					ClusteringAlgorithms.NO_OF_CLUSTERS=Integer.valueOf(value);
					break;
				case "INPUT_RELATIVE_FILENAME":
					ClusteringAlgorithms.INPUT_RELATIVE_FILENAME = value;
					break;
				case "DEBUG_MODE":
					ClusteringAlgorithms.DEBUG_MODE = Boolean.valueOf(value);
					break;	
				case "PRINT_CLUSTER_POINTS":
					ClusteringAlgorithms.PRINT_CLUSTER_POINTS = Boolean.valueOf(value);
					break;
				case "PRINT_SILHOUETTE_SCORE":
					ClusteringAlgorithms.PRINT_SILHOUETTE_SCORE=Boolean.valueOf(value);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
