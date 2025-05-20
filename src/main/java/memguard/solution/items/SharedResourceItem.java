package memguard.solution.items;

import memguard.solution.SolutionColor;

public class SharedResourceItem extends SolutionItem {

	public SharedResourceItem(int processorIndex, int length, boolean isBegin, boolean isEnd) {
		super(length, SolutionColor.getColor(processorIndex, true), isBegin, isEnd);
	}

	public SharedResourceItem(int processorIndex, int length) {
		super(length, SolutionColor.getColor(processorIndex, true));
	}

}
