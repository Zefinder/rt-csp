package memguard.logic;

public class PeriodicTask extends Task {

	private int period;
	
	public PeriodicTask(Task task, int period) {
		super(task.cost, task.deadline);
		this.period = period;
	}
	
	public int getPeriod() {
		return period;
	}

}
