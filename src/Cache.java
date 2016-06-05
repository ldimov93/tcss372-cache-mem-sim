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
	public boolean lookup(Instruction instr) {
		reference++;
		CacheLine lookup = new CacheLine(instr.getAddress(), this);
		long index = lookup.index;
		for (int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			if (cacheEntries[i] != null && lookup.tag == cacheEntries[i].tag) {
				hits++;
				return true;
			}
		}
		return false;
	}
	
	public CacheLine snoop(Instruction instr) {
		CacheLine lookup = new CacheLine(instr.getAddress(), this);
		long index = lookup.index;
		for (int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			if (cacheEntries[i] != null && lookup.tag == cacheEntries[i].tag) {
				return cacheEntries[i];
			}
		}
		return null;
	}

	
	public void addCacheLine(Instruction instr) {
		CacheLine cl = new CacheLine(instr.getAddress(), this);
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
			addCacheLine(instr);
		}
	}

	public void evictCacheLine(long index) {
		Random random = new Random();
		int evict = random.nextInt((int) ((index * cacheAssociativity + cacheAssociativity)
				- (index * cacheAssociativity) + (index * cacheAssociativity)));
		if (cacheEntries[evict] != null && cacheEntries[evict].state == 0) {
			// cacheEntries[evict].writeToMem();
		}
		cacheEntries[evict] = null;
	}
}
