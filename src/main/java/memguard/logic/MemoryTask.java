package memguard.logic;

/**
 * Task with two different costs: a memory cost and a computation cost. It is
 * implied that the memory cost is a blocking resource and the computation cost
 * is not. This can be interpreted as wanted, depending on the scheduling policy
 * used. The implementation is static, meaning that cost values cannot be
 * changed after the task has been created.
 * 
 * @see Task
 * @author Adrien Jakubiak
 */
public class MemoryTask extends Task {

	protected final int memoryCost;
	protected final int computationCost;

	/**
	 * Creates a new task with a memory cost, a computation cost and a deadline. The
	 * total task cost will be the addition of both costs. The name "memory cost"
	 * can be interpreted as wished, meaning that it can be used to represent a
	 * resource other than memory as the code only separates two costs (the name is
	 * only a hint).
	 * 
	 * @param memoryCost      the task memory cost.
	 * @param computationCost the task computation cost.
	 * @param deadline        the task deadline.
	 */
	public MemoryTask(int memoryCost, int computationCost, int deadline) {
		super(memoryCost + computationCost, deadline);
		this.memoryCost = memoryCost;
		this.computationCost = computationCost;
	}

	/**
	 * Returns the task memory cost.
	 * 
	 * @return the task memory cost.
	 */
	public int getMemoryCost() {
		return memoryCost;
	}

	/**
	 * Returns the task computation cost.
	 * 
	 * @return the task computation cost.
	 */
	public int getComputationCost() {
		return computationCost;
	}

}
