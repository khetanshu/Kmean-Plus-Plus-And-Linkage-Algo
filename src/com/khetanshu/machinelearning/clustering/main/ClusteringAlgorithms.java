package com.khetanshu.machinelearning.clustering.main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.khetanshu.corelib.CSVReader;
import com.khetanshu.machinelearning.clustering.algos.Kmeans;
import com.khetanshu.machinelearning.clustering.algos.Linkage;
import com.khetanshu.machinelearning.clustering.util.CONSTANTS;
import com.khetanshu.machinelearning.clustering.util.Initializer;
import com.khetanshu.machinelearning.clustering.vo.Point;
import com.khetanshu.machinelearning.clustering.vo.RawData;

/**
 * FUNCTIONALITIES IMPLEMENTED:
 * 1. Clustering algorithm 
 * 		1. LLYODS
 * 		2. KMEAN ++ 
 * 		3. SINGLE LINKAGE
 * 		4. COMPLETE LINKAGE
 * 		5. AVERAGE LINKAGE
 * 2. EVALUTION METRIC 
 * 		1. HAMMING DISTANCE
 * 		2. SILHOUETTE SCORE
 * 3. Elbow Method 
 *
 * DATA STRUCTURE USED:
 * 1. Disjoint Sets
 * 2. Priority Queue
 * 3. Hash-Map
 * 4. Hash-Set
 * 5. Stack
 * 6. Graph
 * 7. Linked List
 * 
 * ASYMPTOTIC COMPLEXITY(Worst case):
 * The the complexities of the algorithm are highly optimized 
 * Single Linkage 					: 𝑶(𝒎+𝒏)  				{Normal approach 𝑂(𝑛^3)}
 * Complete & Average Linkage 		: 𝑶(𝒎.𝒍𝒈⁡(𝒏))				{Normal approach 𝑂(𝑛^4.lg⁡(𝑛))} 
 * Lloyd’s							: 𝑂(𝑛 ∗𝑘 ∗ 𝑑)
 * Kmean++							: 𝑂(𝑛 ∗𝑘 ∗ 𝑑 + lg⁡(𝑘))
 * 
 * 
 * where,
 * n : # of point ,  k : number of clusters,  d : number of attributes
 * m : # of edges → 𝑶(𝒏(𝒏−𝟏)/𝟐) → 𝑶(𝒏^𝟐)  [This could be optimized further by pre-processing and parallelism]
 */
public class ClusteringAlgorithms {
	public static int NO_OF_CLUSTERS;
	public static String INPUT_RELATIVE_FILENAME;
	public static boolean DEBUG_MODE;
	public static boolean PRINT_CLUSTER_POINTS;
	public static boolean PRINT_SILHOUETTE_SCORE;

	public Map<Integer, Point> extractPoints(List<String[]> data, int headerIndex){
		Map<Integer, Point> points = new HashMap<>();
		int pointID=0;
		for (int i = headerIndex+1; i < data.size(); i++) {
			Map<Integer,Double> dimensions= new HashMap<>();
			for (int j = 0; j < data.get(headerIndex).length; j++) {
				dimensions.put(j, Double.valueOf(data.get(i)[j]));
			}
			points.put(pointID++, new Point(dimensions,-1));
		}
		return points;
	}

	public static void main(String[] args) {
		ClusteringAlgorithms obj= new ClusteringAlgorithms();
		Initializer.initialize("config/config.properties");
		List<String[]> data= CSVReader.readCSV(INPUT_RELATIVE_FILENAME);
		/*Ignore the first row as it would just contain the names of the attribute but in the second row we 
		 * would have its ID in integer*/
		RawData rawData = obj.extractPoints(data, CONSTANTS.FIRST_ROW,CONSTANTS.LAST_COLUMN);

		Map<Integer, Point> points = rawData.getPoints();
		Map<Integer, Point> pointsCopy;
		Map<Integer, Point> updatedPoints;

		/**if totalCluster(i.e. k) = -1 then we need to find the value of K using the elbow method*/
		if(NO_OF_CLUSTERS==-1) {
			NO_OF_CLUSTERS =obj.findNoOfClusterUsingElbowMethod(points);
		}else {

			System.out.printf("\n%10s%s%10s\n","########","LLYODS Output","########");
			updatedPoints= obj.findClustersUsing_LloydsAlgorithm(points, NO_OF_CLUSTERS,CONSTANTS.LLOYDS);
			System.out.printf("\n%s %.3f\n","~Hammming Distance = ", obj.getHammingDistance(rawData.getPointToClassMap(), updatedPoints));
			
			System.out.printf("\n%10s%s%10s\n","########","KMEAN++ Output","########");
			updatedPoints= obj.findClustersUsing_LloydsAlgorithm(points, NO_OF_CLUSTERS,CONSTANTS.KMEANS_PLUS_PLUS);
			System.out.printf("%s %.3f\n","~Hammming Distance = ", obj.getHammingDistance(rawData.getPointToClassMap(), updatedPoints));
			
			System.out.printf("\n%10s%s%10s\n","########","Single Linkage Output","########");
			updatedPoints= obj.findClustersUsing_LinkageAlgorithm(points, NO_OF_CLUSTERS, CONSTANTS.SINGLE_LINKAGE);
			System.out.printf("\n%s %.3f\n","~Hammming Distance = ",obj.getHammingDistance(rawData.getPointToClassMap(), updatedPoints));

			System.out.printf("\n%10s%s%10s\n","########","Complete Linkage Output","########");
			updatedPoints= obj.findClustersUsing_LinkageAlgorithm(points, NO_OF_CLUSTERS, CONSTANTS.COMPLETE_LINKAGE);
			System.out.printf("\n%s %.3f\n","~Hammming Distance = ", obj.getHammingDistance(rawData.getPointToClassMap(), updatedPoints));
			
			System.out.printf("\n%10s%s%10s\n","########","Average Linkage Output","########");
			updatedPoints= obj.findClustersUsing_LinkageAlgorithm(points, NO_OF_CLUSTERS, CONSTANTS.AVERAGE_LINKAGE);
			System.out.printf("\n%s %.3f\n","~Hammming Distance = ", obj.getHammingDistance(rawData.getPointToClassMap(), updatedPoints));

		}
	}

