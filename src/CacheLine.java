public class CacheLine {
	
	public long address;
	public int state;
	//	public long tag;
//	public long index;
	
	public static final int MODIFIED = 1;
	public static final int EXCLUSIVE = 2;
	public static final int SHARED = 3;
	public static final int INVALID = 4;
	public static final int LINESIZE = 16;
	
	public CacheLine(long address) {
		this.address = address;
				
	}
	public CacheLine() {
		
				
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



public static void main(String[] args) {
	
	Cache ca = new Cache(16,4, 16, 10);
	CacheLine cl = new CacheLine(0x3256a);
	System.out.println(cl.getIndex(ca));
	System.out.println(cl.getTag(ca));
	
}
}




