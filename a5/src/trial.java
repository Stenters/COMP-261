import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class trial {

    static String text = Assignment5.readFile(new File(Assignment5.DEFAULT_EDITOR_FILE));

    static String[] teststrings = {
            "ssssssss", "sssbsss", "asdf", "past the mast of the last",
            "Hi", "HeHe",
            "abc", "abdba",
            "aab", "aaaaaaa",
            "ababc", "abababcababa",
            "abc", "abdababcd"
    };

    public static void q1() {
        // Write a short summary of the performance you observed using the two search algorithms.

        String pattern = text.substring(text.length() - 15);

        KMP searcher = new KMP(pattern, text);

        long bruteForceTime = System.currentTimeMillis();
        if (searcher.bruteForce(pattern, text) == -1) {
            throw new RuntimeException("Error with brute force!");
        }
        bruteForceTime = System.currentTimeMillis() - bruteForceTime;

        long kmpTime = System.currentTimeMillis();
        if (searcher.search(pattern, text) == -1) {
            throw new RuntimeException("Error with KMP!");
        }
        kmpTime = System.currentTimeMillis() - kmpTime;

        System.out.printf("Results for Q1:\n\tBF: %d\tKMP: %d\n", bruteForceTime, kmpTime);

    }

    public static void q2() {
        // Report the binary tree of codes your algorithm generates, and the final size of War and Peace after Huffman coding.
        HuffmanCoding coding = new HuffmanCoding(text);
        String tree = coding.toString();

        String coded = coding.encode(text);

        System.out.printf("Results for Q2:\n\tSize: %d (vs %d)\tTree: See attached file\n", coded.length(), text.length());

    }

    public static void q3() {
        // Consider the Huffman coding of war\_and\_peace.txt, taisho.txt, and pi.txt. Which of these achieves the best compression,
        // i.e. the best reduction in size? What makes some of the encodings better than others?
//        String WAP = Assignment5.readFile(new File("data/war_and_peace.txt")),
//                TAISHO = Assignment5.readFile(new File("data/taisho.txt")),
//                PI = Assignment5.readFile(new File("data/pi.txt"));
//
//        HuffmanCoding WAPCoding = new HuffmanCoding(WAP),
//                TAISHOCoding = new HuffmanCoding(TAISHO),
//                PICoding = new HuffmanCoding(PI);
//
//        String WAPEncoded = WAPCoding.encode(WAP),
//                TAISHOEncoded = TAISHOCoding.encode(TAISHO),
//                PIEncoded = PICoding.encode(PI);
//
//        int diffWAP = WAP.length() - WAPEncoded.length(),
//                diffTAISHO = TAISHO.length() - TAISHOEncoded.length(),
//                diffPI = PI.length() - PIEncoded.length();
//
//        System.out.printf("Results for Q3:\n\tDiff WAP: %d\tDiff TAISHO: %d\tDiff PI: %d\tMAX: %d\n",
//                diffWAP, diffTAISHO, diffPI, Math.max(diffPI, Math.max(diffTAISHO, diffWAP)));

        System.out.println("Results for Q3:\n\tDiff WAP: -11530560\tDiff TAISHO: -10958900\tDiff PI: -2539060\tMAX: -2539060");

    }

    public static void q4() {
        // The Lempel-Ziv algorithm has a parameter: the size of the sliding window. On a text of your choice, how does changing
        // the window size affect the quality of the compression?
        LempelZiv w100 = new LempelZiv(),
                w50 = new LempelZiv(50,10),
                w500 = new LempelZiv(500,100);

        int compressed50 = w50.compress(text).length(),
                compressed100 = w100.compress(text).length(),
                compressed500 = w500.compress(text).length();

        System.out.printf("Results for Q4:\n\tSize 50: %d\tSize 100: %d\tSize 500: %d\n",
                compressed50, compressed100, compressed500);
    }

    public static void q5() {
        // What happens if you Huffman encode War and Peace before applying Lempel-Ziv compression to it? Do you get a smaller
        // file size (in characters) overall?
        HuffmanCoding huffman = new HuffmanCoding(text);

        LempelZiv lzWAP = new LempelZiv(), lzHuff = new LempelZiv();

        String LZWAP = lzWAP.compress(text),
                LZHuff = lzHuff.compress(huffman.encode(text));

        System.out.printf("Results for Q5:\n\tLZ only: %d\tLZ & Huff: %d\n",
                LZWAP.length(), LZHuff.length());
    }

    public static void q6() {
        // Explain (1 paragraph) why the two log probabilities are so different.
        String whakatauki = "Hurihia to aroaro ki te ra tukuna to atarangi kia taka ki muri i a koe";
        String saying = "Turn your face to the sun and the shadows fall behind you, translation";

        Ngrams ngram = new Ngrams(text);

        float probA_Tak = ngram.getCharProb("a tak", 'a'); // N = 5
        float probMaori = ngram.calcTotalLogProb(ngram.findCharProbs(whakatauki));
        float probEnglish = ngram.calcTotalLogProb(ngram.findCharProbs(saying));

        System.out.println("Results for Q6:\n\ta tak: " + probA_Tak + ", maori: " + probMaori + ", english: " + probEnglish);
    }

    public static void q7() {
        // Another whakatauki goes: "Titiro whakamuri kia haere whakamua". The Arithmetic Coding algorithm could use an Ngrams model
        // to encode this string. How long would the bit-string encoding of the string be, if the Arithmetic Coding algorithm used Ngrams
        // (up to n=5 as above) that were based on (a) War_and_peace.txt versus (b) the text at
        // http://www.gutenberg.org/files/44897/44897.txt?
        Ngrams wap = new Ngrams(text);
        Ngrams grammar = new Ngrams(Assignment5.readFile(new File("data/grammar.txt")));

        System.out.printf("Results for Q7:\n\tsize of wap ngrams: %d, size of grammar ngrams: %d\n",
                wap.);
        }

    public static void q8() {
        // Suppose Alice has two binary strings (made only of 1 's and 0 's), X and Y . Make a pair of algorithms so that:
        //
        //    Alice can encode X and Y into a single binary string Z , which she sends to Bob.
        //    Bob receives Z and can unambiguously decode it back into X and Y.
        int errors = 0;


        for (int i = 1; i < 100; ++i) {
            for (int j = 1; j < 100; ++j) {
                String encoded = q8Encode(Integer.toBinaryString(i), Integer.toBinaryString(j));
                String[] decoded = q8Decode(encoded);

                if (!Integer.toBinaryString(i).equals(decoded[0]) && Integer.toBinaryString(j).equals(decoded[1])) {
                    System.out.println("error for values: " + i + ", " + j);
                    ++errors;
                } else {
                    System.out.printf("sizes (a,b,c): %d, %d, %d\n",
                            Integer.toBinaryString(i).length(), Integer.toBinaryString(j).length(), encoded.length());
                }
            }
        }

        System.out.printf("Results for Q8:\n\t#errors: %d\n", errors);
    }

    public static String q8Encode(String a, String b) {
        int x = Integer.parseInt(a, 2), y = Integer.parseInt(b, 2),
                div = x/y, mod = x%y;

        return "" + Integer.toBinaryString(div) + ',' + Integer.toBinaryString(mod) + ',' + b;
    }

    public static String[] q8Decode(String encoded) {
        int ind1 = encoded.indexOf(','),
                ind2 = ind1 + 1 + encoded.substring(ind1+1).indexOf(','),
                div = Integer.parseInt(encoded.substring(0,ind1),2),
                mod = Integer.parseInt(encoded.substring(ind1+1,ind2), 2),
                b = Integer.parseInt(encoded.substring(ind2+1));

        String[] res = new String[2];
        res[0] = Integer.toBinaryString(b * div + mod);
        res[1] = Integer.toBinaryString(b);

        return res;
    }



    public static void main(String[] args) {
//        q1();
//        q2();
//        q3();
//        q4();
//        q5();
//        q6();
        q7();
//        q8();
    }
}
