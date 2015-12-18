package server.cache;

public interface CacheOperation {

	/**
	 * Method that adds a resource to the LRU cache
	 * @param resource : resource to add
	 */
	public void addElement(Resource resource);
	
	public void printHitRate();
	
	public void printCache();
}
