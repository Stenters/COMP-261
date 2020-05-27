import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import javax.swing.JFileChooser;

/**
 * The parser and interpreter. The top level parse function, a main method for
 * testing, and several utility methods are provided. You need to implement
 * parseProgram and all the rest of the parser.
 */
public class Parser {

	/**
	 * Top level parse method, called by the World
	 */
	static RobotProgramNode parseFile(File code) {
		Scanner scan = null;
		try {
			scan = new Scanner(code);

			// the only time tokens can be next to each other is
			// when one of them is one of (){},;
			scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

			RobotProgramNode n = parseProgram(scan); // You need to implement this!!!

			scan.close();
			return n;
		} catch (FileNotFoundException e) {
			System.out.println("Robot program source file not found");
		} catch (ParserFailureException e) {
			System.out.println("Parser error:");
			System.out.println(e.getMessage());
			scan.close();
		}
		return null;
	}

	/** For testing the parser without requiring the world */

	public static void main(String[] args) {
		if (args.length > 0) {
			for (String arg : args) {
				File f = new File(arg);
				if (f.exists()) {
					System.out.println("Parsing '" + f + "'");
					RobotProgramNode prog = parseFile(f);
					System.out.println("Parsing completed ");
					if (prog != null) {
						System.out.println("================\nProgram:");
						System.out.println(prog);
					}
					System.out.println("=================");
				} else {
					System.out.println("Can't find file '" + f + "'");
				}
			}
		} else {
			while (true) {
				JFileChooser chooser = new JFileChooser(".");// System.getProperty("user.dir"));
				int res = chooser.showOpenDialog(null);
				if (res != JFileChooser.APPROVE_OPTION) {
					break;
				}
				RobotProgramNode prog = parseFile(chooser.getSelectedFile());
				System.out.println("Parsing completed");
				if (prog != null) {
					System.out.println("Program: \n" + prog);
				}
				System.out.println("=================");
			}
		}
		System.out.println("Done");
	}

	// Useful Patterns

	static Pattern NUMPAT = Pattern.compile("-?\\d+"); // ("-?(0|[1-9][0-9]*)");
	static Pattern OPENPAREN = Pattern.compile("\\(");
	static Pattern CLOSEPAREN = Pattern.compile("\\)");
	static Pattern OPENBRACE = Pattern.compile("\\{");
	static Pattern CLOSEBRACE = Pattern.compile("\\}");

	static RobotProgramNode parseProgram(Scanner s) {
		return ProgramNode.parse(s);
	}

	// utility methods for the parser

	/**
	 * Report a failure in the parser.
	 */
	static void fail(String message, Scanner s) {
		StringBuilder msg = new StringBuilder(message + "\n   @ ...");
		for (int i = 0; i < 5 && s.hasNext(); i++) {
			msg.append(" ").append(s.next());
		}
		throw new ParserFailureException(msg + "...");
	}

