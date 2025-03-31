package memguard.main;

import memguard.logic.MemguardSystem;
import memguard.logic.MemoryTask;
import memguard.solver.memguardUU1C1B.MemguardUU1C1B;
import memguard.solver.memguardUU1C1B.MemguardUU1C1BUpgraded;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionStatus;

public class MainMemguardUU1C1B {

	public static void main(String[] args) {
		int processorNumber = 3;
		// P = 1ms => 65500 reads (for L_R = 16ns)
		int regulationPeriod = 15;
		int latency = 1;
		int[] budgets = { (int) ( 10 ) };

		int memoryCost = 4;
		int computationCost = 0;
		int deadline = 100;
		MemoryTask task = new MemoryTask(memoryCost, computationCost, deadline);

		MemguardSystem system = new MemguardSystem(processorNumber, latency, regulationPeriod, budgets, task);
		MemguardUU1C1B solverv1 = new MemguardUU1C1B(system);
		MemguardUU1C1BUpgraded solverv2 = new MemguardUU1C1BUpgraded(system);

		Solution solutionv1 = solverv1.solve(true);
		Solution solutionv2 = solverv2.solve(true);

		if (solutionv1.getStatus() != SolutionStatus.UNFEASIBLE) {
			solutionv1.displaySolution();
		}

		//		if (solutionv2.getStatus() != SolutionStatus.UNFEASIBLE) {
//			solutionv2.displaySolution();
//		}

		System.out.println("Solving time comparison: %.04f ms vs %.04f ms".formatted(solutionv1.getSolvingTime(),
				solutionv2.getSolvingTime()));
	}

}
