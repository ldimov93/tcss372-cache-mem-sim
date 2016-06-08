/*
 * Winfield Brooks
 * Lachezar Dimov
 * TCSS 372 Final Project
 */

public class CPU {

	// Local caches
	protected Cache L1i;
	protected Cache L1d;
	protected Cache L2;
	// Shared cache and memory
	protected Cache L3;
	protected Memory LM1;
	protected Memory LM2;
	protected Bus bus;

	protected int totalLatency;

	public CPU(Cache l1i, Cache l1d, Cache l2, Cache l3, Memory lm1, Memory lm2, Bus bus) {
		L1i = l1i;
		L1d = l1d;
		L2 = l2;
		L3 = l3;
		LM1 = lm1;
		LM2 = lm2;
		totalLatency = 0;
		this.bus = bus;
	}

	// Handles separating write instructions from reads and other instructions.
	public void lookUp(Instruction instr) {
		if (instr.getInstructionType() != null && instr.getInstructionType().equals("write")) {
			writeInstruction(instr);
		} else {
			updateCache(instr);
		}
	}

	/*
	 * If write through policy will add write latency when a write instruction is called.
	 * Sets cache lines to modified and invalidates any shared cache lines.
	 */
	public void writeInstruction(Instruction instr) {
		if(!bus.isWriteBack()) {
			totalLatency += LM2.writeLatency;
		}
		updateCache(instr);
		L1d.snoop(instr).setToModified();
		L2.snoop(instr).setToModified();
		L3.snoop(instr).setToModified();
		bus.checkModified(instr, this);
	}

	/*
	 * Checks if is in local cache and shared cache / memory. Handles setting shared 
	 * caches to SHARED state.
	 */
	public void updateCache(Instruction instr) {
		if (instr.getIsData() && L1d.lookup(instr) && L1d.snoop(instr).state != 3) {
			totalLatency += L1d.cacheLatency;
		} else if (!instr.getIsData() && L1i.lookup(instr) && L1i.snoop(instr).state != 3) {
			totalLatency += L1i.cacheLatency;
		} else if (L2.lookup(instr) && L2.snoop(instr).state != 3) {
			totalLatency += L2.cacheLatency;
			loadCacheLineL2(instr);
		} else if (L3.lookup(instr) && L3.snoop(instr).state != 3) {
			totalLatency += L3.cacheLatency;
			loadCacheLineL3(instr);
			if(bus.checkShared(instr, this)) {
				if(L3.snoop(instr).state == 0 && bus.isWriteBack()) {
					totalLatency += LM2.writeLatency;
				}
				if(instr.getIsData()) {
					L1d.snoop(instr).setToShared();
				} else {
					L1i.snoop(instr).setToShared();
				}
				L2.snoop(instr).setToShared();
				L3.snoop(instr).setToShared();
			}
		} else {
			if (instr.getAddress() < LM1.size) {
				totalLatency += LM1.readLatency;
			} else {
				totalLatency += LM2.readLatency;
			}
			loadCacheLineMemory(instr);
			
		}
	}

	//Loads into all levels of cache from memory.
	public void loadCacheLineMemory(Instruction instr) {

		if (instr.getIsData()) {
			L1d.addCacheLine(instr);
		} else {
			L1i.addCacheLine(instr);
		}
		L2.addCacheLine(instr);
		L3.addCacheLine(instr);

	}

	//Loads into local caches from L3
	public void loadCacheLineL3(Instruction instr) {

		if (instr.getIsData()) {
			L1d.addCacheLine(instr);
		} else {
			L1i.addCacheLine(instr);
		}
		L2.addCacheLine(instr);
	}

	//Loads into L1 from L2
	public void loadCacheLineL2(Instruction instr) {

		if (instr.getIsData()) {
			L1d.addCacheLine(instr);
		} else {
			L1i.addCacheLine(instr);
		}
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
	
	public Cache getL3() {
		return L3;
	}

}
