import java.util.Map;

public class CPU {
	// functions: lookup instr., lookup instr/read, lookup instr/write
	
	// Local caches
	protected Cache L1i;	
	protected Cache L1d;
	protected Cache L2;
	protected Cache L3;
	protected Memory LM1;
	protected Memory LM2;
	protected int totalLatency;
	
	
//	public CPU(Map<String, Integer> param) {
//		// Initialize L1i and L1d
//		L1i = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
//		L1d = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
//		
//		// Initialize shared L2 cache
//		L2 = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l2_size"), param.get("l2_latency"));
//		
//	}
	
	public CPU(Cache l1i, Cache l1d, Cache l2, Cache l3, Memory lm1, Memory lm2) {
		L1i = l1i;
		L1d = l1d;
		L2 = l2;
		L3 = l3;
		LM1 = lm1;
		LM2 = lm2;
		totalLatency = 0;
		
	}
	
	public void lookUp(long address) {
		if(L1i.lookup(address)) {
			totalLatency += L1i.cacheLatency;
		} else if(L1d.lookup(address)) {
			totalLatency += L1d.cacheLatency;
		} else if(L2.lookup(address)) {
			totalLatency += L2.cacheLatency;
		} else if(L3.lookup(address)) {
			totalLatency += L3.cacheLatency;
			//loadCacheLineL3(address);
		} else {// add lookups for the memory
			totalLatency += LM1.latency;
			loadCacheLineMemory(address);
		}
	}
	
	public void loadCacheLineMemory(long address) {
		
		L1i.addCacheLine(address);
		L1d.addCacheLine(address);
		L2.addCacheLine(address);
		L3.addCacheLine(address);
		
	}
		
	public Cache getL1i() {
		return L1i;
	}

	public Cache getL1d() {
		return L1d;
	}

	public Cache getL2() {
		return L2;
	}
	

}





