import java.util.List;
import java.util.Random;

public class Cache {
	
	protected int cacheAssociativity;
	protected int cacheLineSize;
	protected int cacheSize;
	protected int cacheLatency;

	private int hits;
	private int reference;
	
	private CacheLine[] cacheEntries;
	
	public Cache(int cacheLineSize, int cacheAssociativity, int cacheSize, int cacheLatency) {
		
//		// Add cache lines to list of cache lines based on cache size
//		for(int i = 0; i < cacheSize; i++) {
//			cl.add(new CacheLine());
//        }
		cacheEntries = new CacheLine[cacheLineSize];
		this.cacheLineSize = cacheLineSize;
		this.cacheAssociativity = cacheAssociativity;
		this.cacheSize = cacheSize;
		this.cacheLatency = cacheLatency;
		
	}
	public int getHits() {
		return hits;
	}
	
	public int getReferences() {
		return reference;
	}
	
	public int getCacheLatency() {
		return cacheLatency;
	}
	
	// Lookup given address in cache
	public void lookup(long address) {
		
	}
	
	public void addCacheLine(CacheLine cl) {
		long index = cl.index;
		boolean addedFlag = false;
		for(int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			
			if(cacheEntries[i] == null) {
				cacheEntries[i] = cl;
			}
		}
	}
	
	public void evictCacheLine(long index) {
		Random random = new Random();
		int evict = random.nextInt((int) ((index * cacheAssociativity + cacheAssociativity) - (index * cacheAssociativity)
				+ (index * cacheAssociativity)));
		
		if(cacheEntries[evict].state == 1) {
			//cacheEntries[evict].writeToMem();
		}
		cacheEntries[evict] = null;
	}
}








