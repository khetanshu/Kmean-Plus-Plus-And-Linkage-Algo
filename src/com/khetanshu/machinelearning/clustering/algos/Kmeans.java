package com.khetanshu.machinelearning.clustering.algos;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.khetanshu.machinelearning.clustering.main.ClusteringAlgorithms;
import com.khetanshu.machinelearning.clustering.util.CONSTANTS;
import com.khetanshu.machinelearning.clustering.vo.ClusterVo;
import com.khetanshu.machinelearning.clustering.vo.Point;

/**
 * This class would find the clusters using two different approaches based on the parameter passed in its constructors
 * through "linkageType" {LLYOD, KMEAN++}
 *
 * DATA STRUCTURE USED:
 * 1. Hash-Map (Multi-level)
 * 2. Linked List
 * 
 * ASYMPTOTIC COMPLEXITY(Worst case):
 * The the complexities of the algorithm are
 * Lloyd’s							: 𝑂(𝑛 ∗𝑘 ∗ 𝑑)
 * Kmean++							: 𝑂(𝑛 ∗𝑘 ∗ 𝑑 + lg⁡(𝑘))
 * 
 * 
 * where,
 * n : # of point ,  k : number of clusters,  d : number of attributes
 */
public class Kmeans {
	private Map<Integer,Point>points;
	private Map<Integer,Map<Integer,Double>> clusters; /**<Cluster_Id,Dimensions<Dimension_Id, Value>>*/

	/**	@param( 	pointsMap 	= Map:<#name{1,2,3,....n}, Point:{Assigned Cluster, Map:<dimension_Id, value>}>,
	 * 		  	k			= # of clusters to be found)
	 *  Initially "Assigned Cluster" would default to -1;
	 */
	public Kmeans(Map<Integer,Point>points, int totalClusters,int type) {
		clusters = new HashMap<>();
		//Copying and counting the points
		this.points = new HashMap<>();
		for (Integer key : points.keySet()) {
			Point point = new Point(points.get(key).getDimensions(), -1);
			//			Point point = points.get(key);
			point.setAssignedCluster(-1);
			this.points.put(key, point);
		}

		switch(type) {
		case CONSTANTS.LLOYDS:
			Random random = new Random();
			/**Random selection of initial centroids*/
			for (int clusterId = 1; clusterId <= totalClusters; clusterId++) {
				int randomInt = random.nextInt(points.size());
				Point point =this.points.get(randomInt);
				//point.setAssignedCluster(clusterId);
				Map<Integer,Double>newClusterDimensions= new HashMap<>();
				newClusterDimensions.putAll(point.getDimensions());
				clusters.put(clusterId, newClusterDimensions);
			}
			break;

		case CONSTANTS.KMEANS_PLUS_PLUS:
			findInitialClusters(totalClusters);
			break;

		}
		//System.out.println("Initial Clusters:"+clusters);
	}

	public Map<Integer, Point> findClusters() {
		while(reformClusters()) {
			//			printCentroids();
			recalculateTheCentroids();
			//			printClusters();
		}
		if(ClusteringAlgorithms.PRINT_CLUSTER_POINTS) {
			System.out.println("Centroids:");
			printCentroids();
			printClusters();
		}
		if(ClusteringAlgorithms.PRINT_SILHOUETTE_SCORE)
			printSilhouetteScores();
		return points;
	}

