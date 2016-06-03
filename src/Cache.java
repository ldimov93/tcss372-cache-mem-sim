import java.util.List;

public class Cache {
	
	protected int cacheAssociativity;
	protected int cacheLineSize;
	protected int cacheSize;
	protected int cacheLatency;

	private int hits;
	private int misses;
	
	private List<CacheLine> cl;
	
	public Cache(int cacheLineSize, int cacheAssociativity, int cacheSize, int cacheLatency) {
		
		// Add cache lines to list of cache lines based on cache size
		for(int i = 0; i < cacheSize; i++) {
			cl.add(new CacheLine());
        }
		
		this.cacheLineSize = cacheLineSize;
		this.cacheAssociativity = cacheAssociativity;
		this.cacheSize = cacheSize;
		this.cacheLatency = cacheLatency;
		
	}
	public int getHits() {
		return hits;
	}
	
	public int getMisses() {
		return misses;
	}
	
	public int getCacheLatency() {
		return cacheLatency;
	}
	
	// Lookup given address in cache
	public void lookup(long address) {
		
	}
}