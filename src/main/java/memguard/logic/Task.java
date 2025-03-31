package memguard.logic;

public class Task {

	protected int cost;
	protected int deadline;
	protected int priority;
	
	public Task(int cost, int deadline) {
		this.cost = cost;
		this.deadline = deadline;
	}
	
	public int getCost() {
		return cost;
	}
	
	public int getDeadline() {
		return deadline;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
