/*
 * Winfield Brooks
 * Lachezar Dimov
 * TCSS 372 Final Project
 * 
 * The bus is our main class that connects all the components together. 
 * The bus can be run 2 was from main. The first is default values that coincide
 * with the first set of values from the assignment description.  The second is custom
 * configuration, to do the custom or default comment out the corresponding lines of code 
 * 366-368 to run default and 370 to run in custom mode.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Bus {

	protected static CPU CPUA;
	protected static CPU CPUB;
	protected Cache L1iA;
	protected Cache L1dA;
	protected Cache L2A;
	protected Cache L1iB;
	protected Cache L1dB;
	protected Cache L2B;
	protected Cache L3;
	protected Memory LM1;
	protected Memory LM2;
	private Integer writePolicy;
	public static int[][] stateMatrix;
	private static int instructionCount;
	private static int dataCount;

	//Creates default bus config
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
		stateMatrix = new int[4][4];
		writePolicy = 0;
	}

	//Creates custom bus config
	public Bus(Map<String, Integer> params) {
		L1iA = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L1Size"),
				params.get("L1Latency"));
		L1dA = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L1Size"),
				params.get("L1Latency"));
		L1iB = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L1Size"),
				params.get("L1Latency"));
		L1dB = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L1Size"),
				params.get("L1Latency"));
		L2A = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L2Size"),
				params.get("L2Latency"));
		L2B = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L2Size"),
				params.get("L2Latency"));
		L3 = new Cache(params.get("cacheLineSize"), params.get("cacheAssoc"), params.get("L3Size"),
				params.get("L3Latency"));
		LM1 = new Memory(params.get("1LMSize"), params.get("1LMLatency"), params.get("1LMLatency"));

		// 2LM has both read and write latencies
		LM2 = new Memory(params.get("2LMSize"), params.get("2LMLatencyR"), params.get("2LMLatencyW"));

		CPUA = new CPU(L1iA, L1dA, L2A, L3, LM1, LM2, this);
		CPUB = new CPU(L1iB, L1dB, L2B, L3, LM1, LM2, this);
		writePolicy = params.get("writePolicy");
		stateMatrix = new int[4][4];

	}

	public boolean isWriteBack() {
		return writePolicy == 0;
	}

	/*
	 * If a cache line is hit in L3 the bus checks if it is shared and changes the state.
	 */
	public boolean checkShared(Instruction instr, CPU cpu) {
		boolean isShared = false;
		if (cpu == CPUA) {
			if (CPUB.L1d.snoop(instr) != null) {
				CPUB.L1d.snoop(instr).setToShared();
				isShared = true;
			}
			if (CPUB.L1i.snoop(instr) != null) {
				CPUB.L1i.snoop(instr).setToShared();
				isShared = true;
			}
			if (CPUB.L2.snoop(instr) != null) {
				CPUB.L2.snoop(instr).setToShared();
				isShared = true;
			}
		} else {
			if (CPUA.L1d.snoop(instr) != null) {
				CPUA.L1d.snoop(instr).setToShared();
				isShared = true;
			}
			if (CPUA.L1i.snoop(instr) != null) {
				CPUA.L1i.snoop(instr).setToShared();
				isShared = true;
			}
			if (CPUA.L2.snoop(instr) != null) {
				CPUA.L2.snoop(instr).setToShared();
				isShared = true;
			}
		}
		return isShared;
	}

	/*
	 * If a cache line is set to modified checks for shared lines and sets to invalid.
	 * Will also write if encounters a modified cache line.
	 */
	public void checkModified(Instruction instr, CPU cpu) {

		if (cpu == CPUA) {

			if (CPUB.L2.snoop(instr) != null && CPUB.L2.snoop(instr).state == 0) {
				if (isWriteBack()) {
					CPUA.totalLatency += LM2.writeLatency;
				}
				CPUB.L2.snoop(instr).setToInvalid();
				CPUB.L2.snoop(instr).setToShared();
				CPUA.L1d.snoop(instr).setToShared();
				CPUA.L2.snoop(instr).setToShared();
				CPUA.L3.snoop(instr).setToShared();
				if (CPUB.L1d.snoop(instr) != null) {
					CPUB.L1d.snoop(instr).setToInvalid();
					CPUB.L1d.snoop(instr).setToShared();
				}
			} else {
				if (CPUB.L1d.snoop(instr) != null && CPUB.L1d.snoop(instr).state != 3) {
					CPUB.L1d.snoop(instr).setToInvalid();
				}
				if (CPUB.L2.snoop(instr) != null && CPUB.L2.snoop(instr).state != 3) {
					CPUB.L2.snoop(instr).setToInvalid();
				}
			}
		} else {

			if (CPUA.L2.snoop(instr) != null && CPUA.L2.snoop(instr).state == 0) {
				if (isWriteBack()) {
					CPUB.totalLatency += LM2.writeLatency;
				}
				CPUA.L2.snoop(instr).setToInvalid();
				CPUA.L2.snoop(instr).setToShared();
				CPUB.L1d.snoop(instr).setToShared();
				CPUB.L2.snoop(instr).setToShared();
				CPUB.L3.snoop(instr).setToShared();
				if (CPUA.L1d.snoop(instr) != null) {
					CPUA.L1d.snoop(instr).setToInvalid();
					CPUA.L1d.snoop(instr).setToShared();
				}
			} else {
				if (CPUA.L1d.snoop(instr) != null && CPUA.L1d.snoop(instr).state != 3) {
					CPUA.L1d.snoop(instr).setToInvalid();
				}
				if (CPUB.L2.snoop(instr) != null && CPUB.L2.snoop(instr).state != 3) {
					CPUB.L2.snoop(instr).setToInvalid();
				}
			}
		}
	}

	/*
	 * Reads address trace into list to iterate through.
	 */
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
				instructions.add(instr);
				instructionCount++;
				//if line has data
				if (addressTrace.length > 1) {
					dataCount++;
					Instruction data = new Instruction();
						data.setAddress(Long.parseLong(addressTrace[2]));
						data.setIsData(true);
					if (addressTrace[1].equals("0")) {
						data.setInstructionType("read");
					} else if (addressTrace[1].equals("1")) {
						data.setInstructionType("write");
					}
					instructions.add(data);
				}
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

	
	//Generates output report
	public static void generateReport() {
		PrintStream output;
		StringBuilder sb = new StringBuilder();
		
		
		float CPUAL1HitRate = (float) (CPUA.getL1i().getHits() + CPUA.getL1d().getHits())
				/ (CPUA.getL1i().getReferences() + CPUA.getL1d().getReferences());
		float CPUAL1MissRate = (float) (CPUA.getL1i().getMisses() + CPUA.getL1d().getMisses())
				/ (CPUA.getL1i().getReferences() + CPUA.getL1d().getReferences());

		float CPUAL2HitRate = (float) (CPUA.getL2().getHits())
				/ (CPUA.getL2().getReferences() + CPUA.getL2().getReferences());
		float CPUAL2MissRate = (float) (CPUA.getL2().getMisses() + CPUA.getL2().getMisses())
				/ (CPUA.getL2().getReferences() + CPUA.getL2().getReferences());

		float CPUBL1HitRate = (float) (CPUB.getL1i().getHits() + CPUB.getL1d().getHits())
				/ (CPUB.getL1i().getReferences() + CPUB.getL1d().getReferences());
		float CPUBL1MissRate = (float) (CPUB.getL1i().getMisses() + CPUB.getL1d().getMisses())
				/ (CPUB.getL1i().getReferences() + CPUB.getL1d().getReferences());

		float CPUBL2HitRate = (float) (CPUB.getL2().getHits())
				/ (CPUB.getL2().getReferences() + CPUB.getL2().getReferences());
		float CPUBL2MissRate = (float) (CPUB.getL2().getMisses() + CPUB.getL2().getMisses())
				/ (CPUB.getL2().getReferences() + CPUB.getL2().getReferences());

		float L3HitRate = (float) CPUA.getL3().getHits() / CPUA.getL3().getReferences();
		float L3MissRate = (float) CPUA.getL3().getMisses() / CPUA.getL3().getReferences();

		
		sb.append("\nNumber of Instructions: " + instructionCount);
		sb.append("\nNumber of Read or Write Instructions: " + dataCount);

		sb.append("\nCPU A: Latency: " + CPUA.totalLatency +" ns");
		sb.append("\nCPU B: Latency: " + CPUB.totalLatency +" ns");
		sb.append("\nCombined Latency: " + (CPUA.totalLatency + CPUB.totalLatency) +" ns");
		sb.append("\nCPU A: Average instruction time: " + CPUA.totalLatency / instructionCount);
		sb.append("\nCPU B: Average instruction time: " + CPUB.totalLatency / instructionCount);

		sb.append("\nCPU A: L1i Hits: " + CPUA.L1i.getHits());
		sb.append("\nCPU A: L1i Misses: " + CPUA.L1i.getMisses());
		sb.append("\nCPU A: L1i Accesses: " + CPUA.L1i.getReferences());
		
		sb.append("\nCPU A: L1d Hits: " + CPUA.L1d.getHits());
		sb.append("\nCPU A: L1d Misses: " + CPUA.L1d.getMisses());
		sb.append("\nCPU A: L1d Accesses: " + CPUA.L1d.getReferences());
		
		sb.append("\nCPU A: L2 Hits: " + CPUA.L2.getHits());
		sb.append("\nCPU A: L2 Misses: " + CPUA.L2.getMisses());
		sb.append("\nCPU A: L2 Accesses: " + CPUA.L2.getReferences());
		
		sb.append("\nCPU B: L1i Hits: " + CPUB.L1i.getHits());
		sb.append("\nCPU B: L1i Misses: " + CPUB.L1i.getMisses());
		sb.append("\nCPU B: L1i Accesses: " + CPUB.L1i.getReferences());
		
		sb.append("\nCPU B: L1d Hits: " + CPUB.L1d.getHits());
		sb.append("\nCPU B: L1d Misses: " + CPUB.L1d.getMisses());
		sb.append("\nCPU B: L1d Accesses: " + CPUB.L1d.getReferences());
		
		sb.append("\nCPU B: L2 Hits: " + CPUB.L2.getHits());
		sb.append("\nCPU B: L2 Misses: " + CPUB.L2.getMisses());
		sb.append("\nCPU B: L2 Accesses: " + CPUB.L2.getReferences());
		
		sb.append("\nL3 Hits: " + CPUA.getL3().getHits() + "\nL3 Misses: " + CPUA.getL3().getMisses() + "\n");
		
		
		sb.append("\nCPU A: L1 hit rate: " + CPUAL1HitRate * 100 + "%" + "\nCPU A: L1 miss rate: " + CPUAL1MissRate * 100 + "%");
		sb.append("\nCPU A: L2 hit rate: " + CPUAL2HitRate * 100 + "%" + "\nCPU A: L2 miss rate: " + CPUAL2MissRate * 100 + "%");

		sb.append("\nCPU B: L1 hit rate: " + CPUBL1HitRate * 100 + "%" + "\nCPU B: L1 miss rate: " + CPUBL1MissRate * 100 + "%");
		sb.append("\nCPU B: L2 hit rate: " + CPUBL2HitRate * 100 + "%" + "\nCPU B: L2 miss rate: " + CPUBL2MissRate * 100 + "%");

		sb.append("\nL3 hit rate: " + L3HitRate * 100 + "%" + "\nL3 miss rate: " + L3MissRate * 100 + "%\n");

		sb.append("\nStateMatrix:");
		sb.append("\nModified -> Exclusive: " + stateMatrix[0][1]);
		sb.append("\nModified -> Shared: " + stateMatrix[0][2]);
		sb.append("\nModified -> Invalid: " + stateMatrix[0][3]);
		sb.append("\nExclusive -> Modified: " + stateMatrix[1][0]);
		sb.append("\nExclusive -> Shared: " + stateMatrix[1][2]);
		sb.append("\nExclusive -> Invalid: " + stateMatrix[1][3]);
		sb.append("\nShared -> Modified: " + stateMatrix[2][0]);
		sb.append("\nShared -> Exclusive: " + stateMatrix[2][1]);
		sb.append("\nShared -> Invalid: " + stateMatrix[2][3]);
		sb.append("\nInvalid -> Modified: " + stateMatrix[3][0]);
		sb.append("\nInvalid -> Exclusive: " + stateMatrix[3][1]);
		sb.append("\nInvalid -> Shared: " + stateMatrix[3][2]);

		
		
		try {
			output = new PrintStream(new File("output-2k.txt"));
			output.println(sb.toString());
		} catch (FileNotFoundException e) {
			
		}
		
	}

	public int[][] getStateMatrix() {
		return stateMatrix;
	}

	//Console input for custom config
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

	/*
	 * Main to run default config comment out lines 366-368, to run custom
	 * config comment out line 370
	 */
	public static void main(String[] args) {

		ArrayList<Instruction> list = (ArrayList<Instruction>) readAddressTrace();

		Map<String, Integer> configs = new HashMap<String, Integer>(); 	//console prompt config
		configs = getConfigInput();			//console prompt config
		Bus getOn = new Bus(configs);		//console prompt config
		
//		Bus getOn = new Bus();			//default configuration

		for (int i = 0; i < 50; i++) {
			getOn.CPUA.lookUp(list.get(i));
		}
		for (int i = 0; i < list.size() - 50; i++) {
			getOn.CPUA.lookUp(list.get(i + 50));
			getOn.CPUB.lookUp(list.get(i));

		}
		for (int i = list.size() - 50; i < list.size(); i++) {
			getOn.CPUB.lookUp(list.get(i));
		}
		generateReport();
	}
}
