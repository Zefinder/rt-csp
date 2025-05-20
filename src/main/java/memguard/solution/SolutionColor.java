package memguard.solution;

import java.awt.Color;

public enum SolutionColor {

	RED(new Color(0xFFF2F2), new Color(0xFF8080), "red!5", "red!50"), BLUE(new Color(0xF2F2FF), new Color(0x8080FF), "blue!5", "blue!50"),
	ORANGE(new Color(0xFFF9F2), new Color(0xFFBF80), "orange!5", "orange!50"), GREEN(new Color(0xA6FFA6), new Color(0xA6FFA6), "green!5", "green!35"),
	STALL(new Color(0xA3A3A3), new Color(0xA3A3A3), "black!36", "black!36");

	private Color[] solutionColor;
	private String[] solutionColorString;

	private SolutionColor(Color normalColor, Color sharedResourceColor, String normalColorString,
			String sharedResourceColorString) {
		this.solutionColor = new Color[] { normalColor, sharedResourceColor };
		this.solutionColorString = new String[] { normalColorString, sharedResourceColorString };
	}

	public Color getNormalColor() {
		return solutionColor[0];
	}

	public Color getSharedResourceColor() {
		return solutionColor[1];
	}
	
	public String getNormalColorString() {
		return solutionColorString[0];
	}

	public String getSharedResourceColorString() {
		return solutionColorString[1];
	}
	
	public static Color getColor(int index, boolean isSharedResource) {
		SolutionColor[] values = SolutionColor.values();
		index = index % (values.length - 1);
		int shared = isSharedResource ? 1 : 0;

		return values[index].solutionColor[shared];
	}
	
	public static String getColorString(int index, boolean isSharedResource) {
		SolutionColor[] values = SolutionColor.values();
		index = index % values.length;
		int shared = isSharedResource ? 1 : 0;

		return values[index].solutionColorString[shared];
	}
}
