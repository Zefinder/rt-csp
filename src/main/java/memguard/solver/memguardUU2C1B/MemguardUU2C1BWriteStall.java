package memguard.solver.memguardUU2C1B;

import java.util.ArrayList;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;

import memguard.logic.MemguardRWSystem;
import memguard.logic.ReadWriteTask;
import memguard.logic.Task;
import memguard.solver.MemguardSolver;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionItem;
import memguard.solver.solution.SolutionStatus;

public class MemguardUU2C1BWriteStall extends MemguardSolver {

	/**
	 * This will compute the worst-case response time of a task in a system. This
	 * system contains m processors and only one task (to simplify the process).
	 * 
	 * @param system the MemGuard's system with one task
	 */
	public MemguardUU2C1BWriteStall(MemguardRWSystem system) {
		super(system);
	}

	@Override
	public Solution solve(boolean verbose) {
		// Extract task
		Task task = system.getProcessor(0).getTask(0);

		// Verify that the task is a ReadWriteTask
		if (!(task instanceof ReadWriteTask)) {
			System.err.println("Task must be a ReadWriteTask, abort...");
			System.exit(0);
		}

		// Is a read write system (constructor)
		MemguardRWSystem rwsystem = (MemguardRWSystem) system;
		int processorNumber = system.getProcessorNumber();
		int budget = rwsystem.getBudget(0);
		int regulationPeriod = rwsystem.getRegulationPeriod();
		int readLatency = rwsystem.getReadLatency();
		int writeLatency = rwsystem.getWriteLatency();
		int interferenceProcessorNumber = processorNumber - 1;
		int remainingBudget = regulationPeriod - budget;

		ReadWriteTask rwtask = (ReadWriteTask) task;
		int readCost = rwtask.getReadCost();
		int writeCost = rwtask.getWriteCost();
		int computationAccessNumber = rwtask.getComputationCost();
		int readAccessNumber = readCost / readLatency;
		int writeAccessNumber = writeCost / writeLatency;
		int maxPeriodNumber = Math.ceilDiv(readCost + writeCost + computationAccessNumber, budget - writeLatency + 1);
		int maxReadPerPeriod = Math.floorDiv(budget, readLatency);
		int maxWritePerPeriod = Math.floorDiv(budget, writeLatency);

		if (verbose) {
			System.out.println("Important system variables:");
			System.out.println("\tProcessor number: " + processorNumber);
			System.out.println("\tBudget: " + budget);
			System.out.println("\tRegulation Period: " + regulationPeriod);
			System.out.println("\tRead latency: " + readLatency);
			System.out.println("\tWrite latency: " + writeLatency);
			System.out.println("\tNumber of interfering cores: " + interferenceProcessorNumber);
			System.out.println("\tRemaining budget (P - Q): " + remainingBudget);

			System.out.println("Important task variables:");
			System.out.println("\tRead cost: " + readCost);
			System.out.println("\tWrite cost: " + writeCost);
			System.out.println("\tComputation cost: " + computationAccessNumber);
			System.out.println("\tRead access number: " + readAccessNumber);
			System.out.println("\tWrite access number: " + writeAccessNumber);
			System.out.println("\tMaximum period number: " + maxPeriodNumber);
			System.out.println("\tMaximum read per period: " + maxReadPerPeriod);
			System.out.println("\tMaximum write per period: " + maxWritePerPeriod);
		}

		// Build model
		CpModel model = new CpModel();

		// Create integer variables
		IntVar[] readAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] writeAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] computationAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] voidAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] writeStallVariables = new IntVar[maxPeriodNumber - 1];
		IntVar[] stallVariables = new IntVar[maxPeriodNumber];

		// Create boolean variables
		BoolVar[] moreAccessThanPossibleStallVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] writeStallGraterZeroVariables = new BoolVar[maxPeriodNumber - 1];
		BoolVar[] writeAccessGreaterZeroVariables = new BoolVar[maxPeriodNumber - 1];
		BoolVar[] voidAccessGreaterZeroVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] readFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] writeFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] computationFinishedVariables = new BoolVar[maxPeriodNumber];

		// Define integer variables
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Read access per period
			IntVar readAccesses = model.newIntVar(0, maxReadPerPeriod, "r_%d".formatted(i + 1));

			// Write access per period
			IntVar writeAccesses = model.newIntVar(0, maxWritePerPeriod, "w_%d".formatted(i + 1));

			// Computation unit per period
			IntVar computationAccesses = model.newIntVar(0, regulationPeriod, "e_%d".formatted(i + 1));

			// No accesses unit per period (mandatory to complete the period when nothing is
			// left)
			IntVar voidAccesses = model.newIntVar(0, regulationPeriod, "v_%d".formatted(i + 1));

			// Write stall value (when stall is added because not enough budget/period
			// left).
			if (i != maxPeriodNumber - 1) {
				IntVar writeStall = model.newIntVar(0, regulationPeriod, "eps_%d".formatted(i + 1));
				writeStallVariables[i] = writeStall;
			}

			// Stall value per period
			IntVar stall = model.newIntVar(0, regulationPeriod - budget, "stall_%d".formatted(i + 1));

			// Add to arrays
			readAccessVariables[i] = readAccesses;
			writeAccessVariables[i] = writeAccesses;
			computationAccessVariables[i] = computationAccesses;
			voidAccessVariables[i] = voidAccesses;
			stallVariables[i] = stall;
		}

		// Define boolean variables (note that must have both yes and no constraints per
		// variable)
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Check if more accesses than possible interference
			BoolVar moreAccessThanPossibleStall = model.newBoolVar("more_access_%d".formatted(i + 1));
			LinearExpr stallDifferenceExpr = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], writeLatency * interferenceProcessorNumber)
					.addTerm(writeAccessVariables[i], writeLatency * interferenceProcessorNumber).add(-remainingBudget)
					.build();
			model.addGreaterThan(stallDifferenceExpr, 0).onlyEnforceIf(moreAccessThanPossibleStall);
			model.addLessOrEqual(stallDifferenceExpr, 0).onlyEnforceIf(moreAccessThanPossibleStall.not());
			moreAccessThanPossibleStallVariables[i] = moreAccessThanPossibleStall;

			// Write stall greater than 0 (because positive, != 0 is more efficient)
			if (i != maxPeriodNumber - 1) {
				BoolVar writeStallGreaterZero = model.newBoolVar("eps_%d_gt0".formatted(i + 1));
				model.addDifferent(writeStallVariables[i], 0).onlyEnforceIf(writeStallGreaterZero);
				model.addEquality(writeStallVariables[i], 0).onlyEnforceIf(writeStallGreaterZero.not());
				writeStallGraterZeroVariables[i] = writeStallGreaterZero;
			}

			// Next write greater than 0 (starts at period 2!)
			if (i != 0) {
				BoolVar writeAccessGreaterZero = model.newBoolVar("w_%d_gt0".formatted(i + 1));
				model.addDifferent(writeAccessVariables[i], 0).onlyEnforceIf(writeAccessGreaterZero);
				model.addEquality(writeAccessVariables[i], 0).onlyEnforceIf(writeAccessGreaterZero.not());
				writeAccessGreaterZeroVariables[i - 1] = writeAccessGreaterZero;
			}

			// Void values
			BoolVar voidAccessGreaterZero = model.newBoolVar("v_%d_gt0".formatted(i + 1));
			model.addDifferent(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero);
			model.addEquality(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero.not());
			voidAccessGreaterZeroVariables[i] = voidAccessGreaterZero;

			// Read variables
			BoolVar readAccessFinished = model.newBoolVar("r_%d_finished".formatted(i + 1));
			LinearExprBuilder readBuilder = LinearExpr.newBuilder();
			for (int j = 0; j <= i; j++) {
				readBuilder.addTerm(readAccessVariables[j], readLatency);
			}
			LinearExpr readExpr = readBuilder.build();
			model.addEquality(readExpr, readCost).onlyEnforceIf(readAccessFinished);
			model.addDifferent(readExpr, readCost).onlyEnforceIf(readAccessFinished.not());
			readFinishedVariables[i] = readAccessFinished;

			// Write variables
			BoolVar writeAccessFinished = model.newBoolVar("w_%d_finished".formatted(i + 1));
			LinearExprBuilder writeBuilder = LinearExpr.newBuilder();
			for (int j = 0; j <= i; j++) {
				writeBuilder.addTerm(writeAccessVariables[j], writeLatency);
			}
			LinearExpr writeExpr = writeBuilder.build();
			model.addEquality(writeExpr, writeCost).onlyEnforceIf(writeAccessFinished);
			model.addDifferent(writeExpr, writeCost).onlyEnforceIf(writeAccessFinished.not());
			writeFinishedVariables[i] = writeAccessFinished;

			// Computation variables
			BoolVar computationAccessFinished = model.newBoolVar("e_%d_finished".formatted(i + 1));
			LinearExprBuilder computationBuilder = LinearExpr.newBuilder();
			for (int j = 0; j <= i; j++) {
				computationBuilder.add(computationAccessVariables[j]);
			}
			LinearExpr computationExpr = computationBuilder.build();
			model.addEquality(computationExpr, computationAccessNumber).onlyEnforceIf(computationAccessFinished);
			model.addDifferent(computationExpr, computationAccessNumber).onlyEnforceIf(computationAccessFinished.not());
			computationFinishedVariables[i] = computationAccessFinished;
		}

		// Constraints
		// Max possible read
		LinearExpr maxReadExpr = LinearExpr.newBuilder().addSum(readAccessVariables).build();
		model.addEquality(maxReadExpr, readAccessNumber);

		// Max possible write
		LinearExpr maxWriteExpr = LinearExpr.newBuilder().addSum(writeAccessVariables).build();
		model.addEquality(maxWriteExpr, writeAccessNumber);

		// Max possible computation
		LinearExpr maxComputationExpr = LinearExpr.newBuilder().addSum(computationAccessVariables).build();
		model.addEquality(maxComputationExpr, computationAccessNumber);

		// Max memory access per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr maxMemoryAccessPerPeriodExpr = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], readLatency).addTerm(writeAccessVariables[i], writeLatency)
					.build();
			model.addLessOrEqual(maxMemoryAccessPerPeriodExpr, budget);
		}

		// Compute stall per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr totalAccessStallExpr = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], writeLatency * interferenceProcessorNumber)
					.addTerm(writeAccessVariables[i], writeLatency * interferenceProcessorNumber).build();

			// If more access than possible interference, the stall is P-Q
			model.addEquality(stallVariables[i], remainingBudget)
					.onlyEnforceIf(moreAccessThanPossibleStallVariables[i]);
			model.addEquality(stallVariables[i], totalAccessStallExpr)
					.onlyEnforceIf(moreAccessThanPossibleStallVariables[i].not());
		}

		// epsilon_i != 0 => w_i+1 != 0
		for (int i = 0; i < maxPeriodNumber - 1; i++) {
			model.addImplication(writeStallGraterZeroVariables[i], writeAccessGreaterZeroVariables[i]);
		}

		// Enable void only when nothing is left
		for (int i = 0; i < maxPeriodNumber; i++) {
			model.addImplication(voidAccessGreaterZeroVariables[i], readFinishedVariables[i]);
			model.addImplication(voidAccessGreaterZeroVariables[i], writeFinishedVariables[i]);
			model.addImplication(voidAccessGreaterZeroVariables[i], computationFinishedVariables[i]);
		}

		// Constraint a period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExprBuilder usedResourcesExprBuilder = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], readLatency).addTerm(writeAccessVariables[i], writeLatency)
					.add(computationAccessVariables[i]).add(stallVariables[i]).add(voidAccessVariables[i]);
			if (i != maxPeriodNumber - 1) {
				usedResourcesExprBuilder.add(writeStallVariables[i]);
			}

			LinearExpr usedResourcesExpr = usedResourcesExprBuilder.build();
			model.addEquality(usedResourcesExpr, regulationPeriod);
		}

		// Symmetry breaking:
		/*
		 * Sort per decreasing stall (interference stall + write stall) to ensure that
		 * the biggest amount of stall is generated before using computation tricks.
		 * This doesn't remove existing solutions since for period i and j, i < j, with
		 * period i having 3 stall units and period j having 8 stall units, exchange
		 * them still gives a valid solution.
		 */
		for (int i = 0; i < maxPeriodNumber - 1; i++) {
			LinearExprBuilder b1 = LinearExpr.newBuilder();
			LinearExprBuilder b2 = LinearExpr.newBuilder();
			b1.add(stallVariables[i]);
			b2.add(stallVariables[i + 1]);
			
			if (i != maxPeriodNumber - 2) {
				b1.add(writeStallVariables[i]);
				b2.add(writeStallVariables[i + 1]);
			}
			model.addGreaterOrEqual(b1.build(), b2.build());
		}

		// Add maximization
		LinearExprBuilder maximizationExprBuilder = LinearExpr.newBuilder();
		for (int i = 0; i < maxPeriodNumber; i++) {
			maximizationExprBuilder.add(stallVariables[i]);

			if (i != maxPeriodNumber - 1) {
				maximizationExprBuilder.add(writeStallVariables[i]);
			}
		}
		LinearExpr maximizationExpr = maximizationExprBuilder.build();
		model.maximize(maximizationExpr);

		// Solve model
		CpSolver solver = new CpSolver();
		CpSolverStatus status = solver.solve(model);
		System.out.println("Problem solved in %.4f milliseconds".formatted(solver.wallTime()));

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
				int remainingStall = (int) solver.value(stallVariables[i]);
				int readAccesses = (int) solver.value(readAccessVariables[i]);
				int writeAccesses = (int) solver.value(writeAccessVariables[i]);
				int computationAccesses = (int) solver.value(computationAccessVariables[i]);
				int writeStall = i == maxPeriodNumber - 1 ? 0 : (int) solver.value(writeStallVariables[i]);
				int voidAccesses = (int) solver.value(voidAccessVariables[i]);
				boolean writeStallGreaterZero = i == maxPeriodNumber - 1 ? false
						: (solver.value(writeStallGraterZeroVariables[i]) == 1 ? true : false);
				boolean needsFirstWrite = i == 0 ? false
						: (solver.value(writeAccessGreaterZeroVariables[i - 1]) == 1 ? true : false);

				int unitsPerPeriod = remainingStall + readAccesses * readLatency + writeAccesses * writeLatency
						+ computationAccesses + writeStall + voidAccesses;
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
					System.out.println("Read accesses: %d".formatted(readAccesses));
					System.out.println("Write accesses: %d".formatted(writeAccesses));
					System.out.println("Computation performed: %d".formatted(computationAccesses));
					System.out.println("Inter-processor stall: %d".formatted(remainingStall));
					System.out.println("Write stall: %d".formatted(writeStall));
					System.out.println("Void accesses: %d".formatted(voidAccesses));
					System.out.println("Needs first write: %b".formatted(needsFirstWrite));
					System.out.println("Write stall greater than 0: %b".formatted(writeStallGreaterZero));
				}

				// Add an item with 0 length to begin
				mainProcessorItems.add(new SolutionItem(0, true, true, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(0, true, true, false));
				}

				// If needs first write then put write at first
				if (needsFirstWrite) {
					remainingStall -= addStall(interferenceProcessorNumber, writeLatency, remainingStall,
							mainProcessorItems, interferingProcessorItems);
					mainProcessorItems.add(new SolutionItem(writeLatency, true));
					for (int j = 0; j < interferenceProcessorNumber; j++) {
						interferingProcessorItems.get(j).add(new SolutionItem(writeLatency, false, true, false, false));
					}
					writeAccesses -= 1;
				}

				// Add reads (and stall if any)
				for (int r = 0; r < readAccesses; r++) {
					// If stall left, then put item and update
					remainingStall -= addStall(interferenceProcessorNumber, writeLatency, remainingStall,
							mainProcessorItems, interferingProcessorItems);
					mainProcessorItems.add(new SolutionItem(readLatency, true));
					for (int j = 0; j < interferenceProcessorNumber; j++) {
						interferingProcessorItems.get(j).add(new SolutionItem(readLatency, false, true, false, false));
					}
				}

				// Add writes (and stall if any)
				for (int w = 0; w < writeAccesses; w++) {
					// If stall left, then put item and update
					remainingStall -= addStall(interferenceProcessorNumber, writeLatency, remainingStall,
							mainProcessorItems, interferingProcessorItems);
					mainProcessorItems.add(new SolutionItem(writeLatency, true));
					for (int j = 0; j < interferenceProcessorNumber; j++) {
						interferingProcessorItems.get(j).add(new SolutionItem(writeLatency, false, true, false, false));
					}
				}

				// Add computation
				mainProcessorItems.add(new SolutionItem(computationAccesses, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(computationAccesses, true, false, false));
				}

				// Add remaining stall if any
				mainProcessorItems.add(new SolutionItem(remainingStall, false, false, true, false, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(remainingStall, true, false, false));
				}

				// Add write stall
				mainProcessorItems.add(new SolutionItem(writeStall, false, false, true, false, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(writeStall, true, false, false));
				}

				// Add void access
				mainProcessorItems.add(new SolutionItem(voidAccesses, true, false, false));
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					interferingProcessorItems.get(j).add(new SolutionItem(voidAccesses, true, false, false));
				}

				// Add an item with 0 length to end
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

		return solution;
	}

	// Returns the number of stall unit put
	private int addStall(int interferenceProcessorNumber, int writeLatency, int remainingStall,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		int totalStall = 0;
		if (remainingStall != 0) {
			int stall = Math.min(interferenceProcessorNumber * writeLatency, remainingStall);
			totalStall = stall;

			// Fill main processor stall
			mainProcessorItems.add(new SolutionItem(stall, true, false, false));
			for (int i = 0; i < interferenceProcessorNumber; i++) {
				int processorStall = Math.min(writeLatency, stall);
				stall -= processorStall;

				for (int j = 0; j < interferenceProcessorNumber; j++) {
					if (j == i) {
						interferingProcessorItems.get(j).add(new SolutionItem(processorStall, true));
					} else {
						interferingProcessorItems.get(j)
								.add(new SolutionItem(processorStall, false, true, false, false));
					}
				}
			}
		}

		return totalStall;
	}

}
