package com.khetanshu.machinelearning.clustering.algos;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Vector;

import com.khetanshu.corelib.DisjointSets;
import com.khetanshu.machinelearning.clustering.main.ClusteringAlgorithms;
import com.khetanshu.machinelearning.clustering.util.CONSTANTS;
import com.khetanshu.machinelearning.clustering.vo.ClusterVo;
import com.khetanshu.machinelearning.clustering.vo.EdgeVo;
import com.khetanshu.machinelearning.clustering.vo.Point;

/**
 * This class would find the clusters using three different approaches based on the parameter passed in its constructors
 * through "linkageType" {SINGLE, COMPLETE or AVERAGE}
 * 
 * DATA STRUCTURE USED:
 * 1. Disjoint Sets
 * 2. Priority Queue
 * 3. Hash-Map
 * 4. Hash-Set
 * 5. Stack
 * 6. Linked List
 * 
 * ASYMPTOTIC COMPLEXITY(Worst case):
 * The the complexities of the algorithm are highly optimized 
 * Single Linkage 			: O(m+n) 
 * Complete/Average Linkage 	: O(m.lg(n))
 * > whereas a naive approach using the 2D matrix would lead to O(n^3) [for all three]
 * 
 * where 	n = # of points (or the vertices) 
 * and  		m = # of edges between vertices
 */
@SuppressWarnings("unused")
public class Linkage {
	private PriorityQueue<EdgeVo> edges;
	private Stack<EdgeVo> singleLinkageEdgesStack;
	private List<Integer> vertices;
	private DisjointSets disjointSets;
	private int requiredClusters;
	private Map<Integer, Point> points;
	private int linkageType;

	/** Constructor : 
	 *  This would create the object and initialize the data members with default values, like 
	 *  - Priority queue would be set for ascending order or the edges weight or the distance with the vertices of the edge
	 *  - "vertices" map would be added with all the vertices available for the graph which is the points itself.
	 **/
	public Linkage(Map<Integer, Point> points, int requiredClusters, int linkageType) {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		this.points=points;
		setLinkageType(linkageType);
		if(linkageType==CONSTANTS.SINGLE_LINKAGE) {
			setSingleLinkageEdgesList(new Stack<EdgeVo>());
		}else {
			setEdges(new PriorityQueue<EdgeVo>(10, new Comparator<EdgeVo>() {
				@Override
				public int compare(EdgeVo e1, EdgeVo e2) {
					if(e1.getDistance()<e2.getDistance())
						return -1;
					return 1;
				}
			}));
		}
		vertices = new LinkedList<>();
		for (Integer vertex : points.keySet()) {
			vertices.add(vertex);
		}
		createEdges();
		setDisjointSets(new DisjointSets());
		setRequiredClusters(requiredClusters);
	}

