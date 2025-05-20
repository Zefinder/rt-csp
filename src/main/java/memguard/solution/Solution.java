package memguard.solution;

import java.io.File;
import java.io.IOException;

import memguard.frame.SolutionFrame;
import memguard.solution.items.SolutionItem;

public class Solution {

	private SolutionStatus status;
	private SolutionItem[][] solutionItems;
	private int solutionLength;
	private double solvingTime;

	/**
	 * Builds a solution with a status and a matrix of solution items. Each line
	 * represents a single processor. Solution length should be the same for all
	 * processors. Each items of a processor are put side by side, set the isEmpty
	 * boolean to true if you want to create empty space. Note that this overrides
	 * the isSharedResource flag.
	 * 
	 * @param status        the solution status, either FEASIBLE or OPTIMAL
	 * @param solutionItems the solution items per processor
	 */
	public Solution(SolutionStatus status, SolutionItem[]... solutionItems) {
		this.status = status;
		this.solutionItems = solutionItems;
		this.solvingTime = 0;

		solutionLength = 0;
		if (solutionItems.length > 0) {
			SolutionItem[] firstSolution = solutionItems[0];
			for (SolutionItem item : firstSolution) {
				solutionLength += item.getLength();
			}
		}
	}

	public SolutionStatus getStatus() {
		return status;
	}

	public SolutionItem[][] getSolutionItems() {
		return solutionItems;
	}

	public SolutionItem[] getProcessorSolutionItems(int index) {
		return solutionItems[index];
	}

	public int getSolutionLength() {
		return solutionLength;
	}
	
	public void setSolvingTime(double solvingTime) {
		this.solvingTime = solvingTime;
	}
	
	public double getSolvingTime() {
		return solvingTime;
	}

	public void displaySolution() {
		SolutionFrame frame = new SolutionFrame(this);
		frame.initFrame();
	}
	
	public void saveSolution(File file) throws IOException {
		SolutionFrame frame = new SolutionFrame(this);
		frame.pack();
		frame.saveToFile(file);
		frame.dispose();
	}

}
