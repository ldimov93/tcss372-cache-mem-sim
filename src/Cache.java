/*
 * Winfield Brooks
 * Lachezar Dimov
 * TCSS 372 Final Project
 */


import java.util.Random;

public class Cache {

	protected int cacheAssociativity;
	protected int cacheLineSize;
	protected int cacheSize;
	protected int cacheLatency;
	private int hits;
	private int reference;

	protected CacheLine[] cacheEntries;

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
	
	public int getMisses() {
		return reference - hits;
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
	
	//Return cache line if present in cache
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

	//Adds cache line to cache, calls evict if no space available
	public void addCacheLine(Instruction instr, CPU cpu) {
		CacheLine cl = new CacheLine(instr.getAddress(), this);
		long index = cl.index;
		boolean addedFlag = false;
		for (int i = (int) (index * cacheAssociativity); i < index * cacheAssociativity + cacheAssociativity; i++) {
			if (cacheEntries[i] == null) {
				cacheEntries[i] = cl;
				addedFlag = true;
				return;
			}
		}
		if (!addedFlag) {
			evictCacheLine(cl.index, cpu);
			addCacheLine(instr, cpu);
		}
	}

	//Evicts cache line with random policy
	public void evictCacheLine(long index, CPU cpu) {
		Random random = new Random();
		int evict = (int) (random.nextInt(cacheAssociativity) + index * cacheAssociativity);
		if (cacheEntries[evict] != null && cacheEntries[evict].state == 0) {
			cpu.totalLatency += cpu.LM2.writeLatency;
		}
		cacheEntries[evict] = null;
	}
}
