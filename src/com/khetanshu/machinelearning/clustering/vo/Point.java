package com.khetanshu.machinelearning.clustering.vo;

import java.util.Map;
import java.util.function.BinaryOperator;

public class Point {
	private int assignedCluster;
	private double squareDistanceFromCentroid;
	private Map<Integer,Double> dimensions;
	
	
	public Point() {
	}
	
	public Point(Map<Integer,Double> dimensions,int assignedCluster) {
		setDimensions(dimensions);
		setAssignedCluster(assignedCluster);
		setSquareDistanceFromCentroid(Double.MAX_VALUE);
	}
	
	public static Double calculateSquareDistance(Map<Integer,Double> dimensions_A,Map<Integer,Double> dimensions_B) {
		Double squareDistance =0.0;
		for (Integer dimensionID : dimensions_A.keySet()) {
			squareDistance+=Math.pow(dimensions_B.get(dimensionID)-dimensions_A.get(dimensionID),2);
		}
		return squareDistance;
	}
	
	public static void main(String[] args) {
		String a = new String("AB");
		String b = new String("AC");
		
		int x = a.hashCode();
		int y = b.hashCode();
		int z=(x^y);
		System.out.println(x);
		System.out.println(y);
		System.out.println(z);
		
		System.out.println(Integer.toBinaryString(x));
		System.out.println(Integer.toBinaryString(y));
		System.out.println(Integer.toBinaryString(x^y));
		System.out.println(Integer.bitCount(x^y));
		
	}
//	@Override
//	public String toString() {
//		return "Point [assignedCluster=" + assignedCluster + 
//				/*", squareDistanceFromCentroid="+ squareDistanceFromCentroid +*/ 
//				", dimensions=" + dimensions + "]";
//	}
	
	@Override
	public String toString() {
		return "Point [assignedCluster=" + assignedCluster+" ]";
	}

	public int getAssignedCluster() {
		return assignedCluster;
	}
	public void setAssignedCluster(int assignedCluster) {
		this.assignedCluster = assignedCluster;
	}
	public Map<Integer, Double> getDimensions() {
		return dimensions;
	}
	public void setDimensions(Map<Integer, Double> dimensions) {
		this.dimensions = dimensions;
	}

	public double getSquareDistanceFromCentroid() {
		return squareDistanceFromCentroid;
	}

	public void setSquareDistanceFromCentroid(double squareDistanceFromCentroid) {
		this.squareDistanceFromCentroid = squareDistanceFromCentroid;
	}
	
}
