package server.cache;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SizeBasedCache extends Cache implements CacheOperation {

	private Map<String, Resource> cache;
	

	public SizeBasedCache(long n, long x) {
		super(n, x);
		cache = new HashMap<String, Resource>();
	}

	@Override
	public void addElement(Resource resource) {
		if(resource.getSize() <= n){ // if resource size is too big we don't use the cache
			if(!warmup && access == x){ // Check if warmup is done
				warmup = true;
				access = 0;
			}
			if(cache.containsKey(resource.getName())){ // Check if resource is already in the cache
				if(!(cache.get(resource.getName()).getSize() != resource.getSize())){
					if(warmup){
						byteHit += resource.getSize();
						hit++;
					}
				}
				else{
					weight -= cache.get(resource.getName()).getSize();
					cache.remove(resource.getName());
					addNew(resource, n);
				}
			}
			else{
				addNew(resource, n);
			}
			byteAccess += resource.getSize();
		}
		access++;
	}
	
	/**
	 * Return true if there's an element with the same name
	 * @param name
	 * @return true if there's an element with the same name
	 */
	public boolean isElement(String name){
		return cache.containsKey(name);
	}
	
	/**
	 * Get the resource according to the name
	 * @param name
	 * @return the resource according to the name
	 */
	public Resource getResource(String name){
		return cache.get(name);
	}

	@Override
	public void printHitRate() {
		System.out.println("Size-based Hit rate: "+((hit/access)*100)+" %");
		System.out.println("Size-based Byte hit rate: "+((byteHit/byteAccess)*100)+" %");
	}

	@Override
	public void printCache() {
		PrintWriter writerRemoveSmallest;
		try {
			writerRemoveSmallest = new PrintWriter("cache_size-based.txt", "UTF-8");
			for(Resource resourceTemp : cache.values()){
				writerRemoveSmallest.println(resourceTemp.getName());
			}
			writerRemoveSmallest.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void addNew(Resource resource, long n){
		weight += resource.getSize();
		if(weight > n){
			List<Resource> cacheElements = new ArrayList<Resource>(cache.values());
			Collections.sort(cacheElements, new SizeComparator());
			while(weight > n){ // remove elements as long as there's no place for the new resource
				Resource resourceToRemove = cacheElements.get(0);
				weight -= resourceToRemove.getSize();
				cache.remove(resourceToRemove.getName());
			}
		}
		cache.put(resource.getName(), resource);
	}
}
