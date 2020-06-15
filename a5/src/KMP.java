import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

/**
 * A new KMP instance is created for every substring search performed. Both the
 * pattern and the text are passed to the constructor and the search method. You
 * could, for example, use the constructor to create the match table and the
 * search method to perform the search itself.
 */
public class KMP {
	int[] substringLengths;

	public KMP(String pattern, String text) {
		substringLengths = new int[text.length()];

		for (int i = 0; i < text.length(); ++i) {
			int j = 0;
			while (i+j < text.length() && j < pattern.length() && pattern.toCharArray()[j] == text.toCharArray()[i+j]) {
				++j;
			}
			substringLengths[i] = j;
		}
	}

	/**
	 * Perform KMP substring search on the given text with the given pattern.
	 * 
	 * This should return the starting index of the first substring match if it
	 * exists, or -1 if it doesn't.
	 */
	public int search(String pattern, String text) {
		int i = 0;

		while (i < text.length()) {
			if (substringLengths[i] == pattern.length()) {
				return i;
			} else {
				i += substringLengths[i] + 1;
			}
		}

		return -1;
	}

	public int bruteForce(String pattern, String text) {
		long time = System.currentTimeMillis();

		for (int i = 0; i < text.length() - pattern.length(); ++i) {
			if (text.regionMatches(i, pattern, 0, pattern.length())) {

				System.out.printf("Took: %.3f sec\n", (System.currentTimeMillis() - time) / 1000.);
				return i;
			}
		}

		System.out.printf("Took: %.3f sec\n", (System.currentTimeMillis() - time) / 1000.);
		return -1;
	}
}
