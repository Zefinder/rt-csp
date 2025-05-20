package memguard.solution.items;

import memguard.solution.SolutionColor;

public class ComputationItem extends SolutionItem {

	public ComputationItem(int processorIndex, int length, boolean isBegin, boolean isEnd) {
		super(length, SolutionColor.getColor(processorIndex, false), isBegin, isEnd);
	}

	public ComputationItem(int processorIndex, int length) {
		super(length, SolutionColor.getColor(processorIndex, false));
	}

}