	private void findInitialClusters(int totalClusters) {
		/**Choose the FIRST cluster(centroid point) randomly among the points*/
		Random random = new Random();
		int randomInt = random.nextInt(points.size());
		Point point =this.points.get(randomInt);
		Map<Integer,Double>newClusterDimensions= new HashMap<>();
		newClusterDimensions.putAll(point.getDimensions());
		clusters.put(1, newClusterDimensions);

		for(int i=2;i<=totalClusters;i++) {
			Double maxDistanceFromCentroid = Double.MIN_VALUE;
			Point newClusterPoint=null;
			/**For each point find the distances with the clusters and select the one having maximum distance*/ 
			for(int j=0;j<points.size();j++) {
				if(j!=randomInt) {
					point =this.points.get(j);
					/**For each cluster finding  the distance from this point and would select the cluster with minimum distance*/
					Double minDistanceFromCentroid=Double.MAX_VALUE;
					Point pointWithMinDistanceFromCentroid=null;
					for(Integer clusterId : clusters.keySet()) {
						Map<Integer,Double>dimensions_A = clusters.get(clusterId);
						Map<Integer,Double>dimensions_B = point.getDimensions();
						/**calculate square distance from point to the cluster's centroid*/
						Double squareDistance= calculateSquareDistance(dimensions_A, dimensions_B);
						if(squareDistance<minDistanceFromCentroid) {
							minDistanceFromCentroid = squareDistance;
							pointWithMinDistanceFromCentroid=point;
						}
					}
					if(minDistanceFromCentroid>maxDistanceFromCentroid) {
						maxDistanceFromCentroid=minDistanceFromCentroid;
						newClusterPoint=pointWithMinDistanceFromCentroid;
					}
				}
			}
			/**Selected point having maximum distance from its closets cluster*/
			newClusterDimensions= new HashMap<>();
			newClusterDimensions.putAll(newClusterPoint.getDimensions());
			clusters.put(i, newClusterDimensions);

		}
	}

	Double calculateSquareDistance(Map<Integer,Double>dimensions_A, Map<Integer,Double>dimensions_B) {
		Double squareDistance=new Double(0);
		for (Integer dimensionId : dimensions_A.keySet()) {
			Double pointValue_A= dimensions_A.get(dimensionId);
			Double pointValue_B = dimensions_B.get(dimensionId);
			squareDistance += Math.pow((pointValue_A-pointValue_B),2);
		}
		return squareDistance;
	}


	private void printClusters() {
		for (Integer pointId : points.keySet()) {
			System.out.println("Point#"+ pointId +"{"+points.get(pointId)+"}");
		}
	}

	private void printCentroids() {
		System.out.println();
		System.out.println("Centroid="+clusters);
	}



	public double calculateAggregateClusteringCost() {
		double cost=0;
		for (Integer pointKey : points.keySet()) {
			Point point = points.get(pointKey);
			cost+= Point.calculateSquareDistance(point.getDimensions(), clusters.get(point.getAssignedCluster()));
		}
		cost/=points.size();
		return cost;
	}

	public void printSilhouetteScores(){
		if(ClusteringAlgorithms.DEBUG_MODE) {
			System.out.println("\n**Silhouette Score**");
		}
		/**Create cluster to points map*/
		Map<Integer, List<Integer>> clustersToPoints= new HashMap<>();
		for (Integer pointKey : points.keySet()) {
			Point point = points.get(pointKey);
			List<Integer> pointsList;
			if(clustersToPoints.containsKey(point.getAssignedCluster())) {
				pointsList=clustersToPoints.get(point.getAssignedCluster());
				pointsList.add(pointKey);
			}else {
				pointsList= new LinkedList<>();
				pointsList.add(pointKey);
				clustersToPoints.put(point.getAssignedCluster(),pointsList);
			}
		}

		//Map<Integer, ClusterVo> silhouetteDistanceMap = new HashMap<>();
		double averageSilhouetteScore=0.0;
		/**Initialize silhouetteDistanceMap, and calculate in-cluster average distance*/
		for (Integer clusterKey : clusters.keySet()) {
			ClusterVo clusterInfo = new ClusterVo();
			double inClusterDistance =0.0;
			List<Integer> pointsList = clustersToPoints.get(clusterKey);
			for (Integer pointKey : pointsList) {
				inClusterDistance +=points.get(pointKey).getSquareDistanceFromCentroid();
				/**calculate the out-cluster distance from this point to the points of the other clusters*/
				for (Integer outClusterKey : clustersToPoints.keySet()) {
					if(clusterKey!=outClusterKey) {
						List<Integer> outClusterPointsList = clustersToPoints.get(outClusterKey);
						double outClusterDistance =0.0;
						for (Integer outClusterPointKey : outClusterPointsList) {
							outClusterDistance+=Point.calculateSquareDistance(points.get(pointKey).getDimensions(), points.get(outClusterPointKey).getDimensions());
						}
						outClusterDistance/=outClusterPointsList.size();
						if(outClusterDistance<clusterInfo.getOutClusterMinAverageDistance()) {
							clusterInfo.setOutClusterMinAverageDistance(outClusterDistance);
						}
					}
				}
			}
			clusterInfo.setInClusterAverageDistance(inClusterDistance/pointsList.size());
			double silhouetteScore = (clusterInfo.getOutClusterMinAverageDistance()-clusterInfo.getInClusterAverageDistance())/
					Double.max(clusterInfo.getOutClusterMinAverageDistance(),clusterInfo.getInClusterAverageDistance());
			if(ClusteringAlgorithms.DEBUG_MODE) {
				System.out.printf("Cluster[%2d] - Silhouette Score=[%.3f]\n",clusterKey,silhouetteScore);
			}
			averageSilhouetteScore +=silhouetteScore;
			//silhouetteDistanceMap.put(clusterKey,clusterInfo);
		}
		System.out.printf("Average Silhouette Score=[%.3f]\n",averageSilhouetteScore/clusters.size());
		//		System.out.println(averageSilhouetteScore/clusters.size());

	}


