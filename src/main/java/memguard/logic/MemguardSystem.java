package memguard.logic;

/**
 * System that is extended for Memguard with one counter. It adds a memory
 * access latency, a regulation period and budgets to the system. The
 * implementation is static, meaning that it is not possible to change the
 * system's values except for budgets.
 * 
 * @see System
 * @see Processor
 * @see MemoryTask
 * @author Adrien Jakubiak
 */
public class MemguardSystem extends System {

	protected final int latency;
	protected final int regulationPeriod;
	protected final int[] budgets;

	/**
	 * Creates a new system with the specified latency, regulation period, budgets
	 * and processors. Note that the implementation is not dynamic, you cannot
	 * change the latency, the regulation period, nor add or remove a processor
	 * without creating a new system. Note that it is recommended to have the same
	 * number of processors and budgets.
	 * 
	 * @param latency          the memory latency.
	 * @param regulationPeriod the regulation period.
	 * @param budgets          the budgets allocated to the specified processors.
	 * @param processors       the processors that will run on the processor.
	 */
	public MemguardSystem(int latency, int regulationPeriod, int[] budgets, Processor... processors) {
		super(processors);
		this.latency = latency;
		this.regulationPeriod = regulationPeriod;
		this.budgets = budgets;
	}

	/**
	 * <p>
	 * Creates a new system with a specified number of processors and adds a task on
	 * the first processor. For Memguard analysis when tasks on interference cores
	 * are unknown, only the first processor gets a task and we assume that the
	 * other processors will use memory whenever they can.
	 * </p>
	 * 
	 * <p>
	 * This is equivalent to {@link #MemguardSystem(Processor...)} with a first
	 * processor with one task and empty others.
	 * </p>
	 * 
	 * @param processorNumber  the number of processors to add to the system, must
	 *                         be greater than 0.
	 * @param latency          the memory latency.
	 * @param regulationPeriod the regulation period.
	 * @param budgets          the budgets allocated to the specified processors.
	 * @param task             the task to add to the first processor.
	 */
	public MemguardSystem(int processorNumber, int latency, int regulationPeriod, int[] budgets, Task task) {
		super(processorNumber, task);
		this.latency = latency;
		this.regulationPeriod = regulationPeriod;
		this.budgets = budgets;
	}

	/**
	 * Returns the system's memory latency.
	 * 
	 * @return the system's memory latency.
	 */
	public int getLatency() {
		return latency;
	}

	/**
	 * Returns the system's regulation period.
	 * 
	 * @return the system's regulation period.
	 */
	public int getRegulationPeriod() {
		return regulationPeriod;
	}

	/**
	 * Returns the system's budgets.
	 * 
	 * @return the system's budgets.
	 */
	public int[] getBudgets() {
		return budgets;
	}

	/**
	 * Returns the core's budget specified by the index.
	 * 
	 * @param index the core's index.
	 * @return the core's budget specified by the index.
	 */
	public int getBudget(int index) {
		return budgets[index];
	}
}