	public RawData extractPoints(List<String[]> data, int headerIndex, int predefinedClassIndex){
		RawData rawData = new RawData();
		Map<Integer, Point> points = rawData.getPoints();
		Map<Integer,Integer> pointToClassMap= rawData.getPointToClassMap();
		int pointID=0;
		if(predefinedClassIndex==CONSTANTS.LAST_COLUMN) {
			predefinedClassIndex=data.get(CONSTANTS.FIRST_ROW).length-1;
		}
		for (int i = headerIndex+1; i < data.size(); i++) {
			Map<Integer,Double> dimensions= new HashMap<>();
			for (int j = 0; j < data.get(headerIndex).length; j++) {
				if(j==predefinedClassIndex) {
					pointToClassMap.put(pointID, Integer.valueOf(data.get(i)[j]));
				}else {
					dimensions.put(j, Double.valueOf(data.get(i)[j]));
				}
			}
			points.put(pointID++, new Point(dimensions,-1));
		}
		return rawData;
	}

	public Double getHammingDistance(Map<Integer,Integer> pointToClassMap, Map<Integer, Point> points) {
		Double hammingDistance = new Double(0);
		/**
		 * "pointToClassMap" would contain the mapping of the points and cluster giving the raw data {Lets call its C}
		 * "points" would contain the new mapping between each points and its new assigned cluster to which it belongs {Lets call its C'}
		 */
		Long missedEdges = new Long(0);
		Long totalEdges=new Long(0);
		for (int pointA = 0; pointA < points.size(); pointA++) {
			for (int pointB = pointA+1; pointB < points.size(); pointB++) {
				/** find if i and j are in any cluster of C, if not found then find in C'*/
				if(pointToClassMap.get(pointA)==pointToClassMap.get(pointB)) {
					/** i.e. both point A and B are in same cluster in C
					 * 	Now check if its present in any cluster of C'; if not then increase the count*/
					if(points.get(pointA).getAssignedCluster()!=points.get(pointB).getAssignedCluster()) {
						missedEdges++;
					}
				}else if(points.get(pointA).getAssignedCluster()==points.get(pointB).getAssignedCluster()) {
					/** i.e. both point A and B are in same cluster in C'
					 * 	Now check if its present in any cluster of C; if not then increase the count*/
					if(pointToClassMap.get(pointA)!=pointToClassMap.get(pointB)) {
						missedEdges++;
					}
				}
				totalEdges++;
			}
		}
		hammingDistance =  ((double)missedEdges/(double)totalEdges);
		return hammingDistance;
	}

	public Map<Integer, Point> findClustersUsing_LloydsAlgorithm(Map<Integer, Point> points,int requiredClusters, int type) {
		Kmeans kmeans = new Kmeans(points, requiredClusters,type);
		return kmeans.findClusters();
	}


	public Map<Integer, Point> findClustersUsing_LinkageAlgorithm(Map<Integer, Point> points,int requiredClusters, int linkageType) {
		Linkage linkage= new Linkage(points, requiredClusters,linkageType);
		return linkage.findClusters();
	}

	private int findNoOfClusterUsingElbowMethod(Map<Integer, Point> points) {
		double aggregateCost=0.0;
		double previousCost = 0.0;
		for (int k = 1; k <= 20; k++) {
			Kmeans kmeans = new Kmeans(points, k,CONSTANTS.KMEANS_PLUS_PLUS);
			kmeans.findClusters();
			aggregateCost=kmeans.calculateAggregateClusteringCost();
			//			aggregateCost=Math.sqrt(Math.sqrt(aggregateCost));
			System.out.printf("K= %2d -> aggregate Kmean cost= %.3f, Decline= %.3f\n",k, aggregateCost, previousCost-aggregateCost);
			previousCost=aggregateCost;
			System.out.println(aggregateCost);
		}
		return 0;
	}





}
