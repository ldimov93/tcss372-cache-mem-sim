import java.util.Map;

public class CPU {
	// functions: lookup instr., lookup instr/read, lookup instr/write
	
	// Local caches
	private Cache L1i;	
	private Cache L1d;
	private Cache L2;
	
	// Shared cache
	private Cache L3;
	
	
	public CPU(Map<String, Integer> param) {
		// Initialize L1i and L1d
		L1i = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
		L1d = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
		
		// Initialize shared L2 cache
		L2 = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l2_size"), param.get("l2_latency"));
		
		
		// get L3 cache to work with
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