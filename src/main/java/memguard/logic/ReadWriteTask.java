package memguard.logic;

public class ReadWriteTask extends MemoryTask {

	protected int readCost;
	protected int writeCost;
	
	public ReadWriteTask(int readCost, int writeCost, int computationCost, int deadline) {
		// Be careful, the memory cost is inexact unless read and write latency are the same
		super(readCost + writeCost, computationCost, deadline);
		this.readCost = readCost;
		this.writeCost = writeCost;
	}
	
	public int getReadCost() {
		return readCost;
	}
	
	public int getWriteCost() {
		return writeCost;
	}

}
