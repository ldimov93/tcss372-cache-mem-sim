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
		hits = 0;
		reference = 0;
		
		cacheEntries = new CacheLine[cacheSize];
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
	public boolean lookup(long address) {
		reference++;
		CacheLine lookup = new CacheLine(address, this);
		long index = lookup.index;
		for (int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			if (cacheEntries[i] != null && lookup.tag == cacheEntries[i].tag) {
				hits++;
				return true;
			}
		}
		return false;
	}

	public void addCacheLine(long address) {
		CacheLine cl = new CacheLine(address, this);
		long index = cl.index;
		boolean addedFlag = false;
		for (int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			if (cacheEntries[i] == null) {
				cacheEntries[i] = cl;
				addedFlag = true;
			}
		}
		if (!addedFlag) {
			evictCacheLine(cl.index);
			addCacheLine(address);
		}
	}

	public void evictCacheLine(long index) {
		Random random = new Random();
		int evict = random.nextInt((int) ((index * cacheAssociativity + cacheAssociativity)
				- (index * cacheAssociativity) + (index * cacheAssociativity)));
		if (cacheEntries[evict].state == 1) {
			// cacheEntries[evict].writeToMem();
		}
		cacheEntries[evict] = null;
	}
}
