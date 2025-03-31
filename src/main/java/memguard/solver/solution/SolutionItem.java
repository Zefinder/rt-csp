package memguard.solver.solution;

public class SolutionItem {

	private int length;
	private boolean isSharedResource;
	private boolean isEmpty;
	private boolean isStall;
	private boolean isBegin;
	private boolean isEnd;

	public SolutionItem(int length, boolean isSharedResource, boolean isEmpty, boolean isStall, boolean isBegin,
			boolean isEnd) {
		this.length = length;
		this.isSharedResource = isSharedResource;
		this.isEmpty = isEmpty;
		this.isStall = isStall;
		this.isBegin = isBegin;
		this.isEnd = isEnd;
	}

	public SolutionItem(int length, boolean isEmpty, boolean isBegin, boolean isEnd) {
		this(length, false, isEmpty, false, isBegin, isEnd);
	}

	public SolutionItem(int length, boolean isSharedResource, boolean isEmpty, boolean isBegin, boolean isEnd) {
		this(length, false, isEmpty, false, isBegin, isEnd);
	}

	public SolutionItem(int length, boolean isSharedResource) {
		this(length, isSharedResource, false, false, false, false);
	}

	public int getLength() {
		return length;
	}

	public boolean isSharedResource() {
		return isSharedResource;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public boolean isStall() {
		return isStall;
	}
	
	public boolean isBegin() {
		return isBegin;
	}

	public boolean isEnd() {
		return isEnd;
	}

}
