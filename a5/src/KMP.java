/**
 * A new KMP instance is created for every substring search performed. Both the
 * pattern and the text are passed to the constructor and the search method. You
 * could, for example, use the constructor to create the match table and the
 * search method to perform the search itself.
 */
public class KMP {
	int[] substringLengths;

	public KMP(String pattern, String text) {
		substringLengths = new int[pattern.length()];
		substringLengths[0] = -1;
		substringLengths[1] = 0;
		int i = 2,j = 0;

		while (i < pattern.length()) {
			if (pattern.charAt(i-1) == pattern.charAt(j)) {
				substringLengths[i++] = ++j;
			} else if (j !=0) {
				j = substringLengths[j];
			} else {
				substringLengths[i++] = 0;
			}
		}
	}

	/**
	 * Perform KMP substring search on the given text with the given pattern.
	 * 
	 * This should return the starting index of the first substring match if it
	 * exists, or -1 if it doesn't.
	 */
	public int search(String pattern, String text) {
		int i = 0, j = 0;

		while (i+j < text.length()) {
			if (pattern.charAt(i) == text.charAt(i+j)) {
				if (++i == pattern.length()) {
					return j;
				}
			}else {
				j += i - substringLengths[i];
				i = substringLengths[i] == -1 ? 0 : substringLengths[i];
			}
		}

		return -1;
	}

	public int bruteForce(String pattern, String text) {

		for (int i = 0; i < text.length() - pattern.length() + 1; ++i) {
			if (text.regionMatches(i, pattern, 0, pattern.length())) {
				return i;
			}
		}

		return -1;
	}
}
