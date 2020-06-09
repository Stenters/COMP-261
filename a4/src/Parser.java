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

		try (Scanner scan = new Scanner(code)){
			// the only time tokens can be next to each other is
			// when one of them is one of (){},;
			scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");
			return parseProgram(scan);

		} catch (FileNotFoundException e) {
			System.out.println("Robot program source file not found");
		} catch (ParserFailureException e) {
			System.out.println("Parser error:");
			System.out.println(e.getMessage());
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

	// List to carry variables
	static HashMap<String, ExpressionNode> vars = new HashMap<>();

	static String addVar(Scanner s) {
		// ASSGN ::= VAR "=" EXP ";"
//		String name = Parser.require(Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*"), "Illegal variable name!", s);
		String name = s.next();
		if (!Pattern.matches("\\$[A-Za-z][A-Za-z0-9]*", name)) {
			fail("Illegal variable name '" + name + "'", s);
		}
		Parser.require("=", "Illegal assignment to variable!", s);
		ExpressionNode expr = ExpressionNode.parse(s);
		Parser.require(";", "End of assignment must be a semicolon!", s);

		Parser.vars.put(name, expr);
		return name;
	}

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
			sb.append(s.formattedToString(indentLevel));
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class StatementNode implements RobotProgramNode {
	// STMT  ::= ACT ";" | LOOP | IF | WHILE | ASSGN
	RobotProgramNode action;
	String varname;

	private StatementNode() {}

	public static StatementNode parse(Scanner s) {
		StatementNode sn = new StatementNode();

		if (Parser.checkFor("loop",s)) {
			sn.action = LoopNode.parse(s);
		} else if (Parser.checkFor("if",s)) {
			sn.action = IfNode.parse(s);
		} else if (Parser.checkFor("while", s)) {
			sn.action = WhileNode.parse(s);
		} else if (s.hasNext(
				Pattern.compile("(move|turnL|turnR|turnAround|shieldOn|shieldOff|takeFuel|wait)"))) {
			sn.action = ActionNode.parse(s);
		} else if (s.hasNext(Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*"))){
			sn.varname = Parser.addVar(s);
		} else {
			Parser.fail("Invalid statement!", s);
		}

		return sn;
	}

	@Override
	public void execute(Robot robot) {
		if (action != null) {
			action.execute(robot);
		}
	}

	public String formattedToString(int indentLevel) {
		if (action != null) {
			return action.formattedToString(indentLevel);
		} else if (!varname.equals("")) {
			return "\t".repeat(indentLevel) + varname + " = " + Parser.vars.get(varname).toString() + "\n";
		} else {
			return "";
		}
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

class IfNode implements RobotProgramNode {
	// IF    ::= "if" "(" COND ")" BLOCK [ "elif"  "(" COND ")"  BLOCK ]* [ "else" BLOCK ]
	ConditionNode cond;
	BlockNode ifStatement, elseStatement;
	List<ConditionNode> elseIfConditions = new LinkedList<>();
	List<BlockNode> elseIfStatements = new LinkedList<>();

	private IfNode() {}

	public static IfNode parse(Scanner s) {
		IfNode in = new IfNode();

		Parser.require("\\(", "Invalid start to an if statement!", s);
		in.cond = ConditionNode.parse(s);
		Parser.require("\\)", "Invalid end to an if statement!", s);
		in.ifStatement = BlockNode.parse(s);

		while (Parser.checkFor("elif", s)) {
			Parser.require("\\(", "Invalid start to an if statement!", s);
			in.elseIfConditions.add(ConditionNode.parse(s));
			Parser.require("\\)", "Invalid end to an if statement!", s);
			in.elseIfStatements.add(BlockNode.parse(s));
		}

		if (Parser.checkFor("else", s)) {
			in.elseStatement = BlockNode.parse(s);
		}

		return in;
	}

	@Override
	public void execute(Robot robot) {

		if (cond.execute(robot)) {
			ifStatement.execute(robot);
			return;
		}

		for (int i = 0; i < elseIfStatements.size(); ++i) {
			if (elseIfConditions.get(i).execute(robot)) {
				elseIfStatements.get(i).execute(robot);
				return;
			}
		}

		if (elseStatement != null) {
			elseStatement.execute(robot);
		}
	}

	@Override
	public String formattedToString(int indentLevel) {
		StringBuilder formatString = new StringBuilder("\t".repeat(indentLevel) + "if " + cond.toString() + "\n"
				+ ifStatement.formattedToString(indentLevel + 1));

		for (int i = 0; i < elseIfStatements.size(); ++i) {
			formatString.append("\t".repeat(indentLevel))
					.append("else if ").append(elseIfConditions.get(i).toString()).append("\n")
					.append(elseIfStatements.get(i).formattedToString(indentLevel + 1));
		}

		if (elseStatement != null) {
			formatString.append("\t".repeat(indentLevel)).append("else").append("\n")
					.append(elseStatement.formattedToString(indentLevel + 1));
		}

		return formatString.toString();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class WhileNode implements RobotProgramNode {
	// WHILE ::= "while" "(" COND ")" BLOCK
	ConditionNode cond;
	BlockNode block;

	private WhileNode() {}

	public static WhileNode parse(Scanner s) {
		WhileNode wn = new WhileNode();

		Parser.require("\\(", "Invalid start to a while statement!", s);
		wn.cond = ConditionNode.parse(s);
		Parser.require("\\)", "Invalid end to a while statement!", s);
		wn.block = BlockNode.parse(s);

		return wn;
	}

	@Override
	public void execute(Robot robot) {
		while (cond.execute(robot)) {
			block.execute(robot);
		}
	}

	@Override
	public String formattedToString(int indentLevel) {
		return "\t".repeat(indentLevel) + "while " + cond.toString() + "\n"
				+ block.formattedToString(indentLevel+1);
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

		Parser.require("\\{", "Illegal start to block!", s);

		while (!Parser.checkFor("\\}", s)) {
			if (!s.hasNext()) {
				Parser.fail("Unexpected EOF! Loop terminated with no closing brace!", s);
			}
			bn.statements.add(StatementNode.parse(s));
		}

		if (bn.statements.size() == 0) {
			Parser.fail("Empty body, either remove the loop or add a statement", s);
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

class ActionNode implements RobotProgramNode {
	// ACT ::= ACT   ::= "move" [ "(" EXP ")" ] | "turnL" | "turnR" | "turnAround" |
	//          "shieldOn" | "shieldOff" | "takeFuel" | "wait" [ "(" EXP ")" ]

	private enum  Actions { MOVE, TURNL, TURNR, TURNAROUND, SHIELDON, SHIELDOFF, TAKEFUEL, WAIT }
	Actions action;
	ExpressionNode expression;

	private ActionNode() {}

	public static ActionNode parse(Scanner s) {
		ActionNode an = new ActionNode();
		String next = s.next().toLowerCase();

		switch (next) {
			case "move" -> an.action = Actions.MOVE;
			case "turnl" -> an.action = Actions.TURNL;
			case "turnr" -> an.action = Actions.TURNR;
			case "turnaround" -> an.action = Actions.TURNAROUND;
			case "shieldon" -> an.action = Actions.SHIELDON;
			case "shieldoff" -> an.action = Actions.SHIELDOFF;
			case "takefuel" -> an.action = Actions.TAKEFUEL;
			case "wait" -> an.action = Actions.WAIT;
			default -> Parser.fail("Invalid action '" + next + "', " +
					"options are\n\tMOVE, TURNL, TURNR, TURNAROUND, SHIELDON, SHIELDOFF, TAKEFUEL, WAIT ", s);
		}

		if (an.action == Actions.MOVE || an.action == Actions.WAIT) {
			if (Parser.checkFor("\\(", s)) {
				an.expression = ExpressionNode.parse(s);
				Parser.require("\\)", "Error! Didn't close your paren for your action argument", s);
			}
		}

		Parser.require(";","Improper action ending, should be ';'",s);

		return an;
	}

	@Override
	public void execute(Robot robot) {
		try {
			if (expression != null && (action == Actions.MOVE || action == Actions.WAIT)) {
				int val = expression.execute(robot);
				if (val < 0) {
					System.out.println("Expression was less than zero! (Action = " + action.name() + ")");
					return;
				}

				if (action == Actions.WAIT) { robot.wait(val * 1000); }
				else {
					while (val-- > 0) { robot.move(); }
				}


			} else {
				switch (action) {
					case MOVE -> robot.move();
					case WAIT -> robot.wait();
					case TURNL -> robot.turnLeft();
					case TURNR -> robot.turnRight();
					case TURNAROUND -> robot.turnAround();
					case SHIELDON -> robot.setShield(true);
					case SHIELDOFF -> robot.setShield(false);
					case TAKEFUEL -> robot.takeFuel();
				}
			}
		} catch (InterruptedException e) {
			System.err.println("Robot was interupted while trying to wait!");
		}
	}

	public String formattedToString(int indentlevel) {
		if (expression != null) {
			return "\t".repeat(indentlevel) + action.name() + "(" + expression.toString() + ")";
		}
		return "\t".repeat(indentlevel) + action.name();
	}

	@Override
	public String toString() {
		return formattedToString(0);
	}
}

class ExpressionNode {
	// EXP   ::= NUM | SEN | VAR | OP "(" EXP "," EXP ")"
	int number;
	OperationNode op;
	SensorNode sensor;

	private ExpressionNode() {}

	public static ExpressionNode parse(Scanner s) {
		ExpressionNode en = new ExpressionNode();

		if (s.hasNext(Parser.NUMPAT) && s.hasNextInt()) {
			en.number = s.nextInt();
		} else if (s.hasNext(Pattern.compile("(add|sub|mul|div)"))) {
			en.op = OperationNode.parse(s);
		} else if (s.hasNext(Pattern.compile("(fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist)"))) {
			en.sensor = SensorNode.parse(s);
		} else if (s.hasNext(Pattern.compile("\\$.*"))) {
			String name = s.next();
			if (Parser.vars.containsKey(name)) {
				en = Parser.vars.get(name);
			} else {
				en.number = 0;
			}
		} else {
			Parser.fail("Illegal expression given! needs to be a number (with no leading zeros)" +
					", an operation (add, sub, mul, or div), or a sensor value",s);
		}

		return en;
	}

	public int execute(Robot robot) {
		if (sensor != null) {
			return sensor.execute(robot);
		} else if (op != null) {
			return op.execute(robot);
		}
		return number;
	}

	@Override
	public String toString() {
		if (sensor != null) {
			return sensor.toString();
		} else if (op != null) {
			return op.toString();
		}
		return "" + number;
	}
}

class SensorNode {
	// SEN   ::= "fuelLeft" | "oppLR" | "oppFB" | "numBarrels" |
	//          "barrelLR" [ "(" EXP ")" ] | "barrelFB" [ "(" EXP ")" ] | "wallDist"
	enum Sensor { FUELLEFT, OPPLR, OPPFB, NUMBARRELS, BARRELLR, BARRELFB, WALLDIST}
	Sensor sensor;
	ExpressionNode expr;

	private SensorNode() {}

	public static SensorNode parse(Scanner s) {
		SensorNode sn = new SensorNode();
		String next = s.next().toLowerCase();

		switch (next) {
			case "fuelleft" -> sn.sensor = Sensor.FUELLEFT;
			case "opplr" -> sn.sensor = Sensor.OPPLR;
			case "oppfb" -> sn.sensor = Sensor.OPPFB;
			case "numbarrels" -> sn.sensor = Sensor.NUMBARRELS;
			case "barrellr" -> sn.sensor = Sensor.BARRELLR;
			case "barrelfb" -> sn.sensor = Sensor.BARRELFB;
			case "walldist" -> sn.sensor = Sensor.WALLDIST;
			default -> Parser.fail("illegal sensor option '" + next + "'.\n\tValid options include: "
					+ "FUELLEFT, OPPLR, OPPFB, NUMBARRELS, BARRELLR, BARRELFB, WALLDIST", s);
		}

		if ((sn.sensor == Sensor.BARRELLR || sn.sensor == Sensor.BARRELFB) && Parser.checkFor("\\(", s)) {
			sn.expr = ExpressionNode.parse(s);
			Parser.require("\\)", "Illegal end to sensor expression " + sn.sensor.name(), s);
		}

		return sn;
	}

	public int execute(Robot robot) {
		if (expr != null) {
			if (sensor == Sensor.BARRELLR) {
				return robot.getBarrelLR(expr.execute(robot));
			}else if (sensor == Sensor.BARRELFB) {
				return robot.getBarrelFB(expr.execute(robot));
			}
		}

		return switch (sensor) {
			case FUELLEFT -> robot.getFuel();
			case OPPLR -> robot.getOpponentLR();
			case OPPFB -> robot.getOpponentFB();
			case NUMBARRELS -> robot.numBarrels();
			case BARRELLR -> robot.getClosestBarrelLR();
			case BARRELFB -> robot.getClosestBarrelFB();
			case WALLDIST -> robot.getDistanceToWall();
		};
	}

	@Override
	public String toString() {
		if (expr != null) {
			return sensor.name() + "(" + expr.toString() + ")";
		}
		return sensor.name();
	}
}

class OperationNode {
	// OP    ::= ("add" | "sub" | "mul" | "div") "(" EXP "," EXP ")"
	enum Operation { ADD, SUB, MUL, DIV }
	Operation op;
	ExpressionNode arg1, arg2;

	private OperationNode() {}

	public static OperationNode parse(Scanner s){
		OperationNode on = new OperationNode();

		switch (s.next().toLowerCase()) {
			case "add" -> on.op = Operation.ADD;
			case "sub" -> on.op = Operation.SUB;
			case "mul" -> on.op = Operation.MUL;
			case "div" -> on.op = Operation.DIV;
			default -> Parser.fail("Illegal operation for expression" +
					"\n\tOptions are: add, sub, mul div", s);
		}

		Parser.require("\\(", "Illegal start to an operation, requires a paren", s);
		on.arg1 = ExpressionNode.parse(s);
		Parser.require(",", "Illegal delimiter in an operation, no comma found", s);
		on.arg2 = ExpressionNode.parse(s);
		Parser.require("\\)", "Illegal end to an operation, requires a paren", s);

		return on;
	}

	public int execute(Robot robot) {
		return switch (op) {
			case ADD -> arg1.execute(robot) + arg2.execute(robot);
			case SUB -> arg1.execute(robot) - arg2.execute(robot);
			case MUL -> arg1.execute(robot) * arg2.execute(robot);
			case DIV -> arg1.execute(robot) / arg2.execute(robot);
		};
	}

	@Override
	public String toString() {
		String operation = switch (op) {
			case ADD -> " + ";
			case SUB -> " - ";
			case MUL -> " * ";
			case DIV -> " / ";
		};

		return arg1.toString() + operation + arg2.toString();
	}
}

class ConditionNode {
	// COND  ::= "and" "(" COND "," COND ")" | "or" "(" COND "," COND ")" | "not" "(" COND ")"  |
	//          RELOP "(" EXP "," EXP ")
	enum RELOP {LT, GT, EQ}
	RELOP op;

	enum Condition {AND, OR, NOT}
	Condition cond;

	ExpressionNode exp1, exp2;
	ConditionNode arg1, arg2;

	private ConditionNode() {
	}

	public static ConditionNode parse(Scanner s) {
		ConditionNode cn = new ConditionNode();
		String next = s.next().toLowerCase();

		if (Pattern.matches("(lt|gt|eq)", next)) {

			switch (next) {
				case "lt" -> cn.op = RELOP.LT;
				case "gt" -> cn.op = RELOP.GT;
				case "eq" -> cn.op = RELOP.EQ;
			}

			Parser.require("\\(", "Invalid start to a condition!", s);
			cn.exp1 = ExpressionNode.parse(s);
			Parser.require(",", "Invalid delimiter to a condition!", s);
			cn.exp2 = ExpressionNode.parse(s);
			Parser.require("\\)", "Invalid end to a condition!", s);

		} else {
			switch (next) {
				case "and" -> cn.cond = Condition.AND;
				case "or" -> cn.cond = Condition.OR;
				case "not" -> cn.cond = Condition.NOT;
			}

			Parser.require("\\(", "Invalid start to a logical condition!", s);
			cn.arg1 = ConditionNode.parse(s);
			if (!(cn.cond == Condition.NOT)) {
				Parser.require(",", "Condition requires two arguments" +
						"\n\tArgument given: " + cn.arg1, s);
				cn.arg2 = ConditionNode.parse(s);
			}
			Parser.require("\\)", "Invalid end to a logical condition!", s);

		}

		return cn;
	}

	public boolean execute(Robot robot) {
		if (op != null) {
			return switch (op) {
				case EQ -> exp1.execute(robot) == exp2.execute(robot);
				case LT -> exp1.execute(robot) <  exp2.execute(robot);
				case GT -> exp1.execute(robot) >  exp2.execute(robot);
			};
		} else {
			return switch (cond) {
				case AND -> arg1.execute(robot) && arg2.execute(robot);
				case OR  -> arg1.execute(robot) || arg2.execute(robot);
				case NOT -> !arg1.execute(robot);
			};
		}

	}

	@Override
	public String toString() {
		if (op != null) {
			return op.name() + " (" + exp1.toString() + ", " + exp2 + ")";
		} else if (cond == Condition.NOT) {
			return cond.name() + " " + arg1;
		} else {
			return cond.name() + " (" + arg1 + ", " + arg2 + ")";
		}
	}
}
