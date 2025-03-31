package memguard.solver.memguardUU1C1B;

import java.util.ArrayList;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;

import memguard.logic.MemguardSystem;
import memguard.logic.MemoryTask;
import memguard.logic.Task;
import memguard.solver.MemguardSolver;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionItem;
import memguard.solver.solution.SolutionStatus;

public class MemguardUU1C1B extends MemguardSolver {

	/**
	 * This will compute the worst-case response time of a task in a system. This
	 * system contains m processors and only one task (to simplify the process).
	 * 
	 * @param system the MemGuard's system with one task
	 */
	public MemguardUU1C1B(MemguardSystem system) {
		super(system);
	}

	@Override
	public Solution solve(boolean verbose) {
		// Extract task
		Task task = system.getProcessor(0).getTask(0);

		// Verify that the task is a ReadWriteTask
		if (!(task instanceof MemoryTask)) {
			System.err.println("Task must be a MemoryTask, abort...");
			System.exit(0);
		}

		// Is a read write system (constructor)
		MemguardSystem rwsystem = (MemguardSystem) system;
		int processorNumber = system.getProcessorNumber();
		int budget = rwsystem.getBudget(0);
		int regulationPeriod = rwsystem.getRegulationPeriod();
		int interferenceProcessorNumber = processorNumber - 1;
		int remainingBudget = regulationPeriod - budget;

		MemoryTask mtask = (MemoryTask) task;
		int memoryCost = mtask.getMemoryCost();
		int computationCost = mtask.getComputationCost();
		int maxPeriodNumber = Math.ceilDiv(memoryCost + computationCost, budget);
		int maxInterferenceAccess = Math.ceilDiv(remainingBudget, interferenceProcessorNumber);

		if (verbose) {
			System.out.println("Important system variables:");
			System.out.println("\tProcessor number: " + processorNumber);
			System.out.println("\tBudget: " + budget);
			System.out.println("\tRegulation Period: " + regulationPeriod);
			System.out.println("\tNumber of interfering cores: " + interferenceProcessorNumber);

			System.out.println("Important task variables:");
			System.out.println("\tMemory cost: " + memoryCost);
			System.out.println("\tComputation cost: " + computationCost);
			System.out.println("\tMaximum period number: " + maxPeriodNumber);
			System.out.println("\tRemaining budget (P - Q): " + remainingBudget);
			System.out.println("\tMaximum interference memory access: " + maxInterferenceAccess);
		}

		// Build model
		CpModel model = new CpModel();

		// Create integer variables
		IntVar[] memoryAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] computationAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] voidAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] stallVariables = new IntVar[maxPeriodNumber];

		// Create boolean variables
		BoolVar[] moreAccessThanPossibleStallVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] voidAccessGreaterZeroVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] budgetFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] memoryFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] computationFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] maxStallVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] periodFilledNoVoidVariables = new BoolVar[maxPeriodNumber];

		// Define integer variables
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Memory access per period
			IntVar memoryAccesses = model.newIntVar(0, budget, "m_%d".formatted(i + 1));
			memoryAccessVariables[i] = memoryAccesses;

			// Computation unit per period
			IntVar computationAccesses = model.newIntVar(0, regulationPeriod, "e_%d".formatted(i + 1));
			computationAccessVariables[i] = computationAccesses;

			// No accesses unit per period (mandatory to complete the period when nothing is
			// left)
			IntVar voidAccesses = model.newIntVar(0, regulationPeriod, "v_%d".formatted(i + 1));
			voidAccessVariables[i] = voidAccesses;

			// Stall value per period
			IntVar stall = model.newIntVar(0, regulationPeriod - budget, "stall_%d".formatted(i + 1));
			stallVariables[i] = stall;
		}

		// Define boolean variables (note that must have both yes and no constraints per
		// variable)
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Check if more accesses than possible interference
			BoolVar moreAccessThanPossibleStall = model.newBoolVar("more_access_%d".formatted(i + 1));
			model.addGreaterOrEqual(memoryAccessVariables[i], maxInterferenceAccess)
					.onlyEnforceIf(moreAccessThanPossibleStall);
			model.addLessThan(memoryAccessVariables[i], maxInterferenceAccess)
					.onlyEnforceIf(moreAccessThanPossibleStall.not());
			moreAccessThanPossibleStallVariables[i] = moreAccessThanPossibleStall;

			// Void values
			BoolVar voidAccessGreaterZero = model.newBoolVar("v_%d_gt0".formatted(i + 1));
			model.addDifferent(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero);
			model.addEquality(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero.not());
			voidAccessGreaterZeroVariables[i] = voidAccessGreaterZero;

			// Budget finished in the period
			BoolVar budgetFinished = model.newBoolVar("Q_%d_finished".formatted(i + 1));
			model.addEquality(memoryAccessVariables[i], budget).onlyEnforceIf(budgetFinished);
			model.addDifferent(memoryAccessVariables[i], budget).onlyEnforceIf(budgetFinished.not());
			budgetFinishedVariables[i] = budgetFinished;

			// Read variables
			BoolVar memoryAccessFinished = model.newBoolVar("m_%d_finished".formatted(i + 1));
			LinearExprBuilder memoryBuilder = LinearExpr.newBuilder();
			for (int j = 0; j <= i; j++) {
				memoryBuilder.add(memoryAccessVariables[j]);
			}
			LinearExpr readExpr = memoryBuilder.build();
			model.addEquality(readExpr, memoryCost).onlyEnforceIf(memoryAccessFinished);
			model.addDifferent(readExpr, memoryCost).onlyEnforceIf(memoryAccessFinished.not());
			memoryFinishedVariables[i] = memoryAccessFinished;

			// Computation variables
			BoolVar computationAccessFinished = model.newBoolVar("e_%d_finished".formatted(i + 1));
			LinearExprBuilder computationBuilder = LinearExpr.newBuilder();
			for (int j = 0; j <= i; j++) {
				computationBuilder.add(computationAccessVariables[j]);
			}
			LinearExpr computationExpr = computationBuilder.build();
			model.addEquality(computationExpr, computationCost).onlyEnforceIf(computationAccessFinished);
			model.addDifferent(computationExpr, computationCost).onlyEnforceIf(computationAccessFinished.not());
			computationFinishedVariables[i] = computationAccessFinished;

			// Max stall required
			BoolVar maxStallReq = model.newBoolVar("max_stall_%d".formatted(i + 1));
			model.addBoolOr(new Literal[] { budgetFinished, moreAccessThanPossibleStall })
					.onlyEnforceIf(maxStallReq);
			model.addBoolAnd(new Literal[] { budgetFinished.not(), moreAccessThanPossibleStall.not() })
					.onlyEnforceIf(maxStallReq.not());
			maxStallVariables[i] = maxStallReq;

			// Check if period is filled without void
			BoolVar periodFilledNoVoid = model.newBoolVar("period_filled_no_void_%d".formatted(i + 1));
			LinearExpr accessesExpr = LinearExpr.newBuilder().add(memoryAccessVariables[i])
					.add(computationAccessVariables[i]).add(stallVariables[i]).build();
			model.addEquality(accessesExpr, regulationPeriod).onlyEnforceIf(periodFilledNoVoid);
			model.addDifferent(accessesExpr, regulationPeriod).onlyEnforceIf(periodFilledNoVoid.not());
			periodFilledNoVoidVariables[i] = periodFilledNoVoid;
		}

		// Constraints
		// Max possible memory access
		LinearExpr maxMemoryExpr = LinearExpr.newBuilder().addSum(memoryAccessVariables).build();
		model.addEquality(maxMemoryExpr, memoryCost);

		// Max possible computation
		LinearExpr maxComputationExpr = LinearExpr.newBuilder().addSum(computationAccessVariables).build();
		model.addEquality(maxComputationExpr, computationCost);

		// Compute stall per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr totalAccessStallExpr = LinearExpr.newBuilder()
					.addTerm(memoryAccessVariables[i], interferenceProcessorNumber).build();

			// If all budget is used and there are still resources or more accesses than
			// possible interference, the stall is P - Q
			model.addEquality(stallVariables[i], remainingBudget).onlyEnforceIf(maxStallVariables[i]);
			// Otherwise the stall is (m - 1) * m^i
			model.addEquality(stallVariables[i], totalAccessStallExpr).onlyEnforceIf(maxStallVariables[i].not());
		}

		// Enable void only when nothing is left and period is not full
		for (int i = 0; i < maxPeriodNumber; i++) {
			model.addBoolAnd(new Literal[] { memoryFinishedVariables[i], computationFinishedVariables[i],
					periodFilledNoVoidVariables[i].not() }).onlyEnforceIf(voidAccessGreaterZeroVariables[i]);
			model.addBoolAnd(new Literal[] { voidAccessGreaterZeroVariables[i] })
					.onlyEnforceIf(new Literal[] { memoryFinishedVariables[i], computationFinishedVariables[i],
							periodFilledNoVoidVariables[i].not() });
		}

		// Constraint a period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExprBuilder usedResourcesExprBuilder = LinearExpr.newBuilder().add(memoryAccessVariables[i])
					.add(computationAccessVariables[i]).add(stallVariables[i]).add(voidAccessVariables[i]);

			LinearExpr usedResourcesExpr = usedResourcesExprBuilder.build();
			model.addEquality(usedResourcesExpr, regulationPeriod);
		}

		// Symmetry breaking:
		/*
		 * Sort per decreasing stall to ensure that the biggest amount of stall is
		 * generated before using computation tricks. This doesn't remove existing
		 * solutions since for period i and j, i < j, with period i having 3 stall units
		 * and period j having 8 stall units, exchange them still gives a valid
		 * solution.
		 */
		for (int i = 0; i < maxPeriodNumber - 1; i++) {
			model.addGreaterOrEqual(stallVariables[i], stallVariables[i + 1]);
		}

		/**
		 * Sort per increasing computation use. This is purely esthetic since it will
		 * just create an order in the periods. As for sorting stall, reordering by
		 * computation does not change anything. This must not affect void units since
		 * they are put only when there is no resource left (so put at the end)
		 */
		for (int i = 0; i < maxPeriodNumber - 1; i++) {
			model.addLessOrEqual(computationAccessVariables[i], computationAccessVariables[i + 1])
					.onlyEnforceIf(voidAccessGreaterZeroVariables[i + 1].not());
		}

		// Add maximization
		LinearExpr maximizationExpr = LinearExpr.newBuilder().addSum(stallVariables).build();
		model.maximize(maximizationExpr);

		// Solve model
		CpSolver solver = new CpSolver();
		CpSolverStatus status = solver.solve(model);

		// If solution is optimal, say it!
		Solution solution;
		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
			System.out.println("Solution found: " + status);
			System.out.println("Generating solution...");

			// Write solution
			ArrayList<SolutionItem> mainProcessorItems = new ArrayList<SolutionItem>();
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems = new ArrayList<ArrayList<SolutionItem>>();
			for (int i = 0; i < interferenceProcessorNumber; i++) {
				interferingProcessorItems.add(new ArrayList<SolutionItem>());
			}

			long totalStall = solver.value(maximizationExpr);
			for (int i = 0; i < maxPeriodNumber; i++) {
				int memoryAccesses = (int) solver.value(memoryAccessVariables[i]);
				int computationAccesses = (int) solver.value(computationAccessVariables[i]);
				int stall = (int) solver.value(stallVariables[i]);
				int voidAccesses = (int) solver.value(voidAccessVariables[i]);
				boolean moreAccessThanPossibleStall = solver.booleanValue(moreAccessThanPossibleStallVariables[i]);
				boolean voidAccessGreaterZero = solver.booleanValue(voidAccessGreaterZeroVariables[i]);
				boolean budgetFinished = solver.booleanValue(budgetFinishedVariables[i]);
				boolean memoryFinished = solver.booleanValue(memoryFinishedVariables[i]);
				boolean computationFinished = solver.booleanValue(computationFinishedVariables[i]);
				boolean maxStall = solver.booleanValue(maxStallVariables[i]);
				boolean periodFilledNoVoid = solver.booleanValue(periodFilledNoVoidVariables[i]);

				int unitsPerPeriod = stall + memoryAccesses + computationAccesses + voidAccesses;
				if (unitsPerPeriod != regulationPeriod) {
					System.err.println("Period %d has a wrong nomber of units in it (expected %d but got %d)"
							.formatted(i + 1, regulationPeriod, unitsPerPeriod));
				}

				if (voidAccesses == regulationPeriod) {
					System.out.println("Period %d onwards only has void accesses (CPU idle), skip...".formatted(i + 1));
					break;
				}

				if (verbose) {
					System.out.println();
					System.out.println("Period %d".formatted(i + 1));
					System.out.println("Memory accesses: %d".formatted(memoryAccesses));
					System.out.println("Computation performed: %d".formatted(computationAccesses));
					System.out.println("Inter-processor stall: %d".formatted(stall));
					System.out.println("Void accesses: %d".formatted(voidAccesses));
					System.out.println("More accesses than interference? %b".formatted(moreAccessThanPossibleStall));
					System.out.println("Void access in period? %b".formatted(voidAccessGreaterZero));
					System.out.println("Budget finished? %b".formatted(budgetFinished));
					System.out.println("Memory finished? %b".formatted(memoryFinished));
					System.out.println("Computation finished? %b".formatted(computationFinished));
					System.out.println("Max stall reached? %b".formatted(maxStall));
					System.out.println("Period filled with no void? %b".formatted(periodFilledNoVoid));
				}

				// Add an item with 0 length to begin
				mainProcessorItems.add(new SolutionItem(0, true, true, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(0, true, true, false));
				}

				// Add memory (and stall if any)
				for (int r = 0; r < memoryAccesses; r++) {
					// If stall left, then put item and update
					stall -= addStall(interferenceProcessorNumber, stall, mainProcessorItems,
							interferingProcessorItems);
					mainProcessorItems.add(new SolutionItem(1, true));
					for (int j = 0; j < interferenceProcessorNumber; j++) {
						interferingProcessorItems.get(j).add(new SolutionItem(1, false, true, false, false));
					}
				}

				// Add computation
				mainProcessorItems.add(new SolutionItem(computationAccesses, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(computationAccesses, true, false, false));
				}

				// Add remaining stall if any
				mainProcessorItems.add(new SolutionItem(stall, false, false, true, false, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(stall, true, false, false));
				}

				// Add void access
				mainProcessorItems.add(new SolutionItem(voidAccesses, true, false, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(voidAccesses, true, false, false));
				}

				// Add an empty item to end
				mainProcessorItems.add(new SolutionItem(0, true, false, true));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(0, true, false, true));
				}
			}

			SolutionItem[][] solutionItems = new SolutionItem[processorNumber][];
			SolutionItem[] mainSolutionItems = new SolutionItem[mainProcessorItems.size()];

			for (int i = 0; i < mainProcessorItems.size(); i++) {
				mainSolutionItems[i] = mainProcessorItems.get(i);
			}
			solutionItems[0] = mainSolutionItems;

			for (int i = 1; i < processorNumber; i++) {
				ArrayList<SolutionItem> itemList = interferingProcessorItems.get(i - 1);
				SolutionItem[] interferenceSolutionItems = new SolutionItem[itemList.size()];
				for (int j = 0; j < itemList.size(); j++) {
					interferenceSolutionItems[j] = itemList.get(j);
				}
				solutionItems[i] = interferenceSolutionItems;
			}

			if (verbose) {
				System.out.println();
				System.out.println("Total stall: %d".formatted(totalStall));
			}

			solution = new Solution(SolutionStatus.valueOf(status.toString()), solutionItems);
			System.out.println("Solution successfully created!");
		} else {
			System.err.println("Something is wrong with the model: " + status);
			solution = new Solution(SolutionStatus.UNFEASIBLE);
		}

		System.out.println("Problem solved in %.4f seconds".formatted(solver.wallTime()));
		solution.setSolvingTime(solver.wallTime());

		return solution;
	}

	// Returns the number of stall unit put
	private int addStall(int interferenceProcessorNumber, int remainingStall,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		int totalStall = 0;
		if (remainingStall != 0) {
			int stall = Math.min(interferenceProcessorNumber, remainingStall);
			totalStall = stall;

			// Fill main processor stall
			mainProcessorItems.add(new SolutionItem(stall, true, false, false));
			for (int i = 0; i < interferenceProcessorNumber; i++) {
				// If no stall remaining, exit
				if (stall == 0) {
					break;
				}

				// Else use one stall unit
				stall -= 1;
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					if (j == i) {
						interferingProcessorItems.get(j).add(new SolutionItem(1, true));
					} else {
						interferingProcessorItems.get(j).add(new SolutionItem(1, false, true, false, false));
					}
				}
			}
		}

		return totalStall;
	}

}
