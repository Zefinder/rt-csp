package memguard.logic;

public class MemguardSystem extends System {

	protected int latency;
	protected int regulationPeriod;
	protected int[] budgets;

	public MemguardSystem(int latency, int regulationPeriod, int[] budgets, MemguardProcessor... processors) {
		super(processors);
		this.latency = latency;
		this.regulationPeriod = regulationPeriod;
		this.budgets = budgets;
	}

	/**
	 * Single task system
	 */
	public MemguardSystem(int processorNumber, int latency, int regulationPeriod, int[] budgets, Task task) {
		super(processorNumber, task);
		this.latency = latency;
		this.regulationPeriod = regulationPeriod;
		this.budgets = budgets;
	}
	
	public int getLatency() {
		return latency;
	}
	
	public int getRegulationPeriod() {
		return regulationPeriod;
	}
	
	public int[] getBudgets() {
		return budgets;
	}
	
	public int getBudget(int index) {
		return budgets[index];
	}
}
