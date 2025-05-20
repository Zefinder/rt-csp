package memguard.main;
import java.io.IOException;

import memguard.logic.MemguardRWSystem;
import memguard.logic.ReadWriteTask;
import memguard.solution.Solution;
import memguard.solution.SolutionStatus;
import memguard.solver.memguardUU2C1B.MemguardUU2C1BOpStall;

public class MainMemguardUU2C1B {

	public static void main(String[] args) throws IOException {
		int processorNumber = 3;
		int readLatency = 1;
		int writeLatency = 2;
		int regulationPeriod = 20;
		int[] budgets = { 10 };

		int readCost = 20 * readLatency;
		int writeCost = 20 * writeLatency;
		int computationCost = 200;
		int deadline = 100;
		ReadWriteTask task = new ReadWriteTask(readCost, writeCost, computationCost, deadline);

		MemguardRWSystem system = new MemguardRWSystem(processorNumber, readLatency, writeLatency, regulationPeriod,
				budgets, task);
//		MemguardUU2C1B solver1 = new MemguardUU2C1B(system);
//		Solution solution1 = solver1.solve(true);

		MemguardUU2C1BOpStall solver2 = new MemguardUU2C1BOpStall(system);
		Solution solution2 = solver2.solve(true);
		
//		if (solution1.getStatus() != SolutionStatus.UNFEASIBLE) {
//			solution1.displaySolution();
//		}
		
		if (solution2.getStatus() != SolutionStatus.UNFEASIBLE) {
			solution2.displaySolution();
		}
	}

}
