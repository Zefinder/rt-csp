package memguard.logic;

public class MemguardProcessor extends Processor {

	private int budget;
	
	public MemguardProcessor(int budget, Task... tasks) {
		super(tasks);
		this.budget = budget;
	}
	
	public int getBudget() {
		return budget;
	}

}