	private boolean reformClusters() {
		boolean changeFound =false;
		for (Integer pointID : points.keySet()) {
			Point point = points.get(pointID);
			if(assignNearestCluster(point)) {
				if(changeFound==false) {
					changeFound=true;
				}
			}
		}
		return changeFound;
	}

	/** @param  a point
	 * The function would calculate the distance of the point to each clusters and assign it to the one closer to it*/
	private boolean assignNearestCluster(Point point) {
		boolean changeFound =false;
		Integer newClusterId = null;
		Double minSquareDistance= Double.MAX_VALUE;
		for (Integer clusterId : clusters.keySet()) {
			Map<Integer,Double>dimensions = clusters.get(clusterId);
			Double squareDistance=new Double(0);
			for (Integer dimensionId : dimensions.keySet()) {
				Double centroidValue = dimensions.get(dimensionId);
				Double pointValue = point.getDimensions().get(dimensionId);
				squareDistance += Math.pow((centroidValue-pointValue),2);
			}

			//if(squareDistance<point.getSquareDistanceFromCentroid()) {
			if(squareDistance<minSquareDistance) {
				//point.setSquareDistanceFromCentroid(squareDistance);
				minSquareDistance=squareDistance;
				newClusterId=clusterId;
			}
		}
		if(minSquareDistance!=point.getSquareDistanceFromCentroid()) {
			point.setSquareDistanceFromCentroid(minSquareDistance);
			point.setAssignedCluster(newClusterId);
			changeFound=true;
		}
		return changeFound;
	}

	private void recalculateTheCentroids(){
		Map<Integer,Integer> clustersSize= new HashMap<>();
		/*Initializing*/
		for (Integer clusterId : clusters.keySet()) {
			clustersSize.put(clusterId, 0);
			clusters.get(clusterId).clear();
		}
		for(Integer pointId: points.keySet()) {
			Point point = points.get(pointId);
			Integer clusterId =point.getAssignedCluster();
			Map<Integer, Double> pointDimensions = point.getDimensions();
			Map<Integer, Double> centroidDimensions = clusters.get(clusterId);
			/*Increment the cluster count*/
			clustersSize.put(clusterId, clustersSize.get(clusterId)+1);
			for (Integer dimensionID : pointDimensions.keySet()) {
				if(centroidDimensions.containsKey(dimensionID)) {
					centroidDimensions.put(dimensionID,centroidDimensions.get(dimensionID)+pointDimensions.get(dimensionID));
				}else {
					centroidDimensions.put(dimensionID,pointDimensions.get(dimensionID));
				}
			}
		}
		/*For each cluster calculate the average*/
		for (Integer clusterId : clusters.keySet()) {
			Map<Integer, Double> centroidDimensions = clusters.get(clusterId);
			for (Integer dimensionId : centroidDimensions.keySet()) {
				centroidDimensions.put(dimensionId, centroidDimensions.get(dimensionId)/clustersSize.get(clusterId));
			}
		}
	}
	/*Encapsulation Procedures*/

}
