
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
	protected Bus bus;

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

	public void lookUp(Instruction instr) {
		if (instr.getInstructionType() != null && instr.getInstructionType().equals("write")) {
			writeInstruction(instr);
		} else if (instr.getInstructionType() != null && instr.getInstructionType().equals("read")) {
			readInstruction(instr);
		} else {
			updateCache(instr);
		}
	}

	public void writeInstruction(Instruction instr) {
		updateCache(instr);

		L1d.snoop(instr).setToModified();
		L2.snoop(instr).setToModified();
		L3.snoop(instr).setToModified();

		bus.checkModified(instr, this);
	}

	// read data
	public void readInstruction(Instruction instr) {
		
		// Do a lookup in CPU's local caches L1d and L2
		if (L1d.lookup(instr) && L1d.snoop(instr).state != 3) {
			totalLatency += L1d.getCacheLatency();
		} else if (L2.lookup(instr) && L2.snoop(instr).state != 3) {
			totalLatency += L2.getCacheLatency();
		} else {
			// Request a read on the system bus, since data is either in the other CPU's cache
			// or in memory
			totalLatency += bus.requestRead(instr, this);
		}
	}

	public void updateCache(Instruction instr) {
		if (L1i.lookup(instr) && L1i.snoop(instr).state != 3) {
			totalLatency += L1i.cacheLatency;
		} else if (L1d.lookup(instr) && L1d.snoop(instr).state != 3) {
			totalLatency += L1d.cacheLatency;
		} else if (L2.lookup(instr) && L2.snoop(instr).state != 3) {
			totalLatency += L2.cacheLatency;
			loadCacheLineL2(instr);
		} else if (L3.lookup(instr) && L3.snoop(instr).state != 3) {
			totalLatency += L3.cacheLatency;
			loadCacheLineL3(instr);
			bus.checkShared(instr, this);
		} else {
			if (instr.getAddress() < LM1.size) {
				totalLatency += LM1.readLatency;
			} else {
				totalLatency += LM2.readLatency;
			}
			loadCacheLineMemory(instr);
			
		}
	}

	public void loadCacheLineMemory(Instruction instr) {

		if (instr.getIsData()) {
			L1d.addCacheLine(instr);
		} else {
			L1i.addCacheLine(instr);
		}
		L2.addCacheLine(instr);
		L3.addCacheLine(instr);

	}

	public void loadCacheLineL3(Instruction instr) {

		if (instr.getIsData()) {
			L1d.addCacheLine(instr);
		} else {
			L1i.addCacheLine(instr);
		}
		L2.addCacheLine(instr);
	}

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

}
