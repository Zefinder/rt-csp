package memguard.main;
import java.io.IOException;

import memguard.logic.MemguardRWSystem;
import memguard.logic.ReadWriteTask;
import memguard.solver.memguardUU2C1B.MemguardUU2C1B;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionStatus;

public class MainMemguardUU2C1B {

	public static void main(String[] args) throws IOException {
		int processorNumber = 3;
		int readLatency = 1;
		int writeLatency = 2;
		int regulationPeriod = 10;
		int[] budgets = { 3 };

		int readCost = 3 * readLatency;
		int writeCost = 1 * writeLatency;
		int computationCost = 0;
		int deadline = 100;
		ReadWriteTask task = new ReadWriteTask(readCost, writeCost, computationCost, deadline);

		MemguardRWSystem system = new MemguardRWSystem(processorNumber, readLatency, writeLatency, regulationPeriod,
				budgets, task);
		MemguardUU2C1B solver = new MemguardUU2C1B(system, 2);
		Solution solution = solver.solve(true);

		if (solution.getStatus() != SolutionStatus.UNFEASIBLE) {
			solution.displaySolution();
		}
	}

}
