package memguard.main;

import java.io.File;
import java.io.IOException;

import memguard.logic.MemguardRWSystem;
import memguard.logic.ReadWriteTask;
import memguard.solution.Solution;
import memguard.solution.SolutionStatus;
import memguard.solver.MemguardSolver;
import memguard.solver.Solver;
import memguard.solver.memguardUU2C1B.MemguardUU2C1BOpStall;
import memguard.solver.memguardUU2C1B.MemguardUU2C1BRconWreg;

public class MemguardUU2C1BGenerator {

	// TODO Change / to File.separator
	private static final String READ_REGULATION_WRITE_REGULATION = "./Rreg_Wreg";
	private static final String READ_CONTENTION_WRITE_REGULATION = "./Rcon_Wreg";
	private static final String READ_CONTENTION_WRITE_CONTENTION = "./Rcon_Wcon";
	private static final String BASE_DIRECTORY_FORMAT = "%s/m%d/LR%d/LW%d/";
	private static final String SPECIFIC_DIRECTORY_FORMAT = "%s/P%d/Q%d/";
	private static final String FILE_FORMAT = SPECIFIC_DIRECTORY_FORMAT + "r%d_w%d_e%d.png";

	public static void main(String[] args) throws IOException {
		// Init solver
		Solver.init();
		
		// Set system properties
		int processorNumber = 3;
		int readLatency = 1;
		int writeLatency = 2;

		// Create directories base directories
		String rRegWRegBaseDirPath = BASE_DIRECTORY_FORMAT.formatted(READ_REGULATION_WRITE_REGULATION, processorNumber,
				readLatency, writeLatency);
		File dir = new File(rRegWRegBaseDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String rConWRegBaseDirPath = BASE_DIRECTORY_FORMAT.formatted(READ_CONTENTION_WRITE_REGULATION, processorNumber,
				readLatency, writeLatency);
		dir = new File(rConWRegBaseDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		String rConWConBaseDirPath = BASE_DIRECTORY_FORMAT.formatted(READ_CONTENTION_WRITE_CONTENTION, processorNumber,
				readLatency, writeLatency);
		dir = new File(rConWConBaseDirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// Set task properties
		int deadline = 100;
		int minRegulationPeriod = 30;
		int maxRegulationPeriod = 30;
		int minBudget = 7;
		int maxBudget = 29;
		int minReadAccess = 50;
		int maxReadAccess = 50;
		int minWriteAccess = 50;
		int maxWriteAccess = 50;
		int minComputationAccess = 0;
		int maxComputationAccess = 200;
		int computationStep = 20;

		// Create specific directories
		for (int regulationPeriod = minRegulationPeriod; regulationPeriod <= maxRegulationPeriod; regulationPeriod++) {
			for (int budget = minBudget; budget <= Math.min(regulationPeriod - 1, maxBudget); budget++) {
				float writeToRead = (float) writeLatency / readLatency;
				float bandwidth = (float) budget / regulationPeriod;
				float readBandwidth = 1f / (1 + (processorNumber - 1) * writeToRead);
				float writeBandwidth = 1f / processorNumber;
				
				String dirString;
				if (bandwidth <= readBandwidth) {
					dirString = rRegWRegBaseDirPath;
				} else {
					if (bandwidth <= writeBandwidth) {
						dirString = rConWRegBaseDirPath;
					} else {
						dirString = rConWConBaseDirPath;
					}
				}
				
				dir = new File(SPECIFIC_DIRECTORY_FORMAT.formatted(dirString, regulationPeriod, budget));
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
		}

		int systemCount = 0;
		System.out.println("Starting generation");
		for (int regulationPeriod = minRegulationPeriod; regulationPeriod <= maxRegulationPeriod; regulationPeriod++) {
			for (int budget = minBudget; budget <= Math.min(regulationPeriod - 1, maxBudget); budget++) {
				for (int readAccess = minReadAccess; readAccess <= maxReadAccess; readAccess++) {
					for (int writeAccess = minWriteAccess; writeAccess <= maxWriteAccess; writeAccess++) {
						for (int computationAccess = minComputationAccess; computationAccess <= maxComputationAccess; computationAccess += computationStep) {
							float writeToRead = (float) writeLatency / readLatency;
							float bandwidth = (float) budget / regulationPeriod;
							float readBandwidth = 1f / (1 + (processorNumber - 1) * writeToRead);
							float writeBandwidth = 1f / processorNumber;

							int[] budgets = { budget };
							int readCost = readAccess * readLatency;
							int writeCost = writeAccess * writeLatency;
							int computationCost = computationAccess;
							
							ReadWriteTask task = new ReadWriteTask(readCost, writeCost, computationCost, deadline);
							MemguardRWSystem system = new MemguardRWSystem(processorNumber, readLatency, writeLatency,
									regulationPeriod, budgets, task);

							String dirString;
							MemguardSolver solver;
							if (bandwidth <= readBandwidth) {
								dirString = rRegWRegBaseDirPath;
								solver = new MemguardUU2C1BOpStall(system);
							} else {
								if (bandwidth <= writeBandwidth) {
									dirString = rConWRegBaseDirPath;
									solver = new MemguardUU2C1BRconWreg(system);
								} else {
									dirString = rConWConBaseDirPath;
									solver = new MemguardUU2C1BOpStall(system);
								}
							}
							
							Solution solution = solver.solve(false);
							if (solution.getStatus() != SolutionStatus.UNFEASIBLE) {


								if (++systemCount % 1000 == 0) {
									System.out.println("Number of systems solved: %d".formatted(systemCount));
								}
								
								solution.saveSolution(
										new File(FILE_FORMAT.formatted(dirString, regulationPeriod, budgets[0],
												readCost / readLatency, writeCost / writeLatency, computationAccess)));
							} else {
								System.out.println(
										"System infeasible for: m = %d, LR = %d, LW = %d, P = %d, Q = %d, r = %d, w = %d, e = %d"
												.formatted(processorNumber, readLatency, writeLatency, regulationPeriod,
														budgets[0], readCost / readLatency, writeCost / writeLatency,
														computationAccess));
							}
						}
					}
				}
			}
		}
	}

}
