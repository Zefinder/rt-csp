package memguard.logic;

/**
 * Object that represents a processor. It is designed to be static, so no
 * dynamic task change. A processor is just an array of task, nothing more...
 * 
 * @see System
 * @see Task
 * @author Adrien Jakubiak
 */
public class Processor {

	private final Task[] tasks;

	/**
	 * Creates a new processor with the specified tasks. Note that the
	 * implementation is not dynamic, you cannot add or remove a task without
	 * creating a new processor.
	 * 
	 * @param tasks the tasks that will run on the processor.
	 */
	public Processor(Task... tasks) {
		this.tasks = tasks;
	}

	/**
	 * Returns the tasks that run on the processor.
	 * 
	 * @return an array of tasks.
	 */
	public Task[] getTasks() {
		return tasks;
	}

	/**
	 * Returns the task stored at the specified index.
	 * 
	 * @param index the task index.
	 * @return the task stored at the specified index.
	 */
	public Task getTask(int index) {
		return tasks[index];
	}

	/**
	 * Returns the number of tasks that run on the processor.
	 * 
	 * @return the number of tasks that run on the processor.
	 */
	public int getTaskNumber() {
		return tasks.length;
	}
}