	public Map<Integer, Point> findClusters() {
		/**Make all vertices as a cluster*/
		for ( Integer vertex : vertices) {
			disjointSets.makeSet(vertex);
		}
		/**if linkage type is SINGLE LINKAGE then we need to sort the edges here so that we can the minimum cost edge when we remove the top element
		 * this is not useful for other linkage algorithms like complete and average as there we need to update the edges after a selection has been made.
		 * 
		 * For SINGLE LINKAGE sorting initially would reduce the overall complexity of the algorithm and would lead to final worst case complexity of O(m+n) 
		 * where m = # of edges
		 * and   n = # of vertices
		 **/
		
		if(linkageType==CONSTANTS.SINGLE_LINKAGE) {
			getSingleLinkageEdgesStack().sort(new Comparator<EdgeVo>() {
				@Override
				public int compare(EdgeVo e1, EdgeVo e2) {
					if(e1.getDistance()<e2.getDistance())
						return 1;
					return -1;
				}
			});
		}
		/**Now until we get the required # of clusters we would keep merging clusters with least distance*/
		while (disjointSets.getSize() > requiredClusters) {
			/**Get pair of vertices having least distances and remove it from
			 * -  Stack 					- in case of SINGLE LINKAGE 					(with O(1))
			 * -  Priority Queue 		- in case of COMPLETE AND AVERAGE LINKAGE 	(with lg(n))
			 **/
			EdgeVo edge = getMinDistanceEdge();
			if(edge==null) {
				System.out.println("@Exception found : No more edges.. terminating the program! check the data");
				System.exit(0);
			}
			if(ClusteringAlgorithms.DEBUG_MODE){
				System.out.printf("\n%s %8s %8s","* Merging Edge"+edge ," > P(A):"+getParent(edge.getVertexA()),"P(B):"+getParent(edge.getVertexB()));
			}
			/**Merge this edge*/
			/*System.out.println("Merging = "+ edge);*/
			disjointSets.union(edge.getVertexA(), edge.getVertexB());

			/**Edges would be updated iff the linkage type is COMPLETE or AVERAGE - Not required for SINGLE Linkage are priority queue would
			 * handle it automatically*/
			if(getLinkageType()!=CONSTANTS.SINGLE_LINKAGE) {
				updateEdges(edge);
			}
		}
		assignClusterToPoints();
		/*System.out.println();
		  disjointSets.displayStatus();
		  System.out.println();*/
		if(ClusteringAlgorithms.PRINT_CLUSTER_POINTS)
			printCluster();
		if(ClusteringAlgorithms.PRINT_SILHOUETTE_SCORE)
		printSilhouetteScores();
		return points;
	}

	private void createEdges() {
		Integer vertexA,vertexB;
		Map<Integer, Double> dimensions_A,dimensions_B;
		int i, j;
		for (i = 0; i < vertices.size(); i++) {
			 vertexA = vertices.get(i);
			for (j = i+1; j < vertices.size(); j++) {
				 vertexB = vertices.get(j);
				dimensions_A=points.get(vertexA).getDimensions();
				dimensions_B=points.get(vertexB).getDimensions();
				if(linkageType==CONSTANTS.SINGLE_LINKAGE) {
					singleLinkageEdgesStack.add(new EdgeVo(Point.calculateSquareDistance(dimensions_A, dimensions_B), vertexA, vertexB));
				}else {
					edges.add(new EdgeVo(Point.calculateSquareDistance(dimensions_A, dimensions_B), vertexA, vertexB));
				}
			}
		}
	}
	

	private EdgeVo getMinDistanceEdge() {
		if(linkageType==CONSTANTS.SINGLE_LINKAGE) {
			if(!singleLinkageEdgesStack.isEmpty())
				return singleLinkageEdgesStack.pop();
		}else {
			return edges.poll();
		}
		return null;
	}

	private boolean setContains(EdgeVo mergedEdge, EdgeVo edge) {
		/** if the set with the mergedEdges contains any vertices of Edge when returning true else false*/
		if(disjointSets.findByPathCompression(mergedEdge.getVertexA())==disjointSets.findByPathCompression(edge.getVertexA())||
				disjointSets.findByPathCompression(mergedEdge.getVertexA())==disjointSets.findByPathCompression(edge.getVertexB()))
			return true;

		return false;
	}

	private Integer getParent(Integer vertex){
		return disjointSets.findByPathCompression(vertex);
	}

