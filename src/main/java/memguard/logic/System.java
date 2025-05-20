package memguard.logic;

/**
 * Object that represents a system. It is designed to be static, so no dynamic
 * processor change. A system is just an array of processors, nothing more...
 * 
 * @see Processor
 * @see Task
 * @author Adrien Jakubiak
 */
public class System {

	private final Processor[] processors;

	/**
	 * Creates a new system with the specified processors. Note that the
	 * implementation is not dynamic, you cannot add or remove a processor without
	 * creating a new system.
	 * 
	 * @param processors the processors that will run on the processor.
	 */
	public System(Processor... processors) {
		this.processors = processors;
	}

	/**
	 * <p>
	 * Creates a new system with a specified number of processors and adds a task on
	 * the first processor. This is useful for Memguard analysis tests, where only
	 * the first processor with one task gets analysed, and the others are
	 * considered as interference processors, and will perform accesses
	 * independently of the tasks that run on them.
	 * </p>
	 * 
	 * <p>
	 * This is equivalent to {@link #System(Processor...)} with a first processor
	 * with one task and empty others.
	 * </p>
	 * 
	 * @param processorNumber the number of processors to add to the system, must be
	 *                        greater than 0.
	 * @param task            the task to add to the first processor.
	 */
	public System(int processorNumber, Task task) {
		this(new Processor[processorNumber]);
		processors[0] = new Processor(task);
		for (int i = 1; i < processorNumber; i++) {
			processors[i] = new Processor();
		}
	}

	/**
	 * Returns the processors that compose the system.
	 * 
	 * @return the processors that compose the system.
	 */
	public Processor[] getProcessors() {
		return processors;
	}

	/**
	 * Returns the processor that has the specified index.
	 * 
	 * @param index the processor index.
	 * @return the processor that has the specified index.
	 */
	public Processor getProcessor(int index) {
		return processors[index];
	}

	/**
	 * Returns the number of processors in the system.
	 * 
	 * @return the number of processors in the system.
	 */
	public int getProcessorNumber() {
		return processors.length;
	}

}
