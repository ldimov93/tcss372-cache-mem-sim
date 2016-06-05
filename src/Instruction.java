
public class Instruction {
	private long instruction, data;
	private String instructionType;
	
	public void setInstruction(long instruction) {
		this.instruction = instruction;
	}
	public void setData(long data) {
		this.data = data;
	}
	public void setInstructionType(String theType) {
		instructionType = theType;
	}
	
	public long getInstruction() {
		return instruction;
	}
	
	public String getInstructionType() {
		return instructionType;
	}
}
