package memguard.logic;

public class MemoryTask extends Task {

	protected int memoryCost;
	protected int computationCost;
	
	public MemoryTask(int memoryCost, int computationCost, int deadline) {
		super(memoryCost + computationCost, deadline);
		this.memoryCost = memoryCost;
		this.computationCost = computationCost;
	}
	
	public int getMemoryCost() {
		return memoryCost;
	}
	
	public int getComputationCost() {
		return computationCost;
	}
	
}
