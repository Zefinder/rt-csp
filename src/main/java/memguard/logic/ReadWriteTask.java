package memguard.logic;

/**
 * Task that further subdivides the memory cost into read cost and write cost.
 * Depending on the interpretation, read and write cost can be seen as two
 * different blocking resources for example. The implementation is static,
 * meaning that it is not possible to dynamically modify costs after creating
 * the task. This class inherits from {@link MemoryTask} and inherits all its
 * characteristics.
 * 
 * @see Task
 * @see MemoryTask
 * @author Adrien Jakubiak
 */
public class ReadWriteTask extends MemoryTask {

	protected final int readCost;
	protected final int writeCost;

	/**
	 * <p>
	 * Creates a new task with a read cost, a write cost, a computation cost and a
	 * deadline. The memory cost will be the sum of the read and write costs, and
	 * the total task cost will be the addition of the memory and computation costs.
	 * </p>
	 * 
	 * <p>
	 * As for {@link MemoryTask}, read and write cost only represent resources that
	 * are different from computation and that must not be treated the same way. If
	 * the programmer wants to use it for something else than reads and writes,
	 * there is no mechanism that will stop it from doing so since they are just
	 * names.
	 * </p>
	 * 
	 * @param readCost        the task read cost.
	 * @param writeCost       the task write cost.
	 * @param computationCost the task computation cost.
	 * @param deadline        the task deadline.
	 */
	public ReadWriteTask(int readCost, int writeCost, int computationCost, int deadline) {
		super(readCost + writeCost, computationCost, deadline);
		this.readCost = readCost;
		this.writeCost = writeCost;
	}

	/**
	 * Returns the task's read cost.
	 * @return the task's read cost.
	 */
	public int getReadCost() {
		return readCost;
	}

	/**
	 * Returns the task's write cost.
	 * @return the task's write cost.
	 */
	public int getWriteCost() {
		return writeCost;
	}

}
