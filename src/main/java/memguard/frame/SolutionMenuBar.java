package memguard.frame;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import memguard.solution.Solution;
import memguard.solution.items.SolutionItem;

public class SolutionMenuBar extends JMenuBar {

	private static final long serialVersionUID = 7799297395258825961L;

	public SolutionMenuBar(SolutionFrame frame, Solution solution) {
		JMenu editMenu = new JMenu("Edit");
		JMenuItem toLatexFileItem = new JMenuItem("Transform to LaTeX file");
		JMenuItem toLatexClipboardItem = new JMenuItem("Copy LaTeX code to clipboard");
		JMenuItem toImage = new JMenuItem("Save to image (.png)");

		toLatexFileItem.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int accept = chooser.showSaveDialog(null);

			if (accept == JFileChooser.APPROVE_OPTION) {
				String selectedFilePath = chooser.getSelectedFile().getAbsolutePath();
				if (!selectedFilePath.endsWith(".tex")) {
					selectedFilePath += ".tex";
				}
				File selectedFile = new File(selectedFilePath);
				boolean doCreateFile = true;

				if (selectedFile.exists()) {
					int answer = JOptionPane.showConfirmDialog(null, "File already exists, do you want to overrite it?",
							"File exists", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (answer != JOptionPane.YES_OPTION) {
						doCreateFile = false;
					}
				}

				if (doCreateFile) {
					if (selectedFile.exists()) {
						selectedFile.delete();
					}

					try {
						selectedFile.createNewFile();
						SolutionToLaTeX.createLaTeXFile(solution, selectedFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		toLatexClipboardItem.addActionListener(e -> SolutionToLaTeX.copyToClipboard(solution));

		toImage.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			int accept = chooser.showSaveDialog(null);

			if (accept == JFileChooser.APPROVE_OPTION) {
				String selectedFilePath = chooser.getSelectedFile().getAbsolutePath();
				if (!selectedFilePath.endsWith(".png")) {
					selectedFilePath += ".png";
				}
				File selectedFile = new File(selectedFilePath);
				boolean doCreateFile = true;

				if (selectedFile.exists()) {
					int answer = JOptionPane.showConfirmDialog(null,
							"File already exists, do you want to overrite it?", "File exists",
							JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
					if (answer != JOptionPane.YES_OPTION) {
						doCreateFile = false;
					}
				}

				if (doCreateFile) {
					if (selectedFile.exists()) {
						selectedFile.delete();
					}

					try {
						frame.saveToFile(selectedFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		editMenu.add(toLatexFileItem);
		editMenu.add(toLatexClipboardItem);
		editMenu.add(toImage);
		this.add(editMenu);
	}

	private static class SolutionToLaTeX {
		// Decimal formatter
		private static final NumberFormat FORMATTER = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));

		// Placement values
		private static final float UNIT_WIDTH = 0.5f;
		private static final float UNIT_HEIGHT = 0.75f;
		private static final float PROCESSOR_OFFSET = 2f;
		private static final float TEXT_POSITION_X = 0f;
		private static final float TEXT_POSITION_Y_OFFSET = 0.6f;
		private static final float BRACE_POSITION_X = -0.5f;
		private static final float BRACE_Y_START_POSITION = 0f;
		private static final float BRACE_SIZE_Y_OFFSET = -0.5f;
		private static final float HORIZONTAL_ARROW_X_START_POSITION = 0f;
		private static final float VERTICAL_ARROW_Y_OFFSET = 1f;
		private static final float ITEMS_X_START_POSITION = 1f;

		// String constants
		private static final String COLOR_RGB = "{rgb,255:red,%d; green,%d; blue,%d}";
		private static final String PROCESSOR_TEXT = """
				\\draw (%s,%s) node[align=center,scale=0.8,text width=1.2cm] {$P_%d$};\n""";

		private static final String BRACE = """
				\\draw [decorate,decoration={brace,amplitude=5pt},xshift=5pt] (%s,%s) -- (%s,%s) node [black,midway,xshift=-20pt] {};\n""";

		private static final String ARROW = """
				\\draw [->] (%s,%s) -- (%s,%s);\n""";

		private static final String RECTANGLE = """
				\\draw [fill=%s] (%s,%s) rectangle (%s,%s);\n""";

		private SolutionToLaTeX() {
		}

		private static String solutionToLatex(Solution solution) {
			String latexCode = "";
			SolutionItem[][] allProcessorsSolution = solution.getSolutionItems();
			int processorNumber = allProcessorsSolution.length;
			int solutionLength = solution.getSolutionLength();

			// Draw text
			float currentX = TEXT_POSITION_X;
			float currentY = (processorNumber - 1) * PROCESSOR_OFFSET + TEXT_POSITION_Y_OFFSET;
			latexCode += "% Processor text\n";
			for (int i = 0; i < processorNumber; i++) {
				int currentProcessor = i + 1;
				latexCode += PROCESSOR_TEXT.formatted(FORMATTER.format(currentX), FORMATTER.format(currentY),
						currentProcessor);
				currentY -= PROCESSOR_OFFSET;
			}
			latexCode += "\n";

			// Brace
			latexCode += "% Brace\n";
			currentX = BRACE_POSITION_X;
			currentY = BRACE_Y_START_POSITION;
			float endBraceY = processorNumber * PROCESSOR_OFFSET + BRACE_SIZE_Y_OFFSET;
			latexCode += BRACE.formatted(FORMATTER.format(currentX), FORMATTER.format(currentY),
					FORMATTER.format(currentX), FORMATTER.format(endBraceY));
			latexCode += "\n";

			// Horizontal arrows
			latexCode += "% Horizontal arrows\n";
			currentX = HORIZONTAL_ARROW_X_START_POSITION;
			currentY = (processorNumber - 1) * PROCESSOR_OFFSET;
			float endHArrowX = UNIT_WIDTH * solutionLength;
			for (int i = 0; i < processorNumber; i++) {
				latexCode += ARROW.formatted(FORMATTER.format(currentX), FORMATTER.format(currentY),
						FORMATTER.format(endHArrowX), FORMATTER.format(currentY));
				currentY -= PROCESSOR_OFFSET;
			}
			latexCode += "\n";

			// Rectangles and vertical arrows
			currentX = ITEMS_X_START_POSITION;
			currentY = (processorNumber - 1) * PROCESSOR_OFFSET;
			for (int i = 0; i < processorNumber; i++) {
				int currentProcessor = i + 1;
				latexCode += "%% P%d\n".formatted(currentProcessor);

				SolutionItem[] items = allProcessorsSolution[i];
				for (SolutionItem item : items) {
					if (item.isBegin()) {
						float endVArrowY = currentY + VERTICAL_ARROW_Y_OFFSET;
						latexCode += ARROW.formatted(FORMATTER.format(currentX), FORMATTER.format(currentY),
								FORMATTER.format(currentX), FORMATTER.format(endVArrowY));
					}

					Color color = item.getItemColor();
					if (color != null) {
						int r = color.getRed();
						int g = color.getGreen();
						int b = color.getBlue();
						String colorString = COLOR_RGB.formatted(r, g, b);
						
						float endItemX = currentX + UNIT_WIDTH * item.getLength();
						float endItemY = currentY + UNIT_HEIGHT;
						latexCode += RECTANGLE.formatted(colorString, FORMATTER.format(currentX), FORMATTER.format(currentY),
								FORMATTER.format(endItemX), FORMATTER.format(endItemY));
					}
					currentX += UNIT_WIDTH * item.getLength();

					if (item.isEnd()) {
						float startVArrowY = currentY + VERTICAL_ARROW_Y_OFFSET;
						latexCode += ARROW.formatted(FORMATTER.format(currentX), FORMATTER.format(startVArrowY),
								FORMATTER.format(currentX), FORMATTER.format(currentY));
					}
				}
				latexCode += "\n";
				currentX = ITEMS_X_START_POSITION;
				currentY -= PROCESSOR_OFFSET;
			}

			return latexCode;
		}

		public static void createLaTeXFile(Solution solution, File file) {
			if (file.exists()) {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(file));
					writer.write(solutionToLatex(solution));
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.err.println("File does not exist");
			}
		}

		public static void copyToClipboard(Solution solution) {
			StringSelection selection = new StringSelection(solutionToLatex(solution));
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);
		}
	}

}
