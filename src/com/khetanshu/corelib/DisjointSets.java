package com.khetanshu.corelib;

import java.util.HashMap;
import java.util.Map;

public class DisjointSets {
	private Map<Integer,Integer> ranks;
	private Map<Integer,Integer> parents;
	private Integer totalDisjointSets;
	
	public DisjointSets() {
		ranks= new HashMap<>();
		parents= new HashMap<>();
		totalDisjointSets=0;
	}
	
	public void displayStatus() {
		System.out.println("Parents: "+parents);
		System.out.println("Ranks: "+ranks);
	}
	
	public Integer getParent(Integer node) {
		return parents.get(node);
	}
	
	public int getSize() {
		return totalDisjointSets;
	}
	
	public Boolean makeSet(Integer uniqueNodeId) {
		parents.put(uniqueNodeId, uniqueNodeId);
		ranks.put(uniqueNodeId, 0);
		totalDisjointSets++;
		return true;
	}
	
	public Integer findByPathCompression(Integer uniqueNodeId) {
		if(parents.get(uniqueNodeId)!=uniqueNodeId) {
			Integer newParent = findByPathCompression(parents.get(uniqueNodeId));
			parents.put(uniqueNodeId, newParent);
			return newParent;
		}
		return uniqueNodeId;
	}
	
	/*
	 * @param two unique nodes' IDs 
	 * @return function would return the id of the set that is no more a parent 
	 * i.e. its has become a child of other node passed as an input
	 */
	public void union(Integer uniqueNodeId_A, Integer uniqueNodeId_B) {
		Integer root_A=findByPathCompression(uniqueNodeId_A);
		Integer root_B=findByPathCompression(uniqueNodeId_B);
		
		if(root_A==root_B) {
			return;
		}
		totalDisjointSets--;
		/*i.e. both nodes are in different sets hence can be merged*/
		if(ranks.get(root_A)>ranks.get(root_B)) {
			/*if the rank of A node is more than B then make A as parent of B*/
			parents.put(root_B, root_A);
		}else {
			/*else make B as parent of A but if the rank of both are same then increase the parents rank by 1*/
			parents.put(root_A, root_B);
			if(ranks.get(root_A)==ranks.get(root_B)) {
				ranks.put(root_B, ranks.get(root_B)+1);
			}
		}
		
	}

	
	public static void main(String[] args) {
		/*Testing disjoint sets*/
		DisjointSets set = new DisjointSets();
		System.out.println("Make set 1,2,3,4");
		set.makeSet(1);
		set.makeSet(2);
		set.makeSet(3);
		set.makeSet(4);
		System.out.println("##Size="+set.getSize());
		set.displayStatus();
		System.out.println("\nUnion(1, 2)");
		set.union(1, 2);
		set.displayStatus();
		System.out.println("\nUnion(3, 4)");
		set.union(3, 4);
		set.displayStatus();
		System.out.println("\nUnion(2, 3)");
		set.union(2, 3);
		set.displayStatus();
		System.out.println("\nUnion(1, 2)");
		set.union(1, 2);
		set.displayStatus();
		System.out.println("##Size="+set.getSize());
		System.out.println("\nMake set 5,6,7,8");
		set.makeSet(5);
		set.makeSet(6);
		set.makeSet(7);
		set.makeSet(8);
		System.out.println("##Size="+set.getSize());
		set.displayStatus();
		System.out.println("\nUnion(5, 6), Union(7, 8)");
		set.union(5, 6);
		set.union(7, 8);
		set.displayStatus();
		System.out.println("##Size="+set.getSize());
		System.out.println("\nUnion(5, 8)");
		set.union(5,8);
		set.displayStatus();
		System.out.println("\nUnion(8, 3)");
		set.union(8, 3);
		set.displayStatus();
		System.out.println("##Size="+set.getSize());
		
		
	}

}
