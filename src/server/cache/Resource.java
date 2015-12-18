package server.cache;

import java.util.Comparator;

public class Resource {
	private String name;
	private long size;
	private int[][] image;
	private long accessCounter;
	
	public Resource(String name, long size, int[][] image) {
		super();
		this.name = name;
		this.size = size;
		this.image = image;
		this.accessCounter = 0;
	}
	public void incrementAccessCounter(){
		this.accessCounter++;
	}
	public long getAccessCounter() {
		return accessCounter;
	}
	public void setAccessCounter(long accessCounter) {
		this.accessCounter = accessCounter;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public int[][] getImage(){
		return this.image;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == null) return false;
		if(other == this) return true;
		if(!(other instanceof Resource)) return false;
		Resource otherResource = (Resource) other;
		return this.getName().equals(otherResource.getName());
	}
}

class CounterComparator implements Comparator<Resource>{
	public int compare(Resource a, Resource b) {
		return a.getAccessCounter() < b.getAccessCounter() ? -1 : a.getAccessCounter() == b.getAccessCounter() ? 0 : 1;
    }
}

class SizeComparator implements Comparator<Resource>{
	public int compare(Resource a, Resource b) {
		return a.getSize() < b.getSize() ? -1 : a.getSize() == b.getSize() ? 0 : 1;
    }
}
