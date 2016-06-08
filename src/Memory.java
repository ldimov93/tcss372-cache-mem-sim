/*
 * Winfield Brooks
 * Lachezar Dimov
 * TCSS 372 Final Project
 */

public class Memory {

	public int size;
	public int readLatency;
	public int writeLatency;
	
	public Memory(int size, int readLatency, int writeLatency) {
		this.size = size;
		this.readLatency = readLatency;
		this.writeLatency = writeLatency;
	}
	
}
