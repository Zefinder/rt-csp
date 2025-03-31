package memguard.logic;

public class Processor {
	
	private Task[] tasks;
	
	public Processor(Task... tasks) {
		this.tasks = tasks;
	}
	
	public Task[] getTasks() {
		return tasks;
	}
	
	public Task getTask(int index) {
		return tasks[index];
	}
	
	public int getTaskNumber() {
		return tasks.length;
	}
}
