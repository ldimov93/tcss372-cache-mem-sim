import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
	public static int[][] stateMatrix;
	
	
	public Bus() { 
		L1iA = new Cache(32, 4, 32, 1);
		L1dA = new Cache(32, 4, 32, 1);
		L1iB = new Cache(32, 4, 32, 1);
		L1dB = new Cache(32, 4, 32, 1);
		L2A = new Cache(32, 4, 512, 10);
		L2B = new Cache(32, 4, 512, 10);
		L3 = new Cache(32, 4, 2048, 35);
		LM1 = new Memory(16000, 100, 100);
		LM2 = new Memory(1000000, 250, 400);
		CPUA = new CPU(L1iA, L1dA, L2A, L3, LM1, LM2, this);
		CPUB = new CPU(L1iB, L1dB, L2B, L3, LM1, LM2, this);
		stateMatrix =  new int[4][4];
	}
	
	public Bus(Map<String, Integer> params) {
		L1iA = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
						 params.get("L1Size"), params.get("L1Latency"));
		L1dA = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L1Size"), params.get("L1Latency"));
		L1iB = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L1Size"), params.get("L1Latency"));
		L1dB = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L1Size"), params.get("L1Latency"));
		L2A = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L2Size"), params.get("L2Latency"));
		L2B = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L2Size"), params.get("L2Latency"));
		L3 = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), 
				 params.get("L3Size"), params.get("L3Latency"));
		LM1 = new Memory(params.get("1LMSize"), params.get("1LMLatency"), params.get("1LMLatency"));
		
		// 2LM has both read and write latencies
		LM2 = new Memory(params.get("2LMSize"), params.get("2LMLatencyR"), 
						 params.get("2LMLatencyW")); 
		
		CPUA = new CPU(L1iA, L1dA, L2A, L3, LM1, LM2, this);
		CPUB = new CPU(L1iB, L1dB, L2B, L3, LM1, LM2, this);
		stateMatrix =  new int[4][4];

	}
	
	public void checkShared(Instruction instr, CPU cpu) {
		
		if (cpu == CPUA) {
			if (CPUB.L1d.snoop(instr) != null) {
				CPUB.L1d.snoop(instr).setToShared();
			}
			if (CPUB.L1i.snoop(instr) != null) {
				CPUB.L1i.snoop(instr).setToShared();
			}
			if (CPUB.L2.snoop(instr) != null) {
				CPUB.L2.snoop(instr).setToShared();
			}
		} else {
			if (CPUA.L1d.snoop(instr) != null) {
				CPUA.L1d.snoop(instr).setToShared();
			}
			if (CPUA.L1i.snoop(instr) != null) {
				CPUA.L1i.snoop(instr).setToShared();
			}
			if (CPUA.L2.snoop(instr) != null) {
				CPUA.L2.snoop(instr).setToShared();
			}
		}
	}
		
	public void checkModified(Instruction instr, CPU cpu) {
		
		if (cpu == CPUA) {
			if (CPUB.L1d.snoop(instr) != null) {
				CPUB.L1d.snoop(instr).setToInvalid();
			}
			if (CPUB.L1i.snoop(instr) != null) {
				CPUB.L1i.snoop(instr).setToInvalid();
			}
			if (CPUB.L2.snoop(instr) != null) {
				CPUB.L2.snoop(instr).setToInvalid();
			}
		} else {
			if (CPUA.L1d.snoop(instr) != null) {
				CPUA.L1d.snoop(instr).setToInvalid();
			}
			if (CPUA.L1i.snoop(instr) != null) {
				CPUA.L1i.snoop(instr).setToInvalid();
			}
			if (CPUA.L2.snoop(instr) != null) {
				CPUA.L2.snoop(instr).setToInvalid();
			}
		}
	}
		
	
	
	
	public static List<Instruction> readAddressTrace() {
		String csvFile = "trace-2k.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		List<Instruction> instructions = new ArrayList<Instruction>();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				String[] addressTrace = line.split(csvSplitBy);
				Instruction instr = new Instruction();
				
				instr.setAddress(Long.parseLong(addressTrace[0]));
				if (addressTrace.length == 1) {
					// it's an instruction
					instructions.add(instr);
					continue;
				}
				if (addressTrace[1].equals("0")) {
					instr.setInstructionType("read");
				} else if (addressTrace[1].equals("1")) {
					instr.setInstructionType("write");
				}
				
				if (addressTrace[2] != "") {
					instr.setAddress(Long.parseLong(addressTrace[2]));
					instr.setIsData(true);
				}
				
				// Add instruction to list
				instructions.add(instr);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return instructions;
	}
	
	public static void main(String[] args) {
		
		ArrayList<Instruction> list = (ArrayList<Instruction>) readAddressTrace();
		
		//Map<String, Integer> configs = new HashMap<String, Integer>();
		//configs = getConfigInput();
		
		//Bus getOn = new Bus(configs);
		Bus getOn = new Bus();
		for(int i = 0; i < 50; i++) {
			System.out.println("first " + i);
			getOn.CPUA.lookUp(list.get(i));
		}
		
		for(int i = 0; i < list.size() - 50; i++) {
			System.out.println("second " + i);

			getOn.CPUA.lookUp(list.get(i + 50));
			getOn.CPUB.lookUp(list.get(i));

		}
		
		for(int i = list.size() - 50; i < list.size(); i++) {
			System.out.println("thrid " + i);

			getOn.CPUB.lookUp(list.get(i));

		}
		
		System.out.println(getOn.CPUA.totalLatency);
		System.out.println(getOn.CPUA.getL1i().getHits());
		
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				System.out.print(stateMatrix[i][j] + " ");
			}
			System.out.println();
		}
		
	}

	private static Map<String, Integer> getConfigInput() {
		Map<String, Integer> configs = new HashMap<String, Integer>();
		Scanner in = new Scanner(System.in);
		
		System.out.print("Cache Line/Block size (bytes): ");
		configs.put("cacheLineSize", Integer.parseInt(in.nextLine().trim()));		
		System.out.print("L1d/L1i size (entries): ");
		configs.put("L1Size", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("L1d/L1d latency (ns): ");
		configs.put("L1Latency", Integer.parseInt(in.nextLine().trim()));
		System.out.print("L2 size (entries): ");
		configs.put("L2Size", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("L2 latency (ns): ");
		configs.put("L2Latency", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("L3 size (entries): ");
		configs.put("L3Size", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("L3 latency (ns): ");
		configs.put("L3Latency", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("1LM size (KB): ");
		configs.put("1LMSize", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("1LM latency (ns): ");
		configs.put("1LMLatency", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("2LM size (MB): ");
		configs.put("2LMSize", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("2LM latency Reads (ns): ");
		configs.put("2LMLatencyR", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("2LM latency Writes (ns): ");
		configs.put("2LMLatencyW", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("Cache Associativity (i.e. 4): ");
		configs.put("cacheAssoc", Integer.parseInt(in.nextLine().trim()));	
		System.out.print("Write Policy (0 for writeback or 1 for writethrough): ");
		configs.put("writePolicy", Integer.parseInt(in.nextLine().toLowerCase().trim()));
		
		in.close();
		return configs;
	}
}
