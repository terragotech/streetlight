package com.terragoedge.edgeserver;

public class Occurences {
	
	private int min;
    private int max;
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	@Override
	public String toString() {
		return "Occurences [min=" + min + ", max=" + max + "]";
	}
    
    

}
