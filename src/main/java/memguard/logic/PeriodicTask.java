package memguard.logic;

/**
 * Task that embeds another task to add a priority. All characteristics of the
 * embedded task remain untouched. The implementation is static, meaning that
 * you cannot modify the period after creating the task.
 * 
 * @see Task
 * @author Adrien Jakubiak
 */
public class PeriodicTask extends Task {

	private final Task task;
	private final int period;

	/**
	 * <p>
	 * Created a new periodic task from a task. The period must be strictly greater
	 * than 0. If it is not (negative or zero), the task will be created with a
	 * period of 1. Periods are integers, so no floats allowed.
	 * </p>
	 * 
	 * <p>
	 * Because this task embeds another, it is possible to embed a periodic task,
	 * creating a double periodic task. The interpretation is left to the
	 * programmer, it is possible to create a task that has a memory refresh period
	 * of 10 and a task period of 15 for instance. The programmer will create its
	 * solver according to the number of periods he wanted.
	 * </p>
	 * 
	 * @param task the embedded task.
	 * @param period the period value.
	 */
	public PeriodicTask(Task task, int period) {
		super(task.cost, task.deadline);
		this.task = task;
		this.period = period;
	}

	/**
	 * Returns the embedded task.
	 * 
	 * @return the embedded task.
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * Returns the task period.
	 * 
	 * @return the task period.
	 */
	public int getPeriod() {
		return period;
	}

}
