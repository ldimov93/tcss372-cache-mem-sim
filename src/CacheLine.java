public class CacheLine {
	
	public long address;
	public int state;
	public long tag;
	public long index;
	public Cache cache;
	
	public static final int MODIFIED = 0;
	public static final int EXCLUSIVE = 1;
	public static final int SHARED = 2;
	public static final int INVALID = 3;
	public static final int LINESIZE = 16;
	
	public CacheLine(long address, Cache cache) {
		this.address = address;
		this.cache = cache;
		index = getIndex(cache);
		tag = getTag(cache);
		state = EXCLUSIVE;
	}

	public long getIndex(Cache cache) {
		
		int indexSize = (int) (Math.log(cache.cacheSize / cache.cacheAssociativity) / 
				Math.log(2));
		long index = (address >> (long) (Math.log(LINESIZE) / Math.log(2)) & ((1 << indexSize) - 1));
		
		return index;
		
	}

	public long getTag(Cache cache) {
		
		int indexSize = (int) (Math.log(cache.cacheSize / cache.cacheAssociativity) / 
				Math.log(2));
		long tag = (address >> (long) ((Math.log(LINESIZE) / Math.log(2)) + indexSize));
		
		return tag;
	}

	
	public void setToModified() {
		int prevState = state;
		state = MODIFIED;
		Bus.stateMatrix[prevState][MODIFIED]++;
	}
	
	public void setToExclusive() {
		int prevState = state;
		state = EXCLUSIVE;
		Bus.stateMatrix[prevState][EXCLUSIVE]++;
	}
	
	public void setToShared() {
		int prevState = state;
		state = SHARED;
		Bus.stateMatrix[prevState][SHARED]++;
	}
	
	public void setToInvalid() {
		int prevState = state;
		state = INVALID;
		Bus.stateMatrix[prevState][INVALID]++;
	}
}




