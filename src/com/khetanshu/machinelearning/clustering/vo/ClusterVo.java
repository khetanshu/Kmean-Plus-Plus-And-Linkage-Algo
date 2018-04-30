package com.khetanshu.machinelearning.clustering.vo;

public class ClusterVo {
	private double inClusterAverageDistance;
	private double outClusterMinAverageDistance;

	public ClusterVo() {
		inClusterAverageDistance=0.0;
		setOutClusterMinAverageDistance(Double.MAX_VALUE);
	}

	public double getInClusterAverageDistance() {
		return inClusterAverageDistance;
	}

	public void setInClusterAverageDistance(double inClusterAverageDistance) {
		this.inClusterAverageDistance = inClusterAverageDistance;
	}

	public double getOutClusterMinAverageDistance() {
		return outClusterMinAverageDistance;
	}

	public void setOutClusterMinAverageDistance(double outClusterMinAverageDistance) {
		this.outClusterMinAverageDistance = outClusterMinAverageDistance;
	}

	



}
