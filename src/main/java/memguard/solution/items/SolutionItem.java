package memguard.solution.items;

import java.awt.Color;

public class SolutionItem {

	private int length;
	private Color itemColor;
	private boolean isBegin;
	private boolean isEnd;

	public SolutionItem(int length, Color itemColor, boolean isBegin, boolean isEnd) {
		this.length = length;
		this.itemColor = itemColor;
		this.isBegin = isBegin;
		this.isEnd = isEnd;
	}

	public SolutionItem(int length, Color itemColor) {
		this(length, itemColor, false, false);
	}

	public int getLength() {
		return length;
	}

	public Color getItemColor() {
		return itemColor;
	}
	
	public boolean isBegin() {
		return isBegin;
	}

	public boolean isEnd() {
		return isEnd;
	}

}
