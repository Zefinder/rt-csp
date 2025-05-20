package memguard.logic;

/**
 * System that is extended for Memguard with two counters. It separates the
 * latency into a read and a write latency. The implementation is static,
 * meaning that you cannot modify latencies without creating a new system.
 * 
 * @see MemguardSystem
 * @see Processor
 * @see ReadWriteTask
 * @author Adrien Jakubiak
 */
public class MemguardRWSystem extends MemguardSystem {

	protected final int writeLatency;

	/**
	 * Creates a new system with the specified read latency, write latency,
	 * regulation period, budgets and processors. Note that the implementation is
	 * not dynamic, you cannot change latencies, the regulation period, nor add or
	 * remove a processor without creating a new system. Note that it is recommended
	 * to have the same number of processors and budgets.
	 * 
	 * @param readLatency      the read latency.
	 * @param writelatency     the write latency.
	 * @param regulationPeriod the regulation period.
	 * @param budgets          the budgets allocated to the specified processors.
	 * @param processors       the processors that will run on the processor.
	 */
	public MemguardRWSystem(int readLatency, int writeLatency, int regPeriod, int[] budgets, Processor... processors) {
		super(readLatency, regPeriod, budgets, processors);
		this.writeLatency = writeLatency;
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
	 * This is equivalent to {@link #MemguardRWSystem(Processor...)} with a first
	 * processor with one task and empty others.
	 * </p>
	 * 
	 * @param processorNumber  the number of processors to add to the system, must
	 *                         be greater than 0.
	 * @param readLatency      the read latency.
	 * @param writeLatency     the write latency.
	 * @param regulationPeriod the regulation period.
	 * @param budgets          the budgets allocated to the specified processors.
	 * @param task             the task to add to the first processor.
	 */
	public MemguardRWSystem(int processorNumber, int readLatency, int writeLatency, int regPeriod, int[] budgets,
			Task task) {
		super(processorNumber, readLatency, regPeriod, budgets, task);
		this.writeLatency = writeLatency;
	}

	/**
	 * Returns the system's read latency.
	 * 
	 * @return the system's read latency.
	 */
	public int getReadLatency() {
		return latency;
	}

	/**
	 * Returns the system's write latency.
	 * 
	 * @return the system's write latency.
	 */
	public int getWriteLatency() {
		return writeLatency;
	}

}