	private void updateEdges(EdgeVo mergedEdge) {
		Map<Integer,EdgeVo> foundEdgesMap  = new HashMap<>();
		Map<Integer,Integer> foundEdgesCountMap  = new HashMap<>();
		List<EdgeVo> irrelevantEdges = new LinkedList<>();
		/**Find and remove all the edges except the one's not having A or B*/
		for (Iterator<EdgeVo> iterator = edges.iterator(); iterator.hasNext();) {
			EdgeVo edge = (EdgeVo) iterator.next();
			if(ClusteringAlgorithms.DEBUG_MODE){
				//System.out.println("Removed Edge"+edge);
			}
			if(setContains(mergedEdge,edge)/*edge.containsVertexOf(mergedEdge)*/) {
				/**Store only those edges in map which aren't comprising both the vertices of mergedEdges*/
				//				if(edge.getVertexA()!=mergedEdge.getVertexA() && edge.getVertexA()!=mergedEdge.getVertexB()) {
				int parentOfMergedSet=getParent(mergedEdge.getVertexA());
				if(getParent(edge.getVertexA())!=parentOfMergedSet) {
					edge.setVertexA(getParent(edge.getVertexA()));
					edge.setVertexB(parentOfMergedSet);
					if(foundEdgesMap.containsKey(edge.getVertexA()) ) {
						switch(getLinkageType()) {
						case CONSTANTS.COMPLETE_LINKAGE:
							/**Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
							if(foundEdgesMap.get(edge.getVertexA()).getDistance()<edge.getDistance()) {
								foundEdgesMap.get(edge.getVertexA()).setDistance(edge.getDistance());
								//								foundEdgesMap.put(edge.getVertexA(), edge);
							}
							break;
						case CONSTANTS.AVERAGE_LINKAGE:
							EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexA());
							/**add the distances so that in the end average would be done by diving with the total count*/
							//							foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());//OLD
							foundEdge.setDistance(foundEdge.getDistance() + edge.getDistance()*edge.getMergedEdgesCount());//new
							/**Increment the count of the edge*/
							//edge.setMergedEdgesCount(edge.getMergedEdgesCount()+1);//TODO NEW
							//							foundEdgesCountMap.put(edge.getVertexA(),foundEdgesCountMap.get(edge.getVertexA())+edge.getMergedEdgesCount());
							foundEdgesCountMap.put(edge.getVertexA(),foundEdge.getMergedEdgesCount()+edge.getMergedEdgesCount());
							//							foundEdgesCountMap.put(edge.getVertexA(),foundEdgesCountMap.get(edge.getVertexA())+1);
							break;
						}
					}else {
						if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
							//							foundEdgesCountMap.put(edge.getVertexA(), 1);
							edge.setDistance(edge.getDistance()*edge.getMergedEdgesCount());
							foundEdgesCountMap.put(edge.getVertexA(), edge.getMergedEdgesCount());
						}
						/**If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
						foundEdgesMap.put(edge.getVertexA(), edge);
					}
					//				}else if(edge.getVertexB()!=mergedEdge.getVertexA() && edge.getVertexB()!=mergedEdge.getVertexB()){
				}else if(getParent(edge.getVertexB())!=parentOfMergedSet){	
					edge.setVertexA(parentOfMergedSet);
					edge.setVertexB(getParent(edge.getVertexB()));
					if(foundEdgesMap.containsKey(edge.getVertexB()) ) {
						switch(getLinkageType()) {
						case CONSTANTS.COMPLETE_LINKAGE:
							/**Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
							if(foundEdgesMap.get(edge.getVertexB()).getDistance()<edge.getDistance()) {
								foundEdgesMap.get(edge.getVertexB()).setDistance(edge.getDistance());
								//								foundEdgesMap.put(edge.getVertexB(), edge);
							}
							break;
						case CONSTANTS.AVERAGE_LINKAGE:
							EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexB());
							/**add the distances so that in the end average would be done by diving with the total count*/
							//							foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());//OLD
							foundEdge.setDistance(foundEdge.getDistance() + edge.getDistance()*edge.getMergedEdgesCount());//new
							/**Increment the count of the edge*/
							//edge.setMergedEdgesCount(edge.getMergedEdgesCount()+1);//TODO NEW
							//foundEdgesCountMap.put(edge.getVertexB(),foundEdgesCountMap.get(edge.getVertexB())+edge.getMergedEdgesCount());
							foundEdgesCountMap.put(edge.getVertexB(),foundEdge.getMergedEdgesCount()+edge.getMergedEdgesCount());
							break;
						}
					}else {
						if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
							edge.setDistance(edge.getDistance()*edge.getMergedEdgesCount());
							foundEdgesCountMap.put(edge.getVertexB(), edge.getMergedEdgesCount());
							//							edge.setMergedEdgesCount(1);//TODO NEW
						}
						/**If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
						foundEdgesMap.put(edge.getVertexB(), edge);
					}
				}
				irrelevantEdges.add(edge);
			}
		} 
		edges.removeAll(irrelevantEdges);
		/**Now the foundMap contains the new edges which needs to be considered for the shortest distance selection
		 * hence inserting back into the priority queue
		 */
		for (Integer key : foundEdgesMap.keySet()) {
			EdgeVo edge = foundEdgesMap.get(key);
			if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
				/**finding the average*/
				edge.setDistance(edge.getDistance()/foundEdgesCountMap.get(key));//TODO TESTING TO BE UNCOMMENTED LATER
				edge.setMergedEdgesCount(foundEdgesCountMap.get(key));
				//				edge.setDistance(edge.getDistance()/edge.getMergedEdgesCount());
			}
			if(ClusteringAlgorithms.DEBUG_MODE){
				//System.out.println("New Edge = "+edge);
			}
			edges.add(edge);
		}
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
		/**Calculating the center of mass of each clusters*/
		Map<Integer,Map<Integer,Double>> clusters = new HashMap<>();
		for (Integer clusterKey : clustersToPoints.keySet()) {
			List<Integer> pointsList = clustersToPoints.get(clusterKey);
			Map<Integer, Double> centroidDimensions  = new HashMap<>();
			for (Integer pointKey : pointsList) {
				Map<Integer, Double> pointDimensions = points.get(pointKey).getDimensions();
				for (Integer dimensionId : pointDimensions.keySet()) {
					if(centroidDimensions.containsKey(dimensionId)) {
						centroidDimensions.put(dimensionId,(centroidDimensions.get(dimensionId)+pointDimensions.get(dimensionId)));
					}else {
						centroidDimensions.put(dimensionId,pointDimensions.get(dimensionId));
					}
				}
			}
			for (Integer dimensionId : centroidDimensions.keySet()) {
				centroidDimensions.put(dimensionId,(centroidDimensions.get(dimensionId)/pointsList.size()));
			}
			clusters.put(clusterKey, centroidDimensions);
		}
		
		//Map<Integer, ClusterVo> silhouetteDistanceMap = new HashMap<>();
		double averageSilhouetteScore=0.0;
		/**Initialize silhouetteDistanceMap, and calculate in-cluster average distance*/
		for (Integer clusterKey : clustersToPoints.keySet()) {
			ClusterVo clusterInfo = new ClusterVo();
			double inClusterDistance =0.0;
			List<Integer> pointsList = clustersToPoints.get(clusterKey);
			for (Integer pointKey : pointsList) {
				inClusterDistance +=Point.calculateSquareDistance(points.get(pointKey).getDimensions(), clusters.get(clusterKey));
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
		System.out.printf("Average Silhouette Score=[%.3f]\n",averageSilhouetteScore/clustersToPoints.size());
//		System.out.println(averageSilhouetteScore/clusters.size());

	}

	
	private void printCluster() {
		for (Integer pointId : points.keySet()) {
			Point point = points.get(pointId);
			System.out.println("Point#"+ pointId +"{"+point+"}");
		}
	}

	private void assignClusterToPoints() {
		for (Integer pointId : points.keySet()) {
			Point point = points.get(pointId);
			point.setAssignedCluster(disjointSets.findByPathCompression(pointId));
		}
	}

	/**Getters and Setters*/
	private PriorityQueue<EdgeVo> getEdges() {
		return edges;
	}

	private void setEdges(PriorityQueue<EdgeVo> edges) {
		this.edges = edges;
	}

	private List<Integer> getVertices() {
		return vertices;
	}

	private void setVertices(List<Integer> vertices) {
		this.vertices =null;
	}

	private DisjointSets getDisjointSets() {
		return disjointSets;
	}

	private void setDisjointSets(DisjointSets disjoinSets) {
		this.disjointSets = disjoinSets;
	}

	public int getRequiredClusters() {
		return requiredClusters;
	}

	public void setRequiredClusters(int requiredClusters) {
		this.requiredClusters = requiredClusters;
	}

	public int getLinkageType() {
		return linkageType;
	}

	public void setLinkageType(int linkageType) {
		this.linkageType = linkageType;
	}

	public Stack<EdgeVo> getSingleLinkageEdgesStack() {
		return singleLinkageEdgesStack;
	}

	public void setSingleLinkageEdgesList(Stack<EdgeVo> singleLinkageEdgesStack) {
		this.singleLinkageEdgesStack = singleLinkageEdgesStack;
	}

	//	private void updateEdges(EdgeVo mergedEdge) {
	//		Map<Integer,EdgeVo> foundEdgesMap  = new HashMap<>();
	//		Map<Integer,Integer> foundEdgesCountMap  = new HashMap<>();
	//		List<EdgeVo> irrelevantEdges = new LinkedList<>();
	//		/*Find and remove all the edges except the one's not having A or B*/
	//		for (Iterator<EdgeVo> iterator = edges.iterator(); iterator.hasNext();) {
	//			EdgeVo edge = (EdgeVo) iterator.next();
	//			if(setContains(mergedEdge,edge)/*edge.containsVertexOf(mergedEdge)*/) {
	//				/*Store only those edges in map which aren't comprising both the vertices of mergedEdges*/
	////				if(edge.getVertexA()!=mergedEdge.getVertexA() && edge.getVertexA()!=mergedEdge.getVertexB()) {
	//				
	//				int parentOfMergedSet=getParent(mergedEdge.getVertexA());
	//				if(getParent(edge.getVertexA())!=parentOfMergedSet) {
	//					int parentOfNewVertexA=getParent(edge.getVertexA());
	//					if(foundEdgesMap.containsKey(edge.getVertexA()) ) {
	//						switch(getLinkageType()) {
	//						case CONSTANTS.COMPLETE_LINKAGE:
	//							/*Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
	//							if(foundEdgesMap.get(edge.getVertexA()).getDistance()<edge.getDistance()) {
	//								foundEdgesMap.put(edge.getVertexA(), edge);
	//							}
	//							break;
	//						case CONSTANTS.AVERAGE_LINKAGE:
	//							EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexA());
	//							/*add the distances so that in the end average would be done by diving with the total count*/
	////							foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());//OLD
	//							foundEdge.setDistance(foundEdge.getDistance() + edge.getDistance()*edge.getMergedEdgesCount());//new
	//							/*Increment the count of the edge*/
	//							//edge.setMergedEdgesCount(edge.getMergedEdgesCount()+1);//TODO NEW
	////							foundEdgesCountMap.put(edge.getVertexA(),foundEdgesCountMap.get(edge.getVertexA())+edge.getMergedEdgesCount());
	//							foundEdgesCountMap.put(edge.getVertexA(),foundEdge.getMergedEdgesCount()+edge.getMergedEdgesCount());
	////							foundEdgesCountMap.put(edge.getVertexA(),foundEdgesCountMap.get(edge.getVertexA())+1);
	//							break;
	//						}
	//					}else {
	//						if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
	////							foundEdgesCountMap.put(edge.getVertexA(), 1);
	//							edge.setDistance(edge.getDistance()*edge.getMergedEdgesCount());
	//							foundEdgesCountMap.put(edge.getVertexA(), edge.getMergedEdgesCount());
	//						}
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexA(), edge);
	//					}
	////				}else if(edge.getVertexB()!=mergedEdge.getVertexA() && edge.getVertexB()!=mergedEdge.getVertexB()){
	//				}else if(getParent(edge.getVertexB())!=parentOfMergedSet){	
	//					if(foundEdgesMap.containsKey(edge.getVertexB()) ) {
	//						switch(getLinkageType()) {
	//						case CONSTANTS.COMPLETE_LINKAGE:
	//							/*Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
	//							if(foundEdgesMap.get(edge.getVertexB()).getDistance()<edge.getDistance()) {
	//								foundEdgesMap.put(edge.getVertexB(), edge);
	//							}
	//							break;
	//						case CONSTANTS.AVERAGE_LINKAGE:
	//							EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexB());
	//							/*add the distances so that in the end average would be done by diving with the total count*/
	////							foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());//OLD
	//							foundEdge.setDistance(foundEdge.getDistance() + edge.getDistance()*edge.getMergedEdgesCount());//new
	//							/*Increment the count of the edge*/
	//							//edge.setMergedEdgesCount(edge.getMergedEdgesCount()+1);//TODO NEW
	//							//foundEdgesCountMap.put(edge.getVertexB(),foundEdgesCountMap.get(edge.getVertexB())+edge.getMergedEdgesCount());
	//							foundEdgesCountMap.put(edge.getVertexB(),foundEdge.getMergedEdgesCount()+edge.getMergedEdgesCount());
	//							break;
	//						}
	//					}else {
	//						if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
	//							edge.setDistance(edge.getDistance()*edge.getMergedEdgesCount());
	//							foundEdgesCountMap.put(edge.getVertexB(), edge.getMergedEdgesCount());
	////							edge.setMergedEdgesCount(1);//TODO NEW
	//						}
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexB(), edge);
	//					}
	//				}
	//				irrelevantEdges.add(edge);
	//			}
	//		} 
	//		edges.removeAll(irrelevantEdges);
	//		/*Now the foundMap contains the new edges which needs to be considered for the shortest distance selection
	//		 * hence inserting back into the priority queue
	//		 */
	//		for (Integer key : foundEdgesMap.keySet()) {
	//			EdgeVo edge = foundEdgesMap.get(key);
	//			if(getLinkageType()==CONSTANTS.AVERAGE_LINKAGE) {
	//				/*finding the average*/
	//				edge.setDistance(edge.getDistance()/foundEdgesCountMap.get(key));//TODO TESTING TO BE UNCOMMENTED LATER
	//				edge.setMergedEdgesCount(foundEdgesCountMap.get(key));
	////				edge.setDistance(edge.getDistance()/edge.getMergedEdgesCount());
	//			}
	//			edges.add(edge);
	//		}
	//	}

