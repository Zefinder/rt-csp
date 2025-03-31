package memguard.solver.memguardUU2C1B;

import java.util.ArrayList;

import com.google.ortools.sat.BoolVar;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;

import memguard.logic.MemguardRWSystem;
import memguard.logic.MemguardSystem;
import memguard.logic.ReadWriteTask;
import memguard.logic.Task;
import memguard.solver.MemguardSolver;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionItem;
import memguard.solver.solution.SolutionStatus;

public class MemguardUU2C1BOpStall extends MemguardSolver {

	/**
	 * This will compute the worst-case response time of a task in a system. This
	 * system contains m processors and only one task (to simplify the process).
	 * 
	 * @param system the MemGuard's system with one task
	 */
	public MemguardUU2C1BOpStall(MemguardSystem system) {
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

		if (verbose) {
			System.out.println("Extracting data from system...");
		}
		// Is a read write system (constructor)
		MemguardRWSystem rwsystem = (MemguardRWSystem) system;
		int processorNumber = system.getProcessorNumber();
		int budget = rwsystem.getBudget(0);
		int readLatency = rwsystem.getReadLatency();
		int writeLatency = rwsystem.getWriteLatency();
		int regulationPeriod = rwsystem.getRegulationPeriod() - writeLatency + 1;
		int interferenceProcessorNumber = processorNumber - 1;
		int remainingBudget = regulationPeriod - budget;
		int maxInterferenceAccess = Math.ceilDiv(remainingBudget, interferenceProcessorNumber * writeLatency);

		// TODO Change cost to access
		ReadWriteTask rwtask = (ReadWriteTask) task;
		int readCost = rwtask.getReadCost();
		int writeCost = rwtask.getWriteCost();
		int computationCost = rwtask.getComputationCost();
		int readAccessNumber = readCost / readLatency;
		int writeAccessNumber = writeCost / writeLatency;
		int maxReadPerPeriod = Math.floorDiv(budget, readLatency);
		int maxWritePerPeriod = Math.floorDiv(budget, writeLatency);
		int maxPeriodNumber = Math.ceilDiv(readCost + writeCost + computationCost, budget - writeLatency + 1);
//		maxPeriodNumber = 70;

		if (verbose) {
			System.out.println("Important system variables:");
			System.out.println("\tProcessor number: " + processorNumber);
			System.out.println("\tBudget: " + budget);
			System.out.println("\tRegulation Period: " + regulationPeriod);
			System.out.println("\tRead latency: " + readLatency);
			System.out.println("\tWrite latency: " + writeLatency);
			System.out.println("\tNumber of interfering cores: " + interferenceProcessorNumber);
			System.out.println("\tRemaining budget (P - Q): " + remainingBudget);
			System.out.println("\tMaximum interference memory access: " + maxInterferenceAccess);

			System.out.println("Important task variables:");
			System.out.println("\tRead cost: " + readCost);
			System.out.println("\tWrite cost: " + writeCost);
			System.out.println("\tComputation cost: " + computationCost);
			System.out.println("\tRead access number: " + readAccessNumber);
			System.out.println("\tWrite access number: " + writeAccessNumber);
			System.out.println("\tMaximum period number: " + maxPeriodNumber);
			System.out.println("\tMaximum read per period: " + maxReadPerPeriod);
			System.out.println("\tMaximum write per period: " + maxWritePerPeriod);
		}

		if (verbose) {
			System.out.println("Creating model...");
		}
		// Build model
		CpModel model = new CpModel();

		// Create integer variables
		IntVar[] readAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] writeAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] remainingBudgetVariables = new IntVar[maxPeriodNumber];
		IntVar[] computationAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] voidAccessVariables = new IntVar[maxPeriodNumber];
		IntVar[] stallVariables = new IntVar[maxPeriodNumber];
		IntVar[] readStallVariables = new IntVar[maxPeriodNumber];
		IntVar[] writeStallVariables = new IntVar[maxPeriodNumber];

		// Create boolean variables
		BoolVar[] moreAccessThanPossibleStallVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] voidAccessGreaterZeroVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] budgetFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] budgetNotEnoughReadVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] budgetNotEnoughWriteVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] readFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] writeFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] readInPeriodVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] writeInPeriodVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] computationFinishedVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] readStallGreaterZeroVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] writeStallGreaterZeroVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] maxStallVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] periodFilledNoVoidVariables = new BoolVar[maxPeriodNumber];
		BoolVar[] periodFilledNoOperationStallVariables = new BoolVar[maxPeriodNumber];

		// Define integer variables
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Read access per period
			IntVar readAccesses = model.newIntVar(0, maxReadPerPeriod, "r_%d".formatted(i + 1));
			readAccessVariables[i] = readAccesses;

			// Write access per period
			IntVar writeAccesses = model.newIntVar(0, maxWritePerPeriod, "w_%d".formatted(i + 1));
			writeAccessVariables[i] = writeAccesses;

			// Remaining budget per period
			IntVar remainingBudgetV = model.newIntVar(0, budget, "rem_budget_%d".formatted(i + 1));
			remainingBudgetVariables[i] = remainingBudgetV;

			// Computation unit per period
			IntVar computationAccesses = model.newIntVar(0, regulationPeriod, "e_%d".formatted(i + 1));
			computationAccessVariables[i] = computationAccesses;

			// No accesses unit per period (mandatory to complete the period when nothing is
			// left)
			IntVar voidAccesses = model.newIntVar(0, regulationPeriod, "v_%d".formatted(i + 1));
			voidAccessVariables[i] = voidAccesses;

			// Stall value per period
			IntVar stall = model.newIntVar(0, remainingBudget, "stall_%d".formatted(i + 1));
			stallVariables[i] = stall;

			// Read stall per period
			IntVar readStall = model.newIntVar(0, readLatency - 1, "read_stall_%d".formatted(i + 1));
			readStallVariables[i] = readStall;

			// Write stall per period
			IntVar writeStall = model.newIntVar(0, writeLatency - 1, "write_stall_%d".formatted(i + 1));
			writeStallVariables[i] = writeStall;
		}

		// Define boolean variables (note that must have both yes and no constraints per
		// variable)
		for (int i = 0; i < maxPeriodNumber; i++) {
			// Check if more accesses than possible interference
			BoolVar moreAccessThanPossibleStall = model.newBoolVar("more_access_%d".formatted(i + 1));
			LinearExpr memoryAccessesExpr = LinearExpr.newBuilder().add(readAccessVariables[i])
					.add(writeAccessVariables[i]).build();
			model.addGreaterOrEqual(memoryAccessesExpr, maxInterferenceAccess)
					.onlyEnforceIf(moreAccessThanPossibleStall);
			model.addLessThan(memoryAccessesExpr, maxInterferenceAccess)
					.onlyEnforceIf(moreAccessThanPossibleStall.not());
			moreAccessThanPossibleStallVariables[i] = moreAccessThanPossibleStall;

			// Void values
			BoolVar voidAccessGreaterZero = model.newBoolVar("v_%d_gt0".formatted(i + 1));
			model.addDifferent(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero);
			model.addEquality(voidAccessVariables[i], 0).onlyEnforceIf(voidAccessGreaterZero.not());
			voidAccessGreaterZeroVariables[i] = voidAccessGreaterZero;

			// Budget finished in the period
			BoolVar budgetFinished = model.newBoolVar("Q_%d_finished".formatted(i + 1));
			LinearExpr budgetUsedExpr = LinearExpr.newBuilder().addTerm(readAccessVariables[i], readLatency)
					.addTerm(writeAccessVariables[i], writeLatency).build();
			model.addEquality(budgetUsedExpr, budget).onlyEnforceIf(budgetFinished);
			model.addDifferent(budgetUsedExpr, budget).onlyEnforceIf(budgetFinished.not());
			budgetFinishedVariables[i] = budgetFinished;

			// Budget not enough for a read
			BoolVar budgetNotEnoughRead = model.newBoolVar("budget_not_enough_read_%d".formatted(i + 1));
			model.addLessThan(remainingBudgetVariables[i], readLatency).onlyEnforceIf(budgetNotEnoughRead);
			model.addGreaterOrEqual(remainingBudgetVariables[i], readLatency).onlyEnforceIf(budgetNotEnoughRead.not());
			budgetNotEnoughReadVariables[i] = budgetNotEnoughRead;

			// Budget not enough for a write
			BoolVar budgetNotEnoughWrite = model.newBoolVar("budget_not_enough_write_%d".formatted(i + 1));
			model.addLessThan(remainingBudgetVariables[i], writeLatency).onlyEnforceIf(budgetNotEnoughWrite);
			model.addGreaterOrEqual(remainingBudgetVariables[i], writeLatency)
					.onlyEnforceIf(budgetNotEnoughWrite.not());
			budgetNotEnoughWriteVariables[i] = budgetNotEnoughWrite;

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

			// Read in period
			BoolVar readInPeriod = model.newBoolVar("r_present_%d".formatted(i + 1));
			model.addDifferent(readAccessVariables[i], 0).onlyEnforceIf(readInPeriod);
			model.addEquality(readAccessVariables[i], 0).onlyEnforceIf(readInPeriod.not());
			readInPeriodVariables[i] = readInPeriod;

			// Write in period
			BoolVar writeInPeriod = model.newBoolVar("w_present_%d".formatted(i + 1));
			model.addDifferent(writeAccessVariables[i], 0).onlyEnforceIf(writeInPeriod);
			model.addEquality(writeAccessVariables[i], 0).onlyEnforceIf(writeInPeriod.not());
			writeInPeriodVariables[i] = writeInPeriod;

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

			// Read stall greater than 0
			BoolVar readStallGreaterZero = model.newBoolVar("read_stall_%d".formatted(i + 1));
			if (i != maxPeriodNumber - 1) {
				model.addDifferent(readStallVariables[i], 0).onlyEnforceIf(readStallGreaterZero);
				model.addEquality(readStallVariables[i], 0).onlyEnforceIf(readStallGreaterZero.not());
			} else {
				model.addBoolAnd(new Literal[] { readStallGreaterZero.not() });
			}
			readStallGreaterZeroVariables[i] = readStallGreaterZero;

			// Write stall greater than 0
			BoolVar writeStallGreaterZero = model.newBoolVar("write_stall_%d".formatted(i + 1));
			if (i != maxPeriodNumber - 1) {
				model.addDifferent(writeStallVariables[i], 0).onlyEnforceIf(writeStallGreaterZero);
				model.addEquality(writeStallVariables[i], 0).onlyEnforceIf(writeStallGreaterZero.not());
			} else {
				model.addBoolAnd(new Literal[] { writeStallGreaterZero.not() });
			}
			writeStallGreaterZeroVariables[i] = writeStallGreaterZero;

			// Max stall required
			BoolVar maxStallReq = model.newBoolVar("max_stall_%d".formatted(i + 1));
			model.addBoolOr(new Literal[] { readStallGreaterZero, writeStallGreaterZero, budgetFinished,
					moreAccessThanPossibleStall }).onlyEnforceIf(maxStallReq);
			model.addBoolAnd(new Literal[] { readStallGreaterZero.not(), writeStallGreaterZero.not(),
					budgetFinished.not(), moreAccessThanPossibleStall.not() }).onlyEnforceIf(maxStallReq.not());
			maxStallVariables[i] = maxStallReq;

			// Check if period is filled without void
			BoolVar periodFilledNoVoid = model.newBoolVar("period_filled_no_void_%d".formatted(i + 1));
			LinearExpr accessesNoVoidExpr = LinearExpr.newBuilder().addTerm(readAccessVariables[i], readLatency)
					.addTerm(writeAccessVariables[i], writeLatency).add(computationAccessVariables[i])
					.add(stallVariables[i]).add(readStallVariables[i]).add(writeStallVariables[i]).build();
			model.addEquality(accessesNoVoidExpr, regulationPeriod).onlyEnforceIf(periodFilledNoVoid);
			model.addDifferent(accessesNoVoidExpr, regulationPeriod).onlyEnforceIf(periodFilledNoVoid.not());
			periodFilledNoVoidVariables[i] = periodFilledNoVoid;

			// Check if period is filled without operation stall
			BoolVar periodFilledNoOperationStall = model.newBoolVar("period_filled_no_op_stall_%d".formatted(i + 1));
			LinearExpr accessesNoStallExpr = LinearExpr.newBuilder().addTerm(readAccessVariables[i], readLatency)
					.addTerm(writeAccessVariables[i], writeLatency).add(computationAccessVariables[i])
					.add(stallVariables[i]).build();
			model.addEquality(accessesNoStallExpr, regulationPeriod).onlyEnforceIf(periodFilledNoOperationStall);
			model.addDifferent(accessesNoStallExpr, regulationPeriod).onlyEnforceIf(periodFilledNoOperationStall.not());
			periodFilledNoOperationStallVariables[i] = periodFilledNoOperationStall;
		}

		// Constraints
		// Max possible read access
		LinearExprBuilder maxReadExprBuilder = LinearExpr.newBuilder();
		for (int i = 0; i < maxPeriodNumber; i++) {
			maxReadExprBuilder.addTerm(readAccessVariables[i], readLatency).build();
		}
		model.addEquality(maxReadExprBuilder.build(), readCost);

		// Max possible write access
		LinearExprBuilder maxWriteExprBuilder = LinearExpr.newBuilder();
		for (int i = 0; i < maxPeriodNumber; i++) {
			maxWriteExprBuilder.addTerm(writeAccessVariables[i], writeLatency).build();
		}
		model.addEquality(maxWriteExprBuilder.build(), writeCost);

		// Max possible computation
		LinearExpr maxComputationExpr = LinearExpr.newBuilder().addSum(computationAccessVariables).build();
		model.addEquality(maxComputationExpr, computationCost);

		// Constraint budget per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr memoryAccessExpr = LinearExpr.newBuilder().addTerm(readAccessVariables[i], readLatency)
					.addTerm(writeAccessVariables[i], writeLatency).build();
			model.addLessOrEqual(memoryAccessExpr, budget);
		}

		// Compute remaining budget per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr remainingBudgetExpr = LinearExpr.newBuilder().add(budget)
					.addTerm(readAccessVariables[i], -readLatency).addTerm(writeAccessVariables[i], -writeLatency)
					.build();
			model.addEquality(remainingBudgetVariables[i], remainingBudgetExpr); // #32
		}

		// Compute stall per period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExpr totalAccessStallExpr = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], interferenceProcessorNumber * writeLatency)
					.addTerm(writeAccessVariables[i], interferenceProcessorNumber * writeLatency).build();

			// If all budget is used and there are still resources or more accesses than
			// possible interference, the stall is P - Q
			model.addEquality(stallVariables[i], remainingBudget).onlyEnforceIf(maxStallVariables[i]);
			// Otherwise the stall is (m - 1) * m^i
			model.addEquality(stallVariables[i], totalAccessStallExpr).onlyEnforceIf(maxStallVariables[i].not());
		}

		// Enable read stall only when next one is a read, there is no write stall and
		// the period is not finished and the remaining budget is smaller than the read
		// latency (equivalence)
		for (int i = 0; i < maxPeriodNumber; i++) {
			if (i < maxPeriodNumber - 1) {
				model.addBoolAnd(new Literal[] { readInPeriodVariables[i + 1], writeStallGreaterZeroVariables[i].not(),
						periodFilledNoOperationStallVariables[i].not(), budgetNotEnoughReadVariables[i] })
						.onlyEnforceIf(readStallGreaterZeroVariables[i]);
				model.addBoolAnd(new Literal[] { readStallGreaterZeroVariables[i] })
						.onlyEnforceIf(new Literal[] { readInPeriodVariables[i + 1],
								writeStallGreaterZeroVariables[i].not(), periodFilledNoOperationStallVariables[i].not(),
								budgetNotEnoughReadVariables[i] });
			} else {
				model.addEquality(readStallVariables[i], 0);
			}
		}

		// Enable write stall only when next one is a write, there is no read stall and
		// the period is not finished and the remaining budget is smaller than the write
		// latency (equivalence)
		for (int i = 0; i < maxPeriodNumber; i++) {
			if (i < maxPeriodNumber - 1) {
				model.addBoolAnd(new Literal[] { writeInPeriodVariables[i + 1], readStallGreaterZeroVariables[i].not(),
						periodFilledNoOperationStallVariables[i].not(), budgetNotEnoughWriteVariables[i] })
						.onlyEnforceIf(writeStallGreaterZeroVariables[i]);
				model.addBoolAnd(new Literal[] { writeStallGreaterZeroVariables[i] })
						.onlyEnforceIf(new Literal[] { writeInPeriodVariables[i + 1],
								readStallGreaterZeroVariables[i].not(), periodFilledNoOperationStallVariables[i].not(),
								budgetNotEnoughWriteVariables[i] });
			} else {
				model.addEquality(writeStallVariables[i], 0);
			}
		}

		// Enable void only when nothing is left and period is not full (equivalence)
		for (int i = 0; i < maxPeriodNumber; i++) {
			model.addBoolAnd(new Literal[] { readFinishedVariables[i], writeFinishedVariables[i],
					computationFinishedVariables[i], periodFilledNoVoidVariables[i].not() })
					.onlyEnforceIf(voidAccessGreaterZeroVariables[i]);
			model.addBoolAnd(new Literal[] { voidAccessGreaterZeroVariables[i] })
					.onlyEnforceIf(new Literal[] { readFinishedVariables[i], writeFinishedVariables[i],
							computationFinishedVariables[i], periodFilledNoVoidVariables[i].not() });
		}

		// Constraint a period
		for (int i = 0; i < maxPeriodNumber; i++) {
			LinearExprBuilder usedResourcesExprBuilder = LinearExpr.newBuilder()
					.addTerm(readAccessVariables[i], readLatency).addTerm(writeAccessVariables[i], writeLatency)
					.add(computationAccessVariables[i]).add(stallVariables[i]).add(readStallVariables[i])
					.add(writeStallVariables[i]).add(voidAccessVariables[i]);

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
			LinearExpr stallNowExpr = LinearExpr.newBuilder().add(stallVariables[i]).add(readStallVariables[i])
					.add(writeStallVariables[i]).build();
			LinearExpr stallAfterExpr = LinearExpr.newBuilder().add(stallVariables[i + 1])
					.add(readStallVariables[i + 1]).add(writeStallVariables[i + 1]).build();

			model.addGreaterOrEqual(stallNowExpr, stallAfterExpr);
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
		LinearExpr maximizationExpr = LinearExpr.newBuilder().addSum(stallVariables).addSum(readStallVariables)
				.addSum(writeStallVariables).build();
		model.maximize(maximizationExpr);

		// Solve model
		if (verbose) {
			System.out.println("Launching solver...");
		}
		CpSolver solver = new CpSolver();
//		solver.getParameters().setLogToStdout(true);
//		solver.getParameters().setLogToResponse(true);
//		solver.getParameters().setLogSubsolverStatistics(true);
//		solver.getParameters().setLogSearchProgress(true);
		CpSolverStatus status = solver.solve(model);

		// If solution is optimal, say it!
		Solution solution;
		if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
			if (verbose) {
				System.out.println("Solution found: " + status);
				System.out.println("Generating solution...");
			}

			// Write solution
			ArrayList<SolutionItem> mainProcessorItems = new ArrayList<SolutionItem>();
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems = new ArrayList<ArrayList<SolutionItem>>();
			for (int i = 0; i < interferenceProcessorNumber; i++) {
				interferingProcessorItems.add(new ArrayList<SolutionItem>());
			}

			long totalStall = solver.value(maximizationExpr);
			for (int i = 0; i < maxPeriodNumber; i++) {
				int readAccesses = (int) solver.value(readAccessVariables[i]);
				int writeAccesses = (int) solver.value(writeAccessVariables[i]);
				int remainingBudgetV = (int) solver.value(remainingBudgetVariables[i]);
				int computationAccesses = (int) solver.value(computationAccessVariables[i]);
				int stall = (int) solver.value(stallVariables[i]);
				int readStall = (int) solver.value(readStallVariables[i]);
				int writeStall = (int) solver.value(writeStallVariables[i]);
				int voidAccesses = (int) solver.value(voidAccessVariables[i]);

				boolean moreAccessThanPossibleStall = solver.booleanValue(moreAccessThanPossibleStallVariables[i]);
				boolean voidAccessGreaterZero = solver.booleanValue(voidAccessGreaterZeroVariables[i]);
				boolean budgetFinished = solver.booleanValue(budgetFinishedVariables[i]);
				boolean budgetNotEnoughRead = solver.booleanValue(budgetNotEnoughReadVariables[i]);
				boolean budgetNotEnoughWrite = solver.booleanValue(budgetNotEnoughWriteVariables[i]);
				boolean readFinished = solver.booleanValue(readFinishedVariables[i]);
				boolean writeFinished = solver.booleanValue(writeFinishedVariables[i]);
				boolean readInPeriod = solver.booleanValue(readInPeriodVariables[i]);
				boolean writeInPeriod = solver.booleanValue(writeInPeriodVariables[i]);
				boolean computationFinished = solver.booleanValue(computationFinishedVariables[i]);
				boolean readStallGreaterZero = solver.booleanValue(readStallGreaterZeroVariables[i]);
				boolean writeStallGreaterZero = solver.booleanValue(writeStallGreaterZeroVariables[i]);
				boolean maxStall = solver.booleanValue(maxStallVariables[i]);
				boolean periodFilledNoVoid = solver.booleanValue(periodFilledNoVoidVariables[i]);
				boolean periodFilledNoOperationStall = solver.booleanValue(periodFilledNoOperationStallVariables[i]);

				int unitsPerPeriod = readAccesses * readLatency + writeAccesses * writeLatency + computationAccesses
						+ stall + readStall + writeStall + voidAccesses;
				if (unitsPerPeriod != regulationPeriod) {
					System.err.println("Period %d has a wrong nomber of units in it (expected %d but got %d)"
							.formatted(i + 1, regulationPeriod, unitsPerPeriod));
				}

				if (voidAccesses == regulationPeriod) {
					if (verbose) {
						System.out.println(
								"Period %d onwards only has void accesses (CPU idle), skip...".formatted(i + 1));
					}
					break;
				}

				if (verbose) {
					System.out.println();
					System.out.println("Period %d".formatted(i + 1));
					System.out.println("Read accesses: %d".formatted(readAccesses));
					System.out.println("Write accesses: %d".formatted(writeAccesses));
					System.out.println("Remaining budget: %d".formatted(remainingBudgetV));
					System.out.println("Computation performed: %d".formatted(computationAccesses));
					System.out.println("Inter-processor stall: %d".formatted(stall));
					System.out.println("Read stall: %d".formatted(readStall));
					System.out.println("Write stall: %d".formatted(writeStall));
					System.out.println("Void accesses: %d".formatted(voidAccesses));

					System.out.println("More accesses than interference? %b".formatted(moreAccessThanPossibleStall));
					System.out.println("Void access in period? %b".formatted(voidAccessGreaterZero));
					System.out.println("Budget finished? %b".formatted(budgetFinished));
					System.out.println("Budget not enough for read? %b".formatted(budgetNotEnoughRead));
					System.out.println("Budget not enough for write? %b".formatted(budgetNotEnoughWrite));
					System.out.println("Read finished? %b".formatted(readFinished));
					System.out.println("Write finished? %b".formatted(writeFinished));
					System.out.println("Read in period? %b".formatted(readInPeriod));
					System.out.println("Write in period? %b".formatted(writeInPeriod));
					System.out.println("Computation finished? %b".formatted(computationFinished));
					System.out.println("Read stall in period? %b".formatted(readStallGreaterZero));
					System.out.println("Write stall in period? %b".formatted(writeStallGreaterZero));
					System.out.println("Max stall reached? %b".formatted(maxStall));
					System.out.println("Period filled with no void? %b".formatted(periodFilledNoVoid));
					System.out.println(
							"Period filled with no operation stall? %b".formatted(periodFilledNoOperationStall));
				}

				boolean lastPeriodReadStall = i == 0 ? false
						: solver.booleanValue(readStallGreaterZeroVariables[i - 1]);
				boolean lastPeriodWriteStall = i == 0 ? false
						: solver.booleanValue(writeStallGreaterZeroVariables[i - 1]);

				// Add an item with 0 length to begin
				addStart(interferenceProcessorNumber, mainProcessorItems, interferingProcessorItems);

				// If read stall before, start with a read
				if (lastPeriodReadStall) {
					stall = addRead(interferenceProcessorNumber, stall, readLatency, writeLatency, mainProcessorItems,
							interferingProcessorItems);
					readAccesses--;
				}

				// If write stall before, start with a write
				if (lastPeriodWriteStall) {
					stall = addWrite(interferenceProcessorNumber, stall, writeLatency, mainProcessorItems,
							interferingProcessorItems);
					writeAccesses--;
				}

				// Add read (and stall if any)
				for (int r = 0; r < readAccesses; r++) {
					stall = addRead(interferenceProcessorNumber, stall, readLatency, writeLatency, mainProcessorItems,
							interferingProcessorItems);
				}

				// Add write (and stall if any)
				for (int w = 0; w < writeAccesses; w++) {
					stall = addWrite(interferenceProcessorNumber, stall, writeLatency, mainProcessorItems,
							interferingProcessorItems);
				}

				// Add computation
				addComputation(interferenceProcessorNumber, computationAccesses, mainProcessorItems,
						interferingProcessorItems);

				// Add remaining stall if any
				addStall(interferenceProcessorNumber, stall, mainProcessorItems, interferingProcessorItems);

				// Add read stall if any
				addStall(interferenceProcessorNumber, readStall, mainProcessorItems, interferingProcessorItems);

				// Add write stall if any
				addStall(interferenceProcessorNumber, writeStall, mainProcessorItems, interferingProcessorItems);

				// Add void access
				addVoid(interferenceProcessorNumber, voidAccesses, mainProcessorItems, interferingProcessorItems);

				// Add an empty item to end
				addEnd(interferenceProcessorNumber, mainProcessorItems, interferingProcessorItems);

				// Add write latency - 1 to complete the period
				addStall(interferenceProcessorNumber, writeLatency - 1, mainProcessorItems, interferingProcessorItems);
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
			if (verbose) {
				System.out.println("Solution successfully created!");
			}
		} else {
			if (verbose) {
				System.err.println("Something is wrong with the model: " + status);
			}
			solution = new Solution(SolutionStatus.UNFEASIBLE);
		}

		if (verbose) {
			System.out.println("Problem solved in %.4f seconds".formatted(solver.wallTime()));
		}
		solution.setSolvingTime(solver.wallTime());

		return solution;
	}

	private void addStart(int interferenceProcessorNumber, ArrayList<SolutionItem> mainProcessorItems,
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		mainProcessorItems.add(new SolutionItem(0, true, true, false));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(0, true, true, false));
		}
	}

	private void addEnd(int interferenceProcessorNumber, ArrayList<SolutionItem> mainProcessorItems,
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		mainProcessorItems.add(new SolutionItem(0, true, false, true));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(0, true, false, true));
		}
	}

	private int addRead(int interferenceProcessorNumber, int stall, int readLatency, int writeLatency,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		stall -= addInterProcessorStall(interferenceProcessorNumber, stall, writeLatency, mainProcessorItems,
				interferingProcessorItems);
		mainProcessorItems.add(new SolutionItem(readLatency, true));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(readLatency, false, true, false, false));
		}

		return stall;
	}

	private int addWrite(int interferenceProcessorNumber, int stall, int writeLatency,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		stall -= addInterProcessorStall(interferenceProcessorNumber, stall, writeLatency, mainProcessorItems,
				interferingProcessorItems);
		mainProcessorItems.add(new SolutionItem(writeLatency, true));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(writeLatency, false, true, false, false));
		}

		return stall;
	}

	private void addComputation(int interferenceProcessorNumber, int computationAccesses,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		mainProcessorItems.add(new SolutionItem(computationAccesses, false));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(computationAccesses, true, false, false));
		}
	}

	// Returns the number of stall unit put
	private int addInterProcessorStall(int interferenceProcessorNumber, int remainingStall, int writeLatency,
			ArrayList<SolutionItem> mainProcessorItems, ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		int totalStall = 0;
		if (remainingStall != 0) {
			int stall = Math.min(interferenceProcessorNumber * writeLatency, remainingStall);
			totalStall = stall;

			// Fill main processor stall
			mainProcessorItems.add(new SolutionItem(stall, true, false, false));
			for (int i = 0; i < interferenceProcessorNumber; i++) {
				// If no stall remaining, exit
				if (stall == 0) {
					break;
				}

				// Else use one stall unit
				int placedStall = Math.min(writeLatency, stall);
				stall -= placedStall;
				for (int j = 0; j < interferenceProcessorNumber; j++) {
					if (j == i) {
						interferingProcessorItems.get(j).add(new SolutionItem(placedStall, true));
					} else {
						interferingProcessorItems.get(j).add(new SolutionItem(placedStall, false, true, false, false));
					}
				}
			}
		}

		return totalStall;
	}

	private void addStall(int interferenceProcessorNumber, int stallNumber, ArrayList<SolutionItem> mainProcessorItems,
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		if (stallNumber > 0) {
			mainProcessorItems.add(new SolutionItem(stallNumber, false, false, true, false, false));
			for (int j = 0; j < interferenceProcessorNumber; j++) {
				interferingProcessorItems.get(j).add(new SolutionItem(stallNumber, true, false, false));
			}
		}
	}

	private void addVoid(int interferenceProcessorNumber, int voidAccesses, ArrayList<SolutionItem> mainProcessorItems,
			ArrayList<ArrayList<SolutionItem>> interferingProcessorItems) {
		mainProcessorItems.add(new SolutionItem(voidAccesses, true, false, false));
		for (int j = 0; j < interferenceProcessorNumber; j++) {
			interferingProcessorItems.get(j).add(new SolutionItem(voidAccesses, true, false, false));
		}
	}

}
