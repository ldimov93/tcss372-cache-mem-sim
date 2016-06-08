/*
 * Winfield Brooks
 * Lachezar Dimov
 * TCSS 372 Final Project
 * 
 * Contains information read in from instruction trace
 */


public class Instruction {
	private long address;
	private String instructionType;		//read or write
	private boolean isData = false;
	
	public void setAddress(long address) {
		this.address = address;
	}
	public void setIsData(boolean isData) {
		this.isData = isData;
	}
	public void setInstructionType(String theType) {
		instructionType = theType;
	}
	
	public long getAddress() {
		return address;
	}
	
	public boolean getIsData() {
		return isData;
	}
	
	public String getInstructionType() {
		return instructionType;
	}
}
