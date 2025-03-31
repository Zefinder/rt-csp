package memguard.main;

import java.io.File;
import java.io.IOException;

import memguard.logic.MemguardRWSystem;
import memguard.logic.ReadWriteTask;
import memguard.solver.memguardUU2C1B.MemguardUU2C1B;
import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionStatus;

public class MemguardUU2C1BGenerator {

	// TODO Change / to File.separator
	private static final String READ_REGULATION_WRITE_REGULATION = "./Rreg_Wreg";
	private static final String READ_CONTENTION_WRITE_REGULATION = "./Rcon_Wreg";
	private static final String READ_CONTENTION_WRITE_CONTENTION = "./Rcon_Wcon";
	private static final String BASE_DIRECTORY_FORMAT = "%s/m%d/LR%d/LW%d/";
	private static final String SPECIFIC_DIRECTORY_FORMAT = "%s/P%d/Q%d/";
	private static final String FILE_FORMAT = SPECIFIC_DIRECTORY_FORMAT + "r%d_w%d_e%d.png";

	public static void main(String[] args) throws IOException {
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
		int minRegulationPeriod = 10;
		int maxRegulationPeriod = 10;
		int minBudget = 3;
		int maxBudget = 3;
		int minReadAccess = 1;
		int maxReadAccess = 2;
		int minWriteAccess = 1;
		int maxWriteAccess = 2;
		int minComputationAccess = 0;
		int maxComputationAccess = 0;
		int computationStep = 5;

		// Create specific directories
		for (int regulationPeriod = minRegulationPeriod; regulationPeriod <= maxRegulationPeriod; regulationPeriod++) {
			for (int budget = minBudget; budget <= maxBudget; budget++) {
				dir = new File(SPECIFIC_DIRECTORY_FORMAT.formatted(rRegWRegBaseDirPath, regulationPeriod, budget));
				if (!dir.exists()) {
					dir.mkdirs();
				}

				dir = new File(SPECIFIC_DIRECTORY_FORMAT.formatted(rConWRegBaseDirPath, regulationPeriod, budget));
				if (!dir.exists()) {
					dir.mkdirs();
				}

				dir = new File(SPECIFIC_DIRECTORY_FORMAT.formatted(rConWConBaseDirPath, regulationPeriod, budget));
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
		}

		for (int regulationPeriod = minRegulationPeriod; regulationPeriod <= maxRegulationPeriod; regulationPeriod++) {
			for (int budget = minBudget; budget <= maxBudget; budget++) {
				for (int readAccess = minReadAccess; readAccess <= maxReadAccess; readAccess++) {
					for (int writeAccess = minWriteAccess; writeAccess <= maxWriteAccess; writeAccess++) {
						for (int computationAccess = minComputationAccess; computationAccess <= maxComputationAccess; computationAccess += computationStep) {
							int[] budgets = { budget };
							int readCost = readAccess * readLatency;
							int writeCost = writeAccess * writeLatency;
							int computationCost = computationAccess;

							ReadWriteTask task = new ReadWriteTask(readCost, writeCost, computationCost, deadline);
							MemguardRWSystem system = new MemguardRWSystem(processorNumber, readLatency, writeLatency,
									regulationPeriod, budgets, task);
							MemguardUU2C1B solver = new MemguardUU2C1B(system);
							Solution solution = solver.solve(false);

							if (solution.getStatus() != SolutionStatus.UNFEASIBLE) {
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

								System.out.println(FILE_FORMAT.formatted(dirString, regulationPeriod, budgets[0],
										readCost / readLatency, writeCost / writeLatency, computationAccess));
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
