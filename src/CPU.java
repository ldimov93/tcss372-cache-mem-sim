import java.util.Map;

public class CPU {
	// functions: lookup instr., lookup instr/read, lookup instr/write
	
	// Local caches
	protected Cache L1i;	
	protected Cache L1d;
	protected Cache L2;
	protected Cache L3;
	
	
//	public CPU(Map<String, Integer> param) {
//		// Initialize L1i and L1d
//		L1i = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
//		L1d = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l1_size"), param.get("l1_latency"));
//		
//		// Initialize shared L2 cache
//		L2 = new Cache(param.get("cache_line"), param.get("cache_associativity"), param.get("l2_size"), param.get("l2_latency"));
//		
//	}
	
	public CPU(Cache l1i, Cache l1d, Cache l2, Cache l3) {
		L1i = l1i;
		L1d = l1d;
		L2 = l2;
		L3 = l3;
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
	
	public static void main(String[] args) {
		

	}
}





