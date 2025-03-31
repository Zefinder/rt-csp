package memguard.solver;

import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JOptionPane;

import com.google.ortools.Loader;

import memguard.solver.solution.Solution;

/**
 * <p>
 * Basic solver class that will load OR-Tools's native libraries (if not loaded
 * yet) and create log redirections.
 * </p>
 * 
 * <p>
 * Logs will be dated and will contain the severity. For errors, a pop-up will
 * also show.
 * </p>
 * 
 * <p>
 * Any solver must extends this abstract class to be sure to load everything
 * important.
 * </p>
 */
public abstract class Solver {

	public static final boolean DEBUG = false;
	private static boolean areNativeLibsLoaded = false;
	
	public Solver() {
		// Check if libraries are loaded
		if (!areNativeLibsLoaded) {			
			// Load native libraries
			Loader.loadNativeLibraries();
		
			// If libraries were not loaded, logs are not set
			System.setOut(new ConsoleRedirection(System.out, MessageNature.INFO));
			System.setErr(new ConsoleRedirection(System.err, MessageNature.ERROR));
			
			// Set libraries loaded
			areNativeLibsLoaded = true;
		}
	}
	
	public abstract Solution solve(boolean verbose);

	private static enum MessageNature {
		INFO, ERROR;
	}
	
	private static final class Message {
		private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("'['dd/MM/yyyy HH:mm:ss']'");
		
		private LocalDateTime date;
		private MessageNature nature;
		private String message;

		/**
		 * Create a message
		 * 
		 * @param nature  the nature of the message
		 * @param message the message
		 */
		public Message(MessageNature nature, String message) {
			this.date = LocalDateTime.now();
			this.nature = nature;
			this.message = message;
		}

		@Override
		public String toString() {
			return String.format("%s [%s]: %s", date.format(dateFormat), nature.name(), message);
		}

	}
	
	private static final class ConsoleRedirection extends PrintStream {

		private MessageNature nature;
		
		public ConsoleRedirection(OutputStream out, MessageNature nature) {
			super(out);
			this.nature = nature;
		}
		
		@Override
		public void println(String x) {
			// Create and print the message
			Message message = new Message(nature, x);
			super.println(message.toString());
			
			// If it is an error, display a pop-up with the message
			if (nature == MessageNature.ERROR) {
				JOptionPane.showMessageDialog(null, x, "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
	}
	
}