	//	private void updateEdgesForCompleteLinkage(EdgeVo mergedEdge) {
	//		Map<Integer,EdgeVo> foundEdgesMap  = new HashMap<>();
	//		List<EdgeVo> irrelevantEdges = new LinkedList<>();
	//		/*Find and remove all the edges except the one's not having A or B*/
	//		for (Iterator<EdgeVo> iterator = edges.iterator(); iterator.hasNext();) {
	//			EdgeVo edge = (EdgeVo) iterator.next();
	//			if(edge.containsVertexOf(mergedEdge)) {
	//				/*Store only those edges in map which aren't comprising both the vertices of mergedEdges*/
	//				if(edge.getVertexA()!=mergedEdge.getVertexA() && edge.getVertexA()!=mergedEdge.getVertexB()) {
	//					if(foundEdgesMap.containsKey(edge.getVertexA()) ) {
	//						/*Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
	//						if(foundEdgesMap.get(edge.getVertexA()).getDistance()<edge.getDistance()) {
	//							foundEdgesMap.put(edge.getVertexA(), edge);
	//						}
	//					}else {
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexA(), edge);
	//					}
	//				}else if(edge.getVertexB()!=mergedEdge.getVertexA() && edge.getVertexB()!=mergedEdge.getVertexB()){
	//					if(foundEdgesMap.containsKey(edge.getVertexB()) ) {
	//						/*Save the edge iff the distance of the new edge is more i.e. COMPLETE/MAXIMUM distance approach*/
	//						if(foundEdgesMap.get(edge.getVertexB()).getDistance()<edge.getDistance()) {
	//							foundEdgesMap.put(edge.getVertexB(), edge);
	//						}
	//					}else {
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexB(), edge);
	//					}
	//				}
	//				irrelevantEdges.add(edge);
	//			}
	//		} 
	//		edges.removeAll(irrelevantEdges);
	//		/*Now the foundMap contains the new edges which needs to be considered for the shortest distance selection
	//		 * hence inserting back into the priority queue
	//		 */
	//		for (Integer key : foundEdgesMap.keySet()) {
	//			edges.add(foundEdgesMap.get(key));
	//		}
	//	}
	//
	//	private void updateEdgesForAverageLinkage(EdgeVo mergedEdge) {
	//		Map<Integer,EdgeVo> foundEdgesMap  = new HashMap<>();
	//		Map<Integer,Integer> foundEdgesCountMap  = new HashMap<>();
	//		List<EdgeVo> irrelevantEdges = new LinkedList<>();
	//		
	//		/*Find and remove all the edges except the one's not having A or B*/
	//		for (Iterator<EdgeVo> iterator = edges.iterator(); iterator.hasNext();) {
	//			EdgeVo edge = (EdgeVo) iterator.next();
	//			if(edge.containsVertexOf(mergedEdge)) {
	//				/*Store only those edges in map which aren't comprising both the vertices of mergedEdges*/
	//				if(edge.getVertexA()!=mergedEdge.getVertexA() && edge.getVertexA()!=mergedEdge.getVertexB()) {
	//					if(foundEdgesMap.containsKey(edge.getVertexA()) ) {
	//						EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexA());
	//						/*add the distances so that in the end average would be done by diving with the total count*/
	//						foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());
	//						/*Increment the count of the edge*/
	//						foundEdgesCountMap.put(edge.getVertexA(),foundEdgesCountMap.get(edge.getVertexA())+1);
	//					}else {
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexA(), edge);
	//						foundEdgesCountMap.put(edge.getVertexA(), 1);
	//					}
	//				}else if(edge.getVertexB()!=mergedEdge.getVertexA() && edge.getVertexB()!=mergedEdge.getVertexB()){
	//					if(foundEdgesMap.containsKey(edge.getVertexB()) ) {
	//						EdgeVo foundEdge = foundEdgesMap.get(edge.getVertexB());
	//						/*add the distances so that in the end average would be done by diving with the total count*/
	//						foundEdge.setDistance(foundEdge.getDistance()+ edge.getDistance());
	//						/*Increment the count of the edge*/
	//						foundEdgesCountMap.put(edge.getVertexB(),foundEdgesCountMap.get(edge.getVertexB())+1);
	//					}else {
	//						/*If the map doesn't contains the new vertex which isn't in the cluster yet then save it*/
	//						foundEdgesMap.put(edge.getVertexB(), edge);
	//						foundEdgesCountMap.put(edge.getVertexB(), 1);
	//					}
	//				}
	//				irrelevantEdges.add(edge);
	//			}
	//		} 
	//		edges.removeAll(irrelevantEdges);
	//		/*Now the foundMap contains the new edges which needs to be considered for the shortest distance selection
	//		 * hence inserting back into the priority queue
	//		 */
	//		for (Integer key : foundEdgesMap.keySet()) {
	//			EdgeVo edge = foundEdgesMap.get(key);
	//			/*finding the average*/
	//			edge.setDistance(edge.getDistance()/foundEdgesCountMap.get(key));
	//			edges.add(edge);
	//		}
	//	}

}
