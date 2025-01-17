import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A new instance of HuffmanCoding is created for every run. The constructor is
 * passed the full text to be encoded or decoded, so this is a good place to
 * construct the tree. You should store this tree in a field and then use it in
 * the encode and decode methods.
 */
public class HuffmanCoding {
	Node root;

	/**
	 * This would be a good place to compute and store the tree.
	 */
	public HuffmanCoding(String text) {
		PriorityQueue<Node> queue = new PriorityQueue<>();
		HashMap<Character, Integer> frequencies = new HashMap<>();

		for (char c : text.toCharArray()) {
			if (!frequencies.containsKey(c)) {
				frequencies.put(c, 1);
			} else {
				frequencies.replace(c, frequencies.get(c) + 1);
			}
		}

		for (char c : frequencies.keySet()) {
			queue.add(new Leaf(c, frequencies.get(c)));
		}

		while (queue.size() > 1) {
			Node a = queue.poll(), b = queue.poll();
			BranchNode c = new BranchNode(a, b);
			queue.add(c);
		}

		root = queue.poll();
		root.assignCode("");
	}

	/**
	 * Take an input string, text, and encode it with the stored tree. Should
	 * return the encoded text as a binary string, that is, a string containing
	 * only 1 and 0.
	 */
	public String encode(String text) {
		StringBuilder code = new StringBuilder();

		for (char c : text.toCharArray()) {
			code.append(root.encode(c));
		}

//		String decoded = decode(code.toString());
//		for (int i = 0; i < text.length(); ++i) {
//			if (text.charAt(i) == decoded.charAt(i)) {
//				System.err.println("characters match!: " + text.charAt(i) + ", " + decoded.charAt(i));
//			}
//		}

		return code.toString();
	}

	/**
	 * Take encoded input as a binary string, decode it using the stored tree,
	 * and return the decoded text as a text string.
	 */
	public String decode(String encoded) {
		return root.decode(encoded);
	}

	/**
	 * The getInformation method is here for your convenience, you don't need to
	 * fill it in if you don't wan to. It is called on every run and its return
	 * value is displayed on-screen. You could use this, for example, to print
	 * out the encoding tree.
	 */
	public String getInformation() {
		return root.toString();
	}

	@Override
	public String toString(){
		return root.toString();
	}


	private abstract static class Node implements Comparable<Node>{
		int frequency;
		String code;
		protected static int index = 0;

		@Override
		public int compareTo(Node o) {
			return Integer.compare(frequency, o.frequency);
		}

		public String decode(String encoded) {
			index = 0;
			StringBuilder res = new StringBuilder();
			while (index < encoded.length()) {
				res.append(decodeChar(encoded));
			}
			return res.toString();
		}

		public String debugDecode(String encoded, String decoded) {
			index = 0;
			StringBuilder res = new StringBuilder();
			int i = 0;
			String code;
			char parsedCode;

			while (index < encoded.length()) {
				code = encode(decoded.charAt(i));
				assert code.equals(encoded.substring(index, index + code.length()));
				parsedCode = decodeChar(encoded);
				assert code.equals(encode(parsedCode));
				assert parsedCode == decoded.charAt(i);

				res.append(parsedCode);

				if (!res.toString().equals(decoded.substring(0,++i))) {
					if (res.toString().length() != decoded.substring(0,i).length()) {
						System.err.println("diff sizes!");
					}

					System.out.println("Does not equal:\n'" + res + "'\n'" + decoded.substring(0,i) + "'");
					return "";
				}
			}
			return res.toString();
		}

		public abstract void assignCode(String code);
		public abstract String encode(char c);
		public abstract boolean contains(char c);
		protected abstract char decodeChar(String encoded);

		@Override
		public abstract String toString();
	}

	private static class BranchNode extends Node {
		Node left;
		Node right;

		public BranchNode(Node l, Node r) {
			left = l;
			right = r;
			frequency = l.frequency + r.frequency;
		}

		@Override
		public void assignCode(String code) {
			this.code = code;
			left.assignCode(code + "1");
			right.assignCode(code + "0");
		}

		@Override
		public String encode(char c) {
			if (left.contains(c)) {
				return left.encode(c);
			} else if (right.contains(c)) {
				return right.encode(c);
			}

			throw new RuntimeException("No character in subtree!");
		}

		@Override
		public char decodeChar(String encoded) {
			char code = encoded.charAt(index++);
			if (code == '1') {
				return left.decodeChar(encoded);
			} else {
				return right.decodeChar(encoded);
			}
		}

		@Override
		public boolean contains(char c) {
			return left.contains(c) || right.contains(c);
		}

		@Override
		public String toString() {
			return left.toString() + '\n' + right.toString();
		}
	}

	private static class Leaf extends Node {
		char data;

		public Leaf(char c, Integer integer) {
			data = c;
			frequency = integer;
		}

		@Override
		public void assignCode(String code) {
			this.code = code;
		}

		@Override
		public String encode(char c) {
			if (c == data) return code;

			throw new RuntimeException("Wrong Leaf discovered!");
		}

		@Override
		public char decodeChar(String encoded) {
			return data;
		}

		@Override
		public boolean contains(char c) {
			return c == data;
		}

		@Override
		public String toString() { return code + ":" + " '" + data + "'"; }


	}

}
