package memguard.logic;

public class System {

	private Processor[] processors;
	
	public System(Processor... processors) {
		this.processors = processors;
	}
	
	/**
	 * System with m processors and a task on the first one
	 */
	public System(int processorNumber, Task task) {
		this(new Processor[processorNumber]);
		processors[0] = new Processor(task);
		for (int i = 1; i < processorNumber; i++) {
			processors[i] = new Processor();
		}
	}
	
	public Processor[] getProcessors() {
		return processors;
	}
	
	public Processor getProcessor(int index) {
		return processors[index];
	}
	
	public int getProcessorNumber() {
		return processors.length;
	}

}
