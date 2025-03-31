package memguard.logic;

public class MemguardRWSystem extends MemguardSystem {

	protected int writeLatency;
	
	// Read latency is the latency for the old one
	public MemguardRWSystem(int readLatency, int writeLatency, int regPeriod, int[] budgets, MemguardProcessor... processors) {
		super(readLatency, regPeriod, budgets, processors);
		this.writeLatency = writeLatency;
	}

	public MemguardRWSystem(int processorNumber, int readLatency, int writeLatency, int regPeriod, int[] budgets, Task task) {
		super(processorNumber, readLatency, regPeriod, budgets, task);
		this.writeLatency = writeLatency;
	}
	
	public int getReadLatency() {
		return latency;
	}
	
	public int getWriteLatency() {
		return writeLatency;
	}

}
