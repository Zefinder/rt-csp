package memguard.solution.items;

import java.awt.Color;

import memguard.solution.SolutionColor;

public class StallItem extends SolutionItem {

	private static final Color STALL_COLOR = SolutionColor.STALL.getNormalColor();

	public StallItem(int length, boolean isBegin, boolean isEnd) {
		super(length, STALL_COLOR, isBegin, isEnd);
	}

	public StallItem(int length) {
		super(length, STALL_COLOR);
	}

}
