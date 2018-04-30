package com.khetanshu.machinelearning.clustering.vo;

public class EdgeVo {
	private Double distance;
	private Integer vertexA;
	private Integer vertexB;
	private Integer mergedEdgesCount;
	
	public EdgeVo(Double distance,Integer vertexA,Integer vertexB) {
		setDistance(distance);
		setVertexA(vertexA);
		setVertexB(vertexB);
		setMergedEdgesCount(1);
	}
	
	public boolean containsVertexOf(EdgeVo edge) {
		if(this.getVertexA()==edge.getVertexA()||this.getVertexA()==edge.getVertexB()||
				this.getVertexB()==edge.getVertexA()||this.getVertexB()==edge.getVertexB())
			return true;
		return false;
	}
	
//	public static void main(String[] args) {
//		Stack<EdgeVo> stack = new Stack<EdgeVo>();
//		stack.push(new EdgeVo(5.0, 1, 2));
//		stack.push(new EdgeVo(5.0, 1, 2));
//		stack.push(new EdgeVo(2.0, 3, 1));
//		stack.push(new EdgeVo(4.0, 2, 1));
//		
//		stack.sort(new Comparator<EdgeVo>() {
//				@Override
//				public int compare(EdgeVo e1, EdgeVo e2) {
//					if(e1.getDistance()<e2.getDistance())
//						return 1;
//					return -1;
//				}
//			});
//		
//		System.out.println(stack.pop());
//		System.out.println(stack.pop());
//		System.out.println(stack.pop());
//		System.out.println(stack.pop());
//	}
	
//	@Override
//	public boolean equals(Object obj) {
//		/*This equal function is particularly made to the purpose of finding the edges that contains anyone of the vertex 
//		 * present in the Edge in consideration.
//		 * This is getting used in COMPLETE LINKAGE scenario where we are UPDATING THE EDGES*/
//		if(obj instanceof EdgeVo) {
//			EdgeVo edge = (EdgeVo) obj;
//			
//			if(vertexA==edge.vertexA || vertexA==edge.vertexB || vertexB==edge.vertexA|| vertexB== edge.vertexB) {
//				return true;
//			}else {
//				return false;
//			}
//		}
//		return false;
//		
//	}



	@Override
	public String toString() {
		return String.format(" %10s  %6.3f:Dist %3d:EdgeCount", "<" + vertexA + "," + vertexB + ">" ,Double.valueOf(distance),getMergedEdgesCount());
	}



	public Integer getVertexA() {
		return vertexA;
	}

	public void setVertexA(Integer vertexA) {
		this.vertexA = vertexA;
	}

	public Integer getVertexB() {
		return vertexB;
	}

	public void setVertexB(Integer vertexB) {
		this.vertexB = vertexB;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Integer getMergedEdgesCount() {
		return mergedEdgesCount;
	}

	public void setMergedEdgesCount(Integer mergedEdgesCount) {
		this.mergedEdgesCount = mergedEdgesCount;
	}

	
	
}
