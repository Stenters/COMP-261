import javax.net.ssl.SSLContext;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * A new instance of LempelZiv is created for every run.
 */
public class LempelZiv {
	private final char start = '[', middle = '|', end = ']';

	private final int WINDOW_SIZE = 100, LENGTH_SIZE = 20;

	/**
	 * Take uncompressed input as a text string, compress it, and return it as a
	 * text string.
	 */
	public String compress(String input) {
		String prev = "";
		String next = "";
		StringBuilder res = new StringBuilder();

		boolean isFound;

		int j;

		for (int i = 0; i < input.length(); ++i) {

			prev = input.substring((i > WINDOW_SIZE ? i - WINDOW_SIZE : 0),i);
			next = input.substring(i,(i < input.length() - LENGTH_SIZE ? i + LENGTH_SIZE : input.length()));
			isFound = false;

			while (!isFound && !next.equals("")) {
				if ((j = prev.indexOf(next)) == -1){
					next = next.substring(0, next.length() - 1);

				} else {

					res.append(start)
							.append(i-(WINDOW_SIZE-j) > 0 ? i-(WINDOW_SIZE-j) : i-j)
							.append(middle)
							.append(next.length())
							.append(middle)
							.append(i + next.length() < input.length() ? input.charAt(i + next.length()) : '\0')
							.append(end);

					i += next.length();
					isFound = true;
				}
			}

			if (!isFound) {
				// No match found for any substring
				res.append(start)
						.append('0')
						.append(middle)
						.append('0')
						.append(middle)
						.append(input.charAt(i))
						.append(end);
			}
		}

		System.out.println(res.toString());

		return res.toString();
	}

	/**
	 * Take compressed input as a text string, decompress it, and return it as a
	 * text string.
	 */
	public String decompress(String compressed) {
		StringBuilder res = new StringBuilder();

		for (int i = 0; i < compressed.length(); ++i) {
			if (compressed.charAt(i) != start) {
				res.append(compressed.charAt(i));
			} else {
				// [o|l|c] <-- positions are 1,3,5 greater than i
				// need indecies of two delimiters
				int ind1 = i + compressed.substring(i).indexOf(middle),
					ind2 = ind1 + 1 + compressed.substring(ind1+1).indexOf(middle),
					offset = Integer.parseInt(compressed.substring(i+1,ind1)),
					length = Integer.parseInt(compressed.substring(ind1+1, ind2));
				char next = compressed.charAt(ind2+1);

				res.append(res.substring(res.length()-offset, res.length()-offset+length)).append(next);
				i = ind2 + 2;
			}
		}

		return res.toString();
	}

	/**
	 * The getInformation method is here for your convenience, you don't need to
	 * fill it in if you don't want to. It is called on every run and its return
	 * value is displayed on-screen. You can use this to print out any relevant
	 * information from your compression.
	 */
	public String getInformation() {
		return "";
	}
}
