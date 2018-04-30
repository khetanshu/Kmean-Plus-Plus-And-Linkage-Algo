package com.khetanshu.machinelearning.clustering.algos;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.khetanshu.corelib.DisjointSets;
import com.khetanshu.machinelearning.clustering.vo.EdgeVo;
import com.khetanshu.machinelearning.clustering.vo.Point;


/*
 * Data structures: 
 * 1. Disjoint Sets
 * 2. Priority Queue
 * 3. Hash Map
 * 4. Hash Set
 * 5. Linked List
 * 6. Graphs
 */
public class SingleLinkage {
	private PriorityQueue<EdgeVo> edges;
	private List<Integer> vertices;
	private DisjointSets disjointSets;
	private int requiredClusters;
	private Map<Integer, Point> points;
	
	public SingleLinkage(Map<Integer, Point> points, int requiredClusters) {
		this.points=points;
		setEdges(new PriorityQueue<EdgeVo>(10, new Comparator<EdgeVo>() {
			@Override
			public int compare(EdgeVo e1, EdgeVo e2) {
				if((e1.getDistance()-e2.getDistance())<0)
					return -1;
				return 1;
			}
		}));
		vertices = new LinkedList<>();
		for (Integer vertex : points.keySet()) {
			vertices.add(vertex);
		}
		createEdges();
		setDisjointSets(new DisjointSets());
		setRequiredClusters(requiredClusters);
	}

	private EdgeVo getMinDistanceEdge() {
		return edges.poll();
	}
	
	private void createEdges() {
		for (int i = 0; i < vertices.size(); i++) {
			int vertexA = vertices.get(i);
			for (int j = i+1; j < vertices.size(); j++) {
				int vertexB = vertices.get(j);
				Map<Integer, Double> dimensions_A=points.get(vertexA).getDimensions();
				Map<Integer, Double> dimensions_B=points.get(vertexB).getDimensions();
				edges.add(new EdgeVo(Point.calculateSquareDistance(dimensions_A, dimensions_B), vertexA, vertexB));
			}
		}
	}
	
	public void findClusters() {
		Set<Integer> clusterRoots= new HashSet<>();
		/*Make all vertices as a cluster*/
		for ( Integer vertex : vertices) {
			if(disjointSets.makeSet(vertex)) {
				clusterRoots.add(vertex);
			}	
		}
		/*Now until we get the required # of clusters we would keep merging clusters with least distance*/
		while (clusterRoots.size() > requiredClusters) {
			/*Get pair of vertices having least distances and remove it from the priority queue*/
			EdgeVo edge = getMinDistanceEdge();
			if(edge==null)
			{
				System.out.println("TODO");
			}
			if(!(clusterRoots.contains(edge.getVertexA())&& clusterRoots.contains(edge.getVertexB())))
				continue;
			/*Merge this edge*/
			System.out.println("Merging = "+ edge);
//			Integer childVertex = disjointSets.union(edge.getVertexA(), edge.getVertexB());
//			if(childVertex==-1) {
//				if(disjointSets.getParent(edge.getVertexA())!=edge.getVertexA())
//				{	clusterRoots.remove(edge.getVertexA());}
//				if(disjointSets.getParent(edge.getVertexB())!=edge.getVertexB())
//				{	clusterRoots.remove(edge.getVertexB());}
//			}else {
//				clusterRoots.remove(childVertex);
//			}
		}
		System.out.println("clusterRoots:"+clusterRoots);
		//System.out.println();
		//disjointSets.displayStatus();
		System.out.println();
		printClusters();
	}
	
	
	
	private void printClusters() {
		for (Integer pointId : points.keySet()) {
			Point point = points.get(pointId);
			point.setAssignedCluster(disjointSets.findByPathCompression(pointId));
			System.out.println("Point#"+ pointId +"{"+point+"}");
		}
	}

	
	/*Getters and Setters*/
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
	
//	public static void main(String[] args) {
//		PriorityQueue<EdgeVo> sortedEdges = new PriorityQueue<EdgeVo>(2, new EdgeComparator());
//		sortedEdges.add(new EdgeVo(2.0,-100,3));
//		sortedEdges.add(new EdgeVo(1.0,1,2));
//		sortedEdges.add(new EdgeVo(3.0,1,4));
//		sortedEdges.add(new EdgeVo(2.0,2,3));
//		sortedEdges.add(new EdgeVo(2.0,2,4));
//		sortedEdges.add(new EdgeVo(1.0,4,4));
//		for (int i = 0; i < 6; i++) {
//			System.out.println(sortedEdges.poll());
//		}
//	}

	
	
	
}
