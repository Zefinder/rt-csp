package memguard.frame;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import memguard.solution.Solution;

public class SolutionFrame extends JFrame {

	private static final long serialVersionUID = 4330617490485888399L;
	
	private JPanel solutionPanel;
	
	public SolutionFrame(Solution solution) {
		this.setSize(1000, 500);
		this.setTitle("Solution: " + solution.getStatus().toString());
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(true);
		
		this.setJMenuBar(new SolutionMenuBar(this, solution));
		
		solutionPanel = new SolutionPanel(solution);
		JScrollPane scrollPane = new JScrollPane(solutionPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		this.add(scrollPane);
		
		this.setVisible(false);
	}
	
	public void saveToFile(File file) throws IOException {
		Container contentPane = solutionPanel;
		BufferedImage image = new BufferedImage(contentPane.getWidth(), contentPane.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		contentPane.printAll(g2d);
		g2d.dispose();

		ImageIO.write(image, "png", file);
	}
	
	public void initFrame() {
		this.setVisible(true);
	}

}
