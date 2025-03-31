package memguard.solver;

import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.Literal;

import memguard.solver.solution.Solution;

public class SolverTest extends Solver {

	public SolverTest() {
	}

	@Override
	public Solution solve(boolean verbose) {
		CpModel model = new CpModel();
		CpSolver solver = new CpSolver();

		var valA = model.newIntVar(0, 2, "epsilon_i");
		var valB = model.newIntVar(0, 10, "w_i+1");
		var b1 = model.newBoolVar("epsilon_i_gt0");
		var b2 = model.newBoolVar("w_i+1_gt0");
		var b3 = model.newBoolVar("a");

		model.addDifferent(valA, 0).onlyEnforceIf(b1);
		model.addEquality(valA, 0).onlyEnforceIf(b1.not());

		model.addDifferent(valB, 0).onlyEnforceIf(b2);
		model.addEquality(valB, 0).onlyEnforceIf(b2.not());
		
		model.addImplication(b1, b2);
		model.addGreaterThan(valB, 2).onlyEnforceIf(b1);

		model.addBoolAnd(new Literal[] {b3.not()});
		
		model.maximize(valA);
//		model.minimize(valA);
		solver.solve(model);
		
//		model.addHint(valA, solver.value(valA));
//		model.addHint(valB, solver.value(valB));
//		
//		model.addEquality(valA, Math.round(solver.objectiveValue()));
//		model.minimize(valB);
//		model.maximize(valB);
		
		solver.solve(model);

		System.out.println("epsilon_i = " + solver.value(valA));
		System.out.println("w_i+1 = " + solver.value(valB));
		System.out.println("epsilon_i_gt0 = " + (solver.value(b1) == 0 ? "False" : "True"));
		System.out.println("w_i+1_gt0 = " + (solver.value(b2) == 0 ? "False" : "True"));
		System.out.println("a = " + solver.booleanValue(b3));

		return null;
	}

	public static void main(String[] args) {
		SolverTest test = new SolverTest();
		test.solve(false);
	}
}
