package memguard.logic;

/**
 * Object that represents a task. A task is a basic object with a cost, a
 * deadline and optionally a priority. A task is usually associated to a
 * processor. If a task is periodic, you can use {@link PeriodicTask}. It is a
 * task that embeds another task and adds a period to it. If a task also has a
 * memory cost (or at least two different resources possible), you can use
 * {@link MemoryTask}.
 * 
 * @see System
 * @see Processor
 * @see PeriodicTask
 * @see MemoryTask
 * @author Adrien Jakubiak
 */
public class Task {

	protected final int cost;
	protected final int deadline;
	protected int priority;

	/**
	 * Creates a new task with a specified cost and deadline. To set a priority, see
	 * {@link #setPriority(int)}.
	 * 
	 * @param cost     the task cost.
	 * @param deadline the task deadline.
	 * 
	 * @see #setPriority(int)
	 */
	public Task(int cost, int deadline) {
		this.cost = cost;
		this.deadline = deadline;
	}

	/**
	 * Returns the task cost.
	 * 
	 * @return the task cost.
	 */
	public int getCost() {
		return cost;
	}

	/**
	 * Returns the task deadline.
	 * 
	 * @return the task deadline.
	 */
	public int getDeadline() {
		return deadline;
	}

	/**
	 * Returns the task priority.
	 * 
	 * @return the task priority.
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Sets the task priority. Priorities are integers that can be dynamically
	 * modified. Because Java does not support unsigned values, priorities can be
	 * negative (if you want).
	 * 
	 * @param priority the priority value.
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
