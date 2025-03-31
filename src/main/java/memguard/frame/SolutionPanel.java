package memguard.frame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import memguard.solver.solution.Solution;
import memguard.solver.solution.SolutionColor;
import memguard.solver.solution.SolutionItem;

public class SolutionPanel extends JPanel {

	private static final long serialVersionUID = -26977080926382073L;

	private static final int PANEL_WIDTH_INSET = 50;
	private static final int PANEL_HEIGHT_INSET = 80;
	private static final int ITEM_UNIT_LENGTH = 40;
	private static final int ITEM_UNIT_HEIGHT = 25;
	private static final int ARROW_HEIGHT = 50;
	private static final int ARROW_HEAD_OFFSET_X = 7;
	private static final int ARROW_HEAD_OFFSET_Y = 10;
	private static final int PROCESSOR_WIDTH_INSET = 15;
	private static final int PROCESSOR_TOP_INSET = 80;

	private Solution solution;
	private int solutionLength;

	public SolutionPanel(Solution solution) {
		this.solution = solution;
		this.solutionLength = solution.getSolutionLength();

		int processorNumber = solution.getSolutionItems().length;
		int solutionLength = solution.getSolutionLength();
		
		this.setPreferredSize(new Dimension(2 * PANEL_WIDTH_INSET + solutionLength * ITEM_UNIT_LENGTH,
				2 * PANEL_HEIGHT_INSET + processorNumber * PROCESSOR_TOP_INSET));
	}

	private void drawUpArrow(Graphics2D g2d, int currentX, int currentY) {
		g2d.setColor(Color.black);
		g2d.drawLine(currentX, currentY, currentX, currentY - ARROW_HEIGHT);
		g2d.drawLine(currentX, currentY - ARROW_HEIGHT, currentX + ARROW_HEAD_OFFSET_X,
				currentY - ARROW_HEIGHT + ARROW_HEAD_OFFSET_Y);
		g2d.drawLine(currentX, currentY - ARROW_HEIGHT, currentX - ARROW_HEAD_OFFSET_X,
				currentY - ARROW_HEIGHT + ARROW_HEAD_OFFSET_Y);
	}

	private void drawDownArrow(Graphics2D g2d, int currentX, int currentY) {
		g2d.setColor(Color.black);
		g2d.drawLine(currentX, currentY, currentX, currentY - ARROW_HEIGHT);
		g2d.drawLine(currentX, currentY, currentX + ARROW_HEAD_OFFSET_X, currentY - ARROW_HEAD_OFFSET_Y);
		g2d.drawLine(currentX, currentY, currentX - ARROW_HEAD_OFFSET_X, currentY - ARROW_HEAD_OFFSET_Y);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, 2 * PANEL_WIDTH_INSET + solutionLength * ITEM_UNIT_LENGTH,
				2 * PANEL_HEIGHT_INSET + solution.getSolutionItems().length * PROCESSOR_TOP_INSET);
		g2d.setStroke(new BasicStroke(1.5f));
		
		int currentX = PANEL_WIDTH_INSET;
		int currentY = PANEL_HEIGHT_INSET;
		int currentProcessor = 0;

		for (SolutionItem[] solutionItem : solution.getSolutionItems()) {
			// Draw the first line
			g2d.setColor(Color.black);
			g2d.drawLine(currentX - PROCESSOR_WIDTH_INSET, currentY,
					currentX + PROCESSOR_WIDTH_INSET + solutionLength * ITEM_UNIT_LENGTH, currentY);

			for (SolutionItem item : solutionItem) {
				// If begins something, draw a up arrow
				if (item.isBegin()) {
					drawUpArrow(g2d, currentX, currentY);
				}

				// Draw item (if empty just ignore)
				if (!item.isEmpty()) {
					Color color;
					if (item.isStall()) {
						color = SolutionColor.STALL.getNormalColor();
					} else {						
						color = SolutionColor.getColor(currentProcessor, item.isSharedResource());
					}
					g2d.setColor(color);
					g2d.fillRect(currentX, currentY - ITEM_UNIT_HEIGHT, ITEM_UNIT_LENGTH * item.getLength(), ITEM_UNIT_HEIGHT);
					g2d.setColor(Color.black);
					g2d.drawRect(currentX, currentY - ITEM_UNIT_HEIGHT, ITEM_UNIT_LENGTH * item.getLength(), ITEM_UNIT_HEIGHT);
				}
				currentX += item.getLength() * ITEM_UNIT_LENGTH;

				// If ends something, draw a down arrow
				if (item.isEnd()) {
					drawDownArrow(g2d, currentX, currentY);
				}
			}

			currentX = PANEL_WIDTH_INSET;
			currentY += PROCESSOR_TOP_INSET;
			currentProcessor += 1;
		}
	}
}
