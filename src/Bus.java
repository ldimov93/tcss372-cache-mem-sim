import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
	
	public static void readAddressTrace() {
		String csvFile = "trace-2k.csv";
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		
		List<String> list = new ArrayList<String>();

		try {

			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {

				String[] addressTrace = line.split(csvSplitBy);
				String instruction = addressTrace[0];
				String instructionType;
				String data;
				
				if (addressTrace.length == 1) {
					continue;
				}
				if (addressTrace[1] == "0") {
					instructionType = "read";
				} else if (addressTrace[1] == "1") {
					instructionType = "write";
				}
				
				if (addressTrace[2] != "") {
					data = addressTrace[2];
				}
				System.out.println("Line [Instr= " + addressTrace[0] + ", instruction type = " + addressTrace[1] + " data= "
						+ addressTrace[2] + " ]");
				
				// Add to list
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
	}
	
	public static void main(String[] args) {
		
		Bus getOn = new Bus();
		getOn.CPUA.lookUp(204);
		System.out.println(getOn.CPUA.totalLatency);
		getOn.CPUA.lookUp(204);
		System.out.println(getOn.CPUA.totalLatency);
		
		readAddressTrace();
	}
}