	/**
	 * Requires that the next token matches a pattern. If it matches, it consumes
	 * and returns the token. If not, it throws an exception with an error
	 * message
	 */
	static String require(String p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	static String require(Pattern p, String message, Scanner s) {
		if (s.hasNext(p)) {
			return s.next();
		}
		fail(message, s);
		return null;
	}

	/**
	 * Requires that the next token matches a pattern (which should only match a
	 * number). If it matches, it consumes and returns the token as an integer. If
	 * not, it throws an exception with an error message
	 */
	static int requireInt(String p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	static int requireInt(Pattern p, String message, Scanner s) {
		if (s.hasNext(p) && s.hasNextInt()) {
			return s.nextInt();
		}
		fail(message, s);
		return -1;
	}

	/**
	 * Checks whether the next token in the scanner matches the specified
	 * pattern, if so, consumes the token and returns true. Otherwise returns
	 * false without consuming anything.
	 */
	static boolean checkFor(String p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

	static boolean checkFor(Pattern p, Scanner s) {
		if (s.hasNext(p)) {
			s.next();
			return true;
		} else {
			return false;
		}
	}

}

// You could add the node classes here, as long as they are not declared public (or private)

/*
TODO
	node for each non-terminal
	node or enum for actoins
	foreach node
		execute
		toString
		parse := scanner -> RobotProgramNode
*/

class ProgramNode implements RobotProgramNode {
	// PROG  ::= STMT*
	LinkedList<StatementNode> actions;

	private ProgramNode() {}

	public static ProgramNode parse(Scanner s) {
		ProgramNode pn = new ProgramNode();
		pn.actions = new LinkedList<>();

		while (s.hasNext()) {
			pn.actions.add(StatementNode.parse(s));
		}

		return pn;
	}


	@Override
	public void execute(Robot robot) {
		for (StatementNode s : actions) {
			s.execute(robot);
		}
	}

	@Override
	public String formattedToString(int indentLevel) {
		StringBuilder sb = new StringBuilder("\t".repeat(indentLevel));

		for (StatementNode s : actions) {
			sb.append(s.formattedToString(indentLevel)).append('\n');
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class StatementNode implements RobotProgramNode {
	// STMT  ::= ACT ";" | LOOP
	RobotProgramNode action;

	private StatementNode() {}

	public static StatementNode parse(Scanner s) {
		StatementNode sn = new StatementNode();

		if (Parser.checkFor("loop",s)) {
			sn.action = LoopNode.parse(s);
		} else {
			sn.action = ActionNode.parse(s);
		}

		return sn;
	}

	@Override
	public void execute(Robot robot) {
		action.execute(robot);
	}

	public String formattedToString(int indentLevel) {
		return action.formattedToString(indentLevel);
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class ActionNode implements RobotProgramNode {
	// ACT ::= "move" | "turnL" | "turnR" | "takeFuel" | "wait"
	private enum  Actions { MOVE, TURNL, TURNR, TAKEFUEL, WAIT }
	Actions action;

	private ActionNode() {}

	public static ActionNode parse(Scanner s) {
		ActionNode an = new ActionNode();
		String next = s.next().toLowerCase();

		switch (next) {
			case "move" -> an.action = Actions.MOVE;
			case "turnl" -> an.action = Actions.TURNL;
			case "turnr" -> an.action = Actions.TURNR;
			case "takefuel" -> an.action = Actions.TAKEFUEL;
			case "wait" -> an.action = Actions.WAIT;
			default -> Parser.fail("Invalid action '" + next + "', " +
					"options are\n\t\"move\" | \"turnL\" | \"turnR\" | \"takeFuel\" | \"wait\" ", s);
		}

		Parser.require(";","Improper action ending, should be ';'",s);

		return an;
	}

	@Override
	public void execute(Robot robot) {
		try {
			switch (action) {
				case MOVE -> robot.move();
				case WAIT -> robot.wait();
				case TURNL -> robot.turnLeft();
				case TURNR -> robot.turnRight();
				case TAKEFUEL -> robot.takeFuel();
			}
		} catch (InterruptedException e) {
			System.err.println("Robot was interupted while trying to wait!");
		}
	}

	public String formattedToString(int indentlevel) {
		return "\t".repeat(indentlevel) + action.name();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class LoopNode implements RobotProgramNode {
	// LOOP  ::= "loop" BLOCK
	BlockNode loop;

	private LoopNode() {}

	public static LoopNode parse(Scanner s) {
		LoopNode ln = new LoopNode();
		ln.loop = BlockNode.parse(s);
		return ln;
	}

	@Override
	public void execute(Robot robot) {
		while (true) {
			loop.execute(robot);
		}
	}

	public String formattedToString(int indentLevel) {
		return "\t".repeat(indentLevel) + "loop\n" +
					loop.formattedToString(indentLevel + 1);
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class BlockNode implements RobotProgramNode {
	// BLOCK ::= "{" STMT+ "}"
	List<StatementNode> statements;


	private BlockNode() {}

	public static BlockNode parse(Scanner s) {
		BlockNode bn = new BlockNode();
		bn.statements = new LinkedList<>();

		Parser.require("\\{", "Illegal start to loop!", s);

		while (!Parser.checkFor("\\}", s)) {
			bn.statements.add(StatementNode.parse(s));
		}

		return bn;
	}


	@Override
	public void execute(Robot robot) {
		for (StatementNode s : statements) {
			s.execute(robot);
		}
	}

	public String formattedToString(int indentLevel) {
		StringBuilder sb = new StringBuilder();

		for (StatementNode s : statements) {
			sb.append(s.formattedToString(indentLevel)).append('\n');
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}