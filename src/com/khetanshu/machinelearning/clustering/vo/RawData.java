package com.khetanshu.machinelearning.clustering.vo;

import java.util.HashMap;
import java.util.Map;

public class RawData {
	private Map<Integer, Point> points;
	private Map<Integer,Integer> pointToClassMap;
	public RawData() {
		points = new HashMap<>();
		pointToClassMap= new HashMap<>();
		
	}
	public Map<Integer, Point> getPoints() {
		return points;
	}
	
	public Map<Integer, Integer> getPointToClassMap() {
		return pointToClassMap;
	}
	
}
