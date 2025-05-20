package memguard.solver;

import memguard.logic.System;
import memguard.solution.Solution;

/**
 * Solvers and solutions have a naming convention to know what they correspond
 * to (this is a regex): <code>Memguard(E|U[UK])([0-9]+|n)C([0-9]+|n)B.</code>
 * 
 * <ul>
 * <li>The first group corresponds to Even or Uneven budget. When budget is
 * uneven, it can be Known for all processors or only known for the analyzed
 * processor (thus Unknown).
 * <li>The second group corresponds to the number of Counters the system has. If
 * it is generalized for n counters, then nC.
 * <li>The third group corresponds to the number of Budgets each processor has.
 * If it is generalized for n budgets, then nB.
 * </ul>
 */
public abstract class MemguardSolver extends Solver {

	protected System system;

	public MemguardSolver(System system) {
		this.system = system;
	}

	public abstract Solution solve(boolean verbose);

}
