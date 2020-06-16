import java.util.Set;

public class trial {

    static String[] teststrings = {
            "ssssssss", "sssbsss", "asdf", "past the mast of the last"
//            "Hi", "HeHe",
//            "abc", "abdba",
//            "aab", "aaaaaaa",
//            "ababc", "abababcababa",
//            "abc", "abdababcd"
    };

    public static void main(String[] args) {
//        KMP[] tests = new KMP[teststrings.length/2];
//
//        for (int i = 0; i < teststrings.length; i += 2) {
//            tests[i/2] = new KMP(teststrings[i], teststrings[i+1]);
//        }
//
//        for (int i = 0; i < teststrings.length; i += 2) {
//            System.out.printf("found at index: %d\n",
//                tests[i/2].search(teststrings[i], teststrings[i+1]));
//        }

        LempelZiv sut = new LempelZiv();

        for (String s : teststrings) {
            System.out.println(sut.decompress(sut.compress(s)) + "\n");
        }
    }
}
