package memguard.solution.items;

public class EmptyItem extends SolutionItem {

	public EmptyItem(int length, boolean isBegin, boolean isEnd) {
		super(length, null, isBegin, isEnd);
	}

	public EmptyItem(int length) {
		super(length, null);
	}

}
