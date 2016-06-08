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

	public static List<Instruction> readAddressTrace() {
		String csvFile = "trace-win.csv";
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
				
				//if line has data
				if (addressTrace.length > 1) {
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

	public static void main(String[] args) {

		ArrayList<Instruction> list = (ArrayList<Instruction>) readAddressTrace();

		// Map<String, Integer> configs = new HashMap<String, Integer>();
		// configs = getConfigInput();

		// Bus getOn = new Bus(configs);
		Bus getOn = new Bus();
		for (int i = 0; i < 50; i++) {
			// System.out.println("first " + i);
			getOn.CPUA.lookUp(list.get(i));
		}

		for (int i = 0; i < list.size() - 50; i++) {
			// System.out.println("second " + i);

			getOn.CPUA.lookUp(list.get(i + 50));
			getOn.CPUB.lookUp(list.get(i));

		}

		for (int i = list.size() - 50; i < list.size(); i++) {
			// System.out.println("third " + i);

			getOn.CPUB.lookUp(list.get(i));

		}

		generateReport();

		System.out.println(getOn.CPUA.totalLatency);
		System.out.println(getOn.CPUA.getL1i().getHits());

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				System.out.print(stateMatrix[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println(CPUA.L1i.cacheEntries.length);
		System.out.println("length of instructions list " + list.size());
		System.out.println("references A L1i " + getOn.CPUA.L1i.getReferences());
		System.out.println("hits A L1i " + getOn.CPUA.L1i.getHits());
		System.out.println("references A L1d " + getOn.CPUA.L1d.getReferences());
		System.out.println("hits A L1d " + getOn.CPUA.L1d.getHits());
		System.out.println("references A L2 " + getOn.CPUA.L2.getReferences());
		System.out.println("hits A L2 " + getOn.CPUA.L2.getHits());
		System.out.println("references A L3 " + getOn.CPUA.L3.getReferences());
		System.out.println("hits A L3 " + getOn.CPUA.L3.getHits());
	}

	public static void generateReport() {
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

		float L3HitRate = CPUA.getL3().getHits() / CPUA.getL3().getReferences();
		float L3MissRate = CPUA.getL3().getMisses() / CPUA.getL3().getMisses();

		sb.append("\nCPU A: L1 hit rate: " + CPUAL1HitRate + " L1 miss rate: " + CPUAL1MissRate);
		sb.append("\nCPU A: L2 hit rate: " + CPUAL2HitRate + " L2 miss rate: " + CPUAL2MissRate);

		sb.append("\nCPU B: L1 hit rate: " + CPUBL1HitRate + " L1 miss rate: " + CPUBL1MissRate);
		sb.append("\nCPU B: L2 hit rate: " + CPUBL2HitRate + " L2 miss rate: " + CPUBL2MissRate);

		sb.append("\nL3 hit rate: " + L3HitRate + " L3 miss rate: " + L3MissRate);

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

		System.out.println(sb.toString());
	}

	public int[][] getStateMatrix() {
		return stateMatrix;
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

	// Respond to CPU read request
	public int requestRead(Instruction instr, CPU requester) {
		int latency = 0;
		if (requester == CPUA) {

			// Check local caches of CPUB, since data could be there
			if (CPUB.getL1d().lookup(instr) && CPUB.getL1d().snoop(instr).state != 3) {

				// Copy data to local L1 and L2 caches of requester CPU

				latency += CPUB.getL1d().getCacheLatency();
			} else if (CPUB.getL2().lookup(instr) && CPUB.getL2().snoop(instr).state != 3) {
				// Copy data to local L1 and L2 caches of requester CPU

				latency += CPUB.getL2().getCacheLatency();
			}
		} else if (requester == CPUB) {
			// Check local caches of CPUB, since data could be there
			if (CPUA.getL1d().lookup(instr) && CPUA.getL1d().snoop(instr).state != 3) {

				// Copy data to local L1 and L2 caches of requester CPU

				latency += CPUA.getL1d().getCacheLatency();
			} else if (CPUA.getL2().lookup(instr) && CPUA.getL2().snoop(instr).state != 3) {

				// Copy data to local L1 and L2 caches of requester CPU

				latency += CPUA.getL2().getCacheLatency();
			}
		}
		return latency;
	}
}
