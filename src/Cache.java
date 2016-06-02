public class Cache {
	
	protected int cacheAssociativity;
	protected int cacheLineSize;
	protected int cacheSize;
	protected int cacheLatency;

	private int hits;
	private int misses;
	
	private CacheLine cl[];
	
	public Cache(int cacheLineSize, int cacheAssociativity, int cacheSize, int cacheLatency) {
		
		// create a cache line with given cache line size
		cl = new CacheLine[cacheLineSize];
		
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
	
}