import java.util.*;

/**
 * Ngrams predictive probabilities for text
 */
public class Ngrams {
	/**
	 * The constructor would be a good place to compute and store the Ngrams probabilities.
	 * Take uncompressed input as a text string, and store a List of Maps. The n-th such
     * Map has keys that are prefixes of length n. Each value is itself a Map, from 
     * characters to floats (this is the probability of the char, given the prefix).
	 */
    List<Map<String, Map<Character,Float>>> ngram;  /* nb. suggestion only - you don't have to use
                                                     this particular data structure */

	public Ngrams(String input) {
		String prefix;
		char suffix;

		ngram = new ArrayList<>();
		ngram.add(new HashMap<>());
		ngram.add(new HashMap<>());
		ngram.add(new HashMap<>());
		ngram.add(new HashMap<>());
		ngram.add(new HashMap<>());
		ngram.add(new HashMap<>());

		for (int i = 0; i < 6; ++i) {
			for (int j = 0; j + i + 1 < input.length(); ++j) {
				prefix = input.substring(j, j+i);
				suffix = input.charAt(j+i+1);

				if (ngram.get(i).containsKey(prefix)) {
					if (ngram.get(i).get(prefix).containsKey(suffix)) {
						ngram.get(i).get(prefix).replace(suffix, ngram.get(i).get(prefix).get(suffix) + 1);
					} else {
						ngram.get(i).get(prefix).put(suffix, 1f);
					}
				} else {
					Map<Character, Float> probs = new HashMap<>();
					probs.put(suffix, 1f);
					ngram.get(i).put(prefix, probs);
				}
			}

			for (String s : ngram.get(i).keySet()) {
				float count = 0;

				for (char c : ngram.get(i).get(s).keySet()) {
					count += ngram.get(i).get(s).get(c);
				}

				for (char c: ngram.get(i).get(s).keySet()){
					ngram.get(i).get(s).replace(c, ngram.get(i).get(s).get(c) / count);
				}
			}
		}
	}

	/**
	 * Take a string, and look up the probability of each character in it, under the Ngrams model.
     * Returns a List of Floats (which are the probabilities).
	 */
	public List <Float> findCharProbs(String mystring) {
		List<Float> charProbs = new LinkedList<>();

		if (mystring.length() > 1) {
			charProbs.add(getCharProb(mystring.substring(0, 1), mystring.charAt(1)));
		}if (mystring.length() > 2) {
			charProbs.add(getCharProb(mystring.substring(0, 2), mystring.charAt(2)));
		}if (mystring.length() > 3) {
			charProbs.add(getCharProb(mystring.substring(0, 3), mystring.charAt(3)));
		}if (mystring.length() > 4) {
			charProbs.add(getCharProb(mystring.substring(0, 4), mystring.charAt(4)));
		}

		for(int i = 5; i < mystring.length(); ++i) {
			charProbs.add(getCharProb(mystring.substring(i-5,i), mystring.charAt(i)));
		}

		return charProbs;
	}

	public float getCharProb(String prefix, char charAt) {
		if (ngram.get(prefix.length()).containsKey(prefix)) {
			return ngram.get(prefix.length()).get(prefix).getOrDefault(charAt, 1e-6f);
		} else {
			return getCharProb(prefix.substring(1), charAt);
		}
	}

	/**
	 * Take a list of probabilites (floats), and return the sum of the logs (base 2) in the list.
	 */
	public float calcTotalLogProb(List<Float> charProbs) {
		float sum = 0;

		for (float f : charProbs){
			sum += Math.log(f) / Math.log(2);
		}

		return sum;
	}
}
