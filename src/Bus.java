
public class Bus {

	protected CPU CPUA;
	protected CPU CPUB;
	protected Cache L1iA;	
	protected Cache L1dA;
	protected Cache L2A;
	protected Cache L1iB;	
	protected Cache L1dB;
	protected Cache L2B;
	protected Cache L3;
	protected Memory LM1;
	protected Memory LM2;
	
	public Bus() { 
		L1iA = new Cache(32, 4, 32, 1);
		L1dA = new Cache(32, 4, 32, 1);
		L1iB = new Cache(32, 4, 32, 1);
		L1dB = new Cache(32, 4, 32, 1);
		L2A = new Cache(32, 4, 512, 10);
		L2B = new Cache(32, 4, 512, 10);
		L3 = new Cache(32, 4, 2048, 35);
		LM1 = new Memory(16000, 100);
		LM2 = new Memory(1000000, 250);
		CPUA = new CPU(L1iA, L1dA, L2A, L3, LM1, LM2);
		CPUB = new CPU(L1iB, L1dB, L2B, L3, LM1, LM2);

	}
	
	public static void main(String[] args) {
		
		Bus getOn = new Bus();
		getOn.CPUA.lookUp(204);
		System.out.println(getOn.CPUA.totalLatency);
		getOn.CPUA.lookUp(204);
		System.out.println(getOn.CPUA.totalLatency);

	}
}
